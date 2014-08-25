/**
 * Copyright (c) 2014 Inera AB, <http://inera.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skl.skltpservices.npoadapter.mule;

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleMessage;
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex.updateresponder._1.ObjectFactory;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;
import se.nationellpatientoversikt.*;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;

import javax.xml.datatype.XMLGregorianCalendar;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Maps incoming index update requests to standard RIB engagement index requests. <p/>
 *
 * Supports NPO SendSimpleIndex, SendIndex2 and Update.
 *
 * @author Peter
 *
 */
@Slf4j
public class InboundUpdateIndexTransformer extends AbstractMessageTransformer {

    private static final ObjectFactory of = new ObjectFactory();
    private static final JaxbUtil jaxbUtil = new JaxbUtil(UpdateType.class);

    private String eiLogicalAddress;

    public String getEiLogicalAddress() {
        return eiLogicalAddress;
    }

    public void setEiLogicalAddress(String eiLogicalAddress) {
        this.eiLogicalAddress = eiLogicalAddress;
    }

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) {

        final Object[] payload = (Object[]) message.getPayload();

        UpdateType updateRequest = null;

        if (payload[1] instanceof ArrayOfinfoTypeInfoTypeType) {
            log.debug("SimpleIndex to Update");
            final SendSimpleIndex simpleIndex = new SendSimpleIndex();
            simpleIndex.setSubjectOfCareId((String) payload[0]);
            simpleIndex.setInfoTypes((ArrayOfinfoTypeInfoTypeType) payload[1]);
            simpleIndex.setParameters((ArrayOfparameternpoParameterType) payload[2]);
            updateRequest = map(simpleIndex, message);
        } else if (payload[1] instanceof ArrayOfindexUpdateIndexUpdateType) {
            log.debug("SendIndex2 to Update");
            final SendIndex2 sendIndex2 = new SendIndex2();
            sendIndex2.setSubjectOfCareId((String) payload[0]);
            sendIndex2.setIndexUpdates((ArrayOfindexUpdateIndexUpdateType) payload[1]);
            sendIndex2.setParameters((ArrayOfparameternpoParameterType) payload[2]);
            updateRequest = map(sendIndex2, message);
        } else if (payload[1] instanceof UpdateType) {
            updateRequest = fix(message, (UpdateType) payload[1]);
        } else {
            throw new IllegalStateException("Unexpected type of message: " + payload[1]);
        }

        message.setPayload(new Object[] { getEiLogicalAddress(), updateRequest });

        return message;
    }

    //
    private UpdateType fix(final MuleMessage message, final UpdateType updateRequest) {
        String logicalAddress = null;
        for (final EngagementTransactionType tx : updateRequest.getEngagementTransaction()) {
            domain(tx.getEngagement(), tx.getEngagement().getCategorization());
            if (logicalAddress == null) {
                logicalAddress = tx.getEngagement().getLogicalAddress();
            }
        }

        message.setOutboundProperty(BidirectionalSendIndexTransformer.NPO_PARAM_PREFIX + "hsa_id", logicalAddress);
        message.setOutboundProperty(BidirectionalSendIndexTransformer.NPO_PARAM_PREFIX + "version", "1.1");
        message.setOutboundProperty(BidirectionalSendIndexTransformer.NPO_PARAM_PREFIX + "transaction_id", "NA");

        return updateRequest;
    }

    //
    protected UpdateType map(final SendSimpleIndex simpleIndex, final MuleMessage message) {
        final UpdateType update = of.createUpdateType();
        for (final InfoTypeType info : simpleIndex.getInfoTypes().getInfoType()) {
            final EngagementType engagement = domain(
                    create(simpleIndex.getSubjectOfCareId(), simpleIndex.getParameters().getParameter(), message),
                    info.getInfoTypeId());
            final EngagementTransactionType engagementTransaction = create(!info.isExists(), engagement);
            engagementTransaction.setEngagement(domain(engagement, info.getInfoTypeId()));
            update.getEngagementTransaction().add(engagementTransaction);
        }
        return update;
    }

   /**
     * Returns a {@link Date} date and time representation.
     *
     * @param cal the actual date and time.
     * @return the {@link Date} representation.
     */
    public static Date toDate(XMLGregorianCalendar cal) {
        if (cal != null) {
            final Calendar c = Calendar.getInstance();

            c.set(Calendar.DATE, cal.getDay());
            c.set(Calendar.MONTH, cal.getMonth() - 1);
            c.set(Calendar.YEAR, cal.getYear());
            c.set(Calendar.DAY_OF_MONTH, cal.getDay());
            c.set(Calendar.HOUR_OF_DAY, cal.getHour());
            c.set(Calendar.MINUTE, cal.getMinute());
            c.set(Calendar.SECOND, cal.getSecond());
            c.set(Calendar.MILLISECOND, cal.getMillisecond());

            return c.getTime();
        }
        return null;
    }

    //
    protected UpdateType map(final SendIndex2 sendIndex2, final MuleMessage message) {
        final UpdateType update = of.createUpdateType();
        for (final IndexUpdateType info : sendIndex2.getIndexUpdates().getIndexUpdate()) {
            final EngagementType engagement = domain(
                    create(sendIndex2.getSubjectOfCareId(), sendIndex2.getParameters().getParameter(), message),
                    info.getInfoTypeId());

            engagement.setDataController(info.getCareGiver());
            if (info.getRegistrationTime() != null) {
                engagement.setMostRecentContent(EHRUtil.formatTimestamp(toDate(info.getRegistrationTime())));
            }
            if (info.getRcId() != null) {
                engagement.setBusinessObjectInstanceIdentifier(info.getRcId());
            }
            final EngagementTransactionType engagementTransaction = create(false, engagement);
            update.getEngagementTransaction().add(engagementTransaction);
        }
        return update;
    }

    //
    protected EngagementTransactionType create(final boolean deleteFlag, final EngagementType engagement) {
        final EngagementTransactionType engagementTransaction = new EngagementTransactionType();
        engagementTransaction.setDeleteFlag(deleteFlag);
        engagementTransaction.setEngagement(engagement);
        return engagementTransaction;
    }

    //
    protected EngagementType domain(final EngagementType engagement, final String infoType) {
        switch (infoType) {
            case "vko":
                engagement.setServiceDomain("riv:clinicalprocess:logistics:logistics");
                break;
            case "voo":
                engagement.setServiceDomain("riv:clinicalprocess:healthcond:description");
                break;
            default:
                throw new IllegalArgumentException("Unable to map NPO info type to a RIV service domain: \"" + infoType + "\"");
        }

        engagement.setCategorization(infoType);

        return engagement;
    }

    //
    protected EngagementType create(final String personId, List<NpoParameterType> parameters, final MuleMessage message) {
        final EngagementType engagement = new EngagementType();

        engagement.setRegisteredResidentIdentification(personId);

        BidirectionalSendIndexTransformer.setParameters(message, parameters);

        final String hsaId = message.getOutboundProperty(BidirectionalSendIndexTransformer.NPO_PARAM_PREFIX + "hsa_id");

        engagement.setLogicalAddress(hsaId);
        engagement.setSourceSystem(hsaId);
        engagement.setDataController(hsaId);

        return engagement;
    }
}

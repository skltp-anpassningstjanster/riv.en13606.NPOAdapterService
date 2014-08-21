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
import se.nationellpatientoversikt.*;
import se.skl.skltpservices.npoadapter.router.RouteData;
import se.skl.skltpservices.npoadapter.router.Router;

import java.util.List;

/**
 * Transforms from inbound SendSimpleIndex and SendIndex2 JAXB Beans to String representations.
 * Also creates outbound SendStatus callback requests with the origin request as source. <p/>
 *
 * For outbound callbacks routing the message is updated with actual roting data properties.
 *
 * @author Peter
 */
@Slf4j
public class BidirectionalSendIndexTransformer extends AbstractMessageTransformer {

    private static JaxbUtil jabxUtil = new JaxbUtil(SendIndex2.class, SendSimpleIndex.class, SendStatus.class);

    static final String NPO_PARAM_PREFIX = "npo_param_";

    static final ArrayOfresponseDetailnpoResponseDetailType OK_DETAILS;
    static {
        final NpoResponseDetailType detail = new NpoResponseDetailType();
        detail.setKind("I");
        detail.setCode("0");
        detail.setValue("OK");
        OK_DETAILS = new ArrayOfresponseDetailnpoResponseDetailType();
        OK_DETAILS.getResponseDetail().add(detail);
    }

    private Router router;

    @Override
    public Object transformMessage(MuleMessage message, String encoding) {
        final Object payload = message.getPayload();
        if (payload instanceof Object[]) {
            return transformInbound(message, (Object[]) payload);
        } else if (payload instanceof String) {
            return transformOutbound(message, (String) payload);
        } else {
            throw new IllegalStateException("Unexpected type of message: " + payload);
        }
    }

    //
    private Object transformInbound(MuleMessage message, Object[] payload) {
        String jmsMsg = null;
        if (payload[1] instanceof ArrayOfinfoTypeInfoTypeType) {
            log.debug("Serialize SimpleIndex");
            message.setOutboundProperty("npo_index_type", "SendSimpleIndex");
            final SendSimpleIndex sendSimpleIndex = new SendSimpleIndex();
            sendSimpleIndex.setSubjectOfCareId((String) payload[0]);
            sendSimpleIndex.setInfoTypes((ArrayOfinfoTypeInfoTypeType) payload[1]);
            sendSimpleIndex.setParameters((ArrayOfparameternpoParameterType) payload[2]);
            setParameters(message, sendSimpleIndex.getParameters().getParameter());
            jmsMsg = jabxUtil.marshal(sendSimpleIndex);
        } else if (payload[1] instanceof ArrayOfindexUpdateIndexUpdateType) {
            log.debug("Serialize SendIndex2");
            message.setOutboundProperty("npo_index_type", "SendIndex2");
            final SendIndex2 sendIndex2 = new SendIndex2();
            sendIndex2.setSubjectOfCareId((String) payload[0]);
            sendIndex2.setIndexUpdates((ArrayOfindexUpdateIndexUpdateType) payload[1]);
            sendIndex2.setParameters((ArrayOfparameternpoParameterType) payload[2]);
            setParameters(message, sendIndex2.getParameters().getParameter());
            jmsMsg = jabxUtil.marshal(sendIndex2);
        } else {
            throw new IllegalStateException("Unexpected type of message: " + payload[1]);
        }

        log.debug("Serialized message payload: {}", jmsMsg);

        message.setPayload(jmsMsg);

        return message;
    }

    //
    private Object transformOutbound(MuleMessage message, String payload) {
        final String type = message.getInboundProperty("npo_index_type");
        log.info("Prepare for type: {}", type);

        final SendStatus request = new SendStatus();

        final ArrayOfparameternpoParameterType params = new ArrayOfparameternpoParameterType();
        final NpoParameterType logicalAddress = parameter(message, "hsa_id");
        params.getParameter().add(logicalAddress);
        params.getParameter().add(parameter(message, "transaction_id"));
        params.getParameter().add(parameter(message, "version"));
        request.setParameters(params);
        request.setResponseDetails(OK_DETAILS);

        final RouteData.Route route = this.router.getRoute(logicalAddress.getValue(), true);
        if (route != null) {
            message.setInvocationProperty(OutboundPreProcessor.ROUTE_SERVICE_SOAP_ACTION, route.getSoapAction());
            message.setInvocationProperty(OutboundPreProcessor.ROUTE_ENDPOINT_URL, route.getUrl());
        } else {
            log.error("Unable to find route to outbound system (source), logical address: \"{}\"", logicalAddress.getValue());
        }

        message.setPayload(jabxUtil.marshal(request));

        return message;
    }

    private NpoParameterType parameter(MuleMessage message, String name) {
        final NpoParameterType p = new NpoParameterType();
        p.setName(name);
        p.setValue((String) message.getInboundProperty(NPO_PARAM_PREFIX + name));
        return p;
    }

    //
    static void setParameters(MuleMessage message, List<NpoParameterType> parameters) {
        for (final NpoParameterType p : parameters) {
            message.setOutboundProperty(NPO_PARAM_PREFIX + p.getName(), p.getValue());
        }
    }

    //
    public Router getRouter() {
        return router;
    }

    //
    public void setRouter(Router router) {
        this.router = router;
    }
}

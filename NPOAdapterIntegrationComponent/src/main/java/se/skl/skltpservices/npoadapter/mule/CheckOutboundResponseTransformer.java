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

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.OutboundResponseException;

/**
 * Checks Outbound Web Service responses.
 * <p/>
 * Different outbound services return different payloads. EI Update and NPO SendStatus operations return Java objects according to their
 * interfaces.
 *
 * @author Peter
 */
public class CheckOutboundResponseTransformer extends AbstractMessageTransformer {

    private static final Logger log = LoggerFactory.getLogger(CheckOutboundResponseTransformer.class);

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        final Object payload = message.getPayload();

        if (payload == null) {
            throw new TransformerException(this, new OutboundResponseException("Null payload after calling outbound service", Ehr13606AdapterError.OUTBOUNDRESPONSE_NULL));
        } else {
            log.debug("Response payload is: {}", payload);
            if (payload instanceof UpdateResponseType) {
                if (((UpdateResponseType) payload).getResultCode() == ResultCodeEnum.ERROR) {
                    final String msg = String.format("Engagement index update operation was rejected. Returned error message is: %s", ((UpdateResponseType) payload).getComment());
                    throw new TransformerException(this, new OutboundResponseException(msg, Ehr13606AdapterError.INDEXUPDATE));
                }
                log.debug("EI Update response is OK");
            } else if (payload instanceof Boolean) {
                if (!(Boolean) payload) {
                    throw new TransformerException(this, new OutboundResponseException("SendStatus call to NPOv1 producer failed", Ehr13606AdapterError.INDEXUPDATE_SENDSTATUS));
                }
                log.debug("SendStatus to NPOv1 producer response is true");
            } else {
                throw new TransformerException(this, 
                                               new OutboundResponseException("Unrecognised response type " + payload.getClass().getName() + " after calling outbound service", 
                                               Ehr13606AdapterError.OUTBOUNDRESPONSE_UNRECOGNISED));
            }
        }
        return message;
    }
}

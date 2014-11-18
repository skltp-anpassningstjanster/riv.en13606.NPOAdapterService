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
import org.mule.transformer.AbstractMessageTransformer;

import se.nationellpatientoversikt.ArrayOfparameternpoParameterType;
import se.nationellpatientoversikt.NpoParameterType;

/**
 * Capture hsa_id, transaction_id, version from SendSimpleIndex request.
 * Store in invocation properties in the message.
 *
 * @author Martin Flower
 */
public class InboundUpdateIndexParameterCaptureTransformer extends AbstractMessageTransformer {
	
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) {

        Object[] payloadParts = (Object[])message.getPayload();
        ArrayOfparameternpoParameterType parameters = ((ArrayOfparameternpoParameterType)payloadParts[2]);
        
        for (NpoParameterType p : parameters.getParameter()) {
            if ("hsa_id".equals(p.getName())) {
                message.setInvocationProperty("hsaId", p.getValue());
            }
            if ("transaction_id".equals(p.getName())) {
                message.setInvocationProperty("transactionId", p.getValue());
            }
            if ("version".equals(p.getName())) {
                message.setInvocationProperty("version", p.getValue());
            }
        }
        return message;
    }
}

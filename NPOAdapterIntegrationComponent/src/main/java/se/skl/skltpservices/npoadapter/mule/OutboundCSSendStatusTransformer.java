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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import se.nationellpatientoversikt.ArrayOfparameternpoParameterType;
import se.nationellpatientoversikt.ArrayOfresponseDetailnpoResponseDetailType;
import se.nationellpatientoversikt.NpoParameterType;
import se.nationellpatientoversikt.NpoResponseDetailType;
import se.nationellpatientoversikt.SendStatus;

/**
 * Maps incoming EngagementIndex UpdateResponse to CareSystem SendStatus.
 *
 * @author Martin Flower
 */
public class OutboundCSSendStatusTransformer extends AbstractMessageTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(OutboundCSSendStatusTransformer.class);
	
    static final String NPO_PARAM_PREFIX = "npo_param_";

    private static final JaxbUtil jaxbUtil = new JaxbUtil(SendStatus.class);

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) {
        
        SendStatus sendStatus = null;
        try {
            UpdateResponseType updateResponse = (UpdateResponseType) message.getPayload();
            
            sendStatus = new SendStatus();
            ArrayOfparameternpoParameterType npoParameters = new ArrayOfparameternpoParameterType();
            
            NpoParameterType npoParameter = new NpoParameterType();
            npoParameter.setName("hsa_id");
            npoParameter.setValue((String)message.getInvocationProperty("hsaId"));
            npoParameters.getParameter().add(npoParameter);
    
            npoParameter = new NpoParameterType();
            npoParameter.setName("transaction_id");
            npoParameter.setValue((String)message.getInvocationProperty("transactionId"));
            npoParameters.getParameter().add(npoParameter);
    
            npoParameter = new NpoParameterType();
            npoParameter.setName("version");
            npoParameter.setValue((String)message.getInvocationProperty("version"));
            npoParameters.getParameter().add(npoParameter);
            
            sendStatus.setParameters(npoParameters);

            // TODO - do we want to produce these as well?
            message.setOutboundProperty(NPO_PARAM_PREFIX + "hsa_id"        , (String)message.getInvocationProperty("hsaId"));
            message.setOutboundProperty(NPO_PARAM_PREFIX + "version"       , (String)message.getInvocationProperty("version"));
            message.setOutboundProperty(NPO_PARAM_PREFIX + "transaction_id", (String)message.getInvocationProperty("transactionId"));
            
            ArrayOfresponseDetailnpoResponseDetailType npoResponseDetails = new ArrayOfresponseDetailnpoResponseDetailType();
            
            NpoResponseDetailType detail = new NpoResponseDetailType();
            
            switch(updateResponse.getResultCode()) {
              case OK    : detail.setKind("I"); break;
              case INFO  : detail.setKind("W"); break;
              case ERROR : detail.setKind("E"); break;
            }
            
            detail.setCode(updateResponse.getResultCode().toString());
            detail.setValue(updateResponse.getComment());
            npoResponseDetails.getResponseDetail().add(detail);
            
            sendStatus.setResponseDetails(npoResponseDetails);
        } catch (Exception outboundError) {
        	throw new IllegalStateException(outboundError.getMessage(), outboundError);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("UpdateResponseType transformed into: " + jaxbUtil.marshal(sendStatus));
        }
        message.setPayload(new Object[] { sendStatus.getParameters(), sendStatus.getResponseDetails() });

        return message;
    }
}

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

import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;

/**
 * Transform EngagementIndex UpdateResponse to Boolean for SendSimpleIndexResponse.
 *
 * @author Martin Flower
 */
public class EIUpdateResponseTransformer extends AbstractMessageTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(EIUpdateResponseTransformer.class);

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) {

        Boolean result = Boolean.FALSE;
        
        try {
            UpdateResponseType updateResponse = (UpdateResponseType) message.getPayload();
            
            switch(updateResponse.getResultCode()) {
              case OK    : result = Boolean.TRUE ; break;
              case INFO  : result = Boolean.TRUE ; break;
              case ERROR : result = Boolean.FALSE; break;
              default : throw new IllegalArgumentException("Unexpected UpdateResponseType resultCode:" + updateResponse.getResultCode());
            }
        } catch (Exception e) {
        	throw new IllegalStateException(e.getMessage(), e);
        }
        
        if (log.isDebugEnabled()) {
            log.debug("UpdateResponseType transformed into: " + result);
        }
        message.setPayload(result);
        return message;
    }
}

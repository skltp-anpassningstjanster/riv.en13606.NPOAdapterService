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


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skl.tp.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import skl.tp.vagvalsinfo.v2.VirtualiseringsInfoType;

/**
 * Maps incoming TAK HamtaAllaVirtualiseringarResponseType to a single CareSystem address String
 * (without leading http:// or https://).
 *
 * @author Martin Flower
 */
public class InboundCSTakLookupTransformer extends AbstractMessageTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(InboundCSTakLookupTransformer.class);
	
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) {

        String careSystemUrl = "";
        String hsaId =  (String)message.getInvocationProperty("hsaId");

        if (StringUtils.isBlank(hsaId)) {
            throw new IllegalStateException("invocation property hsaId is missing");
        } else {
            try {
                HamtaAllaVirtualiseringarResponseType h = (HamtaAllaVirtualiseringarResponseType)message.getPayload();
                List<VirtualiseringsInfoType> a = h.getVirtualiseringsInfo();
                for (VirtualiseringsInfoType v : a) {
                    if (hsaId.equalsIgnoreCase(v.getReceiverId())) {
                        careSystemUrl = v.getAdress();
                        if (careSystemUrl.startsWith("https://")) {
                            careSystemUrl = careSystemUrl.substring("https://".length());
                        }
                        if (careSystemUrl.startsWith("http://")) {
                            careSystemUrl = careSystemUrl.substring("http://".length());
                        }
                    }
                }
            } catch (Exception outboundError) {
            	throw new IllegalStateException(outboundError.getMessage(), outboundError);
            }
        }
        if (StringUtils.isBlank(careSystemUrl)) {
            throw new IllegalStateException("No care system url found for hsaId " + hsaId);
        }
        message.setPayload(careSystemUrl);
        log.debug("Mapped hsaId:" + hsaId + " to url:" + careSystemUrl);
        return message;
    }
}

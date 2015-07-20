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


import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.OutboundResponseException;
import se.skl.skltpservices.npoadapter.router.RouteData.Route;
import se.skl.skltpservices.npoadapter.router.Router;

/**
 * Maps hsaId to a producer url using tak cache.
 *
 * @author Martin Flower
 */
public class InboundCSTakLookupTransformer extends AbstractMessageTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(InboundCSTakLookupTransformer.class);
	
	private Router router;
	public void setRouter(Router router) {
	    this.router = router;
	}
	
    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

        log.info("InboundCSTakLookupTransformer");
        
        String careSystemUrl = "";
        String hsaId = (String)message.getInvocationProperty("hsaId");

        if (StringUtils.isBlank(hsaId)) {
            throw new IllegalStateException("invocation property hsaId is missing");
        } else {
            Route route = router.getCallbackRoute(hsaId);
            if (route == null) {
                throw new TransformerException(this, 
                                               new OutboundResponseException("hsaId:" + hsaId + " does not map to a producer url for contract " + Router.CONTRACT_CALLBACK, 
                                                                             Ehr13606AdapterError.ROUTE_CALLBACK_MISSING));
            }
            if (StringUtils.isBlank(route.getUrl())) {
                throw new TransformerException(this, 
                        new OutboundResponseException("hsaId:" + hsaId + " producer url is blank for contract " + Router.CONTRACT_CALLBACK, 
                                                      Ehr13606AdapterError.ROUTE_CALLBACK_URL_BLANK));
            }
            careSystemUrl = route.getUrl();
        }
        if (careSystemUrl.startsWith("https://")) {
            careSystemUrl = careSystemUrl.substring("https://".length());
        } else if (careSystemUrl.startsWith("http://")) {
            careSystemUrl = careSystemUrl.substring("http://".length());
        }
        message.setPayload(careSystemUrl);
        log.debug("Mapped hsaId:" + hsaId + " to CareSystem url:" + careSystemUrl);
        return message;
    }
}

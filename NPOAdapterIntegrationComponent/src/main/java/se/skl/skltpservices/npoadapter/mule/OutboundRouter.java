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
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.api.transport.PropertyScope;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;
import org.mule.routing.outbound.AbstractRecipientList;
import org.mule.transformer.simple.MessagePropertiesTransformer;
import org.mule.transport.http.HttpConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.skl.skltpservices.npoadapter.router.RouteData;
import se.skl.skltpservices.npoadapter.router.Router;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Routes to the source system endpoint.
 *
 * @author Peter
 */
@Slf4j
public class OutboundRouter extends AbstractRecipientList {
    // constants
    public static final String X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID = "x-rivta-original-serviceconsumer-hsaid";
    public static final String SOAP_ACTION = "SOAPAction";
    public static final String SOITOOLKIT_HTTPS_CONNECTOR = "soitoolkit-https-connector";
    public static final String SOITOOLKIT_HTTP_CONNECTOR = "soitoolkit-http-connector";
    public static final String HTTPS_PREFIX = "https://";
    public static final String UTF_8 = "UTF-8";

    // configurable properties (externally)
    private int responseTimeout = 5000;
    private Router router;

    @Override
    protected List<Object> getRecipients(MuleEvent event) throws CouldNotRouteOutboundMessageException {
        try {
            final String logicalAddress = event.getMessage().getInvocationProperty("logical-address");

            final EndpointBuilder eb = getEndpoint(logicalAddress);

            final String originalServiceConsumerId = event.getMessage().getInboundProperty(X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID, "");

            eb.addMessageProcessor(getOutboundTransformer(getOutboundProperties(originalServiceConsumerId)));

            final List<Object> route = Collections.singletonList((Object) eb.buildOutboundEndpoint());

            log.debug("router: " + route.get(0));

            return route;
        } catch (Throwable e) {
            throw new CouldNotRouteOutboundMessageException(event, this, e);
        }
    }

    protected HashMap<String, Object> getOutboundProperties(final String originalServiceConsumerId) {
        final HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(SOAP_ACTION, "urn:riv13606:v1.1:RIV13606REQUEST_EHR_EXTRACT");
        map.put(X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID, originalServiceConsumerId);
        map.put(HttpConstants.HEADER_USER_AGENT, "TP-NPO-ADAPTER/1.0");
        map.put(HttpConstants.HEADER_CONTENT_TYPE, "text/xml; charset=UTF-8");

        return map;
    }

    /**
     * Returns the router. <p>
     *
     * Also assigns a matching soitoolkit HTTPS or HTTP connector by name.
     *
     * @param logicalAddress the logical address (service producer).
     * @return the URL (as a string) to the outbound endpoint.
     */
    protected EndpointBuilder getEndpoint(final String logicalAddress) {
        final String url = getUrl(logicalAddress);

        final EndpointBuilder eb = new EndpointURIEndpointBuilder(new URIBuilder(url, muleContext));
        eb.setResponseTimeout(responseTimeout);
        eb.setExchangePattern(MessageExchangePattern.REQUEST_RESPONSE);
        eb.setEncoding(UTF_8);
        final String connectorName = url.startsWith(HTTPS_PREFIX) ? SOITOOLKIT_HTTPS_CONNECTOR : SOITOOLKIT_HTTP_CONNECTOR;
        eb.setConnector(muleContext.getRegistry().lookupConnector(connectorName));

        return eb;

    }

    //
    protected MessagePropertiesTransformer getOutboundTransformer(final HashMap<String, Object> addProperties) {
        log.info("Outbound message transformers to update/add/remove mule message properties");

        final MessagePropertiesTransformer transformer = new MessagePropertiesTransformer();
        transformer.setMuleContext(muleContext);
        transformer.setOverwrite(true);
        transformer.setScope(PropertyScope.OUTBOUND);
        transformer.setAddProperties(addProperties);

        return transformer;
    }

    /**
     * External configuration property for response timeout in millis.
     *
     * @param responseTimeout the timeout in millis, 0 is none (forever).
     */
    public void setResponseTimeout(final int responseTimeout) {
        log.info("Set global response timeout to: " + responseTimeout);
        this.responseTimeout = responseTimeout;
    }

    /**
     * Returns the URL to the source system in question.
     *
     * @return the URL.
     */
    public String getUrl(final String logicalAddress) {
        log.debug("Find route for logiclaAddress: " + logicalAddress);
        final RouteData.Route route = getRouter().getRoute(logicalAddress);
        if (route == null) {
            throw new IllegalArgumentException("NPOAdapter: No route found for logical address: " + logicalAddress);
        }
        log.debug("Route for logicalAddress: " + route.getUrl());
        return route.getUrl();
    }

    //
    public Router getRouter() {
        return router;
    }


    //
    public void setRouter(Router router) {
        log.info("Set router to: " + router);
        this.router = router;
    }
}

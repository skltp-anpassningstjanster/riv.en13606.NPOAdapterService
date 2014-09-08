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
import org.mule.api.MuleEvent;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.api.routing.RoutingException;
import org.mule.api.transport.PropertyScope;
import org.mule.endpoint.EndpointURIEndpointBuilder;
import org.mule.endpoint.URIBuilder;
import org.mule.routing.outbound.AbstractRecipientList;
import org.mule.transformer.simple.MessagePropertiesTransformer;
import org.mule.transport.http.HttpConstants;
import se.skl.skltpservices.npoadapter.util.Sample;

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

    //
    final static ThreadLocal<Sample> localSample = new ThreadLocal<Sample>();

    @Override
    protected List<Object> getRecipients(MuleEvent event) throws CouldNotRouteOutboundMessageException {
        try {
            final String url = event.getMessage().getInvocationProperty(OutboundPreProcessor.ROUTE_ENDPOINT_URL);

            localSample.set(new Sample(url));

            final EndpointBuilder eb = getEndpoint(url);

            final String originalServiceConsumerId = event.getMessage().getInboundProperty(X_RIVTA_ORIGINAL_SERVICECONSUMER_HSAID, "");

            final String soapAction = event.getMessage().getInvocationProperty(OutboundPreProcessor.ROUTE_SERVICE_SOAP_ACTION);

            eb.addMessageProcessor(getOutboundTransformer(getOutboundProperties(originalServiceConsumerId, soapAction)));

            final List<Object> route = Collections.singletonList((Object) eb.buildOutboundEndpoint());

            log.debug("router: " + route.get(0));

            return route;
        } catch (Throwable e) {
            throw new CouldNotRouteOutboundMessageException(event, this, e);
        }
    }

    @Override
    public MuleEvent route(MuleEvent event) throws RoutingException {
        localSample.remove();
        try {
            return super.route(event);
        } finally {
            final Sample sample = localSample.get();
            if (sample != null) {
                sample.end();
            }
        }
    }

    //
    protected HashMap<String, Object> getOutboundProperties(final String originalServiceConsumerId, final String soapAction) {
        final HashMap<String, Object> map = new HashMap<String, Object>();

        map.put(SOAP_ACTION, soapAction);
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
     * @param url the endpoint url (service producer).
     * @return the URL (as a string) to the outbound endpoint.
     */
    protected EndpointBuilder getEndpoint(final String url) {
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
}

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

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.processor.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.router.RouteData;
import se.skl.skltpservices.npoadapter.router.Router;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.ByteArrayInputStream;

/**
 * The consumer has sent a GetSomethingRequest to VP.
 * In EngagemangsIndex, this request has been associated with a producer.
 * The producer is identified by a logical address.
 * The GetSomethingRequest has been converted by the NPOAdapter into a 13606 request.
 * At this point, the message is ready to be sent to the producer.
 * We have a logical address, we need to find the route.
 * Routing data is held in TAK.
 * But rather than look up data in TAK, we look it up in a cache contained inside the Router.
 *
 * @author Peter
 */
public class OutboundPreProcessor implements MessageProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(OutboundPreProcessor.class);

    public static final String ROUTE_LOGICAL_ADDRESS = "route-logical-address";
    public static final String ROUTE_SERVICE_SOAP_ACTION = "route-service-soap-action";
    public static final String ROUTE_ENDPOINT_URL = "route-endpoint-url";

    // shall be thread-safe
    static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    static final String HEADER_EL = "Header";
    static final String LOGICAL_ADDRESS_EL = "LogicalAddress";

    // properties.
    private Router router;

    @Override
    public MuleEvent process(final MuleEvent event) throws MuleException {
        try {
        	final byte[] payload = event.getMessage().getPayloadAsBytes();

        	// Ignore HTTP GET operations.
        	if (payload[0] == '/') {
        		return event;
        	}
        	final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(payload));
        	final String logicalAddress = extractLogicalAddress(reader);
        	log.debug("Logical address: " + logicalAddress);

        	final MuleMessage message = event.getMessage();
        	message.setInvocationProperty(ROUTE_LOGICAL_ADDRESS, (logicalAddress == null) ? "" : logicalAddress);
        	final RouteData.Route route = router.getRoute(logicalAddress);
        	if (route != null) {
        		message.setInvocationProperty(ROUTE_SERVICE_SOAP_ACTION, route.getSoapAction());
        		message.setInvocationProperty(ROUTE_ENDPOINT_URL, route.getUrl());
        	} else {
        		log.error("Unable to find route to outbound system (source), logical address: \"{}\"", logicalAddress);
        	}
        } catch (Exception err) {
        	log.error("Unable to route to outbound system", err);
        }
        return event;
    }

    //
    private String extractLogicalAddress(final XMLStreamReader reader) throws XMLStreamException {
        String logicalAddress = null;
        boolean headerSection = false;

        int event = reader.getEventType();

        while (reader.hasNext() && (logicalAddress == null)) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    final String startTag = reader.getLocalName();
                    if (HEADER_EL.equals(startTag)) {
                        headerSection = true;
                    }
                    // Don't bother about riv-version in this code
                    if (headerSection && LOGICAL_ADDRESS_EL.equals(startTag)) {
                        reader.next();
                        logicalAddress = reader.getText();
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    final String endTag = reader.getLocalName();
                    if (HEADER_EL.equals(endTag)) {
                        headerSection = false;
                    }
                    break;

                default:
                    break;
            }
            event = reader.next();
        }

        return logicalAddress;
    }

    //
    public void setRouter(Router router) {
        log.info("Set router to: " + router);
        this.router = router;
    }
}

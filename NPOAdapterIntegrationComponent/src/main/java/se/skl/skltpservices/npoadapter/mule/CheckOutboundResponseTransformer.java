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

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import se.nationellpatientoversikt.SendStatusResponse;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/**
 * Checks Outbound Web Service responses. <p/>
 *
 * Different outbound services returns different payloads. EI Update and NPO SendSimpleIndex and SendIndex2 operations returns
 * Java objects according to interfaces while the dynamically routed care system SendStatus callback returns a String with
 * the complete SOAP envelope.
 *
 * @author Peter
 */
@Slf4j
public class CheckOutboundResponseTransformer extends AbstractMessageTransformer {


    //
    private static XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

    @Override
    @SneakyThrows
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        final Object payload = message.getPayload();

        log.debug("Response Payload is: {}", payload);
        if (payload instanceof UpdateResponseType) {
            if (((UpdateResponseType) payload).getResultCode() == ResultCodeEnum.ERROR) {
                final String msg = String.format("Engagement index update operation was rejected. Returned error message is: %s", ((UpdateResponseType) payload).getComment());
                throw new OutboundResponseException(msg);
            }
            log.debug("EI Update response is OK");
        } else if (payload instanceof Boolean) {
            if (!(Boolean) payload) {
                throw new OutboundResponseException("Update index operation was rejected by NPO");
            }
            log.debug("NPO SendSimpleIndex/SendIndex2 response is OK");
        } else if (payload instanceof String) {

            // extract body object
            @Cleanup final XMLStreamReader xmlStreamReader = findStatusResponseBody((String) payload, SendStatusResponse.class);

            Object o = BidirectionalSendIndexTransformer.jaxbUtil.unmarshal(xmlStreamReader);
            if (o instanceof SendStatusResponse) {
                if (!((SendStatusResponse) o).isSuccess()) {
                    throw new OutboundResponseException("SendStatus operation was rejected by the care system");
                }
            }
            log.debug("Care System SendStatus response is OK");
        }

        return message;
    }

    /**
     * Returns a {@link javax.xml.stream.XMLStreamReader} positioned at the payload body tag of @type
     *
     * @param envelope the envelope as a string.
     * @param type the expected body type.
     * @return the stream reader positioned at the body object.
     * @throws XMLStreamException when any I/O error occurs.
     */
    protected XMLStreamReader findStatusResponseBody(final String envelope, final Class<?> type) throws XMLStreamException {
        final XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(new StringReader(envelope));

        xmlStreamReader.getEventType();

        while (xmlStreamReader.hasNext()) {
            if (xmlStreamReader.hasName() && type.getSimpleName().equals(xmlStreamReader.getLocalName())) {
                break;
            }
            xmlStreamReader.next();
        }

        return xmlStreamReader;
    }
}

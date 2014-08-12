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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.processor.MessageProcessor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by Peter on 2014-08-12.
 */
@Slf4j
public class SOAPHeaderExtractor implements MessageProcessor {

    // shall be thread-safe
    static XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    static final String HEADER_EL = "Header";
    static final String TO_EL = "To";
    static final String LOGICAL_ADDRESS_EL = "LogicalAddress";

    @Override
    @SneakyThrows
    public MuleEvent process(MuleEvent event) throws MuleException {
        final byte[] payload = event.getMessage().getPayloadAsBytes();
        final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new ByteArrayInputStream(payload));
        final String logicalAddress = extractLogicalAddress(reader);
        log.debug("Logical address: " + logicalAddress);
        event.getMessage().setInvocationProperty("logical-address", (logicalAddress == null) ? "" : logicalAddress);

        return event;
    }

    @SneakyThrows
    protected byte[] toBytes(final InputStream in) {
        final byte[] buf = new byte[1024];
        final ByteArrayOutputStream os = new ByteArrayOutputStream(2048);

        for (int len = 0; (len = in.read(buf)) > 0; ) {
            os.write(buf, 0, len);
        }

        return os.toByteArray();
    }

    //
    private String extractLogicalAddress(final XMLStreamReader reader) throws XMLStreamException {
        String logicalAddress = null;
        boolean headerFound = false;

        int event = reader.getEventType();

        while (reader.hasNext()) {
            switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    final String localName = reader.getLocalName();
                    if (HEADER_EL.equals(localName)) {
                        headerFound = true;
                    }
                    // Don't bother about riv-version in this code
                    if (headerFound && (TO_EL.equals(localName) || LOGICAL_ADDRESS_EL.equals(localName))) {
                        reader.next();
                        logicalAddress = reader.getText();
                    }
                    break;

                case XMLStreamConstants.END_ELEMENT:
                    if (HEADER_EL.equals(reader.getLocalName())) {
                        return logicalAddress;
                    }
                    break;
                default:
                    break;
            }
            event = reader.next();
        }

        return logicalAddress;
    }
}

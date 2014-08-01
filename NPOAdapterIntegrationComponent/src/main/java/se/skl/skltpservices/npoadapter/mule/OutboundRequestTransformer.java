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
import org.mule.api.transformer.TransformerException;

import javax.xml.stream.XMLStreamReader;

/**
 * Transforms standard RIV Service Contract requests from consumers to a corresponding EHR_EXTRACT request. <p/>
 *
 * @see {@link se.skl.skltpservices.npoadapter.mapper.AbstractMapper}
 *
 * @author Peter
 *
 */
public class OutboundRequestTransformer extends AbstractOutboundTransformer {

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        if (message.getPayload() instanceof Object[]) {
            final Object[] payload = (Object[]) message.getPayload();
            if (payload[1] instanceof XMLStreamReader) {
                final String out = getMapper(message).mapRequest((XMLStreamReader) payload[1]);
                message.setPayload(out);
                return message;
            }
        }
        throw new IllegalArgumentException("NPOAdapter: Unexpected type of message payload (an Object[] with XMLStreamReader was expected): " + message.getPayload());
    }
}


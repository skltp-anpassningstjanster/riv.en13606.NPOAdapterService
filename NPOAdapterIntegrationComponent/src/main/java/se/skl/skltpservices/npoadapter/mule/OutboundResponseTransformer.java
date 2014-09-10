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
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import se.skl.skltpservices.npoadapter.mapper.Mapper;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.util.Sample;

/**
 * Transforms EHR_EXTRACT responses from source systems to tge actual RIV service contract response.
 *
 * @see se.skl.skltpservices.npoadapter.mapper.AbstractMapper
 *
 * @author Peter
 */
@Slf4j
public class OutboundResponseTransformer extends AbstractOutboundTransformer {

    @Override
    public Object transformMessage(final MuleMessage message, final String outputEncoding) throws TransformerException {
        final Mapper mapper = getMapper(message);
        final Sample sample = new Sample(mapper.getClass().getSimpleName() + ".mapResponse");
        try {
            if (message.getExceptionPayload() != null || "500".equals(message.getInboundProperty("http.status"))) {
                return message;
            }
            return sample.ok(mapper.mapResponse(message));
        } catch (MapperException err) {
            throw new TransformerException(this, err);
        } finally {
            sample.end();
        }
    }
}

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
package se.skl.skltpservices.npoadapter.mapper;

import se.skl.skltpservices.npoadapter.mapper.error.MapperException;

import javax.xml.stream.XMLStreamReader;

/**
 * The generic mapper interface.
 *
 * @author Peter
 */
public interface Mapper {

    /**
     * Parses a RIV service contract request from the {@link javax.xml.stream.XMLStreamReader} and maps
     * the object to the actual {@link se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType} representation
     * which is returned as a XML String
     *
     * @param reader the reader to parse the RIV service contract request from.
     * @return the target {@link se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType} as a XML String.
     */
    String mapRequest(final String uniqueId, XMLStreamReader reader) throws MapperException;

    /**
     * Parses a response from the {@link javax.xml.stream.XMLStreamReader} and maps the object to the actual
     * RIV service contract representation from {@link se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType}.
     *
     * @param reader the reader to parse the {@link se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType} from.
     * @return the target RIV service contract as a XML String.
     */
    String mapResponse(final String uniqueId, XMLStreamReader reader) throws MapperException;
}

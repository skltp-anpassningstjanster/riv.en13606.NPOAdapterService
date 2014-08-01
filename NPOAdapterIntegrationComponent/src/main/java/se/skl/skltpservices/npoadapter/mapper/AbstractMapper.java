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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import se.rivta.en13606.ehrextract.v11.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Abstracts all mapping implementations.
 *
 * @author Peter
 */
public abstract class AbstractMapper {
    static final Logger log = LoggerFactory.getLogger(AbstractMapper.class);

    private static final JaxbUtil jaxb = new JaxbUtil("se.rivta.en13606.ehrextract.v11");
    private static final ObjectFactory objectFactory = new ObjectFactory();
    private static final String GETCARECONTACTS = "GetCareContacts";

    // mappers
    private static final CareContactsMapper careContactsMapper = new CareContactsMapper();

    /**
     * Returns the actual mapper instance by the name of the (inbound SOAP) service operation.
     *
     * @param operation the operation name, i.e. from WSDL. Must be not null.
     * @return the corresponding mapper.
     * @throws java.lang.IllegalStateException when no mapper matches the name of the operation.
     */
    public static Mapper getInstance(String operation) {
        assert operation != null;
        log.debug("Lookup mapper for operation: \"" + operation + "\"");
        switch (operation) {
            case GETCARECONTACTS:
                return careContactsMapper;
            default:
                break;
        }
        throw new IllegalStateException("NPOAdapter: Unable to lookup mapper for operation: \"" + operation+ "\"");
    }

    //
    protected RIV13606REQUESTEHREXTRACTResponseType unmarshalEHRResponse(final XMLStreamReader reader) {
        try {
            return (RIV13606REQUESTEHREXTRACTResponseType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    //
    protected String marshalEHRRequest(final RIV13606REQUESTEHREXTRACTRequestType request) {
        final JAXBElement<RIV13606REQUESTEHREXTRACTRequestType> el = objectFactory.createRIV13606REQUESTEHREXTRACTRequest(request);
        return jaxb.marshal(el);
    }

    //
    protected void close(final XMLStreamReader reader) {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}

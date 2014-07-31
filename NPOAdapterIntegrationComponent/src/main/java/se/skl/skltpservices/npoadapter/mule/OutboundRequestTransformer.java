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
import org.mule.config.i18n.CoreMessages;
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsType;
import se.rivta.en13606.ehrextract.v11.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.skl.skltpservices.npoadapter.mapper.CareContactsMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Created by Peter on 2014-07-31.
 */
public class OutboundRequestTransformer extends AbstractMessageTransformer {
    //
    private static final JaxbUtil jaxbIn = new JaxbUtil(GetCareContactsType.class);
    private static final JaxbUtil jaxbOut = new JaxbUtil(RIV13606REQUESTEHREXTRACTRequestType.class);

    private static final ObjectFactory objectFactory = new ObjectFactory();

    private CareContactsMapper mapper = new CareContactsMapper();

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        if (message.getPayload() instanceof Object[]) {
            final Object[] payload = (Object[]) message.getPayload();
            final GetCareContactsType in = unmarshal((XMLStreamReader) ((Object[]) message.getPayload())[1]);

            message.setPayload(marshal(mapper.map(in)));

            return message;
        }
        throw new IllegalArgumentException("Unexpected type of payload: " + message.getPayload());
    }

    private String marshal(final RIV13606REQUESTEHREXTRACTRequestType request) {
        final JAXBElement<RIV13606REQUESTEHREXTRACTRequestType> el = objectFactory.createRIV13606REQUESTEHREXTRACTRequest(request);
        return jaxbOut.marshal(el);
    }

    //
    private GetCareContactsType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetCareContactsType) jaxbIn.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    //
    private void close(XMLStreamReader reader) {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
}

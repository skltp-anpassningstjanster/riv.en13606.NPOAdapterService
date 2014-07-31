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
import org.mule.transformer.AbstractMessageTransformer;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsType;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.CareContactsMapper;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Created by Peter on 2014-07-31.
 */
public class OutboundResponseTransformer extends AbstractMessageTransformer {

    private static final JaxbUtil jaxbOut = new JaxbUtil(GetCareContactsType.class);
    private static final JaxbUtil jaxbIn = new JaxbUtil("se.rivta.en13606.ehrextract.v11");
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private CareContactsMapper mapper = new CareContactsMapper();

    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        final RIV13606REQUESTEHREXTRACTResponseType in = (RIV13606REQUESTEHREXTRACTResponseType) jaxbIn.unmarshal(message.getPayload());
        return marshal(mapper.map(in.getEhrExtract().get(0)));
    }

    private String marshal(final GetCareContactsResponseType in) {
        final JAXBElement<GetCareContactsResponseType> el = objectFactory.createGetCareContactsResponse(in);
        return jaxbOut.marshal(el);
    }
}

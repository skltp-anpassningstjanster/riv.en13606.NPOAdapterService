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
package se.skl.skltpservices.npoadapter.test;

import se.rivta.en13606.ehrextract.v11.EHREXTRACT;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Peter on 2014-07-30.
 */
public class Util {

    public static final String CARECONTACS_TEST_FILE = "/data/CareContacts_SSEN13606-2.1.1.xml";


    // Make it easy to dump the resulting response after map (for dev purposes only)
    @XmlRootElement
    static class Root {
        @XmlElement
        private Object type;
    }


    //
    public static EHREXTRACT loadTestData(final String name) {
        try {
            final JAXBContext context = JAXBContext.newInstance("se.rivta.en13606.ehrextract.v11");
            final Unmarshaller unmarshaller;
            unmarshaller = context.createUnmarshaller();
            final JAXBElement<EHREXTRACT> root = (JAXBElement<EHREXTRACT>) unmarshaller.unmarshal(Util.class.getResourceAsStream(name));
            return root.getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void dump(final T jaxbObject) {
        final JAXBContext context;
        try {
            context = JAXBContext.newInstance(jaxbObject.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(jaxbObject, System.out);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}

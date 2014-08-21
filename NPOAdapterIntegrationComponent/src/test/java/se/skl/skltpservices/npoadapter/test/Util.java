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

import lombok.SneakyThrows;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by Peter on 2014-07-30.
 */
public class Util {

    public static final String CARECONTACS_TEST_FILE = "/data/CareContacts_SSEN13606-2.1.1.xml";
    public static final String CAREDOCUMENTATION_TEST_FILE = "/data/CareDocumentation_SSEN13606-2.1.1.xml";
    public static final String DIAGNOSIS_TEST_FILE = "/data/Diagnosis_SSEN13606-2.1.1.xml";
    public static final String LAB_TEST_FILE = "/data/LaboratoryOrderOutcome_SSEN13606-2.1.1.xml";


    //
    @SneakyThrows
    public static EHREXTRACT loadEhrTestData(final String fileName) {
        return loadEhrTestData(fileName, "se.rivta.en13606.ehrextract.v11");
    }


    @SneakyThrows
    public static EHREXTRACT loadEhrTestData(final String fileName, final String contextPath) {
        final JAXBContext context = JAXBContext.newInstance(contextPath);
        final Unmarshaller unmarshaller;
        unmarshaller = context.createUnmarshaller();
        final JAXBElement<EHREXTRACT> root = (JAXBElement<EHREXTRACT>) unmarshaller.unmarshal(Util.class.getResourceAsStream(fileName));
        final EHREXTRACT ehrextract = root.getValue();
        ehrextract.setRmId("EN 13606"); // hard coded value
        return ehrextract;
    }


    public static <T> void dump(final T jaxbObject) {
        dump(jaxbObject, new OutputStreamWriter(System.out));
    }

    @SneakyThrows
    public static <T> void dump(final T jaxbObject, Writer writer) {
        final JAXBContext context;
        context = JAXBContext.newInstance(jaxbObject.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(jaxbObject, writer);
    }

}

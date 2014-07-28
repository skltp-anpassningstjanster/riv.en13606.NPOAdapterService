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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Peter on 2014-07-28.
 */
public class CareContactsMapperTest {


    private EHREXTRACT ehrextract;
    static JAXBContext context;


    @BeforeClass
    public static void init() throws JAXBException {
        context = JAXBContext.newInstance("se.rivta.en13606.ehrextract.v11");
    }

    @Before
    public void setup() throws JAXBException {
        Unmarshaller unmarshaller = context.createUnmarshaller();
        JAXBElement<EHREXTRACT> root = (JAXBElement<EHREXTRACT>) unmarshaller.unmarshal(getClass().getResourceAsStream("/CareContacts_SSEN13606-2.1.1.xml"));
        ehrextract = root.getValue();
    }

    @Test
    public void testUnmarshalCareContacts() {
        Assert.assertNotNull(ehrextract);
    }


}

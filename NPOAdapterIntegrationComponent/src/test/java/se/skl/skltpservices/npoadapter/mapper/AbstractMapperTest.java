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

import org.junit.Test;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.*;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Peter on 2014-08-01.
 */
public class AbstractMapperTest {

    @Test
    public void testLookupMapper() {
        final Mapper mapper = AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_CARECONTACTS_2);
        assertTrue(mapper instanceof CareContactsMapper);
    }


    @Test(expected = IllegalStateException.class)
    public void testInvalidURNWHenLookupMapper() {
        final Mapper mapper = AbstractMapper.getInstance("no-ns", "no-ns");
    }

    @Test(expected = AssertionError.class)
    public void testWithNull() {
        AbstractMapper.getInstance(null, null);
    }

    @Test
    public void testEhrRequestMapping() {
        final RIV13606REQUESTEHREXTRACTRequestType source = new RIV13606REQUESTEHREXTRACTRequestType();

        source.setSubjectOfCareId(createII());
        source.setTimePeriod(createIVLTS());
        source.getMeanings().add(createCD());
        source.setMaxRecords(createINT());
        source.getParameters().add(createParameter("enduser_assertion", "...Base64-kodat SAML-intyg..."));

        AbstractMapper mapper = new AbstractMapper() {};

        GetEhrExtractType target = mapper.map(source);

        assertNotNull(target.getSubjectOfCareId());
        assertEquals(Integer.valueOf(100), target.getMaxRecords().getValue());
        assertEquals("20080101", target.getTimePeriod().getLow().getValue());
        assertEquals("20080520", target.getTimePeriod().getHigh().getValue());

        assertEquals(1, target.getMeanings().size());
        assertEquals(1, target.getParameters().size());

        assertEquals("voo", target.getMeanings().get(0).getCode());
        assertEquals("enduser_assertion", target.getParameters().get(0).getName().getValue());
        assertEquals("...Base64-kodat SAML-intyg...", target.getParameters().get(0).getValue().getValue());
    }

    //
    static ParameterType createParameter(String name, String value) {
        final ParameterType parameterType = new ParameterType();
        parameterType.setName(createST(name));
        parameterType.setValue(createST(value));

        return parameterType;
    }

    static ST createST(String value) {
        final ST val = new ST();
        val.setValue(value);
        return val;
    }

    static INT createINT() {
        final INT val = new INT();

        val.setValue(100);

        return val;
    }

    static CD createCD() {
        final CD val = new CD();
        val.setCode("voo");
        return val;
    }

    static IVLTS createIVLTS() {
        final IVLTS val = new IVLTS();
        final TS low = new TS();
        low.setValue("20080101");
        final TS high = new TS();
        high.setValue("20080520");
        val.setHigh(high);
        val.setLow(low);
        return val;
    }

    static II createII() {
        final II val = new II();
        val.setRoot("191212121212");
        val.setExtension("1.2.752.129.2.1.3");
        return val;
    }

}

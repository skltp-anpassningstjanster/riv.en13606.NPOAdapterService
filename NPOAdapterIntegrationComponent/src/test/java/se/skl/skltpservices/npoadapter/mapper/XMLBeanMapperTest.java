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

import riv.ehr.patientsummary._1.EHREXTRACT;
import se.skl.skltpservices.npoadapter.test.Util;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Tests XML Bean mapper.
 *
 * @author Peter
 */
public class XMLBeanMapperTest {

    @XmlRootElement
    static class Root {
        @XmlElement
        private se.rivta.en13606.ehrextract.v11.EHREXTRACT ehrextract;

        public Root() {
        }

        Root(se.rivta.en13606.ehrextract.v11.EHREXTRACT ehrextract) {
            this.ehrextract = ehrextract;
        }

        @Override
        public String toString() {
            final StringWriter writer = new StringWriter();
            try {
				Util.dump(this, writer);
			} catch (JAXBException e) {
			    throw new RuntimeException(e);
			}
            return writer.toString();
        }
    }


    @Test
    public void testEhrExtractMapping_CareCareContacts() throws JAXBException {
        testEhrExtractMapping(Util.loadEhrTestData(Util.CARECONTACS_TEST_FILE));
    }

    @Test
    public void testEhrExtractMapping_CareCareDocumentation() throws JAXBException {
        testEhrExtractMapping(Util.loadEhrTestData(Util.CAREDOCUMENTATION_TEST_FILE));
    }

    @Test
    public void testEhrExtractMapping_Diagnosis() throws JAXBException {
        testEhrExtractMapping(Util.loadEhrTestData(Util.DIAGNOSIS_TEST_FILE));
    }

    @Test
    public void testEhrExtractMapping_Laboratory() throws JAXBException {
        testEhrExtractMapping(Util.loadEhrTestData(Util.LAB_TEST_FILE_1));
    }


    /**
     * Perform a complete round-trip mapping from baseline -> destination -> baseline and check that the source baseline
     * is the same as the target baseline.
     *
     * @param baseline the actual test data baseline.
     */
    public void testEhrExtractMapping(se.rivta.en13606.ehrextract.v11.EHREXTRACT baseline) {
        final EHREXTRACT target = XMLBeanMapper.dozerBeanMapper.map(baseline, EHREXTRACT.class);
        final se.rivta.en13606.ehrextract.v11.EHREXTRACT result = XMLBeanMapper.dozerBeanMapper.map(target, se.rivta.en13606.ehrextract.v11.EHREXTRACT.class);

        final Root baselineRoot  = new Root(baseline);
        final Root resultRoot = new Root(result);

        assertEquals(baselineRoot.toString(), resultRoot.toString());
    }

}

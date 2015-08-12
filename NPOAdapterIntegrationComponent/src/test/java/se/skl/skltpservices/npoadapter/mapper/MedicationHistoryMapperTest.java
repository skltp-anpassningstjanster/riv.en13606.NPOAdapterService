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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleMessage;

import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin Flower
 */
public class MedicationHistoryMapperTest {

    private static EHREXTRACT ehrextract;

    @BeforeClass
    public static void init() throws JAXBException {
        ehrextract = Util.loadEhrTestData(Util.MEDICALHISTORY_TEST_FILE);
    }

    // Make it easy to dump the resulting response after createTS
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetMedicationHistoryResponseType type;
    }

    private void dump(final GetMedicationHistoryResponseType responseType) {
        Root root = new Root();
        root.type = responseType;
        try {
            Util.dump(root);
        } catch (JAXBException j) {
            fail(j.getLocalizedMessage());
        }
    }

    @Test
    public void testMapFromEhrToMedicationHistory() {
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        MedicationHistoryMapper mapper = new MedicationHistoryMapper();
        GetMedicationHistoryResponseType responseType = mapper.mapEhrExtract(Arrays.asList(ehrextract), mockMessage);
        assertNotNull(responseType);

        dump(responseType);
        
        List<MedicationMedicalRecordType> mmrs = responseType.getMedicationMedicalRecord();

        assertTrue(mmrs.size() == 4);

        for (MedicationMedicalRecordType mmr : mmrs) {
            assertNotNull(mmr.getMedicationMedicalRecordHeader());
            assertNotNull(mmr.getMedicationMedicalRecordBody());
        }
    }
    
    @Test
    public void getNonBlank() {
        
        Map<String,String> ehr13606values = new HashMap<String,String>();
        ehr13606values.put("aaa", "aaavalue");
        ehr13606values.put("bbb", "bbbvalue");
        ehr13606values.put("ccc", "cccvalue");
        ehr13606values.put("ddd", "dddvalue");
        
        MedicationHistoryMapper objectUnderTest = new MedicationHistoryMapper();
        assertEquals("aaavalue", objectUnderTest.getNonBlank(ehr13606values, "aaa","bbb","ccc"));
        assertEquals("dddvalue", objectUnderTest.getNonBlank(ehr13606values, "ddd","",null));
        assertEquals("NA"      , objectUnderTest.getNonBlank(ehr13606values, "eee","",null));
        assertEquals("NA"      , objectUnderTest.getNonBlank(ehr13606values, (String[])null));
        assertEquals("cccvalue", objectUnderTest.getNonBlank(ehr13606values, "ccc","bbb","ccc"));
        assertEquals("NA"      , objectUnderTest.getNonBlank((Map<String,String>)null, (String[])null));
        assertEquals("NA"      , objectUnderTest.getNonBlank(new HashMap<String,String>(), ""));
        assertEquals("cccvalue", objectUnderTest.getNonBlank(ehr13606values, null,"","    ","ccc"));
    }
    
}

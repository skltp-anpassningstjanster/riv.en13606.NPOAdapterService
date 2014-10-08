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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.BeforeClass;
import org.junit.Test;

import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin
 */
public class MedicationHistoryMapperTest {

    private static SimpleDateFormat timeStampFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    static {
        timeStampFormatter.setLenient(false);
    }

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

    //
    private void dump(final GetMedicationHistoryResponseType responseType) {
        Root root = new Root();
        root.type = responseType;
        Util.dump(root);
    }

    @Test
    public void testMapFromEhrToMedicationHistory() {
        MedicationHistoryMapper mapper = new MedicationHistoryMapper();
        GetMedicationHistoryResponseType responseType = mapper.mapEhrExtract(Arrays.asList(ehrextract));
        assertNotNull(responseType);
        dump(responseType);
        List<MedicationMedicalRecordType> mmrs = responseType.getMedicationMedicalRecord();

        assertTrue(mmrs.size() == 4);

        for (MedicationMedicalRecordType mmr : mmrs) {
            mmr.getMedicationMedicalRecordHeader();
            mmr.getMedicationMedicalRecordBody();
        }
    }
}

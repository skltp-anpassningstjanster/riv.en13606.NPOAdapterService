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

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.BeforeClass;
import org.junit.Test;

import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin F
 */
public class ReferralOutcomeMapperTest {

    private static EHREXTRACT ehrextract;

    @BeforeClass
    public static void init() throws JAXBException {
        ehrextract = Util.loadEhrTestData(Util.REFERRALOUTCOME_TEST_FILE);
    }

    // Make it easy to dump the resulting response
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetReferralOutcomeResponseType type;
    }

    //
    private void dump(final GetReferralOutcomeResponseType responseType) {
        Root root = new Root();
        root.type = responseType;
        Util.dump(root);
    }

    @Test
    public void testMapFromEhrToMedicationHistory() {
        ReferralOutcomeMapper mapper = new ReferralOutcomeMapper();
        GetReferralOutcomeResponseType responseType = mapper.mapEhrExtract(Arrays.asList(ehrextract));
        assertNotNull(responseType);

        dump(responseType);
        
        List<ReferralOutcomeType> ros = responseType.getReferralOutcome();
        
        assertTrue(ros.size() == 1);

        for (ReferralOutcomeType ro : ros) {
            assertNotNull(ro.getReferralOutcomeHeader());
            assertNotNull(ro.getReferralOutcomeBody());
        }
    }
}

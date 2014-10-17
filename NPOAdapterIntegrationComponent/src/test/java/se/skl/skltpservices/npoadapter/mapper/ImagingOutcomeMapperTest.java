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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.BeforeClass;
import org.junit.Test;

import riv.clinicalprocess.healthcond.actoutcome._3.ImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.test.Util;

public class ImagingOutcomeMapperTest {
	
	private static EHREXTRACT ehrExtract;
	private static ImagingOutcomeMapper mapper;
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	
	@BeforeClass
	public static void init() {
		ehrExtract = Util.loadEhrTestData(Util.IMAGE_TEST_FILE);
		mapper = new ImagingOutcomeMapper();
		ehrResp.getEhrExtract().add(ehrExtract);
	}
	

    // Make it easy to dump the resulting response
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetImagingOutcomeResponseType type;
    }

    //
    private void dump(final GetImagingOutcomeResponseType responseType) {
        Root root = new Root();
        root.type = responseType;
        Util.dump(root);
    }
	
	
    @Test
    public void testMapFromEhrToImagingOutcome() {
        GetImagingOutcomeResponseType responseType = mapper.mapResponseType(ehrResp,"uniqueId");
        assertNotNull(responseType);

        dump(responseType);
        
        List<ImagingOutcomeType> ros = responseType.getImagingOutcome();
        
        assertTrue(ros.size() == 4);

        for (ImagingOutcomeType ro : ros) {
            assertNotNull(ro.getImagingOutcomeHeader());
            assertNotNull(ro.getImagingOutcomeBody());
        }
    }
}

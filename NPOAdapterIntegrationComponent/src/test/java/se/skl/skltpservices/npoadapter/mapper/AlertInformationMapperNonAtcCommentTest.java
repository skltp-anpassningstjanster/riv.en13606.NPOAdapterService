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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;

import riv.clinicalprocess.healthcond.description._2.PharmaceuticalHypersensitivityType;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.test.Util;

public class AlertInformationMapperNonAtcCommentTest {
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	private static EHREXTRACT ehrExctract;
	
	private static AlertInformationMapper mapper;
	
	private static CLUSTER testCluster;
	
	private static final String TEST_DATA_1 = UUID.randomUUID().toString();
	private static final String TEST_DATA_2 = UUID.randomUUID().toString();
	private static final String TEST_DATA_3 = UUID.randomUUID().toString();
	
	@BeforeClass
	public static void init() throws JAXBException {
		mapper = AlertInformationMapperTest.getAlertInformationMapper();
		ehrExctract = Util.loadEhrTestData(Util.ALERT_TEST_FILE);
		ehrResp.getEhrExtract().add(ehrExctract);
		
		//Pharma
		ELEMENT substans = new ELEMENT();
		substans.setMeaning(cdType("upp-okh-lmo-sub"));
		substans.setValue(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		
		ELEMENT substansEjAtc = new ELEMENT();
		substansEjAtc.setMeaning(cdType("upp-okh-lmo-sea"));
		substansEjAtc.setValue(EHRUtil.stType(TEST_DATA_1));
		
		ELEMENT pharma = new ELEMENT();
		pharma.setMeaning(cdType("upp-okh-lmo-lmp"));
		pharma.setValue(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		
		//Create test-cluster
		testCluster = new CLUSTER();
		testCluster.getParts().add(substans);
		testCluster.getParts().add(substansEjAtc);
		testCluster.getParts().add(pharma);
	}
	
	private static CD cdType(final String code) {
		final CD cd = new CD();
		cd.setCode(code);
		return cd;
	}
	
	private static CD cdType(final String code, final String codeSystem, final String displayName) {
		final CD cd = new CD();
		cd.setCode(code);
		cd.setCodeSystem(codeSystem);
		cd.setDisplayName(EHRUtil.stType(displayName));
		return cd;
	}

	@Test
	public void testMapPharmaceuticalHypersensitivity() throws Exception {
		final PharmaceuticalHypersensitivityType testObj = mapper.mapPharmaceuticalHypersensitivity(testCluster);
		assertEquals(TEST_DATA_1, testObj.getAtcSubstance().getCode());
		assertEquals(TEST_DATA_2, testObj.getAtcSubstance().getCodeSystem());
		assertEquals(TEST_DATA_3, testObj.getAtcSubstance().getDisplayName());
		assertEquals(TEST_DATA_1, testObj.getNonATCSubstance());
		assertEquals("comment not provided by care system", testObj.getNonATCSubstanceComment());
		assertFalse(testObj.getPharmaceuticalProductId().isEmpty());
		assertEquals(TEST_DATA_1, testObj.getPharmaceuticalProductId().get(0).getCode());
		assertEquals(TEST_DATA_2, testObj.getPharmaceuticalProductId().get(0).getCodeSystem());
		assertEquals(TEST_DATA_3, testObj.getPharmaceuticalProductId().get(0).getDisplayName());
	}

}

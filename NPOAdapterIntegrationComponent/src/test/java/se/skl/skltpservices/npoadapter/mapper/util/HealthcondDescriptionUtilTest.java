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
package se.skl.skltpservices.npoadapter.mapper.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import se.rivta.en13606.ehrextract.v11.FUNCTIONALROLE;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDHEALTHCAREPROFESSIONAL;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;


public class HealthcondDescriptionUtilTest {
	
	private static final String TEST_DATA_1 = UUID.randomUUID().toString();
	private static final String TEST_DATA_2 = UUID.randomUUID().toString();
	
	private static final String PRO_ID = UUID.randomUUID().toString();
	private static final String FAC_ID = UUID.randomUUID().toString();

	@Test
	public void testMapHealtcareProfessionalType() throws Exception {
		FUNCTIONALROLE composer = new FUNCTIONALROLE();
		II pro = new II();
		II fac = new II();
		pro.setExtension(PRO_ID);
		fac.setExtension(FAC_ID);
		composer.setHealthcareFacility(fac);
		composer.setPerformer(pro);
		
		Map<String, ORGANISATION> orgs = new HashMap<String, ORGANISATION>();
		Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> pros = new HashMap<String, IDENTIFIEDHEALTHCAREPROFESSIONAL>();
		
		ORGANISATION org = new ORGANISATION();
		org.setExtractId(fac);
		IDENTIFIEDHEALTHCAREPROFESSIONAL hcp = new IDENTIFIEDHEALTHCAREPROFESSIONAL();
		hcp.setExtractId(pro);
		
		orgs.put(UUID.randomUUID().toString(), new ORGANISATION());
		orgs.put(FAC_ID, org);
		orgs.put(UUID.randomUUID().toString(), new ORGANISATION());
		pros.put(UUID.randomUUID().toString(), new IDENTIFIEDHEALTHCAREPROFESSIONAL());
		pros.put(UUID.randomUUID().toString(), new IDENTIFIEDHEALTHCAREPROFESSIONAL());
		pros.put(PRO_ID, hcp);
		
		HealthcareProfessionalType answer = HealthcondDescriptionUtil.mapHealtcareProfessionalType(composer, orgs, pros, null);
		assertEquals(pro.getExtension(), answer.getHealthcareProfessionalCareGiverHSAId());
		assertEquals(fac.getExtension(), answer.getHealthcareProfessionalCareUnitHSAId());
	}

	@Test
	public void testMapPersonIdType() throws Exception {
		II ii = new II();
		ii.setRoot(TEST_DATA_1);
		ii.setExtension(TEST_DATA_2);
		PersonIdType type = HealthcondDescriptionUtil.mapPersonIdType(ii);
		assertEquals(ii.getRoot(), type.getType());
		assertEquals(ii.getExtension(), type.getId());
	}

	@Test
	public void testIiType() throws Exception {
		PersonIdType type = new PersonIdType();
		type.setId(TEST_DATA_1);
		type.setType(TEST_DATA_2);
		II ii = HealthcondDescriptionUtil.iiType(type);
		assertEquals(type.getId(), ii.getExtension());
		assertEquals(type.getType(), ii.getRoot());
	}

	@Test
	public void testIVLTSType() throws Exception {
		DatePeriodType datePeriod = new DatePeriodType();
		datePeriod.setEnd(TEST_DATA_1);
		datePeriod.setStart(TEST_DATA_2);
		IVLTS iv = HealthcondDescriptionUtil.IVLTSType(datePeriod);
		assertEquals(iv.getHigh().getValue(), datePeriod.getEnd());
		assertEquals(iv.getLow().getValue(), datePeriod.getStart());
	}

}

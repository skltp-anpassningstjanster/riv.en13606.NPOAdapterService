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

import org.junit.BeforeClass;
import org.junit.Test;
import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.test.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CareDocumentationMapperTest {
	
	private static EHREXTRACT ehrExctract;
	private static CareDocumentationMapper mapper;
	private static GetCareDocumentationType careDocType;
	private static FUNCTIONALROLE functionalRole;
	private static Map<String, ORGANISATION> orgs;
	private static Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> pros;
	private static AUDITINFO audit;
	
	
	private static final String TEST_CODE_SYSTEM = "1.2.752.129.2.2.2.1";
	private static final String TEST_CODE = "voo";
	
	private static final String TEST_VALUE_1 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_2 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_3 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_4 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_5 = UUID.randomUUID().toString();
	
	private static final int TEST_INT_VALUE_1 = 500;
	
	

	
	@BeforeClass
	public static void init() {
		ehrExctract = Util.loadEhrTestData(Util.CARECONTACS_TEST_FILE);
		mapper = new CareDocumentationMapper();
		careDocType = new GetCareDocumentationType();
		final PersonIdType personId = new PersonIdType();
		personId.setId(TEST_VALUE_1);
		personId.setType(TEST_VALUE_2);
		careDocType.setPatientId(personId);
		careDocType.setSourceSystemHSAid(TEST_VALUE_3);
		final DatePeriodType datePeriod = new DatePeriodType();
		datePeriod.setEnd(TEST_VALUE_4);
		datePeriod.setStart(TEST_VALUE_5);
		careDocType.setTimePeriod(datePeriod);

		//TODO: add some test data....
		functionalRole = new FUNCTIONALROLE();
		orgs = new HashMap<String, ORGANISATION>();
		pros = new HashMap<String, IDENTIFIEDHEALTHCAREPROFESSIONAL>();
		audit = new AUDITINFO();
	}
	
	
	@Test
	public void mapResponseTypeTest() {
	}
	
	@Test
	public void map13606RequestTest() {
		RIV13606REQUESTEHREXTRACTRequestType type = mapper.map13606Request(careDocType);
		assertEquals(TEST_INT_VALUE_1, type.getMaxRecords().getValue().intValue());
		assertEquals(TEST_CODE, type.getMeanings().get(0).getCode());
		assertEquals(TEST_CODE_SYSTEM, type.getMeanings().get(0).getCodeSystem());
		assertEquals(TEST_VALUE_1, type.getSubjectOfCareId().getExtension());
		assertEquals(TEST_VALUE_2, type.getSubjectOfCareId().getRoot());
	}
	
	@Test
	public void mapPersonIdTypeTest() {
		final II iiNull = new II();
		iiNull.setExtension(null);
		iiNull.setRoot(null);
		
		PersonIdType type = mapper.mapPersonIdType(iiNull);
		assertNull(type.getId());
		assertNull(type.getType());
		
		type = mapper.mapPersonIdType(null);
		assertNull(type.getId());
		assertNull(type.getType());
		
		final II ii = new II();
		ii.setExtension(TEST_VALUE_1);
		ii.setRoot(TEST_VALUE_2);
		type = mapper.mapPersonIdType(ii);
		assertEquals(TEST_VALUE_1, type.getId());
		assertEquals(TEST_VALUE_2, type.getType());
		
	}


	@Test
	public void mapHealtcareProfessionalTypeTest() {
		HealthcareProfessionalType type = mapper.mapHealtcareProfessionalType(functionalRole, orgs, pros, audit);
	}
	
	

}

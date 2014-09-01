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
import org.mule.api.MuleMessage;
import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.test.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
	
	private static final int TEST_INT_VALUE_1 = 225;
	
	

	
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
	

	
	
}

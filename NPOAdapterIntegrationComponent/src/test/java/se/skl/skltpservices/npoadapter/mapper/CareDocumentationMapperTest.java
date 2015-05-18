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

import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;

import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;

public class CareDocumentationMapperTest {
	
	private static GetCareDocumentationType careDocType;
	private static final String TEST_VALUE_1 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_2 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_3 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_4 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_5 = UUID.randomUUID().toString();
	
	
	@BeforeClass
	public static void init() throws JAXBException {
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

		// TODO: add some test data....
	}
	

	// TODO - implement some tests
	@Test
	public void mapResponseTypeTest() {
	}
}

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
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Test;

import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.TS;

public class EHRUtilTest {
	
	private static final String TEST_CODE_SYSTEM = "1.2.752.129.2.2.2.1";
	private static final String TEST_CODE = "voo";
	
	private static final String TEST_VALUE_1 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_2 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_3 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_4 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_5 = UUID.randomUUID().toString();
	
	private static final int TEST_INT_VALUE_1 = 500;
	
	
	@Test
	public void intTypeTest() {
		INT intType = EHRUtil.intType(TEST_INT_VALUE_1);
		assertEquals(TEST_INT_VALUE_1, intType.getValue().intValue());
	}
		
	@Test
	public void tsTypeTest() {
		TS ts = EHRUtil.tsType(TEST_VALUE_1);
		assertEquals(ts.getValue(), TEST_VALUE_1);
		ts = EHRUtil.tsType(null);
		assertNull(ts.getValue());
	}
}

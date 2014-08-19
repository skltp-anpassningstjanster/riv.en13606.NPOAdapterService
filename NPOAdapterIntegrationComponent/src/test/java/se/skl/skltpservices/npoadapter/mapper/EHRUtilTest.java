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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.EN;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.ENXP;
import se.rivta.en13606.ehrextract.v11.HEALTHCAREPROFESSIONALROLE;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.PERSON;
import se.rivta.en13606.ehrextract.v11.ParameterType;
import se.rivta.en13606.ehrextract.v11.SECTION;
import se.rivta.en13606.ehrextract.v11.SOFTWAREORDEVICE;
import se.rivta.en13606.ehrextract.v11.ST;
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
	public void testIntType() {
		INT intType = EHRUtil.intType(TEST_INT_VALUE_1);
		assertEquals(TEST_INT_VALUE_1, intType.getValue().intValue());
	}
		
	@Test
	public void testTsType() {
		TS ts = EHRUtil.tsType(TEST_VALUE_1);
		assertEquals(ts.getValue(), TEST_VALUE_1);
		ts = EHRUtil.tsType(null);
		assertNull(ts.getValue());
	}

	@Test
	public void testGetElementTextValue() throws Exception {
		ELEMENT e = new ELEMENT();
		e.setValue(new ST());
		((ST)e.getValue()).setValue(TEST_VALUE_1);
		assertEquals(TEST_VALUE_1, EHRUtil.getElementTextValue(e));
	}

	@Test
	public void testStType() throws Exception {
		assertNull(EHRUtil.stType(null));
		assertEquals(TEST_VALUE_1, EHRUtil.stType(TEST_VALUE_1).getValue());
	}

	@Test
	public void testGetPartValue() throws Exception {
		List<EN> list = new ArrayList<EN>();
		list.add(new EN());
		list.get(0).getPart().add(new ENXP());
		list.get(0).getPart().get(0).setValue(TEST_VALUE_2);
		assertEquals(TEST_VALUE_2, EHRUtil.getPartValue(list));
	}

	@Test
	public void testFirstItem() throws Exception {
		List<String> list = new ArrayList<String>();
		list.add(TEST_VALUE_1);
		list.add(TEST_VALUE_2);
		list.add(TEST_VALUE_3);
		assertEquals(TEST_VALUE_1, EHRUtil.firstItem(list));
	}

	@Test
	public void testGetCDCode() throws Exception {
		final CD cd = new CD();
		cd.setCode(TEST_VALUE_1);
		assertEquals(TEST_VALUE_1, EHRUtil.getCDCode(cd));
	}

	@Test
	public void testLookupDemographicIdentity() throws Exception {
		List<IDENTIFIEDENTITY> list = new ArrayList<IDENTIFIEDENTITY>();
		final String HSA_ID = TEST_VALUE_1;
		list.add(new PERSON());
		list.add(new ORGANISATION());
		list.add(new SOFTWAREORDEVICE());
		list.get(0).setExtractId(new II());
		list.get(0).getExtractId().setExtension(TEST_VALUE_2);
		list.get(1).setExtractId(new II());
		list.get(1).getExtractId().setExtension(HSA_ID);
		list.get(2).setExtractId(new II());
		list.get(2).getExtractId().setExtension(TEST_VALUE_3);
		assertEquals(HSA_ID, EHRUtil.lookupDemographicIdentity(list, HSA_ID).getExtractId().getExtension());
	}

	@Test
	public void testCreateParameter() throws Exception {
		ParameterType param = EHRUtil.createParameter(TEST_VALUE_1, TEST_VALUE_2);
		assertEquals(TEST_VALUE_1, param.getName().getValue());
		assertEquals(TEST_VALUE_2, param.getValue().getValue());
	}

	@Test
	public void testFindElement() throws Exception {
		List<CONTENT> list = new ArrayList<CONTENT>();
		list.add(new ENTRY());
		list.add(new SECTION());
		list.add(new ENTRY());
		((ENTRY)list.get(0)).getItems().add(new ELEMENT());
		((ENTRY)list.get(2)).getItems().add(new ELEMENT());
		((ENTRY)list.get(0)).getItems().get(0).setMeaning(createCD(TEST_VALUE_1));
		((ENTRY)list.get(2)).getItems().get(0).setMeaning(createCD(TEST_VALUE_2));
		assertEquals(TEST_VALUE_1, EHRUtil.findEntryElement(list, TEST_VALUE_1).getMeaning().getCode());
	}
	
	private CD createCD(final String code) {
		final CD cd = new CD();
		cd.setCode(code);
		return cd;
	}
}

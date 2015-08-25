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
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import riv.clinicalprocess.healthcond.description._2.DiagnosisBodyType;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TS;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author torbjorncla
 */
public class DiagnosisMapperTest {
	
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	private static EHREXTRACT ehrExctract;
	private static final DiagnosisMapper mapper = Mockito.spy(new DiagnosisMapper());
	
	private final static CD cd = new CD();
	private final static ST st = new ST();
	
	private static final String TEST_DATA_1 = UUID.randomUUID().toString();
	private static final String TEST_DATA_2 = UUID.randomUUID().toString();
		
	@BeforeClass
	public static void init() throws JAXBException {
		ehrExctract = Util.loadEhrTestData(Util.DIAGNOSIS_TEST_FILE);
		ehrResp.getEhrExtract().add(ehrExctract);
		
		cd.setCode(TEST_DATA_1);
		cd.setId(TEST_DATA_2);
		
		st.setValue(TEST_DATA_1);
	}
	
	@Test
	public void testMapDiagnosisBodyType() throws Exception {
		boolean typeTouch = false;
		CD cd = null;
		
		for(COMPOSITION comp : ehrExctract.getAllCompositions()) {
			DiagnosisBodyType body = Mockito.spy(mapper.mapDiagnosisBodyType(comp));
			TS time = null;
			ST simpleText = null;
			for(CONTENT c : comp.getContent()) {
				ENTRY e = (ENTRY) c;
				for(ITEM i : e.getItems()) {
					ELEMENT elm = (ELEMENT) i;
					switch(i.getMeaning().getCode()) {
					case DiagnosisMapper.CODE_ELEMENT:
						cd = (CD) elm.getValue();
						assertEquals(cd.getCode(), body.getDiagnosisCode().getCode());
						assertEquals(cd.getCodeSystem(), body.getDiagnosisCode().getCodeSystem());
						assertEquals(cd.getDisplayName().getValue(), body.getDiagnosisCode().getDisplayName());
						break;
					case DiagnosisMapper.TIME_ELEMENT:
						time = (TS) elm.getValue();
						assertTrue(body.getDiagnosisTime().startsWith(time.getValue()));
						break;
					case DiagnosisMapper.TYPE_ELEMENT:
						simpleText = (ST) elm.getValue();
						assertEquals(simpleText.getValue(), body.getTypeOfDiagnosis().value());
						typeTouch = true;
						break;
					}
				}
			}
			assertTrue(typeTouch);
		}
	}
}

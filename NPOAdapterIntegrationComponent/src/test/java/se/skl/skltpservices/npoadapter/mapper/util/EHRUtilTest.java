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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.LoggerFactory;

import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description._2.ResultType;
import riv.clinicalprocess.healthcond.description.enums._2.ResultCodeEnum;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisType;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.EN;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.ENXP;
import se.rivta.en13606.ehrextract.v11.FUNCTIONALROLE;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.PERSON;
import se.rivta.en13606.ehrextract.v11.ParameterType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailTypeCodes;
import se.rivta.en13606.ehrextract.v11.SECTION;
import se.rivta.en13606.ehrextract.v11.SOFTWAREORDEVICE;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TS;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;

public class EHRUtilTest {
    
	private static final String TEST_VALUE_1 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_2 = UUID.randomUUID().toString();
	private static final String TEST_VALUE_3 = UUID.randomUUID().toString();
	
	private static final int TEST_INT_VALUE_1 = 500;


    @Test
    public void testGenericMapping() {
        final PersonIdType personIdType1 = new PersonIdType();
        personIdType1.setType("1234");
        personIdType1.setId("id");
        final II ii = EHRUtil.iiType(personIdType1);
        assertEquals(personIdType1.getId(), ii.getExtension());
        assertEquals(personIdType1.getType(), ii.getRoot());

        riv.clinicalprocess.healthcond.actoutcome._3.PersonIdType personIdType2 = new riv.clinicalprocess.healthcond.actoutcome._3.PersonIdType();
        personIdType2.setType(personIdType1.getType());
        personIdType2.setId(personIdType1.getId());
        final II ii2 = EHRUtil.iiType(personIdType1);
        assertEquals(personIdType2.getId(), ii2.getExtension());
        assertEquals(personIdType2.getType(), ii2.getRoot());

        final ResponseDetailType detail = new ResponseDetailType();

        detail.setText(createST("value"));
        detail.setTypeCode(ResponseDetailTypeCodes.I);

        final ResultType resultType = EHRUtil.resultType("logId", Arrays.asList(detail), ResultType.class);

        assertEquals(detail.getText().getValue(), resultType.getMessage());
        assertEquals(ResultCodeEnum.INFO, resultType.getResultCode());
        assertEquals("logId", resultType.getLogId());


    }

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
		assertNull(ts);
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

    private ST createST(final String value) {
        final ST st = new ST();
        st.setValue(value);
        return st;
    }

    //
    private GetDiagnosisType createDiagnosisTestRequest() {
        GetDiagnosisType requestRivta = new GetDiagnosisType();
        requestRivta.setPatientId(new PersonIdType());
        requestRivta.getPatientId().setId("PatientIdUnitTest");
        requestRivta.getPatientId().setType("PatientTypeUnitTest");
        requestRivta.setSourceSystemHSAId("SourceSystemHSAUnitTest");
        requestRivta.setTimePeriod(new DatePeriodType());
        requestRivta.getTimePeriod().setEnd("EndUnitTest");
        requestRivta.getTimePeriod().setStart("StartUnitTest");
        requestRivta.getCareUnitHSAId().add("CareUnitHSAIdUnitTest");
        return requestRivta;
    }

    @Test
    public void testRequestWithNoCareUnitHsaId() throws MapperException {
        final CD purpose = createCD("codeUnitTest");
        purpose.setCodeSystem("codeSystemUnitTest");
        final GetDiagnosisType req = createDiagnosisTestRequest();
        req.getCareUnitHSAId().clear();
        final RIV13606REQUESTEHREXTRACTRequestType reqOut = EHRUtil.requestType(req, purpose, "id", "producerHsaId");
        assertEquals(3, reqOut.getParameters().size());
    }

    @Test
	public void testRequestType() throws Exception {
        final CD purpose = createCD("codeUnitTest");
        purpose.setCodeSystem("codeSystemUnitTest");

        final GetDiagnosisType requestRivta = createDiagnosisTestRequest();

		final RIV13606REQUESTEHREXTRACTRequestType request13606 = EHRUtil.requestType(requestRivta, purpose, "id", "producerHsaId");

		assertEquals(request13606.getMeanings().get(0).getCode(), purpose.getCode());
		assertEquals(request13606.getMeanings().get(0).getCodeSystem(), purpose.getCodeSystem());
		assertEquals(request13606.getSubjectOfCareId().getRoot(), requestRivta.getPatientId().getType());
		assertEquals(request13606.getSubjectOfCareId().getExtension(), requestRivta.getPatientId().getId());
		assertEquals(request13606.getTimePeriod().getHigh().getValue(), requestRivta.getTimePeriod().getEnd());
		assertEquals(request13606.getTimePeriod().getLow().getValue(), requestRivta.getTimePeriod().getStart());
		assertEquals(3, request13606.getParameters().size());
		
		for (ParameterType param : request13606.getParameters()) {
	        switch (param.getName().getValue()) {
	        case "version":
                assertEquals("1.1", param.getValue().getValue());
                break;
	        case "hsa_id":
                assertEquals("producerHsaId", param.getValue().getValue());
                break;
	        case "transaction_id":
                assertEquals("id", param.getValue().getValue());
                break;
            default:
                fail("unexpected parameter value " + param.getName().getValue());
	        }
		}
	}

    @Test
    public void xmlGregorianCalendarToDateTest() throws DatatypeConfigurationException {
        final GregorianCalendar cal = new GregorianCalendar();
        final XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        final Date date = EHRUtil.toDate(xmlCal);
        assertEquals(cal.getTime(), date);
    }

    
    @Test
    public void storeCareUnitHsaIds() {
        MuleMessage mockMessage = mock(MuleMessage.class);

        GetAlertInformationType getAlertInformationType = new GetAlertInformationType();
        getAlertInformationType.getCareUnitHSAId().add("def");
        getAlertInformationType.getCareUnitHSAId().add("abc");
        getAlertInformationType.getCareUnitHSAId().add("def");
        getAlertInformationType.getCareUnitHSAId().add("ABC");
        getAlertInformationType.getCareUnitHSAId().add("");
        getAlertInformationType.getCareUnitHSAId().add(null);
        getAlertInformationType.getCareUnitHSAId().add("def");
        EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(getAlertInformationType, mockMessage, LoggerFactory.getLogger(EHRUtil.class));
        
        List<String> expectedStored = new ArrayList<String>();
        expectedStored.add("DEF");
        expectedStored.add("ABC");
        verify(mockMessage).setInvocationProperty(EHRUtil.CAREUNITHSAIDS, expectedStored);
    }
    
    
    @Test
    public void retrieveCareUnitHsaIds() {
        List<String> careUnitHsaIds = new ArrayList<String>();
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ABC");
        careUnitHsaIds.add("");
        careUnitHsaIds.add(null);
        careUnitHsaIds.add("def");
        
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getInvocationProperty(EHRUtil.CAREUNITHSAIDS)).thenReturn(careUnitHsaIds);
        List<String> retrievedCareUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(mockMessage, LoggerFactory.getLogger(EHRUtil.class));
        
        assertTrue(retrievedCareUnitHsaIds == careUnitHsaIds);
    }

    
    @Test
    public void retainWithNullFilterAndMissingHsaId() {
        
        COMPOSITION composition13606 = new COMPOSITION();
        composition13606.getOtherParticipations().add(new FUNCTIONALROLE());
        composition13606.getOtherParticipations().get(0).setFunction(new CD());
        composition13606.getOtherParticipations().get(0).getFunction().setCode(EHRUtil.INFORMATIONSÄGARE);
        composition13606.getOtherParticipations().get(0).setPerformer(new II());
        composition13606.getOtherParticipations().get(0).getPerformer().setExtension("");
        
        assertTrue(EHRUtil.retain(composition13606, null, LoggerFactory.getLogger(EHRUtil.class)));
    }

    

    @Test
    public void retainWithNullFilter() {
        
        COMPOSITION composition13606 = new COMPOSITION();
        composition13606.getOtherParticipations().add(new FUNCTIONALROLE());
        composition13606.getOtherParticipations().get(0).setFunction(new CD());
        composition13606.getOtherParticipations().get(0).getFunction().setCode(EHRUtil.INFORMATIONSÄGARE);
        composition13606.getOtherParticipations().get(0).setPerformer(new II());
        composition13606.getOtherParticipations().get(0).getPerformer().setExtension("abc");
        
        assertTrue(EHRUtil.retain(composition13606, null, LoggerFactory.getLogger(EHRUtil.class)));
    }

    
    @Test
    public void retainWithEmptyFilter() {
        
        COMPOSITION composition13606 = new COMPOSITION();
        composition13606.getOtherParticipations().add(new FUNCTIONALROLE());
        composition13606.getOtherParticipations().get(0).setFunction(new CD());
        composition13606.getOtherParticipations().get(0).getFunction().setCode(EHRUtil.INFORMATIONSÄGARE);
        composition13606.getOtherParticipations().get(0).setPerformer(new II());
        composition13606.getOtherParticipations().get(0).getPerformer().setExtension("abc");
        
        assertTrue(EHRUtil.retain(composition13606, new ArrayList<String>(), LoggerFactory.getLogger(EHRUtil.class)));
    }
    
    
    @Test
    public void retainWithFilterMatch() {
        
        COMPOSITION composition13606 = new COMPOSITION();
        composition13606.getOtherParticipations().add(new FUNCTIONALROLE());
        composition13606.getOtherParticipations().get(0).setFunction(new CD());
        composition13606.getOtherParticipations().get(0).getFunction().setCode(EHRUtil.INFORMATIONSÄGARE);
        composition13606.getOtherParticipations().get(0).setPerformer(new II());
        composition13606.getOtherParticipations().get(0).getPerformer().setExtension("abc");
        
        List<String> careUnitHsaIds = new ArrayList<String>();
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ABC");
        careUnitHsaIds.add("");
        careUnitHsaIds.add(null);
        careUnitHsaIds.add("def");
        
        assertTrue(EHRUtil.retain(composition13606, careUnitHsaIds, LoggerFactory.getLogger(EHRUtil.class)));
    }
    

    @Test
    public void retainWithFilterNoMatch() {
        
        COMPOSITION composition13606 = new COMPOSITION();
        composition13606.getOtherParticipations().add(new FUNCTIONALROLE());
        composition13606.getOtherParticipations().get(0).setFunction(new CD());
        composition13606.getOtherParticipations().get(0).getFunction().setCode(EHRUtil.INFORMATIONSÄGARE);
        composition13606.getOtherParticipations().get(0).setPerformer(new II());
        composition13606.getOtherParticipations().get(0).getPerformer().setExtension("zzz");
        
        List<String> careUnitHsaIds = new ArrayList<String>();
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ABC");
        careUnitHsaIds.add("");
        careUnitHsaIds.add(null);
        careUnitHsaIds.add("def");
        
        assertFalse(EHRUtil.retain(composition13606, careUnitHsaIds, LoggerFactory.getLogger(EHRUtil.class)));
    }

    
    @Test
    public void retainWithFilterMissingHsaId() {
        
        COMPOSITION composition13606 = new COMPOSITION();
        composition13606.getOtherParticipations().add(new FUNCTIONALROLE());
        composition13606.getOtherParticipations().get(0).setFunction(new CD());
        composition13606.getOtherParticipations().get(0).getFunction().setCode(EHRUtil.INFORMATIONSÄGARE);
        composition13606.getOtherParticipations().get(0).setPerformer(new II());
        composition13606.getOtherParticipations().get(0).getPerformer().setExtension(null);
        
        List<String> careUnitHsaIds = new ArrayList<String>();
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ABC");
        careUnitHsaIds.add("");
        careUnitHsaIds.add(null);
        careUnitHsaIds.add("def");
        
        assertFalse(EHRUtil.retain(composition13606, careUnitHsaIds, LoggerFactory.getLogger(EHRUtil.class)));
    }
    
}

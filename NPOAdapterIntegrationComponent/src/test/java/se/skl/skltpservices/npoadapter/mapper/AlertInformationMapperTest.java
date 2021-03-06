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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.clinicalprocess.healthcond.description._2.AlertInformationBodyType;
import riv.clinicalprocess.healthcond.description._2.CVType;
import riv.clinicalprocess.healthcond.description._2.OtherHypersensitivityType;
import riv.clinicalprocess.healthcond.description._2.PharmaceuticalHypersensitivityType;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationResponseType;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.test.Util;

public class AlertInformationMapperTest extends MapperTest {
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	private static EHREXTRACT ehrExtract;
	
    protected static final Logger log = LoggerFactory.getLogger(AlertInformationMapper.class);

	
	private static AlertInformationMapper mapper;
	
	private static CLUSTER testCluster;
	
	private static final String TEST_DATA_1 = UUID.randomUUID().toString();
	private static final String TEST_DATA_2 = UUID.randomUUID().toString();
	private static final String TEST_DATA_3 = UUID.randomUUID().toString();
	
	@BeforeClass
	public static void init() throws JAXBException {
		mapper = getAlertInformationMapper();
		ehrExtract = Util.loadEhrTestData(Util.ALERT_TEST_FILE);
		ehrResp.getEhrExtract().add(ehrExtract);
		
		//SensitivyElement.
		ELEMENT agensOverkanslighet = new ELEMENT();
		ELEMENT agensKod = new ELEMENT();
		agensOverkanslighet.setMeaning(cdType("upp-okh-aok-age"));
		agensOverkanslighet.setValue(EHRUtil.stType(TEST_DATA_1));
		agensKod.setMeaning(cdType("upp-okh-aok-agk"));
		agensKod.setValue(EHRUtil.stType(TEST_DATA_2));
		
		//Pharma
		ELEMENT substans = new ELEMENT();
		substans.setMeaning(cdType("upp-okh-lmo-sub"));
		substans.setValue(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		
		ELEMENT substansEjAtc = new ELEMENT();
		substansEjAtc.setMeaning(cdType("upp-okh-lmo-sea"));
		substansEjAtc.setValue(EHRUtil.stType(TEST_DATA_1));
		
		ELEMENT substansEjAtcComment = new ELEMENT();
		substansEjAtcComment.setMeaning(cdType("upp-okh-lmo-eak"));
		substansEjAtcComment.setValue(EHRUtil.stType(TEST_DATA_2));
		
		ELEMENT pharma = new ELEMENT();
		pharma.setMeaning(cdType("upp-okh-lmo-lmp"));
		pharma.setValue(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		
		
		//Create test-cluster
		testCluster = new CLUSTER();
		testCluster.getParts().add(agensOverkanslighet);
		testCluster.getParts().add(agensKod);
		testCluster.getParts().add(substans);
		testCluster.getParts().add(substansEjAtc);
		testCluster.getParts().add(substansEjAtcComment);
		testCluster.getParts().add(pharma);
	}
	
	static AlertInformationMapper getAlertInformationMapper() {
		AlertInformationMapper mapper = (AlertInformationMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_ALERT_2);
		return mapper;
	}

    @Test
    public void testMapResponseType() throws Exception {
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn(TEST_DATA_1);
        
        GetAlertInformationResponseType response = mapper.mapResponse(ehrResp, mockMessage);
        assertNotNull(response);
        assertFalse(response.getAlertInformation().isEmpty());
        assertNotNull(response.getAlertInformation().get(0).getAlertInformationBody());
        assertNotNull(response.getAlertInformation().get(0).getAlertInformationHeader());
        assertEquals("Givare"  , response.getAlertInformation().get(0).getAlertInformationHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareGiverHSAId());
        assertEquals("Enhet"   , response.getAlertInformation().get(0).getAlertInformationHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareUnitHSAId());
        assertEquals("uto"     , response.getAlertInformation().get(7).getAlertInformationBody().getRelatedAlertInformation().get(0).getTypeOfAlertInformationRelationship().getCode());
        assertEquals("Utökar"  , response.getAlertInformation().get(7).getAlertInformationBody().getRelatedAlertInformation().get(0).getTypeOfAlertInformationRelationship().getDisplayName());
        assertEquals("ers"     , response.getAlertInformation().get(9).getAlertInformationBody().getRelatedAlertInformation().get(0).getTypeOfAlertInformationRelationship().getCode());
        assertEquals("Ersätter", response.getAlertInformation().get(9).getAlertInformationBody().getRelatedAlertInformation().get(0).getTypeOfAlertInformationRelationship().getDisplayName());
    }
    
    @Test
    public void testTimeElement() throws Exception {
    	MuleMessage mockMessage = mock(MuleMessage.class);
    	when(mockMessage.getUniqueId()).thenReturn("messageId");
    	final GetAlertInformationResponseType response = mapper.mapResponse(ehrResp, mockMessage);
    	
    	final AlertInformationBodyType body = response.getAlertInformation().get(0).getAlertInformationBody();
    	assertEquals("20080103", body.getAscertainedDate());
    	assertEquals("20080104153000", body.getVerifiedTime());
    	
    }

    @Test
    public void testMapResponseTypeNullFilter() throws Exception {
        
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getInvocationProperty(EHRUtil.CAREUNITHSAIDS)).thenReturn(null);
        when(mockMessage.getUniqueId()).thenReturn(TEST_DATA_1);
        
        GetAlertInformationResponseType response = mapper.mapResponse(ehrResp, mockMessage);
        assertNotNull(response);
        assertFalse(response.getAlertInformation().isEmpty());
        assertNotNull(response.getAlertInformation().get(0).getAlertInformationBody());
        assertNotNull(response.getAlertInformation().get(0).getAlertInformationHeader());
        assertEquals("Givare", response.getAlertInformation().get(0).getAlertInformationHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareGiverHSAId());
        assertEquals("Enhet", response.getAlertInformation().get(0).getAlertInformationHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareUnitHSAId());
    }
    
	@Test
	public void testMapResponseTypeFilter() throws Exception {
	    
	    List<String> careUnitHsaIds = new ArrayList<String>();
	    careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ABC");
        careUnitHsaIds.add("");
        careUnitHsaIds.add(null);
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ENQUIRY-VE-1");
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ENHET");
	    
	    MuleMessage mockMessage = mock(MuleMessage.class);
	    when(mockMessage.getInvocationProperty(EHRUtil.CAREUNITHSAIDS)).thenReturn(careUnitHsaIds);
	    when(mockMessage.getUniqueId()).thenReturn(TEST_DATA_1);
	    
		GetAlertInformationResponseType response = mapper.mapResponse(ehrResp, mockMessage);
		assertNotNull(response);
		assertFalse(response.getAlertInformation().isEmpty());
		assertNotNull(response.getAlertInformation().get(0).getAlertInformationBody());
		assertNotNull(response.getAlertInformation().get(0).getAlertInformationHeader());
        assertEquals("Givare", response.getAlertInformation().get(0).getAlertInformationHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareGiverHSAId());
        assertEquals("Enhet", response.getAlertInformation().get(0).getAlertInformationHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareUnitHSAId());
	}
	
    @Test
    public void testMapResponseTypeFilterNoMatches() throws Exception {
        
        List<String> careUnitHsaIds = new ArrayList<String>();
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("ABC");
        careUnitHsaIds.add("");
        careUnitHsaIds.add(null);
        careUnitHsaIds.add("def");
        careUnitHsaIds.add("abc");
        careUnitHsaIds.add("def");
        
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getInvocationProperty(EHRUtil.CAREUNITHSAIDS)).thenReturn(careUnitHsaIds);
        when(mockMessage.getUniqueId()).thenReturn(TEST_DATA_1);
        GetAlertInformationResponseType response = mapper.mapResponse(ehrResp, mockMessage);
        assertNotNull(response);
        assertTrue(response.getAlertInformation().isEmpty());
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
	
	private void verifyCDType(final CD cd, final CVType cv) {
		assertEquals(cv.getCode(), cv.getCode());
		assertEquals(cv.getCodeSystem(), cv.getCodeSystem());
		assertEquals(cv.getDisplayName(), cv.getDisplayName());
	}
	
	@Test
	public void testMapOtherHypersensititivyType() throws Exception {
		final OtherHypersensitivityType testObj = mapper.mapOtherHypersensititivyType(testCluster);
		assertEquals(TEST_DATA_1, testObj.getHypersensitivityAgent());
		assertEquals(TEST_DATA_2, testObj.getHypersensitivityAgentCode().getOriginalText());
	}

	@Test
	public void testMapPharmaceuticalHypersensitivity() throws Exception {
		final PharmaceuticalHypersensitivityType testObj = mapper.mapPharmaceuticalHypersensitivity(testCluster);
		assertEquals(TEST_DATA_1, testObj.getAtcSubstance().getCode());
		assertEquals(TEST_DATA_2, testObj.getAtcSubstance().getCodeSystem());
		assertEquals(TEST_DATA_3, testObj.getAtcSubstance().getDisplayName());
		assertEquals(TEST_DATA_1, testObj.getNonATCSubstance());
		assertEquals(TEST_DATA_2, testObj.getNonATCSubstanceComment());
		assertFalse(testObj.getPharmaceuticalProductId().isEmpty());
		assertEquals(TEST_DATA_1, testObj.getPharmaceuticalProductId().get(0).getCode());
		assertEquals(TEST_DATA_2, testObj.getPharmaceuticalProductId().get(0).getCodeSystem());
		assertEquals(TEST_DATA_3, testObj.getPharmaceuticalProductId().get(0).getDisplayName());
	}

	@Test
	public void testMapHypersensitivity() throws Exception {
		final AlertInformationBodyType testObj = new AlertInformationBodyType();
		
		final ELEMENT typeOfHypersens = new ELEMENT();
		final ELEMENT alvarlighetsGrad = new ELEMENT();
		final ELEMENT visshetsGrad = new ELEMENT();
		
		typeOfHypersens.setMeaning(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		alvarlighetsGrad.setMeaning(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		visshetsGrad.setMeaning(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		
		mapper.mapHypersensitivity(testObj, typeOfHypersens, "upp-okh-typ");
		verifyCDType(typeOfHypersens.getMeaning(), testObj.getHypersensitivity().getTypeOfHypersensitivity());
		mapper.mapHypersensitivity(testObj, alvarlighetsGrad, "upp-okh-avg");
		verifyCDType(alvarlighetsGrad.getMeaning(), testObj.getHypersensitivity().getDegreeOfSeverity());
		mapper.mapHypersensitivity(testObj, visshetsGrad, "upp-okh-vhg");
		verifyCDType(visshetsGrad.getMeaning(), testObj.getHypersensitivity().getDegreeOfCertainty());
	}

	@Test
	public void testMapSeriousDisease() throws Exception {
		final AlertInformationBodyType testObj = new AlertInformationBodyType();
		final ELEMENT alvarligSjukdom = new ELEMENT();
		alvarligSjukdom.setMeaning(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		mapper.mapSeriousDisease(testObj,  alvarligSjukdom, "upp-uas-invalid");
		assertNull(testObj.getSeriousDisease().getDisease());
		mapper.mapSeriousDisease(testObj, alvarligSjukdom, "upp-uas-sjd");
		verifyCDType(alvarligSjukdom.getMeaning(), testObj.getSeriousDisease().getDisease());
	}

	@Test
	public void testMapTreatment() throws Exception {
		final AlertInformationBodyType testObj = new AlertInformationBodyType();
		
		final ELEMENT behandling = new ELEMENT();
		final ELEMENT lakemedelsBehandling = new ELEMENT();
		final ELEMENT behandlingsKod = new ELEMENT();
		
		behandling.setValue(EHRUtil.stType(TEST_DATA_1));
		lakemedelsBehandling.setValue(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		behandlingsKod.setMeaning(cdType(TEST_DATA_1, TEST_DATA_2, TEST_DATA_3));
		
		mapper.mapTreatment(testObj, behandling, "upp-ube-beh");
		assertEquals(TEST_DATA_1, testObj.getTreatment().getTreatmentDescription());
		mapper.mapTreatment(testObj,  lakemedelsBehandling, "upp-ube-lbe");
		verifyCDType(lakemedelsBehandling.getMeaning(), testObj.getTreatment().getPharmaceuticalTreatment());
		mapper.mapTreatment(testObj, behandlingsKod, "upp-ube-kod");
		verifyCDType(behandlingsKod.getMeaning(), testObj.getTreatment().getTreatmentCode());
	}

	@Test
	public void testMapCommunicableDisease() throws Exception {
		final AlertInformationBodyType testObj = new AlertInformationBodyType();
		
		final CLUSTER smittsamSjukdom = new CLUSTER();
		final ELEMENT smittsamSjukdomKod = new ELEMENT();
		final ELEMENT smittsamSjukdomVag = new ELEMENT();
		
		smittsamSjukdom.setMeaning(cdType("upp-arb"));
		smittsamSjukdomKod.setMeaning(cdType("upp-arb-smf-sjd", TEST_DATA_2, TEST_DATA_3));
		smittsamSjukdomVag.setMeaning(cdType("upp-arb-smf-vag", TEST_DATA_2, TEST_DATA_3));
		smittsamSjukdom.getParts().add(smittsamSjukdomKod);
		smittsamSjukdom.getParts().add(smittsamSjukdomVag);
		
		mapper.mapCommunicableDisease(testObj, smittsamSjukdom);
		verifyCDType(smittsamSjukdomKod.getMeaning(), testObj.getCommunicableDisease().getCommunicableDiseaseCode());
		verifyCDType(smittsamSjukdomVag.getMeaning(), testObj.getCommunicableDisease().getRouteOfTransmission());
	}

	@Test
	public void testMapRestrictionOfCare() throws Exception {
		final AlertInformationBodyType testObj = new AlertInformationBodyType();
		final ELEMENT vardbegransning = new ELEMENT();
		vardbegransning.setMeaning(cdType("upp-vbe-vbe"));
		vardbegransning.setValue(EHRUtil.stType(TEST_DATA_1));
		mapper.mapRestrictionOfCare(testObj, vardbegransning);
		assertEquals(TEST_DATA_1, testObj.getRestrictionOfCare().getRestrictionOfCareComment());
	}

	@Test
	public void testMapUnstructuredAlertInformation() throws Exception {
		final ELEMENT ostruktTitel = new ELEMENT();
		final ELEMENT ostruktText = new ELEMENT();
		ostruktTitel.setMeaning(cdType("upp-est-rub"));
		ostruktText.setMeaning(cdType("upp-est-inh"));
		ostruktTitel.setValue(EHRUtil.stType(TEST_DATA_1));
		ostruktText.setValue(EHRUtil.stType(TEST_DATA_2));
		
		final AlertInformationBodyType testObj = new AlertInformationBodyType();
		mapper.mapUnstructuredAlertInformation(testObj, ostruktTitel);
		mapper.mapUnstructuredAlertInformation(testObj, ostruktText);
		assertEquals(TEST_DATA_1, testObj.getUnstructuredAlertInformation().getUnstructuredAlertInformationHeading());
		assertEquals(TEST_DATA_2, testObj.getUnstructuredAlertInformation().getUnstructuredAlertInformationContent());
	}

	@Test
	public void testMapBodyType() throws Exception {
		final COMPOSITION comp = new COMPOSITION();
		final ENTRY content = new ENTRY();
		
		final ELEMENT konstDatum = new ELEMENT();
		konstDatum.setMeaning(cdType("upp-upp-kdt"));
		konstDatum.setValue(EHRUtil.tsType(TEST_DATA_1));
		final ELEMENT verifDatum = new ELEMENT();
		verifDatum.setMeaning(cdType("upp-upp-vtp"));
		verifDatum.setValue(EHRUtil.tsType(TEST_DATA_2));
		final ELEMENT giltlighetsTid = new ELEMENT();
		giltlighetsTid.setMeaning(cdType("upp-upp-ght"));
		final IVLTS ivlts = new IVLTS();
		ivlts.setHigh(EHRUtil.tsType(TEST_DATA_1));
		ivlts.setLow(EHRUtil.tsType(TEST_DATA_2));
		giltlighetsTid.setValue(ivlts);
		final ELEMENT uppmarksamKom = new ELEMENT();
		uppmarksamKom.setMeaning(cdType("upp-upp-kom"));
		uppmarksamKom.setValue(EHRUtil.stType(TEST_DATA_3));
		
		content.getItems().add(uppmarksamKom);
		content.getItems().add(giltlighetsTid);
		content.getItems().add(verifDatum);
		content.getItems().add(konstDatum);
		comp.getContent().add(content);
		
		AlertInformationBodyType testObj = mapper.mapBodyType(comp);
		assertEquals(TEST_DATA_1.substring(0, "yyyyMMdd".length()), testObj.getAscertainedDate());
		assertEquals(TEST_DATA_2, testObj.getVerifiedTime());
		assertEquals(TEST_DATA_1, testObj.getValidityTimePeriod().getEnd());
		assertEquals(TEST_DATA_2, testObj.getValidityTimePeriod().getStart());
		assertEquals(TEST_DATA_3, testObj.getAlertInformationComment());
	}

	
	
    @Test
    public void defaultAscertainedDate() {
        String responseXml = getRivtaXml(mapper, Util.ALERT_TEST_FILE, true);
        assertTrue (responseXml.contains("<ns2:ascertainedDate>20150302</ns2:ascertainedDate"));
        assertTrue (responseXml.contains("<ns2:validityTimePeriod><ns2:start>20150302011259</ns2:"));
    }
	
}

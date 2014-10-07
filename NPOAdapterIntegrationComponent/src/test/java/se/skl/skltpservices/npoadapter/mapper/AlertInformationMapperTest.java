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

import static org.junit.Assert.*;

import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import riv.clinicalprocess.healthcond.description._2.AlertInformationBodyType;
import riv.clinicalprocess.healthcond.description._2.CVType;
import riv.clinicalprocess.healthcond.description._2.OtherHypersensitivityType;
import riv.clinicalprocess.healthcond.description._2.PharmaceuticalHypersensitivityType;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.test.Util;
import se.rivta.en13606.ehrextract.v11.*;

public class AlertInformationMapperTest {
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	private static EHREXTRACT ehrExctract;
	
	private static AlertInformationMapper mapper;
	
	private static CLUSTER testCluster;
	
	private static final String TEST_DATA_1 = UUID.randomUUID().toString();
	private static final String TEST_DATA_2 = UUID.randomUUID().toString();
	private static final String TEST_DATA_3 = UUID.randomUUID().toString();
	
	@BeforeClass
	public static void init() {
		mapper = new AlertInformationMapper();
		ehrExctract = Util.loadEhrTestData(Util.ALERT_TEST_FILE);
		ehrResp.getEhrExtract().add(ehrExctract);
		
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
	
	@Test
	public void testMapResponseType() throws Exception {
		GetAlertInformationResponseType resp = mapper.mapResponseType(ehrResp, TEST_DATA_1);
		assertNotNull(resp);
		assertFalse(resp.getAlertInformation().isEmpty());
		assertNotNull(resp.getAlertInformation().get(0).getAlertInformationBody());
		assertNotNull(resp.getAlertInformation().get(0).getAlertInformationHeader());
	}
	
	private void print(GetAlertInformationResponseType resp) throws JAXBException {
		JAXBContext context =
		        JAXBContext.newInstance(GetAlertInformationResponseType.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(new JAXBElement<GetAlertInformationResponseType>(new QName("uri","local"), GetAlertInformationResponseType.class, resp), System.out);
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
		konstDatum.setValue(EHRUtil.stType(TEST_DATA_1));
		final ELEMENT verifDatum = new ELEMENT();
		verifDatum.setMeaning(cdType("upp-upp-vtp"));
		verifDatum.setValue(EHRUtil.stType(TEST_DATA_2));
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
		assertEquals(TEST_DATA_1, testObj.getAscertainedDate());
		assertEquals(TEST_DATA_2, testObj.getVerifiedTime());
		assertEquals(TEST_DATA_1, testObj.getValidityTimePeriod().getEnd());
		assertEquals(TEST_DATA_2, testObj.getValidityTimePeriod().getStart());
		assertEquals(TEST_DATA_3, testObj.getAlertInformationComment());
	}
}

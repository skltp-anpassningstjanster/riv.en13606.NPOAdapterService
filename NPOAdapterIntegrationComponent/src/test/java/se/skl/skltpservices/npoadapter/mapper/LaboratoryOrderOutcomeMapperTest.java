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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.test.Util;

public class LaboratoryOrderOutcomeMapperTest extends MapperTest {

    private static final Logger log = LoggerFactory.getLogger(LaboratoryOrderOutcomeMapperTest.class);
    
    private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
    private static EHREXTRACT ehrExtract;
    private static LaboratoryOrderOutcomeMapper mapper;

    @BeforeClass
    public static void init() throws JAXBException {
        ehrExtract = Util.loadEhrTestData(Util.LAB_TEST_FILE_1);
        ehrResp.getEhrExtract().add(ehrExtract);
        mapper = Mockito.spy(getLaboratoryOrderOutcomeMapper());
    }

    private static LaboratoryOrderOutcomeMapper getLaboratoryOrderOutcomeMapper() {
    	LaboratoryOrderOutcomeMapper mapper = (LaboratoryOrderOutcomeMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_LABORATORY_3);
    	return mapper;
    }
    
    @Test
    public void testMapResponseType() throws Exception {
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        final GetLaboratoryOrderOutcomeResponseType type = mapper.mapResponse(ehrResp, mockMessage);
        assertNotNull(type.getResult());
        assertEquals("Klinisk kemi", type.getLaboratoryOrderOutcome().get(0).getLaboratoryOrderOutcomeBody().getDiscipline());
    }

    @Test
    public void mapResponse1() {

        String responseXml = getRivtaXml(getLaboratoryOrderOutcomeMapper(), Util.LAB_TEST_FILE_1, true);
        
        assertTrue(responseXml.contains("<GetLaboratoryOrderOutcomeResponse"));
        
        // header
        
        assertTrue(responseXml.contains("laboratoryOrderOutcomeHeader"));
        assertTrue(responseXml.contains("laboratoryOrderOutcomeHeader><ns2:documentId>SE2321000164-1002Kem09042908060036603115<"));
        assertTrue(responseXml.contains("documentId><ns2:sourceSystemHSAId>SE2321000164-1002<"));
        assertTrue(responseXml.contains("<ns2:documentTime>20080512131000</ns2:documentTime>"));
        assertTrue(responseXml.contains("id><ns2:type>1.2.752.129.2.1.3.1<"));
        assertTrue(responseXml.contains("type></ns2:patientId><ns2:accountableHealthcareProfessional><ns2:authorTime>20090429132009<"));
        assertTrue(responseXml.contains("authorTime><ns2:healthcareProfessionalHSAId>"));
        assertTrue(responseXml.contains("healthcareProfessionalHSAId><ns2:healthcareProfessionalName>Gunvor Martinsson, distriktsläkare<"));
        assertTrue(responseXml.contains("healthcareProfessionalName><ns2:healthcareProfessionalRoleCode><ns2:code>201011<"));
        assertTrue(responseXml.contains("code><ns2:codeSystem>1.2.752.129.2.2.1.4<"));
        assertTrue(responseXml.contains("codeSystem><ns2:displayName>Distriktsläkare/Specialist allmänmedicin<"));
        assertTrue(responseXml.contains("healthcareProfessionalRoleCode><ns2:healthcareProfessionalOrgUnit><ns2:orgUnitHSAId>SE2321000164-7381037594544"));
        assertTrue(responseXml.contains("orgUnitHSAId><ns2:orgUnitName>Kumla Vårdcentral Rehabiliteringsenheten<"));
        assertTrue(responseXml.contains("healthcareProfessionalOrgUnit><ns2:healthcareProfessionalCareUnitHSAId>SE2321000164-12ab<"));
        //assertTrue(responseXml.contains("healthcareProfessionalCareUnitHSAId><ns2:healthcareProfessionalCareGiverHSAId>SE2321000164-ab12</ns2:healthcareProfessionalCareGiverHSAId></ns2:accountableHealthcareProfessional><ns2:legalAuthenticator><ns2:signatureTime>20090429132009"));
        assertTrue(responseXml.contains("legalAuthenticatorHSAId>SE2321000164-73810375900035709176621<"));
        assertTrue(responseXml.contains("legalAuthenticatorName>Gunvor Martinsson, distriktsläkare<"));
        assertTrue(responseXml.contains("legalAuthenticator><ns2:approvedForPatient>false<"));
        assertTrue(responseXml.contains("approvedForPatient><ns2:careContactId>123456-7890-00010"));

         // body
        
        assertTrue(responseXml.contains("laboratoryOrderOutcomeBody><"));
        assertTrue(responseXml.contains("laboratoryOrderOutcomeBody><ns2:resultType>Definitivsvar<"));
        assertTrue(responseXml.contains("resultType><ns2:registrationTime>20090429132009<"));
        assertTrue(responseXml.contains("registrationTime><ns2:discipline>Klinisk kemi<"));
        assertTrue(responseXml.contains("discipline><ns2:resultReport>Någon utlåtandetext<"));
        assertTrue(responseXml.contains("resultReport><ns2:resultComment>Någon text som kommenterar svaret.<"));
        assertTrue(responseXml.contains("resultComment><ns2:accountableHealthcareProfessional><ns2:authorTime>20080512131000<"));
        assertTrue(responseXml.contains("authorTime><ns2:healthcareProfessionalHSAId>SE2321000164-73810375900035709176621<"));
        assertTrue(responseXml.contains("healthcareProfessionalHSAId><ns2:healthcareProfessionalName>Gunvor Martinsson, distriktsläkare<"));
        assertTrue(responseXml.contains("healthcareProfessionalName><ns2:healthcareProfessionalRoleCode><ns2:code>201011<"));
        assertTrue(responseXml.contains("code><ns2:codeSystem>1.2.752.129.2.2.1.4<"));
        assertTrue(responseXml.contains("codeSystem><ns2:displayName>Distriktsläkare/Specialist allmänmedicin<"));
        assertTrue(responseXml.contains("healthcareProfessionalRoleCode><ns2:healthcareProfessionalOrgUnit><ns2:orgUnitHSAId>SE2321000164-7381037594544"));
        assertTrue(responseXml.contains("orgUnitHSAId><ns2:orgUnitName>Kumla Vårdcentral Rehabiliteringsenheten<"));
        assertTrue(responseXml.contains("healthcareProfessionalOrgUnit><ns2:healthcareProfessionalCareUnitHSAId>SE2321000164-12ab<"));
        assertTrue(responseXml.contains("healthcareProfessionalCareUnitHSAId><ns2:healthcareProfessionalCareGiverHSAId>SE2321000164-ab12<"));
        assertTrue(responseXml.contains("accountableHealthcareProfessional><ns2:analysis><ns2:analysisId><ns2:root>1.2.752.129.2.1.2.1"));
        assertTrue(responseXml.contains("root><ns2:extension>SE2321000164-1002Kem09042908060036603115<"));
        assertTrue(responseXml.contains("analysisId><ns2:analysisTime><ns2:start>20090429083600<"));
        assertTrue(responseXml.contains("start><ns2:end>20090429083600</ns2:end></ns2:analysisTime"));
        assertTrue(responseXml.contains("analysisTime><ns2:analysisCode><ns2:code>SWE05074"));
        assertTrue(responseXml.contains("code><ns2:codeSystem>1.2.752.108.1<"));
        assertTrue(responseXml.contains("codeSystem><ns2:displayName>-Hemoglobin<"));
        assertTrue(responseXml.contains("analysisCode><ns2:analysisStatus>Avklarad<"));
        assertTrue(responseXml.contains("analysisStatus><ns2:analysisComment>En text som kommenterar analysresultatet.<"));
        assertTrue(responseXml.contains("analysisComment><ns2:specimen>B<"));
        assertFalse(responseXml.contains("specimen><method>")); // no und-kkm-uat-met test data in qa
        assertTrue(responseXml.contains("specimen><ns2:relationToAnalysis><ns2:analysisId><ns2:root>testRoot<"));
        assertTrue(responseXml.contains("root><ns2:extension>testExtentsion</ns2:extension></ns2:analysisId>"));
        assertTrue(responseXml.contains("relationToAnalysis><ns2:analysisOutcome><ns2:outcomeValue>0,35<"));
        assertTrue(responseXml.contains("outcomeValue><ns2:outcomeUnit>µkat/L<"));
        assertTrue(responseXml.contains("outcomeUnit><ns2:observationTime>20090429131800<"));
        assertTrue(responseXml.contains("observationTime><ns2:pathologicalFlag>false<"));
        assertTrue(responseXml.contains("pathologicalFlag><ns2:outcomeDescription>Text som beskriver värdet ytterligare."));
        assertTrue(responseXml.contains("pathologicalFlag><ns2:referenceInterval>0,75<"));
        assertFalse(responseXml.contains("referencePopulation")); // no und-kkm-utf-pop test data in qa
        assertTrue(responseXml.contains("order><ns2:orderId>123456-7890-00020<"));
        assertTrue(responseXml.contains("orderId><ns2:orderReason>Patologiskt?<"));
    }

    @Test
    public void mapResponse2() {
        String responseXml = getRivtaXml(getLaboratoryOrderOutcomeMapper(), Util.LAB_TEST_FILE_2, true);
        assertTrue(responseXml.contains("signatureTime"));
    }
    
}

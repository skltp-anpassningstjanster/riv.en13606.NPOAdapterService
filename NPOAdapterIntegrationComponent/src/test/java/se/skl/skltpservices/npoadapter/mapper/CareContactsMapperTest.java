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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.clinicalprocess.logistics.logistics._2.CareContactBodyType;
import riv.clinicalprocess.logistics.logistics._2.CareContactType;
import riv.clinicalprocess.logistics.logistics._2.HealthcareProfessionalType;
import riv.clinicalprocess.logistics.logistics._2.OrgUnitType;
import riv.clinicalprocess.logistics.logistics._2.PatientSummaryHeaderType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * Created by Peter on 2014-07-28.
 */
public class CareContactsMapperTest {

    private static final Logger log = LoggerFactory.getLogger(CareContactsMapperTest.class);

    private static SimpleDateFormat timeStampFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    static {
        timeStampFormatter.setLenient(false);
    }

    private static EHREXTRACT ehrextract;

    @BeforeClass
    public static void init() throws JAXBException {
        ehrextract = Util.loadEhrTestData(Util.CARECONTACTS_TEST_FILE_1);
    }

    // Make it easy to dump the resulting response after createTS (for dev purposes only)
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetCareContactsResponseType type;
    }

    //
    private void dump(final GetCareContactsResponseType responseType) {
        Root root = new Root();
        root.type = responseType;
        //Util.dump(root);
    }

	private CareContactsMapper getCareContactsMapper() {
		CareContactsMapper mapper = (CareContactsMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_CARECONTACTS_2);
		return mapper;
	}

    @Test
    public void testMapFromEhrToCareContracts() {
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        CareContactsMapper mapper = getCareContactsMapper();
        GetCareContactsResponseType responseType = mapper.mapResponse(Arrays.asList(ehrextract), mockMessage);
        assertNotNull(responseType);

        dump(responseType);

        assertNotNull(responseType.getCareContact());
        assertEquals(4, responseType.getCareContact().size());

        for (final CareContactType careContactType : responseType.getCareContact()) {
            verifyCareContactHeader(careContactType.getCareContactHeader());
            verifyCareContactBody(careContactType.getCareContactBody());
        }
    }


    private void verifyCareContactHeader(PatientSummaryHeaderType careContactHeader) {
        assertNotNull(careContactHeader.getDocumentId());
        assertNotNull(careContactHeader.getSourceSystemHSAId());
        verifyAccountableHealthcareProfessional(careContactHeader.getAccountableHealthcareProfessional());

        assertNotNull(careContactHeader.getPatientId());
        assertFalse(careContactHeader.isApprovedForPatient());
    }

    private void verifyAccountableHealthcareProfessional(HealthcareProfessionalType accountableHealthcareProfessional) {
        assertNotNull(accountableHealthcareProfessional);
        verifyTimeStampType(accountableHealthcareProfessional.getAuthorTime(), false);
        assertNotNull(accountableHealthcareProfessional.getHealthcareProfessionalHSAId());
        assertNotNull(accountableHealthcareProfessional.getHealthcareProfessionalName());
        verifyOrgUnit(accountableHealthcareProfessional.getHealthcareProfessionalOrgUnit());

        assertNull(accountableHealthcareProfessional.getHealthcareProfessionalRoleCode());

        assertNotNull(accountableHealthcareProfessional.getHealthcareProfessionalCareGiverHSAId());
        assertNotNull(accountableHealthcareProfessional.getHealthcareProfessionalCareUnitHSAId());
    }

    private void verifyOrgUnit(OrgUnitType healthcareProfessionalOrgUnit) {
        assertNotNull(healthcareProfessionalOrgUnit);
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitEmail());
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitAddress());
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitName());
        assertNull(healthcareProfessionalOrgUnit.getOrgUnitLocation());
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitTelecom());
    }


    private void verifyCareContactBody(CareContactBodyType careContactBody) {
        assertNotNull(careContactBody.getCareContactCode());
        assertTrue(1 <= careContactBody.getCareContactCode() && 5 >= careContactBody.getCareContactCode());
        assertNull(careContactBody.getCareContactReason());
        assertNotNull(careContactBody.getCareContactStatus());
        assertTrue(1 <= careContactBody.getCareContactStatus() && 5 >= careContactBody.getCareContactStatus());

        verifyOrgUnit(careContactBody.getCareContactOrgUnit());

        assertNotNull(careContactBody.getCareContactTimePeriod());

        verifyTimeStampType(careContactBody.getCareContactTimePeriod().getStart(), false);
        verifyTimeStampType(careContactBody.getCareContactTimePeriod().getEnd(), true);
    }

    private void verifyTimeStampType(String timestamp, boolean nullable) {
        if (!nullable) {
            assertNotNull(timestamp);
        }
        if (timestamp != null) {
            try {
                timeStampFormatter.parse(timestamp);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    
    @Test
    public void mapResponseJämtland() {

        CareContactsMapper objectUnderTest = getCareContactsMapper();

        // load xml from test file - this contains an <ehr_extract/>
        StringBuilder xml13606Response = new StringBuilder();
        try (@SuppressWarnings("resource") Scanner inputStringScanner = new Scanner(getClass().getResourceAsStream(Util.CARECONTACTS_TEST_FILE_2), "UTF-8").useDelimiter("\\z")) {
            while (inputStringScanner.hasNext()) {
                xml13606Response.append(inputStringScanner.next());
            }
        }

        // wrap the <ehr_extract/> in a <RIV13606REQUEST_EHR_EXTRACT_response/>
        // opening tag
        xml13606Response.insert("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length(),"<RIV13606REQUEST_EHR_EXTRACT_response xmlns=\"urn:riv13606:v1.1\">");
        // closing tag
        xml13606Response.append("</RIV13606REQUEST_EHR_EXTRACT_response>\n");
        
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        Reader xmlReader = new StringReader(xml13606Response.toString());
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(xmlReader);

            MuleMessage mockMuleMessage = mock(MuleMessage.class);
            when(mockMuleMessage.getPayload()).thenReturn(xmlStreamReader);
            // argumentCaptor will capture the converted xml
            ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);

            // method being exercised
            objectUnderTest.mapResponse(mockMuleMessage);
            
            // verifications & assertions
            verify(mockMuleMessage).setPayload(argumentCaptor.capture());
            String responseXml = (String)argumentCaptor.getValue();
            log.debug(responseXml);
            log.debug("");
            
            int occurrences = 0;
            Pattern p = Pattern.compile("careContactBody");
            Matcher m = p.matcher(responseXml);
            while (m.find()) {
                occurrences++;
            }
            assertEquals(22,occurrences);
            
            assertTrue(responseXml.contains("<GetCareContactsResponse"));
            
        } catch (XMLStreamException e) {
            fail(e.getLocalizedMessage());
        } catch (MapperException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    @Test
    public void mapResponseNorrbotten() {

        CareContactsMapper objectUnderTest = getCareContactsMapper();

        // load xml from test file - this contains an <ehr_extract/>
        StringBuilder xml13606Response = new StringBuilder();
        try (@SuppressWarnings("resource") Scanner inputStringScanner = new Scanner(getClass().getResourceAsStream(Util.CARECONTACTS_TEST_FILE_3), "UTF-8").useDelimiter("\\z")) {
            while (inputStringScanner.hasNext()) {
                xml13606Response.append(inputStringScanner.next());
            }
        }

        // wrap the <ehr_extract/> in a <RIV13606REQUEST_EHR_EXTRACT_response/>
        // opening tag
        xml13606Response.insert("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length(),"<RIV13606REQUEST_EHR_EXTRACT_response xmlns=\"urn:riv13606:v1.1\">");
        // closing tag
        xml13606Response.append("</RIV13606REQUEST_EHR_EXTRACT_response>\n");
        
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        Reader xmlReader = new StringReader(xml13606Response.toString());
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(xmlReader);

            MuleMessage mockMuleMessage = mock(MuleMessage.class);
            when(mockMuleMessage.getPayload()).thenReturn(xmlStreamReader);
            // argumentCaptor will capture the converted xml
            ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);

            // method being exercised
            objectUnderTest.mapResponse(mockMuleMessage);
            
            // verifications & assertions
            verify(mockMuleMessage).setPayload(argumentCaptor.capture());
            String responseXml = (String)argumentCaptor.getValue();
            log.debug(responseXml);
            log.debug("");
            
            int occurrences = 0;
            Pattern p = Pattern.compile("careContactBody");
            Matcher m = p.matcher(responseXml);
            while (m.find()) {
                occurrences++;
            }
            assertEquals(38,occurrences);
            
            assertTrue(responseXml.contains("<GetCareContactsResponse"));
            
        } catch (XMLStreamException e) {
            fail(e.getLocalizedMessage());
        } catch (MapperException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
}

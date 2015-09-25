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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin Flower
 */
public class CareDocumentationMapperTest {

    
    protected static final Logger log = LoggerFactory.getLogger(CareDocumentationMapper.class);
    
	private CareDocumentationMapper getCareDocumentationMapper() {
		CareDocumentationMapper mapper = (CareDocumentationMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_CAREDOCUMENTATION_2);
		return mapper;
	}
	
    @Test
    public void mapResponse() {

        CareDocumentationMapper objectUnderTest = getCareDocumentationMapper();

        // load xml from test file - this contains an <ehr_extract/>
        StringBuilder xml13606Response = new StringBuilder();
        try (@SuppressWarnings("resource") Scanner inputStringScanner = new Scanner(getClass().getResourceAsStream(Util.CAREDOCUMENTATION_TEST_FILE), "UTF-8").useDelimiter("\\z")) {
            while (inputStringScanner.hasNext()) {
                xml13606Response.append(inputStringScanner.next());
            }
        }

        // wrap the <ehr_extract/> in a <RIV13606REQUEST_EHR_EXTRACT_response/>
        // opening tag
        xml13606Response.insert("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length(),"<RIV13606REQUEST_EHR_EXTRACT_response xmlns=\"urn:riv13606:v1.1\">");
        // closing tag
        xml13606Response.append("</RIV13606REQUEST_EHR_EXTRACT_response>\n");
        
        // pass the <RIV13606REQUEST_EHR_EXTRACT_response/> message into the CareDocumentationMapper - expect back a <GetCareDocumentationResponse/>
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
            
            assertTrue (responseXml.contains("sourceSystemHSAid>SE2321000164-1006</"));
            assertTrue (responseXml.contains("<GetCareDocumentationResponse"));
            assertTrue (responseXml.contains("documentId>SE2321000164-1006Dok19381221704420090512082720692684000-1</"));
            assertTrue (responseXml.contains("Allmänmedicinska mottagningen vårdcentralen Forshaga"));
            assertTrue (responseXml.contains("<ns2:clinicalDocumentNoteTitle>Epikris</ns2:clinicalDocumentNoteTitle>"));
            assertFalse(responseXml.contains("This should not appear"));

        } catch (XMLStreamException e) {
            fail(e.getLocalizedMessage());
        } catch (MapperException e) {
            fail(e.getLocalizedMessage());
        }
    }
}

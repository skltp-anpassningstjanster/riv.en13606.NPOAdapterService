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

import org.mockito.ArgumentCaptor;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.mapper.error.MapperException;

public abstract class MapperTest {

    private static final Logger log = LoggerFactory.getLogger(MapperTest.class);

    
    
    // Use the mapper to convert the 13606 test file xml into rivta xml
    protected String getRivtaXml(Mapper mapper, String xml13606TestFile) {
        return getRivtaXml(mapper, xml13606TestFile, false);
    }
    
    // Use the mapper to convert the 13606 test file xml into rivta xml
    protected String getRivtaXml(Mapper mapper, String xml13606TestFile, boolean continuation) {
        
        String responseXml = "";
        
        // load xml from test file - this contains an <ehr_extract/>
        StringBuilder xml13606Response = new StringBuilder();
        try (@SuppressWarnings("resource") Scanner inputStringScanner = new Scanner(getClass().getResourceAsStream(xml13606TestFile), "UTF-8").useDelimiter("\\z")) {
            while (inputStringScanner.hasNext()) {
                xml13606Response.append(inputStringScanner.next());
            }
        }

        // wrap the <ehr_extract/> in a <RIV13606REQUEST_EHR_EXTRACT_response/>
        // opening tag
        xml13606Response.insert("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length(),"<RIV13606REQUEST_EHR_EXTRACT_response xmlns=\"urn:riv13606:v1.1\">");

        if (continuation) {
            // presence of continuation token means that more information is available, and requires further call(s)
            // this processing is not implemented by NPÖ Adapter - instead we log a warning
            xml13606Response.append("<continuation_token value=\"abc\"></continuation_token>\n");
        }
        
        // closing tag
        xml13606Response.append("</RIV13606REQUEST_EHR_EXTRACT_response>\n");
        
        // pass the <RIV13606REQUEST_EHR_EXTRACT_response/> message into the ReferralOutcomeMapper - expect back a <GetReferralOutcomeResponse/>
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        Reader xmlReader = new StringReader(xml13606Response.toString());
        XMLStreamReader xmlStreamReader;
        try {
            xmlStreamReader = xmlInputFactory.createXMLStreamReader(xmlReader);

            MuleMessage mockMuleMessage = mock(MuleMessage.class);
            when(mockMuleMessage.getPayload()).thenReturn(xmlStreamReader);
            // argumentCaptor will capture the converted xml
            ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
            
            // method being exercised on the object under test
            mapper.mapResponse(mockMuleMessage);
            
            // verifications & assertions
            verify(mockMuleMessage).setPayload(argumentCaptor.capture());
            responseXml = (String)argumentCaptor.getValue();
            log.debug(responseXml);
        } catch (XMLStreamException e) {
            fail(e.getLocalizedMessage());
        } catch (MapperException e) {
            fail(e.getCause().getLocalizedMessage());
        }
        return responseXml;
    }

}

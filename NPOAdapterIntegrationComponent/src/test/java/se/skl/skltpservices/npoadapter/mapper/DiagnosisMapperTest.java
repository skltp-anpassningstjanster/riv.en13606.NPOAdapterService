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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.util.Scanner;
import java.util.UUID;

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

import riv.clinicalprocess.healthcond.description._2.DiagnosisBodyType;
import riv.clinicalprocess.healthcond.description.enums._2.DiagnosisTypeEnum;
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
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author torbjorncla
 */
public class DiagnosisMapperTest {

    private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
    private static EHREXTRACT ehrExtract;
    private static DiagnosisMapper mapper;

    private final static CD cd = new CD();
    private final static ST st = new ST();

    private static final String TEST_DATA_1 = UUID.randomUUID().toString();
    private static final String TEST_DATA_2 = UUID.randomUUID().toString();

    private static final Logger log = LoggerFactory.getLogger(DiagnosisMapperTest.class);

    @BeforeClass
    public static void init() throws JAXBException {
    	mapper = Mockito.spy(getDiagnosisMapper());
    	
        ehrExtract = Util.loadEhrTestData(Util.DIAGNOSIS_TEST_FILE);
        ehrResp.getEhrExtract().add(ehrExtract);

        cd.setCode(TEST_DATA_1);
        cd.setId(TEST_DATA_2);

        st.setValue(TEST_DATA_1);
    }
    
    private static DiagnosisMapper getDiagnosisMapper() {
    	DiagnosisMapper mapper = (DiagnosisMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_DIAGNOSIS_2);
    	return mapper;
    }    

    @Test
    public void testMapDiagnosisBodyType() throws Exception {
        boolean typeTouch = false;
        CD cd = null;

        for (COMPOSITION composition : ehrExtract.getAllCompositions()) {
            DiagnosisBodyType body = Mockito.spy(mapper.mapDiagnosisBodyType(composition));
            TS time = null;
            ST simpleText = null;
            for (CONTENT c : composition.getContent()) {
                ENTRY e = (ENTRY) c;
                for (ITEM i : e.getItems()) {
                    ELEMENT elm = (ELEMENT) i;
                    switch (i.getMeaning().getCode()) {
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
                        assertTrue(simpleText.getValue().equals(body.getTypeOfDiagnosis().value())
                               || (simpleText.getValue().equals("Kronisk diagnos") 
                                    && 
                                   body.isChronicDiagnosis() != null
                                    && 
                                   body.isChronicDiagnosis().booleanValue() 
                                    && 
                                   body.getTypeOfDiagnosis().equals(DiagnosisTypeEnum.HUVUDDIAGNOS)));
                        typeTouch = true;
                        break;
                    }
                }
            }
            assertTrue(typeTouch);
        }
    }

    @Test
    public void mapResponse() {

        DiagnosisMapper objectUnderTest = getDiagnosisMapper();

        // load xml from test file - this contains an <ehr_extract/>
        StringBuilder xml13606Response = new StringBuilder();
        try (@SuppressWarnings("resource")
        Scanner inputStringScanner = new Scanner(getClass().getResourceAsStream(Util.DIAGNOSIS_TEST_FILE), "UTF-8").useDelimiter("\\z")) {
            while (inputStringScanner.hasNext()) {
                xml13606Response.append(inputStringScanner.next());
            }
        }

        // wrap the <ehr_extract/> in a <RIV13606REQUEST_EHR_EXTRACT_response/>
        // opening tag
        xml13606Response.insert("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length(),
                "<RIV13606REQUEST_EHR_EXTRACT_response xmlns=\"urn:riv13606:v1.1\">");
        // closing tag
        xml13606Response.append("</RIV13606REQUEST_EHR_EXTRACT_response>\n");

        // pass the <RIV13606REQUEST_EHR_EXTRACT_response/> message into the Mapper - expect back a <GetDiagnosisResponse/>
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
            String responseXml = (String) argumentCaptor.getValue();

            log.debug(responseXml);

            assertTrue(responseXml.contains("GetDiagnosisResponse>"));
            assertTrue(responseXml.contains("typeOfDiagnosis>Huvuddiagnos"));
            assertTrue(responseXml.contains("chronicDiagnosis>true"));
            assertTrue(responseXml.contains("documentId>SE2321000164-1004Dia19381221704420090512083134940624000-1</"));
            assertTrue(responseXml.contains("healthcareProfessionalCareUnitHSAId>SE2321000164-12ab"));
            assertTrue(responseXml.contains("healthcareProfessionalCareGiverHSAId>SE2321000164-ab12"));
            assertTrue(responseXml.contains("relatedDiagnosis><"));
            assertTrue(responseXml.contains("documentId>SE123-relatedDiagnosis<"));
        } catch (XMLStreamException e) {
            fail(e.getLocalizedMessage());
        } catch (MapperException e) {
            fail(e.getLocalizedMessage());
        }
    }

}

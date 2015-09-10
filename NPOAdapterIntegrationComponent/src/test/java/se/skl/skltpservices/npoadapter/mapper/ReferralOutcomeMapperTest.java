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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

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

import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.ReferralOutcomeTypeCodeEnum;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin Flower
 */
public class ReferralOutcomeMapperTest {

    private static EHREXTRACT ehrextract;
    
    private static final Logger log = LoggerFactory.getLogger(ReferralOutcomeMapperTest.class);

    @BeforeClass
    public static void init() throws JAXBException {
        ehrextract = Util.loadEhrTestData(Util.REFERRALOUTCOME_TEST_FILE);
    }

    // Make it easy to dump the resulting response
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetReferralOutcomeResponseType type;
    }

    //
    private void dump(final GetReferralOutcomeResponseType responseType) throws JAXBException {
        Root root = new Root();
        root.type = responseType;
        Util.dump(root);
    }
    
    private ReferralOutcomeMapper getReferralOutcomeMapper() {
    	ReferralOutcomeMapper mapper = (ReferralOutcomeMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_REFERRALOUTCOME);
    	return mapper;
    }

    @Test
    public void testMapFromEhrToReferralOutcome() throws JAXBException {
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        ReferralOutcomeMapper mapper = getReferralOutcomeMapper();
        GetReferralOutcomeResponseType responseType = mapper.mapEhrExtract(Arrays.asList(ehrextract), mockMessage);
        assertNotNull(responseType);

        dump(responseType);
        
        List<ReferralOutcomeType> ros = responseType.getReferralOutcome();
        
        assertTrue(ros.size() == 1);

        for (ReferralOutcomeType ro : ros) {
            assertNotNull(ro.getReferralOutcomeHeader());
            assertNotNull(ro.getReferralOutcomeBody());
        }
    }

	@Test
	public void testInterpretOutcomeType() {
		final ReferralOutcomeMapper mapper = getReferralOutcomeMapper();
		
		assertEquals(ReferralOutcomeTypeCodeEnum.SR, mapper.interpretOutcomeType("TILL"));
		assertEquals(ReferralOutcomeTypeCodeEnum.SS, mapper.interpretOutcomeType("DEF"));
		
		assertNull(mapper.interpretOutcomeType("FEL"));
		assertNull(mapper.interpretOutcomeType(null));
		assertNull(mapper.interpretOutcomeType(UUID.randomUUID().toString()));
	}


    @Test
    public void mapResponse() {

        ReferralOutcomeMapper objectUnderTest = getReferralOutcomeMapper();

        // load xml from test file - this contains an <ehr_extract/>
        StringBuilder xml13606Response = new StringBuilder();
        try (@SuppressWarnings("resource") Scanner inputStringScanner = new Scanner(getClass().getResourceAsStream(Util.REFERRALOUTCOME_TEST_FILE), "UTF-8").useDelimiter("\\z")) {
            while (inputStringScanner.hasNext()) {
                xml13606Response.append(inputStringScanner.next());
            }
        }

        // wrap the <ehr_extract/> in a <RIV13606REQUEST_EHR_EXTRACT_response/>
        // opening tag
        xml13606Response.insert("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length(),"<RIV13606REQUEST_EHR_EXTRACT_response xmlns=\"urn:riv13606:v1.1\">");
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

            // method being exercised
            objectUnderTest.mapResponse(mockMuleMessage);

            
            // verifications & assertions
            verify(mockMuleMessage).setPayload(argumentCaptor.capture());
            String responseXml = (String)argumentCaptor.getValue();
            log.debug(responseXml);
            
            assertTrue(responseXml.contains("<GetReferralOutcomeResponse"));
            assertTrue(responseXml.contains("referralOutcomeTypeCode>SS"));
            assertTrue(responseXml.contains("referralOutcomeTitle>Allmän Remiss Vårdcentralen Strängnäs EDI"));
            assertTrue(responseXml.contains("referralOutcomeText>Svar: test Svarsdatum: 100503 Dikterande"));
            assertTrue(responseXml.contains("act><ns2:actCode><ns2:code>620<"));
            assertTrue(responseXml.contains("<ns2:codeSystem>1.2.752.129.2.2.2.1<"));
            assertTrue(responseXml.contains("<ns2:actText>Önskad undersökning: test"));
            assertTrue(responseXml.contains("actText><ns2:actTime>20100504110000<"));
            assertFalse(responseXml.contains("actResult"));
            assertTrue(responseXml.contains("referral><ns2:referralId>9871961"));
            assertTrue(responseXml.contains("referralId><ns2:referralReason>Önskad undersökning  test Anamnes, status: test"));
            assertTrue(responseXml.contains("referralReason><ns2:referralTime>20100503165800<"));
            assertTrue(responseXml.contains("referralAuthor><ns2:authorTime>20100503165800<"));
            assertTrue(responseXml.contains("authorTime><ns2:healthcareProfessionalName>SONSVE<"));
            // 

            
            
        } catch (XMLStreamException e) {
            fail(e.getLocalizedMessage());
        } catch (MapperException e) {
            fail(e.getLocalizedMessage());
        }
    }
}

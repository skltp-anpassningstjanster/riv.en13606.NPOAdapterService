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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleMessage;

import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.ReferralOutcomeTypeCodeEnum;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin Flower
 */
public class ReferralOutcomeMapperTest extends MapperTest {

    private static EHREXTRACT ehrextractOneResponse;
    
    @BeforeClass
    public static void init() throws JAXBException {
        ehrextractOneResponse = Util.loadEhrTestData(Util.REFERRALOUTCOME_TEST_FILE_1);
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
        GetReferralOutcomeResponseType responseType = mapper.mapEhrExtract(Arrays.asList(ehrextractOneResponse), mockMessage);
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
    public void mapOneResponse() {

        String responseXml = getRivtaXml(getReferralOutcomeMapper(), Util.REFERRALOUTCOME_TEST_FILE_1, true);

        assertTrue(responseXml.contains("<GetReferralOutcomeResponse"));
        assertTrue(responseXml.contains("sourceSystemHSAId><ns2:documentTime>20100503165801</ns2:documentTime><ns2:patientId>"));
        assertTrue(responseXml.contains("referralOutcomeTypeCode>SS"));
        assertTrue(responseXml.contains("referralOutcomeTitle>Allmän Remiss Vårdcentralen Strängnäs EDI"));
        assertTrue(responseXml.contains("referralOutcomeText>Svar: test Svarsdatum: 100503 Dikterande"));
        assertTrue(responseXml.contains("act><ns2:actCode><ns2:code>620<"));
        assertTrue(responseXml.contains("<ns2:codeSystem>1.2.752.129.2.2.2.1<"));
        assertTrue(responseXml.contains("<ns2:actText>Önskad undersökning: test"));
        assertTrue(responseXml.contains("actText><ns2:actTime>20100503165804<"));
        assertFalse(responseXml.contains("actResult"));
        assertTrue(responseXml.contains("referral><ns2:referralId>9871961"));
        assertTrue(responseXml.contains("referralId><ns2:referralReason>Önskad undersökning  test Anamnes, status: test"));
        assertTrue(responseXml.contains("referralReason><ns2:referralTime>20100503165805<"));
        assertTrue(responseXml.contains("</ns2:patientId><ns2:accountableHealthcareProfessional><ns2:authorTime>20100503165801</ns2:authorTime>")); //20100503165801
        assertTrue(responseXml.contains("healthcareProfessionalHSAId><ns2:healthcareProfessionalName>Jarl Sternum<"));
    }
    
    
    @Test
    public void mapMultipleResponses() {
        
        String responseXml = getRivtaXml(getReferralOutcomeMapper(), Util.REFERRALOUTCOME_TEST_FILE_2);
            
        int occurrences = 0;
        Pattern p = Pattern.compile("referralOutcomeBody");
        Matcher m = p.matcher(responseXml);
        while (m.find()) {
            occurrences++;
        }
        assertEquals(6,occurrences);
        
        assertTrue(responseXml.contains("<GetReferralOutcomeResponse"));
        assertTrue(responseXml.contains("referralOutcomeTypeCode>SS"));
    }
    
    
    @Test
    public void mapBlekinge() {

        String responseXml = getRivtaXml(getReferralOutcomeMapper(), Util.REFERRALOUTCOME_TEST_FILE_3);
            
        int occurrences = 0;
        Pattern p = Pattern.compile("referralOutcomeBody");
        Matcher m = p.matcher(responseXml);
        while (m.find()) {
            occurrences++;
        }
        assertEquals(6,occurrences);
        
        assertTrue(responseXml.contains("<GetReferralOutcomeResponse"));
        assertTrue(responseXml.contains("referralOutcomeTypeCode>SS"));
        assertTrue(responseXml.contains("<ns2:referralAuthor><ns2:authorTime>20150703114102</ns2:authorTime><ns2:healthcareProfessionalHSAId>MO0775</ns2:healthcareProfessionalHSAId><ns2:healthcareProfessionalName>Monica Burman"));
        assertFalse(responseXml.contains("</ns2:healthcareProfessionalCareGiverHSAId></ns2:referralAuthor>"));
    }
    
}



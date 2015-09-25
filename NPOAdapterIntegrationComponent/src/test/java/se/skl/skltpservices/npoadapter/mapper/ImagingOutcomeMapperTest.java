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

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mule.api.MuleMessage;

import riv.clinicalprocess.healthcond.actoutcome._3.ECGReferralType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImageRecordingType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.test.Util;

public class ImagingOutcomeMapperTest {
	
	private static EHREXTRACT ehrExtract;
	private static ImagingOutcomeMapper mapper;
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	
	private static GetImagingOutcomeResponseType resp;
	
	private static ImagingOutcomeType record1;
	
	@BeforeClass
	public static void init() throws JAXBException {
		ehrExtract = Util.loadEhrTestData(Util.IMAGE_TEST_FILE);
		mapper = (ImagingOutcomeMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_IMAGING_1);
		ehrResp.getEhrExtract().add(ehrExtract);
		final MuleMessage mockMessage = mock(MuleMessage.class);
		when(mockMessage.getUniqueId()).thenReturn("1234");
		resp = mapper.mapResponse(ehrResp, mockMessage);
		for(ImagingOutcomeType rec : resp.getImagingOutcome()) {
			if(rec.getImagingOutcomeHeader().getDocumentId().equals("OREBMKT3_9500619_3_1")) {
				record1 = rec;
				break;
			}
		}
	
	}
	

    // Make it easy to dump the resulting response
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetImagingOutcomeResponseType type;
    }

    //
    private void dump(final GetImagingOutcomeResponseType responseType) throws JAXBException {
        Root root = new Root();
        root.type = responseType;
        Util.dump(root);
    }
	
    @Ignore
    public void dump() throws JAXBException {
    	MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
    	dump(mapper.mapResponse(ehrResp, mockMessage));
    }
    
    
	@Test
	public void testHeader() {
		assertEquals("OREBMKT3_9500619_3_1", record1.getImagingOutcomeHeader().getDocumentId());
        assertEquals("20090928082247", record1.getImagingOutcomeHeader().getDocumentTime());
	}
    
    @Test
    public void testBody() {
    	assertEquals("DEF", record1.getImagingOutcomeBody().getTypeOfResult().toString());
    	assertEquals("20090928082247", record1.getImagingOutcomeBody().getResultTime());
    	assertEquals("Svar: XXXXXXXXX Svarsdatum: 090925 Dikterande läkare: XXXXXXXXX Signerande läkare: XXXXXXXXX", record1.getImagingOutcomeBody().getResultReport());
    	assertNull(record1.getImagingOutcomeBody().getResultComment());
    	
    	assertTrue(record1.getImagingOutcomeBody().getRadiationDose().isEmpty());
    	assertNull(record1.getImagingOutcomeBody().getPatientData());
    	
    }
    
    @Test
    public void testImagingRecordingInBody() {
    	assertFalse(record1.getImagingOutcomeBody().getImageRecording().isEmpty());
    	assertEquals(1, record1.getImagingOutcomeBody().getImageRecording().size());
    	final ImageRecordingType re = record1.getImagingOutcomeBody().getImageRecording().get(0);
    	assertEquals("OREBMKT3_9500619_3_1", re.getRecordingId().getExtension());
    	assertEquals("20090928082247", re.getExaminationTimePeriod().getStart());
    	assertEquals("20090928082247", re.getExaminationTimePeriod().getEnd());
    }
    
    @Test
    public void testReferralInBody() {
    	assertNotNull(record1.getImagingOutcomeBody().getReferral());
    	final ECGReferralType ref = record1.getImagingOutcomeBody().getReferral();
    	assertEquals("9500619", ref.getReferralId());
    	assertEquals("Önskad undersökning HKF höger Anamnes, status: XXXXXXXXX", ref.getReferralReason());
    }
    
    @Ignore
    public void testException() {
    	MuleMessage mockMuleMessage = Mockito.mock(MuleMessage.class);
    	try {
    		Reader stringReader = new StringReader("abc");
    		XMLInputFactory factory = XMLInputFactory.newInstance();
    		try {
				XMLStreamReader sr = factory.createXMLStreamReader(stringReader);
	    		when(mockMuleMessage.getPayload()).thenReturn(sr);
				mapper.mapResponse(mockMuleMessage);
				fail("Exception expected");
			} catch (MapperException e) {
				assertTrue(e.getCause().getMessage().startsWith("javax.xml.bind.UnmarshalException\n - with linked exception:\n[com.ctc.wstx.exc.WstxUnexpectedCharException: Unexpected character 'a' (code 97) in prolog; expected '<'\n at [row,col {unknown-source}]: [1,1]]"));
			} 
    	} catch (XMLStreamException e) {
    	    fail(e.getLocalizedMessage());
    	}
    }
    
    
    @Ignore
    public void datePeriod() {
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getInvocationProperty("route-logical-address")).thenReturn("abc");
        
        String getImagingOutcomeRequestXml 
        = " <urn1:GetImagingOutcome xmlns:urn1=\"urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcomeResponder:1\" " +
          "                         xmlns:urn2=\"urn:riv:clinicalprocess:healthcond:actoutcome:3\">  "+ 
          "  <urn1:patientId>                             " +
          "   <urn2:id>191212121212</urn2:id>             " +
          "   <urn2:type>1.2.752.129.2.1.3.1</urn2:type>  " +
          "  </urn1:patientId>                            " +
          "  <urn1:datePeriod>                            " +
          "   <urn2:start>20150430</urn2:start>           " +
          "   <urn2:end>20150531</urn2:end>               " +
          "  </urn1:datePeriod>                           " +
          " </urn1:GetImagingOutcome>                     ";
        
        Reader stringReader = new StringReader(getImagingOutcomeRequestXml);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            try {
                XMLStreamReader sr = factory.createXMLStreamReader(stringReader);
                when(mockMessage.getPayload()).thenReturn(sr);
                @SuppressWarnings("unused")
                MuleMessage ignoredInMockitoContext = mapper.mapRequest(mockMessage);

                ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
                verify(mockMessage).setPayload(argument.capture());
                
                String payload13606 = (String)argument.getValue();             
                
//              <?xml version="1.0" encoding="UTF-8" standalone="yes"?><RIV13606REQUEST_EHR_EXTRACT_request xmlns="urn:riv13606:v1.1"><subject_of_care_id extension="191212121212" root="1.2.752.129.2.1.3.1"/><meanings codeSystem="1.2.752.129.2.2.2.1" code="und-bdi"/><parameters><name value="hsa_id"/><value value="abc"/></parameters><parameters><name value="transaction_id"/></parameters><parameters><name value="version"/><value value="1.1"/></parameters></RIV13606REQUEST_EHR_EXTRACT_request>
                
                assertTrue(payload13606.contains("<time_period"));                
                
            } catch (XMLStreamException xe) {
                fail (xe.getLocalizedMessage());
            }
        } catch (MapperException me) {
            fail(me.getLocalizedMessage());
        }
    }
    
}

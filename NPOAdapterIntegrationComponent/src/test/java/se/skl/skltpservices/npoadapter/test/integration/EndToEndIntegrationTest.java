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
package se.skl.skltpservices.npoadapter.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistory._2.rivtabp21.GetMedicationHistoryResponderInterface;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcome._1.rivtabp21.GetImagingOutcomeResponderInterface;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcome._3.rivtabp21.GetLaboratoryOrderOutcomeResponderInterface;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcome._3.rivtabp21.GetReferralOutcomeResponderInterface;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeType;
import riv.clinicalprocess.healthcond.description.enums._2.ResultCodeEnum;
import riv.clinicalprocess.healthcond.description.getalertinformation._2.rivtabp21.GetAlertInformationResponderInterface;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationResponseType;
import riv.clinicalprocess.healthcond.description.getcaredocumentation._2.rivtabp21.GetCareDocumentationResponderInterface;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.healthcond.description.getdiagnosis._2.rivtabp21.GetDiagnosisResponderInterface;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisResponseType;
import riv.clinicalprocess.logistics.logistics.getcarecontacts._2.rivtabp21.GetCareContactsResponderInterface;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsType;
import se.skl.skltpservices.npoadapter.test.stub.EhrExtractWS;

/**
 * Created by Peter on 2014-08-14.
 */
public class EndToEndIntegrationTest extends AbstractIntegrationTestCase {

	// TODO: Collect Endpoints from configuration
    private static final String ALERT_INFORMATION_ENDPOINT  = "http://localhost:33001/npoadapter/getalertinformation/v2";
    private static final String CARE_CONTACTS_ENDPOINT      = "http://localhost:33001/npoadapter/getcarecontacts/v2";
	private static final String CARE_DOCUMENTATION_ENDPOINT = "http://localhost:33001/npoadapter/getcaredocumentation/v2";
	private static final String DIAGNOSIS_ENDPOINT          = "http://localhost:33001/npoadapter/getdiagnosis/v2";
    private static final String IMAGING_OUTCOME_ENDPOINT    = "http://localhost:33001/npoadapter/getimagingoutcome/v1";
	private static final String LABORATORY_ENDPOINT         = "http://localhost:33001/npoadapter/getlaboratoryorderoutcome/v3";
    private static final String MEDICATION_HISTORY_ENDPOINT = "http://localhost:33001/npoadapter/getmedicationhistory/v2";
    private static final String REFERRAL_OUTCOME_ENDPOINT   = "http://localhost:33001/npoadapter/getreferraloutcome/v3";
	
	private static final String LOGICAL_ADDRESS_VS_1 = "VS-1";
    private static final String LOGICAL_ADDRESS_VS_2 = "VS-2";
	private static final String INVALID_LOGICAL_ADDRESS = "XX000000-00";
	
	
    private final GetAlertInformationResponderInterface       getAlertInformationResponderInterface;
	private final GetCareDocumentationResponderInterface      getCareDocumentationServices;
	private final GetCareContactsResponderInterface           getCareContactsServices;
	private final GetDiagnosisResponderInterface              getDiagnosisServices;
	private final GetLaboratoryOrderOutcomeResponderInterface getLaboratoryOrderOutcomeServices;
    private final GetImagingOutcomeResponderInterface         getImagingOutcomeResponderInterface;
    private final GetMedicationHistoryResponderInterface      getMedicationHistoryResponderInterface;
    private final GetReferralOutcomeResponderInterface        getReferralOutcomeResponderInterface;

    
    JaxbUtil jaxbUtil = new JaxbUtil(GetAlertInformationResponseType.class, 
                                     GetCareContactsResponseType.class, 
                                     GetCareDocumentationResponseType.class,
                                     GetDiagnosisResponseType.class,
                                     GetImagingOutcomeResponseType.class,
                                     GetLaboratoryOrderOutcomeResponseType.class,
                                     GetMedicationHistoryResponseType.class,
                                     GetReferralOutcomeResponseType.class);

    
    private riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.ObjectFactory alertInformationObjectFactory
    = new riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.ObjectFactory();
    
    private riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.ObjectFactory careContactsObjectFactory
    = new riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.ObjectFactory();
    
    private riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.ObjectFactory careDocumentationObjectFactory
    = new riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.ObjectFactory();
    
    private riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.ObjectFactory diagnosisObjectFactory
    = new riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.ObjectFactory();
    
    private riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.ObjectFactory imagingOutcomeObjectFactory
    = new riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.ObjectFactory();
    
    private riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.ObjectFactory laboratoryOrderOutcomeObjectFactory
    = new riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.ObjectFactory();
    
    private riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.ObjectFactory medicationHistoryObjectFactory
    = new riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.ObjectFactory();
    
    private riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.ObjectFactory referralOutcomeObjectFactory
    = new riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.ObjectFactory();
    
    //
    static Object create(JaxWsProxyFactoryBean jaxWs) {
        final HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(0);
        policy.setReceiveTimeout(180000);
        policy.setAllowChunking(true);

        final Object service = jaxWs.create();
        final Client client = ClientProxy.getClient(service);
        ((HTTPConduit) client.getConduit()).setClient(policy);

        return service;
    }

    public EndToEndIntegrationTest() {
    	setDisposeContextPerClass(true);

    	final JaxWsProxyFactoryBean jaxWs = new JaxWsProxyFactoryBean();

        jaxWs.setServiceClass(GetAlertInformationResponderInterface.class);
        jaxWs.setAddress(ALERT_INFORMATION_ENDPOINT);
        getAlertInformationResponderInterface = (GetAlertInformationResponderInterface) create(jaxWs);
        
		jaxWs.setServiceClass(GetCareContactsResponderInterface.class);
		jaxWs.setAddress(CARE_CONTACTS_ENDPOINT);
		getCareContactsServices = (GetCareContactsResponderInterface) create(jaxWs);
		
		jaxWs.setServiceClass(GetCareDocumentationResponderInterface.class);
		jaxWs.setAddress(CARE_DOCUMENTATION_ENDPOINT);
		getCareDocumentationServices = (GetCareDocumentationResponderInterface) create(jaxWs);
		
		jaxWs.setServiceClass(GetDiagnosisResponderInterface.class);
		jaxWs.setAddress(DIAGNOSIS_ENDPOINT);
		getDiagnosisServices = (GetDiagnosisResponderInterface) create(jaxWs);
		
		jaxWs.setServiceClass(GetLaboratoryOrderOutcomeResponderInterface.class);
		jaxWs.setAddress(LABORATORY_ENDPOINT);
		getLaboratoryOrderOutcomeServices = (GetLaboratoryOrderOutcomeResponderInterface) create(jaxWs);
		
        jaxWs.setServiceClass(GetMedicationHistoryResponderInterface.class);
        jaxWs.setAddress(MEDICATION_HISTORY_ENDPOINT);
        getMedicationHistoryResponderInterface = (GetMedicationHistoryResponderInterface) create(jaxWs);
        
        jaxWs.setServiceClass(GetReferralOutcomeResponderInterface.class);
        jaxWs.setAddress(REFERRAL_OUTCOME_ENDPOINT);
        getReferralOutcomeResponderInterface = (GetReferralOutcomeResponderInterface) create(jaxWs);
        
        jaxWs.setServiceClass(GetImagingOutcomeResponderInterface.class);
        jaxWs.setAddress(IMAGING_OUTCOME_ENDPOINT);
        getImagingOutcomeResponderInterface = (GetImagingOutcomeResponderInterface) create(jaxWs);
    }
    
    @Before
    public void init() throws Exception {
    	super.doSetUp();
    }


    
    // AlertInformation
    
    @Test
    public void GetAlertInformationEN13606SuccessTest() {
        GetAlertInformationResponseType resp = getAlertInformationResponderInterface.getAlertInformation(
                LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createAlertInformationType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(resp.getAlertInformation().isEmpty());
    }

    @Ignore
    @Test
    public void GetAlertInformationRIVSuccessTest() {
        GetAlertInformationResponseType response = getAlertInformationResponderInterface.getAlertInformation(
                LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createAlertInformationType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(response.getAlertInformation().isEmpty());
        validateXmlAgainstSchema(alertInformationObjectFactory.createGetAlertInformationResponse(response),
                                "/core_components/clinicalprocess_healthcond_description_enum_2.1.xsd", 
                                "/core_components/clinicalprocess_healthcond_description_2.1.xsd",
                                "/interactions/GetAlertInformationInteraction/GetAlertInformationResponder_2.0.xsd");
      
    }

    
    // CareContacts
    
    @Test
    public void GetCareContactsEN13606SourceSuccessTest() {
		GetCareContactsResponseType resp = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER));
		assertFalse(resp.getCareContact().isEmpty());
    }

    @Test
    public void GetCareContactsNotFoundTest() {
        GetCareContactsType req = IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER);
        req.getPatientId().setId(EhrExtractWS.PATIENT_ID_NOT_FOUND);
        GetCareContactsResponseType resp = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS_VS_1, req);
        assertTrue(resp.getCareContact().isEmpty());
    }

    @Test
    public void GetEhrCareContactsRIVSourceSuccessTest() {
        GetCareContactsResponseType response = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(response.getCareContact().isEmpty());

        validateXmlAgainstSchema(careContactsObjectFactory.createGetCareContactsResponse(response),
                "/core_components/clinicalprocess_logistics_logistics_enum_2.0.xsd", 
                "/core_components/clinicalprocess_logistics_logistics_2.0.xsd",
                "/interactions/GetCareContactsInteraction/GetCareContactsResponder_2.0.xsd");
    }

    
    // CareDocumentation
    
    @Test
    public void GetCareDocumentationEN136060SuccessTest() {
		GetCareDocumentationResponseType resp = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
		assertFalse(resp.getCareDocumentation().isEmpty());
    }
    
    @Test(expected=SOAPFaultException.class)
    public void GetCareDocumentationRoutingExceptionTest() {
    	getCareDocumentationServices.getCareDocumentation(INVALID_LOGICAL_ADDRESS, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
    }
    
    @Test
    public void GetCareDocumentationBackEndExceptionTest() {
        GetCareDocumentationResponseType resp = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.TRIGGER_ERROR_MESSAGE));
        assertNotNull(resp.getResult());
        assertEquals(resp.getResult().getResultCode(), ResultCodeEnum.ERROR);
    }

    @Test
    public void GetCareDocumentationRIVSourceSuccessTest() {
        GetCareDocumentationResponseType response = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(response.getCareDocumentation().isEmpty());

        validateXmlAgainstSchema(careDocumentationObjectFactory.createGetCareDocumentationResponse(response),
                "/core_components/clinicalprocess_healthcond_description_enum_2.1.xsd", 
                "/core_components/clinicalprocess_healthcond_description_2.1_ext.xsd",
                "/core_components/clinicalprocess_healthcond_description_2.1.xsd",
                "/interactions/GetCareDocumentationInteraction/GetCareDocumentationResponder_2.1.xsd");
    }
    
    
    // Diagnosis
    
    @Test
    public void GetDiagnosisEN136060SuccessTest() {
    	GetDiagnosisResponseType resp = getDiagnosisServices.getDiagnosis(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetDiagnosisType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getDiagnosis().isEmpty());
    }
    
    @Test
    public void GetDiagnosisRIVSuccessTest() {
    	GetDiagnosisResponseType response = getDiagnosisServices.getDiagnosis(LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createGetDiagnosisType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(response.getDiagnosis().isEmpty());

        validateXmlAgainstSchema(diagnosisObjectFactory.createGetDiagnosisResponse(response),
                "/core_components/clinicalprocess_healthcond_description_enum_2.1.xsd", 
                "/core_components/clinicalprocess_healthcond_description_2.1.xsd",
                "/interactions/GetDiagnosisInteraction/GetDiagnosisResponder_2.0.xsd");
    }

    
    // ImagingOutcome
    
    @Test
    public void GetImagingOutcomeEN13606SuccessTest() {
        GetImagingOutcomeType type = IntegrationTestDataUtil.createImagingOutcomeType(IntegrationTestDataUtil.NO_TRIGGER);
        GetImagingOutcomeResponseType resp = getImagingOutcomeResponderInterface.getImagingOutcome(LOGICAL_ADDRESS_VS_1, type);
        assertTrue (resp.getImagingOutcome().size() == 4);
        assertEquals("Svar: XXXXXXXXX Svarsdatum: 090925 Dikterande läkare: XXXXXXXXX Signerande läkare: XXXXXXXXX", resp.getImagingOutcome().get(0).getImagingOutcomeBody().getResultReport());
    }
    
    @Ignore
    public void GetImagingOutcomeEN13606Exception() {
        GetImagingOutcomeType type = IntegrationTestDataUtil.createImagingOutcomeType(IntegrationTestDataUtil.NO_TRIGGER);
        type.setPatientId(null);
        try{
            getImagingOutcomeResponderInterface.getImagingOutcome(LOGICAL_ADDRESS_VS_1, type);
            fail("Exception expected");
        } catch (SOAPFaultException me) {
            assertTrue(me.getMessage().startsWith("Marshalling Error: cvc-complex-type.2.4.a: Invalid content was found"));
        }
    }
    
    @Test
    public void GetImagingOutcomeRIVSuccessTest() {
        GetImagingOutcomeType giot = IntegrationTestDataUtil.createImagingOutcomeType(IntegrationTestDataUtil.NO_TRIGGER);
        GetImagingOutcomeResponseType response = getImagingOutcomeResponderInterface.getImagingOutcome(LOGICAL_ADDRESS_VS_2, giot);
        assertFalse(response.getImagingOutcome().isEmpty());
        validateXmlAgainstSchema(imagingOutcomeObjectFactory.createGetImagingOutcomeResponse(response),
                "/core_components/clinicalprocess_healthcond_actoutcome_enum_3.1.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1_ext.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1.xsd",
                "/interactions/GetImagingOutcomeInteraction/GetImagingOutcomeResponder_1.0.xsd");
    }

    
    // LaboratoryOrderOutcome
    
    @Test
    public void GetLaboratoryOrderOutcomeEN13606SuccessTest() {
    	GetLaboratoryOrderOutcomeResponseType resp =  getLaboratoryOrderOutcomeServices.getLaboratoryOrderOutcome(LOGICAL_ADDRESS_VS_1, 
    			IntegrationTestDataUtil.createGetLaboratoryOrderOutcomeType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getLaboratoryOrderOutcome().isEmpty());
    }
    
    @Test
    public void GetLaboratoryOrderOutcomeRIVSuccessTest() {
    	GetLaboratoryOrderOutcomeResponseType response = getLaboratoryOrderOutcomeServices.getLaboratoryOrderOutcome(LOGICAL_ADDRESS_VS_2,
    			IntegrationTestDataUtil.createGetLaboratoryOrderOutcomeType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(response.getLaboratoryOrderOutcome().isEmpty());
    	
        validateXmlAgainstSchema(laboratoryOrderOutcomeObjectFactory.createGetLaboratoryOrderOutcomeResponse(response),
                "/core_components/clinicalprocess_healthcond_actoutcome_enum_3.1.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1_ext.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1.xsd",
                "/interactions/GetLaboratoryOrderOutcomeInteraction/GetLaboratoryOrderOutcomeResponder_3.1.xsd");
    }
    

    // MedicationHistory
	
    @Test
    public void GetMedicationHistoryEN13606SuccessTest() {
        GetMedicationHistoryResponseType resp = getMedicationHistoryResponderInterface.getMedicationHistory(
                LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createMedicationHistoryType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(resp.getMedicationMedicalRecord().isEmpty());
    }
    
    @Test
    public void GetMedicationHistoryRIVSuccessTest() {
        GetMedicationHistoryResponseType response = getMedicationHistoryResponderInterface.getMedicationHistory(
                LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createMedicationHistoryType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(response.getMedicationMedicalRecord().isEmpty());
        JAXBElement<GetMedicationHistoryResponseType> element = medicationHistoryObjectFactory.createGetMedicationHistoryResponse(response);
        try {
            validateXmlAgainstSchema(element,
                "/core_components/clinicalprocess_activityprescription_actoutcome_enum_2.0.xsd",
                "/core_components/clinicalprocess_activityprescription_actoutcome_2.0.xsd",
                "/interactions/GetMedicationHistoryInteraction/GetMedicationHistoryResponder_2.0.xsd");
        } catch (AssertionError ae) {
            // TODO - GetMedicationHistory - treatmentInterval
            assertTrue(ae.getMessage().startsWith("Validation error: cvc-complex-type.2.4.b: The content of element 'ns12:dispensationAuthorization' is not complete. One of '{\"urn:riv:clinicalprocess:activityprescription:actoutcome:2\":dispensationAuthorizerComment, \"urn:riv:clinicalprocess:activityprescription:actoutcome:2\":firstDispensationBefore, \"urn:riv:clinicalprocess:activityprescription:actoutcome:2\":prescriptionSignatura}' is expected."));
        }
    }
    

    // ReferralOutcome
    
    @Test
    public void GetReferralOutcomeEN13606SuccessTest() {
        GetReferralOutcomeType type = IntegrationTestDataUtil.createReferralOutcomeType(IntegrationTestDataUtil.NO_TRIGGER);
        GetReferralOutcomeResponseType resp = getReferralOutcomeResponderInterface.getReferralOutcome(LOGICAL_ADDRESS_VS_1, type);
        assertFalse(resp.getReferralOutcome().isEmpty());
    }
    
    @Test
    public void GetReferralOutcomeEN13606NullTest() {
        try {
            @SuppressWarnings("unused")
            GetReferralOutcomeResponseType notused = getReferralOutcomeResponderInterface.getReferralOutcome(LOGICAL_ADDRESS_VS_1, null);
            fail("Expected SOAPFaultException");
        } catch (SOAPFaultException sfee) {
        } 
    }
    
    @Test
    public void GetReferralOutcomeRIVSuccessTest() {
        GetReferralOutcomeResponseType response = getReferralOutcomeResponderInterface.getReferralOutcome(
                LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createReferralOutcomeType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(response.getReferralOutcome().isEmpty());

        validateXmlAgainstSchema(referralOutcomeObjectFactory.createGetReferralOutcomeResponse(response),
                "/core_components/clinicalprocess_healthcond_actoutcome_enum_3.1.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1_ext.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1.xsd",
                "/interactions/GetReferralOutcomeInteraction/GetReferralOutcomeResponder_3.1.xsd");
    }

    
    // ---
    
    private void validateXmlAgainstSchema(Object element, String ... xsds) {
        
        String xml = jaxbUtil.marshal(element);
        logger.debug(xml);
        List<Source> schemaFiles = new ArrayList<Source>();
        for (String xsd : xsds) {
            schemaFiles.add(new StreamSource(getClass().getResourceAsStream(xsd)));
        }
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = factory.newSchema(schemaFiles.toArray(new StreamSource[schemaFiles.size()]));
            Validator validator = schema.newValidator();
            validator.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception)
						throws SAXException {
					fail(String.format("Validation warning: %s", exception.getMessage()));
				}
				@Override
				public void error(SAXParseException exception)
						throws SAXException {
					fail(String.format("Validation error: %s", exception.getMessage()));
					
				}
				@Override
				public void fatalError(SAXParseException exception)
						throws SAXException {
					fail(String.format("Validation fatal error: %s", exception.getMessage()));
					
				}
            });
            validator.validate(new StreamSource(new StringReader(xml)));
            assertTrue(true);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    // ---
    
    @Test
    public void UpdateTakCacheTest() throws Exception {
    	Flow flow = (Flow) getFlowConstruct("update-tak-cache-http-flow");
    	MuleEvent event = getTestEvent("", flow);
    	flow.process(event);
    }
}

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

import java.util.HashMap;
import java.util.Map;

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

/**
 * Created by Peter on 2014-08-14.
 */
public class EndToEndIntegrationTest extends AbstractIntegrationTestCase {

	//TODO: Collect Endpoints from configuration
	private static final String CARE_DOCUMENTATION_ENDPOINT = "http://localhost:33001/npoadapter/getcaredocumentation/v2";
	private static final String CARE_CONTACTS_ENDPOINT = "http://localhost:33001/npoadapter/getcarecontacts/v2";
	private static final String DIAGNOSIS_ENDPOINT = "http://localhost:33001/npoadapter/getdiagnosis/v2";
	private static final String LABORATORY_ENDPOINT = "http://localhost:33001/npoadapter/getlaboratoryorderoutcome/v3";
	private static final String ALERT_INFORMATION_ENDPOINT = "http://localhost:33001/npoadapter/getalertinformation/v2";
    private static final String MEDICATION_HISTORY_ENDPOINT = "http://localhost:33001/npoadapter/getmedicationhistory/v2";
    private static final String REFERRAL_OUTCOME_ENDPOINT = "http://localhost:33001/npoadapter/getreferraloutcome/v3";
    private static final String IMAGING_OUTCOME_ENDPOINT = "http://localhost:33001/npoadapter/getimagingoutcome/v1";
	
	private static final String LOGICAL_ADDRESS_VS_1 = "VS-1";
    private static final String LOGICAL_ADDRESS_VS_2 = "VS-2";
	private static final String INVALID_LOGICAL_ADDRESS = "XX000000-00";
	
	
	private final GetCareDocumentationResponderInterface      getCareDocumentationServices;
	private final GetCareContactsResponderInterface           getCareContactsServices;
	private final GetDiagnosisResponderInterface              getDiagnosisServices;
	private final GetLaboratoryOrderOutcomeResponderInterface getLaboratoryOrderOutcomeServices;
	private final GetAlertInformationResponderInterface       getAlertInformationResponderInterface;
    private final GetMedicationHistoryResponderInterface      getMedicationHistoryResponderInterface;
    private final GetReferralOutcomeResponderInterface        getReferralOutcomeResponderInterface;
    private final GetImagingOutcomeResponderInterface         getImagingOutcomeResponderInterface;

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
    	final Map<String, Object> props = new HashMap<String, Object>();
    	props.put("schema-validation-enabled", true);
    	jaxWs.setProperties(props);

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
		
		jaxWs.setServiceClass(GetAlertInformationResponderInterface.class);
		jaxWs.setAddress(ALERT_INFORMATION_ENDPOINT);
		getAlertInformationResponderInterface = (GetAlertInformationResponderInterface) create(jaxWs);
		
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

    @Test
    public void GetCareContactsEN13606SourceSuccessTest() {
		GetCareContactsResponseType resp = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER));
		assertFalse(resp.getCareContact().isEmpty());
    }


    @Test
    public void GetCareContactsNotFoundTest() {
        GetCareContactsType req = IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER);
        req.getPatientId().setId("200112121212");
        GetCareContactsResponseType resp = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS_VS_1, req);
        assertTrue(resp.getCareContact().isEmpty());
    }


    @Test
    public void GetEhrCareContactsRIVSourceSuccessTest() {
        GetCareContactsResponseType resp = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(resp.getCareContact().isEmpty());
    }
    
    @Test
    public void GetCareDocumentationEN136060SuccessTest() {
		GetCareDocumentationResponseType resp = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
		assertFalse(resp.getCareDocumentation().isEmpty());
    }
    
    @Test
    public void GetCareDocumentationRIVSourceSuccessTest() {
    	GetCareDocumentationResponseType resp = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
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
    public void GetDiagnosisEN136060SuccessTest() {
    	GetDiagnosisResponseType resp = getDiagnosisServices.getDiagnosis(LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createGetDiagnosisType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getDiagnosis().isEmpty());
    }
    
    @Test
    public void GetDiagnosisRIVSuccessTest() {
    	GetDiagnosisResponseType resp = getDiagnosisServices.getDiagnosis(LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createGetDiagnosisType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getDiagnosis().isEmpty());
    }

    @Test
    public void GetLaboratoryOrderOutcomeEN13606SuccessTest() {
    	GetLaboratoryOrderOutcomeResponseType resp =  getLaboratoryOrderOutcomeServices.getLaboratoryOrderOutcome(LOGICAL_ADDRESS_VS_1, 
    			IntegrationTestDataUtil.createGetLaboratoryOrderOutcomeType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getLaboratoryOrderOutcome().isEmpty());
    }
    
    @Test
    public void GetLaboratoryOrderOutcomeRIVSuccessTest() {
    	GetLaboratoryOrderOutcomeResponseType resp = getLaboratoryOrderOutcomeServices.getLaboratoryOrderOutcome(LOGICAL_ADDRESS_VS_2,
    			IntegrationTestDataUtil.createGetLaboratoryOrderOutcomeType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getLaboratoryOrderOutcome().isEmpty());
    }
    
    @Test
    public void GetAlertInformationEN13606SuccessTest() {
    	GetAlertInformationResponseType resp = getAlertInformationResponderInterface.getAlertInformation(
    			LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createAlertInformationType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getAlertInformation().isEmpty());
    }
    
    @Test
    public void GetAlertInformationRIVSuccessTest() {
    	GetAlertInformationResponseType resp = getAlertInformationResponderInterface.getAlertInformation(
    			LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createAlertInformationType(IntegrationTestDataUtil.NO_TRIGGER));
    	assertFalse(resp.getAlertInformation().isEmpty());
    }
    
    // ---
	
    @Test
    public void GetMedicationHistoryEN13606SuccessTest() {
        GetMedicationHistoryResponseType resp = getMedicationHistoryResponderInterface.getMedicationHistory(
                LOGICAL_ADDRESS_VS_1, IntegrationTestDataUtil.createMedicationHistoryType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(resp.getMedicationMedicalRecord().isEmpty());
    }
    
    @Test
    public void GetMedicationHistoryRIVSuccessTest() {
        GetMedicationHistoryResponseType resp = getMedicationHistoryResponderInterface.getMedicationHistory(
                LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createMedicationHistoryType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(resp.getMedicationMedicalRecord().isEmpty());
    }
    
    // ---
    
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
        GetReferralOutcomeResponseType resp = getReferralOutcomeResponderInterface.getReferralOutcome(
                LOGICAL_ADDRESS_VS_2, IntegrationTestDataUtil.createReferralOutcomeType(IntegrationTestDataUtil.NO_TRIGGER));
        assertFalse(resp.getReferralOutcome().isEmpty());
    }
    
    // ---
    
    @Test
    public void GetImagingOutcomeEN13606SuccessTest() {
        GetImagingOutcomeType type = IntegrationTestDataUtil.createImagingOutcomeType(IntegrationTestDataUtil.NO_TRIGGER);
        GetImagingOutcomeResponseType resp = getImagingOutcomeResponderInterface.getImagingOutcome(LOGICAL_ADDRESS_VS_1, type);
        assertFalse(resp.getImagingOutcome().isEmpty());
    }
    
    @Test
    public void GetImagingOutcomeRIVSuccessTest() {
    	GetImagingOutcomeType giot = IntegrationTestDataUtil.createImagingOutcomeType(IntegrationTestDataUtil.NO_TRIGGER);
        GetImagingOutcomeResponseType resp = getImagingOutcomeResponderInterface.getImagingOutcome(LOGICAL_ADDRESS_VS_2, giot);
        assertFalse(resp.getImagingOutcome().isEmpty());
    }

    // ---
    
    @Test
    public void UpdateTakCacheTest() throws Exception {
    	Flow flow = (Flow) getFlowConstruct("update-tak-cache-http-flow");
    	MuleEvent event = getTestEvent("", flow);
    	flow.process(event);
    }
}

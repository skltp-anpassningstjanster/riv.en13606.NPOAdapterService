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

import javax.xml.ws.soap.SOAPFaultException;

import lombok.extern.slf4j.Slf4j;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mule.api.MuleEvent;
import org.mule.construct.Flow;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;

import riv.clinicalprocess.healthcond.description.enums._2.ResultCodeEnum;
import riv.clinicalprocess.healthcond.description.getcaredocumentation._2.rivtabp21.GetCareDocumentationResponderInterface;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.logistics.logistics.getcarecontacts._2.rivtabp21.GetCareContactsResponderInterface;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType;

/**
 * Created by Peter on 2014-08-14.
 */
@Slf4j
public class EndToEndIntegrationTest extends AbstractIntegrationTestCase {
	
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("NPOAdapter-config");
	
	//TODO: Collect Endpoints from configuration
	private static final String CARE_DOCUMENTATION_ENDPOINT = "http://localhost:11000/npoadapter/getcaredocumentation";
	private static final String CARE_CONTACTS_ENDPOINT = "http://localhost:11000/npoadapter/getcarecontacts";
	
	private static final String LOGICAL_ADDRESS = "SE123456-00";
	private static final String INVALID_LOGICAL_ADDRESS = "XX000000-00";
	
	
	private final GetCareDocumentationResponderInterface getCareDocumentationServices;
	private final GetCareContactsResponderInterface getCareContactsServices;

    public EndToEndIntegrationTest() {
    	setDisposeContextPerClass(true);
    	
    	final JaxWsProxyFactoryBean jaxWs = new JaxWsProxyFactoryBean();
    	
		jaxWs.setServiceClass(GetCareContactsResponderInterface.class);
		jaxWs.setAddress(CARE_CONTACTS_ENDPOINT);
		getCareContactsServices = (GetCareContactsResponderInterface) jaxWs.create();
		
		jaxWs.setServiceClass(GetCareDocumentationResponderInterface.class);
		jaxWs.setAddress(CARE_DOCUMENTATION_ENDPOINT);
		getCareDocumentationServices = (GetCareDocumentationResponderInterface) jaxWs.create();
    }
    
    @Before
    public void init() throws Exception {
    	super.doSetUp();
    }

    @Test
    public void GetCareContactsSuccessTest() {
		GetCareContactsResponseType resp = getCareContactsServices.getCareContacts(LOGICAL_ADDRESS, IntegrationTestDataUtil.createGetCareContactsType(IntegrationTestDataUtil.NO_TRIGGER));
		assertFalse(resp.getCareContact().isEmpty());
    }
    
    @Test
    public void GetCareDocumentationSuccessTest() {
		GetCareDocumentationResponseType resp = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
		assertFalse(resp.getCareDocumentation().isEmpty());
    }
    
    @Test(expected=SOAPFaultException.class)
    public void GetCareDocumentationRoutingExceptionTest() {
    	getCareDocumentationServices.getCareDocumentation(INVALID_LOGICAL_ADDRESS, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.NO_TRIGGER));
    }
    
    @Test
    public void GetCareDocumentationBackEndExceptionTest() {
    	GetCareDocumentationResponseType resp = getCareDocumentationServices.getCareDocumentation(LOGICAL_ADDRESS, IntegrationTestDataUtil.createGetCareDocumentationType(IntegrationTestDataUtil.TRIGGER_ERROR_MESSAGE));
    	assertNotNull(resp.getResult());
    	assertEquals(resp.getResult().getResultCode(), ResultCodeEnum.ERROR);
    }
	
    @Test
    public void UpdateTakCacheTest() throws Exception {
    	Flow flow = (Flow) getFlowConstruct("update-tak-cache-flow");
    	MuleEvent event = getTestEvent("", flow);
    	flow.process(event);
    }
}

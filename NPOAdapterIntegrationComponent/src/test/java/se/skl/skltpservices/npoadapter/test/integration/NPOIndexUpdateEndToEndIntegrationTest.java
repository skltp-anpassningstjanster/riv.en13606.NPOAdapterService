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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.nationellpatientoversikt.ArrayOfinfoTypeInfoTypeType;
import se.nationellpatientoversikt.ArrayOfparameternpoParameterType;
import se.nationellpatientoversikt.InfoTypeType;
import se.nationellpatientoversikt.NPOSoap;
import se.nationellpatientoversikt.NpoParameterType;
import se.skl.skltpservices.npoadapter.mapper.AbstractMapper;
import se.skl.skltpservices.npoadapter.test.stub.EiUpdateWS;

public class NPOIndexUpdateEndToEndIntegrationTest extends AbstractIntegrationTestCase {
	
	private static final List<String> categorizations = new ArrayList<String>();
	
	private static final String NPO_ADAPTER_STUB = "http://localhost:33001/npoadapter/npo/v1";
	private static final String TEST_SUBJECT_OF_CARE = "191101011234";
    
	private final NPOSoap npoServices;
	
	public NPOIndexUpdateEndToEndIntegrationTest() {
		setDisposeContextPerClass(true);
		
		final JaxWsProxyFactoryBean jaxWs = new JaxWsProxyFactoryBean();
		jaxWs.setAddress(NPO_ADAPTER_STUB);
		jaxWs.setServiceClass(NPOSoap.class);
		npoServices = (NPOSoap) create(jaxWs);
	}
	
	
	/**
	 * 
	 * To help so that NPO-Index update is not forgotten when implementing new TK.
	 * See InboundUpdateIndex.class to resolve any failed tests.
	 * 
	 */
	@BeforeClass
	public static void setupCategorizations() throws IllegalArgumentException, IllegalAccessException {
		final Field[] fields = AbstractMapper.class.getDeclaredFields();
		ConcreteDummy dummy = new ConcreteDummy();
		for(Field f : fields) {
			if(f.getModifiers() == (Modifier.FINAL + Modifier.PUBLIC + Modifier.STATIC)) {
				if(f.getName().startsWith("INFO_")) {
					categorizations.add((String) f.get(dummy));
				}
			}
		}
	}
	
	static class ConcreteDummy extends AbstractMapper {}
	
	@Before
	public void init() throws Exception {
		super.doSetUp();
	}
	
	@Test
	public void testSendSimpleIndexSuccess() {
	    logger.debug("Number of categorizations:" + categorizations.size());
		for(String cat : categorizations) {
		    
		    ArrayOfinfoTypeInfoTypeType a = createTypeTypeType(cat, true);
		    ArrayOfparameternpoParameterType p = createParameters();
			assertTrue(npoServices.sendSimpleIndex(TEST_SUBJECT_OF_CARE, a, p));
			logger.debug("categorization:" + cat + " completed successfully");
	        try {
	            logger.debug("1 second pause to let the Adapter call SendStatus asynchronously before the next test is started");
	            Thread.sleep(1000);
	        } catch(InterruptedException ex) {
	            // ignore
	        }
		}
	}
	
    @Test
    public void testSendSimpleIndexEiTimeout() {
		try {
			npoServices.sendSimpleIndex(EiUpdateWS.SUBJECT_OF_CARE_ID_EI_TIMEOUT, createTypeTypeType(categorizations.get(0), true), createParameters());
			fail("expected exception");
		}
		catch(SOAPFaultException e) {			
			assertTrue(e.getMessage().contains("Read timed out"));
		}
    }
	
    @Test
    public void testSendSimpleIndexEiException() {
		try {
			npoServices.sendSimpleIndex(EiUpdateWS.SUBJECT_OF_CARE_ID_EI_EXCEPTION, createTypeTypeType(categorizations.get(0), true), createParameters());
			fail("expected exception");
		}
		catch(SOAPFaultException e) {			
			assertTrue(e.getMessage().contains("Exception occurred in engagementindex"));
		}
    }
    
	@Test
	public void testSendSimpleIndexFailInvalidCategorization() {
		try {
			npoServices.sendSimpleIndex(TEST_SUBJECT_OF_CARE, createTypeTypeType("FAIL", true), createParameters());
			fail("expected exception");
		}
		catch(SOAPFaultException e) {			
			assertTrue(e.getMessage().contains("errorCode:3003"));
		}
	}
	
	@Test
	public void testSendSimpleIndexFailNullCategorization() {
		try {
			npoServices.sendSimpleIndex(TEST_SUBJECT_OF_CARE, createTypeTypeType(null, true), createParameters());
			fail("expected exception");
		}
		catch(SOAPFaultException e) {			
			assertTrue(e.getMessage().contains("errorCode:3003"));
		}
	}
	
	
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
	
	private ArrayOfinfoTypeInfoTypeType createTypeTypeType(final String categorization, final boolean exists) {
		final ArrayOfinfoTypeInfoTypeType typeTypeType = new ArrayOfinfoTypeInfoTypeType();
		final InfoTypeType typeType = new InfoTypeType();
		typeType.setExists(exists);
		typeType.setInfoTypeId(categorization);
		typeTypeType.getInfoType().add(typeType);
		return typeTypeType;
	}
	
	private ArrayOfparameternpoParameterType createParameters() {
		final ArrayOfparameternpoParameterType params = new ArrayOfparameternpoParameterType();
		final NpoParameterType hsaId = new NpoParameterType();
		final NpoParameterType transact = new NpoParameterType();
		final NpoParameterType version = new NpoParameterType();
		hsaId.setName("hsa_id");
		hsaId.setValue("VS-1");
		transact.setName("transaction_id");
		transact.setValue("NPOIndexUpdateEndToEndIntegrationTest");
		version.setName("version");
		version.setValue("1.1");
		params.getParameter().add(version);
		params.getParameter().add(transact);
		params.getParameter().add(hsaId);
		return params;
	}
	
}

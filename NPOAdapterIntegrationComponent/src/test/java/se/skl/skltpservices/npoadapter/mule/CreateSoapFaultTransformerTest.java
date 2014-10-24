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
package se.skl.skltpservices.npoadapter.mule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.DefaultMuleMessage;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.EndpointException;
import org.mule.api.endpoint.EndpointURI;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.endpoint.DefaultOutboundEndpoint;
import org.mule.message.DefaultExceptionPayload;

import se.skl.skltpservices.npoadapter.mapper.error.MapperException;

public class CreateSoapFaultTransformerTest {
	
	private static CreateSoapFaultTransformer transformer;
	
	private static final String TEST_ADDRESS = "ThisIsATestAddress";
	private static final String TEST_EXCEPTION_MESSAGE = "ThisIsATestExceptionMessage";
	private static String UNIQUE_ID;
	
	private static ImmutableEndpoint endpoint;
	private static EndpointURI uri;
	private static MuleMessage messageContainingExceptionPayload;
	private static ExceptionPayload payload;
	private static MuleContext ctx;
	
	
	@BeforeClass
	public static void init() throws InitialisationException, EndpointException, MuleException {
		transformer = new CreateSoapFaultTransformer();
		ctx = Mockito.mock(MuleContext.class);
		
		messageContainingExceptionPayload = new DefaultMuleMessage("payload", ctx);
		UNIQUE_ID = messageContainingExceptionPayload.getUniqueId();
		
		payload = new DefaultExceptionPayload(new MapperException(TEST_EXCEPTION_MESSAGE));
		messageContainingExceptionPayload.setExceptionPayload(payload);

		uri = Mockito.mock(EndpointURI.class);
		Mockito.when(uri.getAddress()).thenReturn(TEST_ADDRESS);
		endpoint = Mockito.mock(DefaultOutboundEndpoint.class);
		Mockito.when(endpoint.getEndpointURI()).thenReturn(uri);
		transformer.setEndpoint(endpoint);
	}

	@Test
	public void testTransformMessage() throws Exception {

	    // exercise public method
		MuleMessage muleMessage = (MuleMessage) transformer.transformMessage(messageContainingExceptionPayload, "UTF-8");
		
        final Map<String,String> details = new LinkedHashMap<String,String>();
        details.put("id", UNIQUE_ID);
		
        // exercise protected method
		String soapFaultString = transformer.createSoapFaultString("Server", CreateSoapFaultTransformer.ERRORMESSAGEPREFIX + " " + TEST_EXCEPTION_MESSAGE, TEST_ADDRESS, details);
		
		// compare results
		assertEquals(soapFaultString, (String) muleMessage.getPayload());
		assertTrue(((String)muleMessage.getPayload()).contains(CreateSoapFaultTransformer.ERRORMESSAGEPREFIX));
		assertTrue(((String)muleMessage.getPayload()).contains("faultcode"));
		assertTrue(((String)muleMessage.getPayload()).contains("faultstring"));
	}
}

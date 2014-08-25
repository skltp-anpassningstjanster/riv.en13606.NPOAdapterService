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

import lombok.extern.slf4j.Slf4j;
import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;

import java.text.MessageFormat;

/**
 * Generate textual soap error message with implemenation specific parameters such as mule-message uniqueId.
 * 
 * 
 * @author torbjorncla
 *
 */
@Slf4j
public class CreateSoapFaultTransformer extends AbstractMessageTransformer {

	private static final String SOAP_FAULT_V11 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
			+ "  <soapenv:Header/>"
			+ "  <soapenv:Body>"
			+ "    <soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
			+ "      <faultcode>soap:Server</faultcode>\n"
			+ "      <faultstring>{0}</faultstring>\n"
			+ "      <faultactor>{1}</faultactor>\n"
			+ "      <detail>\n"
			+ "        <id>{2}</id>\n"
			+ "      </detail>\n"
			+ "    </soap:Fault>" + "  </soapenv:Body>" + "</soapenv:Envelope>";

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		ExceptionPayload ep = message.getExceptionPayload();
		if (ep == null) {
			return message;
		}

		String soapFault = createSoapFaultFromExceptionPayload(ep, message);

		message.setExceptionPayload(null);
		message.setProperty("http.status", 500, PropertyScope.OUTBOUND);
		message.setPayload(soapFault);
		return message;
	}

	protected String createSoapFaultFromExceptionPayload(ExceptionPayload ep, MuleMessage message) {
		Throwable e = (ep.getRootException() != null) ? ep.getRootException()
				: ep.getException();

		final String errMsg = e.getMessage();
		final String endpoint = getEndpoint().getEndpointURI().getAddress();
		final String id = message.getUniqueId();
		return createSoapFault(errMsg, endpoint, id);
	}

	protected String createSoapFault(String errMsg, String endpoint,
			String id) {
		return MessageFormat.format(SOAP_FAULT_V11, errMsg, endpoint, id);
	}

}

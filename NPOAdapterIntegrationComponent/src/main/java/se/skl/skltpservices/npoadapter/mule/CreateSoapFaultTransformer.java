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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.mule.api.ExceptionPayload;
import org.mule.api.MuleMessage;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.mapper.error.AdapterException;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;

/**
 * Convert a MuleMessage with exception payload to a MuleMessage with a soap fault payload.
 * 
 * Soap fault contains textual information, including implementation specific parameters such as mule-message uniqueId.
 * 
 * Generally the Adapter should avoid returning faults. As far as possible we want to transform the data
 * in the message, and pass the message on. But if we are unable to pass the message on, then return a fault 
 * containing as much useful information as possible in order to help locate where the problem is.
 * 
 * @author torbjorncla
 */
public class CreateSoapFaultTransformer extends AbstractMessageTransformer {
	
	private static final Logger log = LoggerFactory.getLogger(CreateSoapFaultTransformer.class);
	
	protected static final String ERRORMESSAGEPREFIX = "[ehr13606 adapter]"; 

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws org.mule.api.transformer.TransformerException {

	    ExceptionPayload ep = message.getExceptionPayload();
		if (ep == null) {
			return message;
		}
		
        message.setPayload(createSoapFaultStringFromExceptionPayload(ep, message));
		message.setExceptionPayload(null);
		message.setProperty("http.status", 500, PropertyScope.OUTBOUND);
		return message;
	}

	
	protected String createSoapFaultStringFromExceptionPayload(ExceptionPayload ep, MuleMessage message) {
	    
		Throwable e = (ep.getRootException() != null) ? ep.getRootException() : ep.getException();
		
		String errorMessage = ERRORMESSAGEPREFIX + " " + e.getMessage();

		
		// prepend the error code from the exception, if there is one
		if (e instanceof AdapterException) {
			Ehr13606AdapterError error = ((AdapterException)e).getEhr13606AdapterError();
			if (error.isDefinedError()) {
			    errorMessage = "[errorCode:" + error.getErrorCode() + "] " + errorMessage;
			}
		}
		
		final String endpoint = getEndpoint().getEndpointURI().getAddress();
		
		final Map<String,String> detail = new LinkedHashMap<String,String>();
		detail.put("id", message.getUniqueId());
		// possible to add further fields into detail
		
		// set all faults to faultcode 'Server' 
		// (although a validation or data error would be faultcode 'Client')
		return createSoapFaultString("Server", errorMessage, endpoint, detail);
	}

	
    // --- --------------------------------------------------------------------
	// xml String is xmlEncoded by the SOAPMessage.
	//
	// faultcode
	//   one of VersionMismatch, MustUnderstand, Client, Server
	// faultstring
	//   human readable error message
	// faultactor
	//   The value of the faultactor attribute is a URI identifying the source.
	//   The URI of the invoked Web service.
	// details
	//   Holds a list of detail entries.
	//   Holds application specific error information related to the Body element.
	//   Indicates that the Body element was processed.
	// --- --------------------------------------------------------------------
	protected String createSoapFaultString(final String faultcode, final String faultstring, final String faultactor, final Map<String,String> details) {

	    String soapFaultAsString = "";
	    
        try {
            MessageFactory mf = MessageFactory.newInstance();
            SOAPMessage soapMessage = mf.createMessage();
            SOAPFault sft = soapMessage.getSOAPBody().addFault();
            sft.setFaultCode(new QName(SOAPConstants.URI_NS_SOAP_ENVELOPE, faultcode));
            sft.setFaultString(faultstring);
            sft.setFaultActor(faultactor);
            
            if (details != null && !details.isEmpty()) {
                Detail detail = sft.addDetail();
                Iterator<Entry<String, String>> it = details.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
                    QName entryName = new QName(pair.getKey());
                    DetailEntry entry = detail.addDetailEntry(entryName);
                    entry.addTextNode(pair.getValue());
                }
            }
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            soapMessage.writeTo(os);
            soapFaultAsString = new String(os.toByteArray(),"UTF-8");
        } catch (IOException | SOAPException e) {
            throw new RuntimeException("Fatal exception attempting to create soap fault", e);
        }
        
        if (logger.isDebugEnabled()) {
            prettyprintXml(soapFaultAsString);
        }
        
        return soapFaultAsString;
	}

	
    // 
	private void prettyprintXml(String soapFaultString) {
        try {
            Source xmlInput = new StreamSource(new StringReader(soapFaultString));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            logger.debug("\n" + xmlOutput.getWriter().toString());        
        } catch (javax.xml.transform.TransformerException ioe) {
            log.error("Unexpected exception in prettyprintXml", ioe);
        }
    }
}

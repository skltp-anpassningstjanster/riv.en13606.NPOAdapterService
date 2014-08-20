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
package se.skl.skltpservices.npoadapter.test.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.test.Util;

import javax.jws.WebService;

/**
 * Test stub always returning a fix response.
 */
@WebService(serviceName = "RIV13606REQUEST_EHR_EXTRACT_Service",
        endpointInterface = "se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTPortType",
        portName = "RIV13606REQUEST_EHR_EXTRACT_Port",
        targetNamespace = "urn:riv13606:v1.1")
public class EhrExtractWS implements RIV13606REQUESTEHREXTRACTPortType {
    //
    static final Logger log = LoggerFactory.getLogger(EhrExtractWS.class);

    private static final String VKO = "vko";
    private static final String VOO = "voo";
    
    //Public accessible for testing.
    public static final String NOT_IMPLEMENTED_YET_TEXT = "This function is not yet implemented";
    public static final String INTEGRATION_TEST_ERROR_TEXT = "This is a error message";
    public static final String PATIENT_ID_TRIGGER_ERROR = "triggerError";
    public static final String PATIENT_ID_TRIGGER_WARNING = "triggerWarning";
    public static final String PATIENT_ID_TRIGGER_INFO = "triggerInfo";
        
    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTCONTINUATION(RIV13606REQUESTEHREXTRACTCONTINUATIONRequestType request) {
        return null;
    }

    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACT(RIV13606REQUESTEHREXTRACTRequestType request) {
    	//See if error flow is triggered.
    	switch(request.getSubjectOfCareId().getExtension()) {
    	case PATIENT_ID_TRIGGER_ERROR:
    		return createAlternativeResponse(ResponseDetailTypeCodes.E, INTEGRATION_TEST_ERROR_TEXT);
    	case PATIENT_ID_TRIGGER_WARNING:
    		return createAlternativeResponse(ResponseDetailTypeCodes.W, NOT_IMPLEMENTED_YET_TEXT);
    	case PATIENT_ID_TRIGGER_INFO:
    		return createAlternativeResponse(ResponseDetailTypeCodes.I, NOT_IMPLEMENTED_YET_TEXT);
    	}
    	
    	
    	//Return testdata
    	final RIV13606REQUESTEHREXTRACTResponseType responseType = new RIV13606REQUESTEHREXTRACTResponseType();
        switch(request.getMeanings().get(0).getCode()) {
        case VKO:
        	log.info("Recived VKO Request");
        	responseType.getEhrExtract().add(getTestData(Util.CARECONTACS_TEST_FILE));
        	break;
        case VOO:
        	log.info("Recived VOO Request");
        	responseType.getEhrExtract().add(getTestData(Util.CAREDOCUMENTATION_TEST_FILE));
        	break;
        default:
        	return createAlternativeResponse(ResponseDetailTypeCodes.E, NOT_IMPLEMENTED_YET_TEXT);
        }
        return responseType;
    }
    
    protected EHREXTRACT getTestData(final String path) {
    	return Util.loadEhrTestData(path);
    }
    
    protected RIV13606REQUESTEHREXTRACTResponseType createAlternativeResponse(final ResponseDetailTypeCodes code, final String msg) {
    	final RIV13606REQUESTEHREXTRACTResponseType resp = new RIV13606REQUESTEHREXTRACTResponseType();
    	final ResponseDetailType detail = new ResponseDetailType();
    	final ST st = new ST();
    	final CD cd = new CD();
    	st.setValue(msg);
    	detail.setTypeCode(code);
    	detail.setCode(cd);
    	detail.setText(st);
    	resp.getResponseDetail().add(detail);
    	return resp;
    }
}

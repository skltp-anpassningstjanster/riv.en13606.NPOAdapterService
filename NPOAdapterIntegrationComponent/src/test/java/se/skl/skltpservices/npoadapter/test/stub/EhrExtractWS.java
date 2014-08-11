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

import javax.jws.WebParam;
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

    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTCONTINUATION(@WebParam(partName = "RIV13606REQUEST_EHR_EXTRACT_request", name = "RIV13606REQUEST_EHR_EXTRACT_CONTINUATION_request", targetNamespace = "urn:riv13606:v1.1") RIV13606REQUESTEHREXTRACTCONTINUATIONRequestType request) {
        return null;
    }

    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACT(@WebParam(partName = "RIV13606REQUEST_EHR_EXTRACT_request", name = "RIV13606REQUEST_EHR_EXTRACT_request", targetNamespace = "urn:riv13606:v1.1") RIV13606REQUESTEHREXTRACTRequestType request) {
        log.info("call received: ");
        final EHREXTRACT ehrExtract = Util.loadTestData(Util.CARECONTACS_TEST_FILE);
        final RIV13606REQUESTEHREXTRACTResponseType responseType = new RIV13606REQUESTEHREXTRACTResponseType();

        responseType.getEhrExtract().add(ehrExtract);

        return responseType;
    }
}

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

import javax.jws.WebService;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import riv.ehr.patientsummary.getehrextract._1.rivtabp21.GetEhrExtractResponderInterface;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * Created by Peter on 2014-08-15.
 */
@WebService(      serviceName = "GetEhrExtractResponderService",
            endpointInterface = "riv.ehr.patientsummary.getehrextract._1.rivtabp21.GetEhrExtractResponderInterface",
              targetNamespace = "urn:riv:ehr:patientsummary:GetEhrExtract:1:rivtabp21",
                     portName = "GetEhrExtractResponderPort")
public class GetEhrExtractWS implements GetEhrExtractResponderInterface {
	
	private final static Logger log = LoggerFactory.getLogger(GetEhrExtractWS.class);
	
    @Override
    public GetEhrExtractResponseType getEhrExtract(String logicalAddress, GetEhrExtractType request) {
    	try {
    		final String infoType = request.getMeanings().get(0).getCode();
    		final se.rivta.en13606.ehrextract.v11.EHREXTRACT baseline = getBaselineData(infoType);
    		final RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType = new RIV13606REQUESTEHREXTRACTResponseType();
    		riv13606REQUESTEHREXTRACTResponseType.getEhrExtract().add(baseline);

    		final GetEhrExtractResponseType responseType = XMLBeanMapper.getInstance().map(riv13606REQUESTEHREXTRACTResponseType, GetEhrExtractResponseType.class);
    		return responseType;
    	} catch (Exception err) {
    		return new GetEhrExtractResponseType();
    	}
    }

    protected se.rivta.en13606.ehrextract.v11.EHREXTRACT getBaselineData(final String infoType) throws JAXBException {
        switch (infoType) {
            case "vko":
                log.info("Received vko request");
                return Util.loadEhrTestData(Util.CARECONTACTS_TEST_FILE_1);
            case "voo":
                log.info("Received voo request");
                return Util.loadEhrTestData(Util.CAREDOCUMENTATION_TEST_FILE);
            case "dia":
            	log.info("Received dia request");
            	return Util.loadEhrTestData(Util.DIAGNOSIS_TEST_FILE);
            case "und-kkm-kli":
            	log.info("Received und-kkm-kli request");
            	return Util.loadEhrTestData(Util.LAB_TEST_FILE_1);
            case "upp":
            	log.info("Received upp request");
            	return Util.loadEhrTestData(Util.ALERT_TEST_FILE);
            case "lkm-ord":
                log.info("Received lkm request");
                return Util.loadEhrTestData(Util.MEDICATIONHISTORY_TEST_FILE_1);
            case "und-kon":
                log.info("Received und-kon request");
                return Util.loadEhrTestData(Util.REFERRALOUTCOME_TEST_FILE_1);
            case "und-bdi":
                log.info("Received und-bdi request");
                return Util.loadEhrTestData(Util.IMAGINGOUTCOME_TEST_FILE);
            default:
                throw new IllegalArgumentException("Unknown information type (meanings code): " + infoType);
        }
    }

}

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

import lombok.extern.slf4j.Slf4j;
import riv.ehr.patientsummary.getehrextract._1.rivtabp21.GetEhrExtractResponderInterface;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * Created by Peter on 2014-08-15.
 */
@Slf4j
@WebService(serviceName = "GetEhrExtractResponderService",
        endpointInterface = "riv.ehr.patientsummary.getehrextract._1.rivtabp21.GetEhrExtractResponderInterface",
        targetNamespace = "urn:riv:ehr:patientsummary:GetEhrExtract:1:rivtabp21",
        portName = "GetEhrExtractResponderPort")
public class GetEhrExtractWS implements GetEhrExtractResponderInterface {
    @Override
    public GetEhrExtractResponseType getEhrExtract(String logicalAddress, GetEhrExtractType request) {

        final String infoType = request.getMeanings().get(0).getCode();
        final se.rivta.en13606.ehrextract.v11.EHREXTRACT baseline = getBaselineData(infoType);
        final RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType = new RIV13606REQUESTEHREXTRACTResponseType();
        riv13606REQUESTEHREXTRACTResponseType.getEhrExtract().add(baseline);

        final GetEhrExtractResponseType responseType = XMLBeanMapper.getInstance().map(riv13606REQUESTEHREXTRACTResponseType, GetEhrExtractResponseType.class);
        return responseType;
    }

    protected se.rivta.en13606.ehrextract.v11.EHREXTRACT getBaselineData(final String infoType) {
        switch (infoType) {
            case "vko":
                log.info("Received VKO Request");
                return Util.loadEhrTestData(Util.CARECONTACS_TEST_FILE);
            case "voo":
                log.info("Received VOO Request");
                return Util.loadEhrTestData(Util.CAREDOCUMENTATION_TEST_FILE);
            case "dia":
            	log.info("Received DIA Request");
            	return Util.loadEhrTestData(Util.DIAGNOSIS_TEST_FILE);
            case "und-kkm-kli":
            	log.info("und-kkm-kli");
            	return Util.loadEhrTestData(Util.LAB_TEST_FILE);
            case "upp":
            	log.info("Received UPP Request");
            	return Util.loadEhrTestData(Util.ALERT_TEST_FILE);
            case "lkm":
                log.info("Received LKM Request");
                return Util.loadEhrTestData(Util.MEDICALHISTORY_TEST_FILE);
            case "und-kon":
                log.info("Received UND-KON Request");
                return Util.loadEhrTestData(Util.REFERRALOUTCOME_TEST_FILE);
            case "und-bdi":
                log.info("Received UND-BDI Request");
                return Util.loadEhrTestData(Util.IMAGINGOUTCOME_TEST_FILE);
            default:
                throw new IllegalArgumentException("Unknown information type (meanings code): " + infoType);
        }
    }

}

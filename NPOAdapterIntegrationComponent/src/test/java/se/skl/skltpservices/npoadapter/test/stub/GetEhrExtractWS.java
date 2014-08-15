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

import lombok.extern.slf4j.Slf4j;
import riv.ehr.patientsummary._1.EHREXTRACT;
import riv.ehr.patientsummary.getehrextract._1.rivtabp21.GetEhrExtractResponderInterface;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.skl.skltpservices.npoadapter.mapper.AbstractMapper;
import se.skl.skltpservices.npoadapter.test.Util;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlRootElement;

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
    public GetEhrExtractResponseType getEhrExtract(@WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress,
                                                   @WebParam(partName = "parameters", name = "GetEhrExtract", targetNamespace = "urn:riv:ehr:patientsummary:GetEhrExtractResponder:1") GetEhrExtractType request) {

        final GetEhrExtractResponseType responseType = new GetEhrExtractResponseType();

        final se.rivta.en13606.ehrextract.v11.EHREXTRACT baseline;
        final String infoType = request.getMeanings().get(0).getCode();
        switch(infoType) {
            case "vko":
                log.info("Received VKO Request");
                baseline = Util.loadEhrTestData(Util.CARECONTACS_TEST_FILE);
                break;
            case "voo":
                log.info("Received VOO Request");
                baseline = Util.loadEhrTestData(Util.CAREDOCUMENTATION_TEST_FILE);
                break;
            default:
                throw new IllegalArgumentException("Unknown information type (meanings code): " + infoType);
        }

        final EHREXTRACT ehrextract = AbstractMapper.getDozerBeanMapper().map(baseline, EHREXTRACT.class);
        responseType.getEhrExtract().add(ehrextract);

        return responseType;
    }

}

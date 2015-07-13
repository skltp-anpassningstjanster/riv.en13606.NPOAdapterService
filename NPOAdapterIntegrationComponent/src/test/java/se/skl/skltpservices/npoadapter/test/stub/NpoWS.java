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

import java.util.Arrays;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.nationellpatientoversikt.ArrayOfcheckConsistencyCheckConsistencyType;
import se.nationellpatientoversikt.ArrayOfdeletionDeletionType;
import se.nationellpatientoversikt.ArrayOfindexUpdateIndexUpdateType;
import se.nationellpatientoversikt.ArrayOfinfoTypeInfoTypeType;
import se.nationellpatientoversikt.ArrayOfparameternpoParameterType;
import se.nationellpatientoversikt.ArrayOfresponseDetailnpoResponseDetailType;
import se.nationellpatientoversikt.ArrayOfsubjectOfCareIdString;
import se.nationellpatientoversikt.InfoTypeType;
import se.nationellpatientoversikt.NPOSoap;
import se.nationellpatientoversikt.NpoParameterType;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.OutboundResponseException;

/**
 * Stub implementation of NPOv1 interface
 * Used when NPOAdapter needs to call NPOv1 - for sendSimpleIndex.
 */
@WebService(serviceName       = "NPO",
            endpointInterface = "se.nationellpatientoversikt.NPOSoap",
            portName          = "NPOSoap",
            targetNamespace   = "http://nationellpatientoversikt.se")
public class NpoWS implements NPOSoap {

    // mandatory npo parameters.
    static final List<String> NPO_PARAMETERS = Arrays.asList("hsa_id", "transaction_id", "version");

    private static final Logger log = LoggerFactory.getLogger(NpoWS.class);
    
    /**
     * Return true if service is alive.
     */
    @Override
    public Boolean notifyAlive(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") 
                               ArrayOfparameternpoParameterType parameters) {
        log.info("NpoWS (stub) notifyAlive - true");
        return Boolean.TRUE;
    }
    
    /**
     * Called by NPOAdapter when FORWARD_INDEX_TO_NPO=true
     */
    @Override
    public Boolean sendSimpleIndex(
            @WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se")
            String subjectOfCareId,
            @WebParam(name = "info_types", targetNamespace = "http://nationellpatientoversikt.se")
            ArrayOfinfoTypeInfoTypeType infoTypes,
            @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se")
            ArrayOfparameternpoParameterType parameters
        ) {
        log.info("NpoWS (stub) sendSimpleIndex");
        
        log.info("subjectOfCareId:{}", subjectOfCareId);
        
        for (InfoTypeType infoTypeType : infoTypes.getInfoType()) {
            log.info("infoTypeId:{}", infoTypeType.getInfoTypeId());
            log.info("infoTypeIsExists:{}",infoTypeType.isExists());
        }
        
        for (NpoParameterType parameter : parameters.getParameter()) {
            log.info("parameter:{}, value:{}", parameter.getName(), parameter.getValue());
        }
        return Boolean.TRUE;
    }
    
    // --- ------------

    /** Not implemented */
    @Override
    public Boolean sendIndex2(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "index_updates", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfindexUpdateIndexUpdateType indexUpdates, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters) {
        throw appendToNativeException(new OutboundResponseException("Unsupported operation, adapter doesn't support sendIndex2", Ehr13606AdapterError.UNSUPPORTED));
    }

    /** Not implemented */
    @Override
    public Boolean sendDeletions(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "deletions", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfdeletionDeletionType deletions, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters) {
        throw appendToNativeException(new OutboundResponseException("Unsupported operation, adapter doesn't support sendDeletions", Ehr13606AdapterError.UNSUPPORTED));
    }

    /** Not implemented */
    @Override
    public void checkConsistency(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar fromTime, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(mode = WebParam.Mode.OUT, name = "updates", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfcheckConsistencyCheckConsistencyType> updates, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw appendToNativeException(new OutboundResponseException("Unsupported operation, adapter doesn't support checkConsistency", Ehr13606AdapterError.UNSUPPORTED));
    }

    /** Not implemented */
    @Override
    public void getConsistencyList(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar fromTime, @WebParam(mode = WebParam.Mode.OUT, name = "subject_of_care_ids", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfsubjectOfCareIdString> subjectOfCareIds, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw appendToNativeException(new OutboundResponseException("Unsupported operation, adapter doesn't support getConsistencyList", Ehr13606AdapterError.UNSUPPORTED));
    }
    
    protected IllegalArgumentException appendToNativeException(final OutboundResponseException error) {
    	return new IllegalArgumentException(error.getMessage(), error);
    }
}

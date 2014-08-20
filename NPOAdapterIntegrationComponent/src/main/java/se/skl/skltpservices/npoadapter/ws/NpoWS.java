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
package se.skl.skltpservices.npoadapter.ws;

import org.apache.commons.lang.StringUtils;
import se.nationellpatientoversikt.*;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Peter on 2014-08-19.
 */
@WebService(serviceName = "NPO",
        endpointInterface = "se.nationellpatientoversikt.NPOSoap",
        portName = "NPOSoap",
        targetNamespace = "http://nationellpatientoversikt.se")
public class NpoWS implements NPOSoap {

    // mandatory npo parameters.
    static final List<String> NPO_PARAMETERS = Arrays.asList("hsa_id", "transaction_id", "version");

    @Override
    public Boolean sendSimpleIndex(String subjectOfCareId, ArrayOfinfoTypeInfoTypeType infoTypes, ArrayOfparameternpoParameterType parameters) {

        validate(parameters);

        return Boolean.TRUE;
    }

    //
    private void validate(final ArrayOfparameternpoParameterType parameters) {

        final List<String> expected = new ArrayList<String>(NPO_PARAMETERS);
        for (final NpoParameterType p : parameters.getParameter()) {
            if (StringUtils.isBlank(p.getValue())) {
                throw new IllegalArgumentException("Invalid (empty) value for mandatory request parameter: " + p.getName());
            }
            expected.remove(p.getName());
        }
        if (!expected.isEmpty()) {
            throw new IllegalArgumentException("Missing mandatory request parameter(s): " + expected);
        }
    }

    @Override
    public Boolean sendDeletions(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "deletions", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfdeletionDeletionType deletions, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters) {
        throw new IllegalArgumentException("Unsupported operation, adapter doesn't support sendDeletions");
    }

    @Override
    public void checkConsistency(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar fromTime, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(mode = WebParam.Mode.OUT, name = "updates", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfcheckConsistencyCheckConsistencyType> updates, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw new IllegalArgumentException("Unsupported operation, adapter doesn't support checkConsistency");
    }

    @Override
    public Boolean notifyAlive(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters) {
        return Boolean.TRUE;
    }

    @Override
    public Boolean sendIndex2(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "index_updates", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfindexUpdateIndexUpdateType indexUpdates, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters) {
        validate(parameters);
        return Boolean.TRUE;
    }

    @Override
    public void getConsistencyList(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar fromTime, @WebParam(mode = WebParam.Mode.OUT, name = "subject_of_care_ids", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfsubjectOfCareIdString> subjectOfCareIds, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw new IllegalArgumentException("Unsupported operation, adapter doesn't support getConsistencyList");
    }
}

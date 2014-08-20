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

import com.sun.org.apache.xpath.internal.operations.Bool;
import se.nationellpatientoversikt.*;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;

/**
 * Created by Peter on 2014-08-20.
 */
@WebService(serviceName = "CS",
        endpointInterface = "se.nationellpatientoversikt.CSSoap",
        portName = "CSSoap",
        targetNamespace = "http://nationellpatientoversikt.se")
public class CareSystemWS implements CSSoap {
    @Override
    public void getSimpleIndex(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(mode = WebParam.Mode.INOUT, name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") Holder<XMLGregorianCalendar> fromTime, @WebParam(mode = WebParam.Mode.OUT, name = "subject_of_care_info_types", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfsubjectOfCareInfoTypeSubjectOfCareInfoTypesType> subjectOfCareInfoTypes, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw new IllegalStateException("Operation not supported.");
    }

    @Override
    public void getPatientList(@WebParam(name = "purpose", targetNamespace = "http://nationellpatientoversikt.se") String purpose, @WebParam(name = "info_type_ids", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfinfoTypeIdsItemString infoTypeIds, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(mode = WebParam.Mode.INOUT, name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") Holder<XMLGregorianCalendar> fromTime, @WebParam(mode = WebParam.Mode.OUT, name = "subject_of_care_ids", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfsubjectOfCareIdString> subjectOfCareIds, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw new IllegalStateException("Operation not supported.");
    }

    @Override
    public Boolean sendStatus(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfresponseDetailnpoResponseDetailType responseDetails) {
        return Boolean.TRUE;
    }

    @Override
    public void getIndex2(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar fromTime, @WebParam(name = "to_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar toTime, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(mode = WebParam.Mode.OUT, name = "index_updates", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfindexUpdateIndexUpdateType> indexUpdates, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw new IllegalStateException("Operation not supported.");
    }

    @Override
    public void getDeletions(@WebParam(name = "subject_of_care_id", targetNamespace = "http://nationellpatientoversikt.se") String subjectOfCareId, @WebParam(name = "from_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar fromTime, @WebParam(name = "to_time", targetNamespace = "http://nationellpatientoversikt.se") XMLGregorianCalendar toTime, @WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters, @WebParam(mode = WebParam.Mode.OUT, name = "deletions", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfdeletionDeletionType> deletions, @WebParam(mode = WebParam.Mode.OUT, name = "response_details", targetNamespace = "http://nationellpatientoversikt.se") Holder<ArrayOfresponseDetailnpoResponseDetailType> responseDetails) {
        throw new IllegalStateException("Operation not supported.");
    }

    @Override
    public Boolean checkAlive(@WebParam(name = "parameters", targetNamespace = "http://nationellpatientoversikt.se") ArrayOfparameternpoParameterType parameters) {
        return Boolean.TRUE;
    }
}

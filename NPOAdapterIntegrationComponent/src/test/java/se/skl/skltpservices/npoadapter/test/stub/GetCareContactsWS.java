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

import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponderInterface;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsType;

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Created by Peter on 2014-07-30.
 */
@WebService(serviceName = "GetCareContactsResponderService",
        endpointInterface = "se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponderInterface",
        portName = "GetCareContactsResponderPort",
        targetNamespace = "urn:riv:clinicalprocess:logistics:logistics:GetCareContacts:2:rivtabp21")
public class GetCareContactsWS implements GetCareContactsResponderInterface {
    @Override
    public GetCareContactsResponseType getCareContacts(@WebParam(partName = "LogicalAddress", name = "LogicalAddress", targetNamespace = "urn:riv:itintegration:registry:1", header = true) String logicalAddress, @WebParam(partName = "parameters", name = "GetCareContacts", targetNamespace = "urn:riv:clinicalprocess:logistics:logistics:GetCareContactsResponder:2") GetCareContactsType parameters) {
        return null;
    }
}

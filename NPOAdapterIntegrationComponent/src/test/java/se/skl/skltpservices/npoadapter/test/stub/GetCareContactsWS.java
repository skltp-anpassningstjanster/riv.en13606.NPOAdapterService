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

import riv.clinicalprocess.logistics.logistics.getcarecontacts._2.rivtabp21.GetCareContactsResponderInterface;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsType;

/**
 * Created by Peter on 2014-07-30.
 */
@WebService(serviceName = "GetCareContactsResponderService",
        endpointInterface = "riv.clinicalprocess.logistics.logistics.getcarecontacts._2.rivtabp21.GetCareContactsResponderInterface",
        portName = "GetCareContactsResponderPort",
        targetNamespace = "urn:riv:clinicalprocess:logistics:logistics:GetCareContacts:2:rivtabp21")
public class GetCareContactsWS implements GetCareContactsResponderInterface {
    @Override
    public GetCareContactsResponseType getCareContacts(String logicalAddress, GetCareContactsType parameters) {
        final GetCareContactsResponseType response = new GetCareContactsResponseType();

        response.getCareContact();

        return response;
    }


}

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
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

import javax.jws.WebService;


/**
 * Created by Peter on 2014-08-20.
 */
@Slf4j
@WebService(serviceName = "UpdateResponderService",
        endpointInterface = "riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface",
        portName = "UpdateResponderPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class EiUpdateWS implements UpdateResponderInterface {

    @Override
    public UpdateResponseType update(String logicalAddress, UpdateType parameters) {

        log.info("EI Update called.");

        final UpdateResponseType response = new UpdateResponseType();

        response.setComment("Looks alright.");
        response.setResultCode(ResultCodeEnum.OK);

        return response;
    }
}

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

import lombok.extern.slf4j.Slf4j;
import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

import javax.jws.WebService;

/**
 * Web Service implementing the Update engagement index service <p/>
 *
 * Please also see the mule flow "npo-update-service.xml" for the full service implementation and
 * to understand how this Web Service is utilized.
 *
 * @author peter
 */
@Slf4j
@WebService(serviceName = "UpdateResponderService",
        endpointInterface = "riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface",
        portName = "UpdateResponderPort",
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class UpdateWS implements UpdateResponderInterface {
    @Override
    public UpdateResponseType update(String logicalAddress, UpdateType request) {

        for (final EngagementTransactionType transaction : request.getEngagementTransaction()) {
            validate(transaction.getEngagement());
        }

        final UpdateResponseType response = new UpdateResponseType();
        response.setResultCode(ResultCodeEnum.OK);
        response.setComment("OK");

        return response;
    }

    //
    private void validate(EngagementType engagement) {
        notnull(engagement.getRegisteredResidentIdentification(), "registeredResidentIdentification");
        notnull(engagement.getCategorization(), "categorization");
        notnull(engagement.getSourceSystem(), "sourceSystem");
        notnull(engagement.getServiceDomain(), "serviceDomain");
        notnull(engagement.getDataController(), "dataController");
        notnull(engagement.getLogicalAddress(), "logicalAddress");
        notnull(engagement.getBusinessObjectInstanceIdentifier(), "businessObjectInstanceIdentifier");
        notnull(engagement.getClinicalProcessInterestId(), "clinicalProcessInterestId");
    }

    //
    private void notnull(final Object o, final String s) {
        if (o == null) {
            throw new IllegalArgumentException("Value for element \"" + s + "\" must be set (not null)");
        }
    }
}

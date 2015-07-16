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

import riv.itintegration.engagementindex._1.EngagementTransactionType;
import riv.itintegration.engagementindex._1.EngagementType;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;
import riv.itintegration.engagementindex.updateresponder._1.UpdateType;

import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Peter on 2014-08-20.
 */
@WebService(serviceName = "UpdateResponderService", 
      endpointInterface = "riv.itintegration.engagementindex.update._1.rivtabp21.UpdateResponderInterface", 
               portName = "UpdateResponderPort", 
        targetNamespace = "urn:riv:itintegration:engagementindex:Update:1:rivtabp21")
public class EiUpdateWS implements UpdateResponderInterface {

    private static final Logger log = LoggerFactory.getLogger(EiUpdateWS.class);
    
    public static final String SUBJECT_OF_CARE_ID_EI_TIMEOUT   = "192202021234";
    public static final String SUBJECT_OF_CARE_ID_EI_EXCEPTION = "193303031234";

    @Override
    public UpdateResponseType update(String logicalAddress, UpdateType parameters) {
        log.info("ei update called.");
        final UpdateResponseType response = new UpdateResponseType();
        
        if (parameters.getEngagementTransaction().size() < 1) {
            throw new RuntimeException("Update called with no engagement transactions");
        }
        
        EngagementTransactionType ett = parameters.getEngagementTransaction().get(0);
        EngagementType et = ett.getEngagement();
        
        if (SUBJECT_OF_CARE_ID_EI_EXCEPTION.equals(et.getRegisteredResidentIdentification())) {
            throw new RuntimeException("Exception occurred in engagementindex");
        }
        
        if (SUBJECT_OF_CARE_ID_EI_TIMEOUT.equals(et.getRegisteredResidentIdentification())) {
            log.debug("engagmentindex pauses 60 seconds to provoke mule timeout");
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                log.debug("engagment index wakens from 60 second delay and continues happily");
            }
        }

        // ignore all other inputs
        
        response.setComment("Engagement index - update was successful");
        response.setResultCode(ResultCodeEnum.OK);

        return response;
    }
}

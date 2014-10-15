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
package se.skl.skltpservices.npoadapter.mapper;

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleMessage;

import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
/**
 * Maps from 
 *  GetEHRExctract (und-kon) 
 * to 
 *  RIV GetReferralOutcomeResponseType v2.0. <p>
 *
 * @author martin flower
 */
@Slf4j
public class RIVReferralOutcomeMapper extends ReferralOutcomeMapper {

	@Override
	public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
		try {
			final GetReferralOutcomeType req = unmarshal(payloadAsXMLStreamReader(message));
			final RIV13606REQUESTEHREXTRACTRequestType ehrRequest = EHRUtil.requestType(req, MEANING_UND);
			final GetEhrExtractType ehrExtractType = XMLBeanMapper.map(ehrRequest);
			message.setPayload(ehrExtractType(ehrExtractType));
            return message;
		} catch (Exception err) {
			log.error("Error when transforming ReferralOutcome request", err);
			throw new MapperException("Error when transforming ReferralOutcome request");
		}
	}
	
	
	@Override
	public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
		try {
			final GetEhrExtractResponseType resp = ehrExtractResponseType(payloadAsXMLStreamReader(message));
			final RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType = XMLBeanMapper.map(resp);
			final GetReferralOutcomeResponseType responseType = map(riv13606REQUESTEHREXTRACTResponseType, message.getUniqueId());
			message.setPayload(marshal(responseType));
            return message;
		} catch (Exception err) {
			log.error("Error when transforming ReferralOutcome response", err);
			throw new MapperException("Error when transforming ReferralOutcome response");
		}
	}
}

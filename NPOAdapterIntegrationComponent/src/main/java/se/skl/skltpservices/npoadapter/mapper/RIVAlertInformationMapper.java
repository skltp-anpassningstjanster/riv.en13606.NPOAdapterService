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

import org.mule.api.MuleMessage;

import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationResponseType;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Maps GetAlertInformationMapper TK from GetEhrExtract
 * 
 * @author torbjorncla
 *
 */
@Slf4j
public class RIVAlertInformationMapper extends AlertInformationMapper {
	
	@Override
	public MuleMessage mapRequest(MuleMessage message) throws MapperException {
		try {
			final GetAlertInformationType req = unmarshal(payloadAsXMLStreamReader(message));
			final RIV13606REQUESTEHREXTRACTRequestType ehrRequest = EHRUtil.requestType(req, MEANING_UPP);
			final GetEhrExtractType ehrExtractType = XMLBeanMapper.map(ehrRequest);
			message.setPayload(ehrExtractType(ehrExtractType));
			return message;
		} catch (Exception err) {
			throw new MapperException("Error when transforming request", err);
		}
	}

	@Override
	public MuleMessage mapResponse(MuleMessage message) throws MapperException {
		try {
			final GetEhrExtractResponseType ehrResp = ehrExtractResponseType(payloadAsXMLStreamReader(message));
			final RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType = XMLBeanMapper.map(ehrResp);
			final GetAlertInformationResponseType responseType = mapResponseType(riv13606REQUESTEHREXTRACTResponseType, message.getUniqueId());
			message.setPayload(marshal(responseType));
			return message;
		} catch (Exception err) {
			throw new MapperException("Error when transforming response", err);
		}
	}
	
}

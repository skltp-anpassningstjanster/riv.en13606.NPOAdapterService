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

import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;

import javax.xml.stream.XMLStreamReader;

public class RIVCareDocumentationMapper extends CareDocumentationMapper {

	@Override
	public String mapRequest(String uniqueId, XMLStreamReader reader) throws MapperException {
		try {
			GetCareDocumentationType req = unmarshal(reader);
			final RIV13606REQUESTEHREXTRACTRequestType ehrRequest = map13606Request(req);
			final GetEhrExtractType ehrExtractType = XMLBeanMapper.map(ehrRequest);
			return ehrExtractType(ehrExtractType);
		} catch (Exception err) {
			throw new MapperException("Exception when mapping request", err);
		}
	}

	@Override
	public String mapResponse(String unqiueId, XMLStreamReader reader) throws MapperException {
		try {
			final GetEhrExtractResponseType resp = ehrExtractResponseType(reader);
	        final RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType = XMLBeanMapper.map(resp);
			final GetCareDocumentationResponseType responseType = mapResponseType(unqiueId, riv13606REQUESTEHREXTRACTResponseType);
			return marshal(responseType);
		} catch (Exception err) {
			throw new MapperException("Exception when mapping response", err);
		}
	}
	
}

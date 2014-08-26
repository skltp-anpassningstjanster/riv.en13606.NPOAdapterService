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

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MuleMessage;
import riv.clinicalprocess.healthcond.description._2.CVType;
import riv.clinicalprocess.healthcond.description._2.DiagnosisBodyType;
import riv.clinicalprocess.healthcond.description._2.DiagnosisType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description.enums._2.DiagnosisTypeEnum;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisResponseType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.ObjectFactory;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.HealthcondDescriptionUtil;

import se.rivta.en13606.ehrextract.v11.*;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps from EHR_EXTRACT (dia v2.1) to RIV GetDiagnosisResponseType v2.0. <p>
 *
 * Riv contract spec (TKB): "http://rivta.se/downloads/ServiceContracts_clinicalprocess_healthcond_description_2.1_RC3.zip"
 * 
 * @author torbjorncla
 *
 */
@Slf4j
public class DiagnosisMapper extends AbstractMapper implements Mapper {
	
	private static final JaxbUtil jaxb = new JaxbUtil(GetDiagnosisType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	private static final int MAX_ROWS = 500;
	
	public static final CD MEANING_VOO = new CD();
    static {
        MEANING_VOO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VOO.setCode("dia");
    }
	
	protected static final String TIME_ELEMENT = "dia-dia-tid";
	protected static final String CODE_ELEMENT = "dia-dia-kod";
	protected static final String TYPE_ELEMENT = "dia-dia-typ";

	@Override
	public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
		try {
			final GetDiagnosisType req = unmarshal(payloadAsXMLStreamReader(message));
			message.setPayload(riv13606REQUESTEHREXTRACTRequestType(map13606Request(req)));
            return message;
		} catch (Exception err) {
			log.error("Error when transforming Diagnosis request", err);
			throw new MapperException("Error when transforming Diagnosis request");
		}
	}

	@Override
	public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
		try {
			final RIV13606REQUESTEHREXTRACTResponseType ehrResp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			final GetDiagnosisResponseType resp = mapResponseType(ehrResp, message.getUniqueId());
			message.setPayload(marshal(resp));
            return message;
		} catch (Exception err) {
			log.error("Error when transforming Diagnosis response", err);
			throw new MapperException("Error when transforming Diagnosis response");
		}
	}
	
	/**
	 * Create request type.
	 * @param req
	 * @return
	 */
	protected RIV13606REQUESTEHREXTRACTRequestType map13606Request(final GetDiagnosisType req) {
		final RIV13606REQUESTEHREXTRACTRequestType type = new RIV13606REQUESTEHREXTRACTRequestType();
		type.getMeanings().add(MEANING_VOO);
		type.setMaxRecords(EHRUtil.intType(MAX_ROWS));
		type.setSubjectOfCareId(HealthcondDescriptionUtil.iiType(req.getPatientId()));
		type.setTimePeriod(HealthcondDescriptionUtil.IVLTSType(req.getTimePeriod()));
		return type;
	}
	
	protected String marshal(final GetDiagnosisResponseType response) {
        final JAXBElement<GetDiagnosisResponseType> el = objFactory.createGetDiagnosisResponse(response);
        return jaxb.marshal(el);
    }
	
	protected GetDiagnosisType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetDiagnosisType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }
	
	/**
	 * Create response.
	 * Collects organisation and healthcare-professional into maps with HSAId as key.
	 * So other functions dont need to itterat over document each time.
	 * @param ehrResp response to be loaded into soap-payload.
	 * @param uniqueId mule-message uniqueId.
	 * @return
	 */
	protected GetDiagnosisResponseType mapResponseType(RIV13606REQUESTEHREXTRACTResponseType ehrResp, final String uniqueId) {
		final GetDiagnosisResponseType resp = new GetDiagnosisResponseType();
		resp.setResult(EHRUtil.mapResultType(uniqueId, ehrResp.getResponseDetail()));
		if(ehrResp.getEhrExtract().isEmpty()) {
			return resp;
		}
		
		final Map<String, ORGANISATION> orgs = new HashMap<String, ORGANISATION>();
		final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps = new HashMap<String, IDENTIFIEDHEALTHCAREPROFESSIONAL>();
		
		final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
		
		for(IDENTIFIEDENTITY entity : ehrExtract.getDemographicExtract()) {
			if(entity instanceof ORGANISATION) {
				final ORGANISATION org = (ORGANISATION) entity;
				if(org.getExtractId() != null) {
					orgs.put(org.getExtractId().getExtension(), org);
				}
			}
			if(entity instanceof IDENTIFIEDHEALTHCAREPROFESSIONAL) {
				final IDENTIFIEDHEALTHCAREPROFESSIONAL hp = (IDENTIFIEDHEALTHCAREPROFESSIONAL) entity;
				if(hp.getExtractId() != null) {
					hps.put(hp.getExtractId().getExtension(), hp);
				}
			}
		}
		
		final String systemHSAId = EHRUtil.getSystemHSAId(ehrExtract);
		final PersonIdType subjectOfCare = HealthcondDescriptionUtil.mapPersonIdType(ehrExtract.getSubjectOfCare());
		
		for(COMPOSITION comp : ehrResp.getEhrExtract().get(0).getAllCompositions()) {
			final DiagnosisType type = new DiagnosisType();
			type.setDiagnosisHeader(HealthcondDescriptionUtil.mapHeaderType(comp, systemHSAId, subjectOfCare, orgs, hps, TIME_ELEMENT));
			type.setDiagnosisBody(mapDiagnosisBodyType(comp));
			resp.getDiagnosis().add(type);
		}
		
		return resp;
	}
	

	/**
	 * Creates message body.
	 * @param comp returns new bodyType for ehr composition.
	 * @return
	 */
	protected DiagnosisBodyType mapDiagnosisBodyType(final COMPOSITION comp) {
		final DiagnosisBodyType type = new DiagnosisBodyType();
		
		for(CONTENT content : comp.getContent()) {
  			if(content instanceof ENTRY) {
  				ENTRY e = (ENTRY) content;
  				for(ITEM item : e.getItems()) {
  					if(item instanceof ELEMENT) {
  						ELEMENT elm = (ELEMENT) item;
  						if(elm.getValue() != null && elm.getMeaning() != null && elm.getMeaning().getCode() != null) {
  							switch(elm.getMeaning().getCode()) {
  							case TIME_ELEMENT:
  								if(elm.getValue() instanceof TS) {
  									type.setDiagnosisTime(((TS)elm.getValue()).getValue());
  								}
  								break;
  							case TYPE_ELEMENT:
  								if(elm.getValue() instanceof ST) {
  									final ST simpleText = (ST) elm.getValue();
  									type.setTypeOfDiagnosis(interpret(simpleText.getValue()));
  								}
  								//TODO: Verify
  								if(type.getTypeOfDiagnosis() != null) {
  									type.setChronicDiagnosis(type.getTypeOfDiagnosis() == DiagnosisTypeEnum.HUVUDDIAGNOS);
  								}
  								break;
  							case CODE_ELEMENT:
  								if(elm.getValue() instanceof CD) {
  									type.setDiagnosisCode(mapCVType((CD) elm.getValue()));
  								}
  								break;
  							}
  						}
  					}
  				}
  			}
  		}
		
		//TODO: Not used? verify.
		//type.getRelatedDiagnosis()
		return type;
	}
	
	/**
	 * Mapps enum between different domains.
	 * @param diagnosisType
	 * @return
	 */
	protected DiagnosisTypeEnum interpret(final String diagnosisType) {
		try {
			return DiagnosisTypeEnum.fromValue(diagnosisType);
		} catch (Exception err) {
			log.warn(String.format("Could not map DiagnosisType of value: %s", diagnosisType));
			return null;
		}
	}
	
	/**
	 * Helper to map CVType from EHR CD type.
	 * @param codeType
	 * @return
	 */
	protected CVType mapCVType(final CD codeType) {
		final CVType cv = new CVType();
		cv.setCode(codeType.getCode());
		cv.setCodeSystem(codeType.getCodeSystem());
		if(codeType.getDisplayName() != null) {
			cv.setDisplayName(codeType.getDisplayName().getValue());
		}
		return cv;
	}
	
	


}

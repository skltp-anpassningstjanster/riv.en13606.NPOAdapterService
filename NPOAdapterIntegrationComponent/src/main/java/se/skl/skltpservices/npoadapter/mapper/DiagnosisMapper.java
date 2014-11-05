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

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.description._2.*;
import riv.clinicalprocess.healthcond.description.enums._2.DiagnosisTypeEnum;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisResponseType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

/**
 * Maps from EHR_EXTRACT (dia v1.1) to RIV GetDiagnosisResponseType v2.0. <p>
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

	public static final CD MEANING_DIA = new CD();
    static {
        MEANING_DIA.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_DIA.setCode(INFO_DIA);
    }
	
	protected static final String TIME_ELEMENT = "dia-dia-tid";
	protected static final String CODE_ELEMENT = "dia-dia-kod";
	protected static final String TYPE_ELEMENT = "dia-dia-typ";
	protected static final String CHRONIC_DIAGNOSIS = "Kronisk diagnos";

	@Override
	public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
		try {
			final GetDiagnosisType req = unmarshal(payloadAsXMLStreamReader(message));
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(req, MEANING_DIA)));
			return message;
		} catch (Exception err) {
            throw new MapperException("Exception when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
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
            throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
		}
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
	 * @return a diagnosis response.
	 */
	protected GetDiagnosisResponseType mapResponseType(RIV13606REQUESTEHREXTRACTResponseType ehrResp, final String uniqueId) {
		final GetDiagnosisResponseType resp = new GetDiagnosisResponseType();
		resp.setResult(EHRUtil.resultType(uniqueId, ehrResp.getResponseDetail(), ResultType.class));
		if(ehrResp.getEhrExtract().isEmpty()) {
			return resp;
		}
				
		final EHREXTRACT ehrExctract = ehrResp.getEhrExtract().get(0);
		final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExctract);
		
		for(COMPOSITION comp : ehrExctract.getAllCompositions()) {
			final DiagnosisType type = new DiagnosisType();
			type.setDiagnosisHeader(EHRUtil.patientSummaryHeader(comp, sharedHeaderExtract, TIME_ELEMENT, PatientSummaryHeaderType.class));
			type.getDiagnosisHeader().setCareContactId(getCareContactId(comp));
			type.setDiagnosisBody(mapDiagnosisBodyType(comp));
			resp.getDiagnosis().add(type);
		}
		
		return resp;
	}
	
	/**
	 * Diagnosis specific method because of careContactId location.
	 * @param comp
	 * @return careContactId.
	 */
	protected String getCareContactId(final COMPOSITION comp) {
		for(CONTENT content : comp.getContent()) {
			if(content instanceof ENTRY) {
				final ENTRY entry = (ENTRY) content;
				return EHRUtil.careContactId(entry.getLinks());
			}
		}
		return null;
	}
	

	//
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
  									if(StringUtils.equalsIgnoreCase(simpleText.getValue(), CHRONIC_DIAGNOSIS)) {
  										type.setTypeOfDiagnosis(DiagnosisTypeEnum.HUVUDDIAGNOS);
  										type.setChronicDiagnosis(true);
  									} else {
  										type.setTypeOfDiagnosis(interpret(simpleText.getValue()));
  									}
  								}
  								break;
  							case CODE_ELEMENT:
  								if(elm.getValue() instanceof CD) {
  									type.setDiagnosisCode(EHRUtil.cvType((CD) elm.getValue(), CVType.class));
  								}
  								break;
  							}
  						}
  					}
  				}
  			}
  		}
		return type;
	}
	
	/**
	 * Mapps enum between different domains.
	 * @param diagnosisType
	 * @return the actual enum, or null if none matches.
	 */
	protected DiagnosisTypeEnum interpret(final String diagnosisType) {
		try {
			return DiagnosisTypeEnum.fromValue(diagnosisType);
		} catch (Exception err) {
			log.warn(String.format("Could not map DiagnosisType of value: %s", diagnosisType));
			return null;
		}
	}
}

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.description._2.*;
import riv.clinicalprocess.healthcond.description.enums._2.ClinicalDocumentNoteCodeEnum;
import riv.clinicalprocess.healthcond.description.enums._2.ClinicalDocumentTypeCodeEnum;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;


/**
 * Maps from EHR_EXTRACT (voo v1.1) to RIV GetCareDocumentationResponseType<p>
 *
 * @author torbjorncla
 */
public class CareDocumentationMapper extends AbstractMapper implements Mapper {
	
	private static final Logger log = LoggerFactory.getLogger(CareDocumentationMapper.class);
	
	private static JaxbUtil jaxb = new JaxbUtil(GetCareDocumentationType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	
	private static final String TIME_ELEMENT = "voo-voo-tid";
	private static final String TEXT_ELEMENT = "voo-voo-txt";

	public static final CD MEANING_VOO = new CD();
    static {
        MEANING_VOO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VOO.setCode(INFO_VOO);
    }
    
	
	@Override
	public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
		log.debug("Transforming request");
		try {
			final GetCareDocumentationType req = unmarshal(payloadAsXMLStreamReader(message));
			message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(req, MEANING_VOO, message.getUniqueId(), message.getInvocationProperty("route-logical-address"))));
            return message;
		} catch (Exception err) {
			throw new MapperException("Exception when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
		}
	}

	@Override
	public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
		log.debug("Transforming response - start");
		try {
			final RIV13606REQUESTEHREXTRACTResponseType resp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			final GetCareDocumentationResponseType responseType = mapResponseType(message.getUniqueId(), resp);
			message.setPayload(marshal(responseType));
	        log.debug("Transformed response - end");
            return message;
		} catch (Exception err) {
			throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
		}
	}
		
	protected GetCareDocumentationType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetCareDocumentationType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }
	
	/**
	 * Maps EHREXTRACT from RIV13606REQUESTEHREXTRACT to GetCareDocumentationResponse <p/>
     *
     * @param unqiueId the unique message correlation id.
	 * @param ehrResp subset from RIV136060REQUESTEHREXTRACT.
	 * @return GetCareDocumentationType for marshaling.
	 */
	protected GetCareDocumentationResponseType mapResponseType(final String unqiueId, final RIV13606REQUESTEHREXTRACTResponseType ehrResp) {
	    log.debug("Populating GetCareDocumentationResponse using ehrResp (" + ehrResp.getClass().getName() + ")");
		final GetCareDocumentationResponseType getCareDocumentationResponse = new GetCareDocumentationResponseType();
		getCareDocumentationResponse.setResult(EHRUtil.resultType(unqiueId, ehrResp.getResponseDetail(), Result.class));

        if (ehrResp.getEhrExtract().isEmpty()) {
			return getCareDocumentationResponse;
		}
		
		final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
		final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
		
		for (COMPOSITION comp : ehrExtract.getAllCompositions()) {
			final CareDocumentationType doc = new CareDocumentationType();
			doc.setCareDocumentationHeader(EHRUtil.patientSummaryHeader(comp, sharedHeaderExtract, TIME_ELEMENT, CPatientSummaryHeaderType.class, true, true));
			doc.getCareDocumentationHeader().setCareContactId(EHRUtil.careContactId(comp.getLinks()));
            doc.getCareDocumentationHeader().setSourceSystemHSAid(EHRUtil.getSystemHSAId(ehrExtract));
			doc.setCareDocumentationBody(mapBodyType(comp));
			getCareDocumentationResponse.getCareDocumentation().add(doc);
		}		
        log.debug("Finished populating GetCareDocumentationResponse");
		return getCareDocumentationResponse;
	}
	
	protected CareDocumentationBodyType mapBodyType(final COMPOSITION comp) {
		final CareDocumentationBodyType type = new CareDocumentationBodyType();
		final ClinicalDocumentNoteType note = new ClinicalDocumentNoteType();
		type.setClinicalDocumentNote(note);

		if(comp.getMeaning() != null) {
			final String code = comp.getMeaning().getCode();
			if(isDocumentNoteCodeEnum(code)) {
				note.setClinicalDocumentNoteCode(ClinicalDocumentNoteCodeEnum.fromValue(code));
			} else if(isDocumentTypeCodeEnum(code)) {
				note.setClinicalDocumentTypeCode(ClinicalDocumentTypeCodeEnum.fromValue(code));
			} else {
				log.warn("Not able to map documentcode of value: " + code);
			}
		}
		
		//Only txt is supported.
		final ELEMENT txt = EHRUtil.findEntryElement(comp.getContent(), TEXT_ELEMENT);
		if(txt != null) {
			note.setClinicalDocumentNoteText(EHRUtil.getElementTextValue(txt));
			if(txt.getMeaning() != null && txt.getMeaning().getDisplayName() != null) {				
				note.setClinicalDocumentNoteTitle(txt.getMeaning().getDisplayName().getValue());
			}
		}
		return type;
	}
	
			
	protected String marshal(final GetCareDocumentationResponseType response) {
        final JAXBElement<GetCareDocumentationResponseType> el = objFactory.createGetCareDocumentationResponse(response);
        return jaxb.marshal(el);
    }
			
	protected boolean isDocumentTypeCodeEnum(final String code) {
		try {
			ClinicalDocumentTypeCodeEnum.fromValue(code);
			return true;
		} catch (IllegalArgumentException err) {
			return false;
		}
	}
	
	protected boolean isDocumentNoteCodeEnum(final String code) {
		try {
			ClinicalDocumentNoteCodeEnum.fromValue(code);
			return true;
		} catch (IllegalArgumentException err) {
			return false;
		}
	}
}

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
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.description._2.*;
import riv.clinicalprocess.healthcond.description.enums._2.ClinicalDocumentNoteCodeEnum;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.HealthcondDescriptionUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps from EHR_EXTRACT (voo v2.1) to RIV GetCareDocumentationResponseType v2.0. <p>
 *
 * Riv contract spec (TKB): "http://rivta.se/downloads/ServiceContracts_clinicalprocess_healthcond_description_2.1_RC3.zip"
 * 
 * @author torbjorncla
 *
 */
@Slf4j
public class CareDocumentationMapper extends AbstractMapper implements Mapper {
	
	private static JaxbUtil jaxb = new JaxbUtil(GetCareDocumentationType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	
	private static final String UNKNOWN_VALUE = "-- UNKOWN_VALUE -- ";
	
	private static final String TIME_ELEMENT = "voo-voo-tid";
	private static final String TEXT_ELEMENT = "voo-voo-txt";
	
	private static final int MAX_ROWS = 500;

	public static final CD MEANING_VOO = new CD();
    static {
        MEANING_VOO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VOO.setCode("voo");
    }
    
	
	@Override
	public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
		log.debug("Transforming Request");
		try {
			final GetCareDocumentationType req = unmarshal(payloadAsXMLStreamReader(message));
			message.setPayload(riv13606REQUESTEHREXTRACTRequestType(map13606Request(req)));
            return message;
		} catch (Exception err) {
			throw new MapperException("Exception when mapping request", err);
		}
	}

	@Override
	public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
		log.debug("Transforming Response");
		try {
			final RIV13606REQUESTEHREXTRACTResponseType resp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			final GetCareDocumentationResponseType responseType = mapResponseType(message.getUniqueId(), resp);
			message.setPayload(marshal(responseType));
            return message;
		} catch (Exception err) {
			throw new MapperException("Exception when mapping response", err);
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
	 * Maps EHREXTRACT from RIV13606REQUESTEHREXTRACT to GetCareDocumentation
	 * @param riv subset from RIV136060REQUESTEHREXTRACT.
	 * @return GetCareDocumentationType for marshaling.
	 */
	protected GetCareDocumentationResponseType mapResponseType(final String unqiueId, final RIV13606REQUESTEHREXTRACTResponseType riv) {
		final GetCareDocumentationResponseType resp = new GetCareDocumentationResponseType();
		resp.setResult(HealthcondDescriptionUtil.mapResultType(unqiueId, riv.getResponseDetail()));
		if(riv.getEhrExtract().isEmpty()) {
			return resp;
		}
		final EHREXTRACT ehrExtract = riv.getEhrExtract().get(0);
		
		final PersonIdType person = HealthcondDescriptionUtil.mapPersonIdType(ehrExtract.getSubjectOfCare());
		
		final Map<String, ORGANISATION> orgs = new HashMap<String, ORGANISATION>();
		final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps = new HashMap<String, IDENTIFIEDHEALTHCAREPROFESSIONAL>();
		
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
		
		final String systemHSAid = EHRUtil.getSystemHSAId(ehrExtract);
		for(COMPOSITION comp : ehrExtract.getAllCompositions()) {
			final CareDocumentationType doc = new CareDocumentationType();
			doc.setCareDocumentationHeader(HealthcondDescriptionUtil.mapHeaderType(comp, systemHSAid, person, orgs, hps, TIME_ELEMENT));
			doc.setCareDocumentationBody(mapBodyType(comp));
			resp.getCareDocumentation().add(doc);			
		}		
		return resp;
	}
	
		
	protected CareDocumentationBodyType mapBodyType(final COMPOSITION comp) {
		final CareDocumentationBodyType type = new CareDocumentationBodyType();
		final ClinicalDocumentNoteType note = new ClinicalDocumentNoteType();
		type.setClinicalDocumentNote(note);
		//TODO:
		//Are there other supported types?
		final ELEMENT txt = EHRUtil.findEntryElement(comp.getContent(), TEXT_ELEMENT);
		if(txt != null) {
			//TODO: Mapping between old and new EHR codes
			note.setClinicalDocumentNoteCode(ClinicalDocumentNoteCodeEnum.UTR);
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
			
	protected RIV13606REQUESTEHREXTRACTRequestType map13606Request(final GetCareDocumentationType req) {
		final RIV13606REQUESTEHREXTRACTRequestType type = new RIV13606REQUESTEHREXTRACTRequestType();
		type.getMeanings().add(MEANING_VOO);
		type.setMaxRecords(EHRUtil.intType(MAX_ROWS));
		type.setSubjectOfCareId(HealthcondDescriptionUtil.iiType(req.getPatientId()));
		type.setTimePeriod(HealthcondDescriptionUtil.IVLTSType(req.getTimePeriod()));
		return type;
	}
}

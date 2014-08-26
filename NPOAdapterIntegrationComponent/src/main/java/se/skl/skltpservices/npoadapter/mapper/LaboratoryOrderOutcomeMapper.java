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

import riv.clinicalprocess.healthcond.actoutcome._3.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.actoutcome._3.LaboratoryOrderOutcomeBodyType;
import riv.clinicalprocess.healthcond.actoutcome._3.LaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome._3.OrderType;
import riv.clinicalprocess.healthcond.actoutcome._3.PersonIdType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDHEALTHCAREPROFESSIONAL;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.error.NotImplementedException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.HealthcondActOutcomeUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps from EHR_EXTRACT (und-kkm-kli v2.1) to RIV GetLaboratoryOrderOutcomeResponseType v3.0. <p>
 *
 * Riv contract spec (TKB): "http://rivta.se/downloads/ServiceContracts_clinicalprocess_healthcond_actoutcome_3.0_RC1.zip"
 * 
 * @author torbjorncla
 *
 */
@Slf4j
public class LaboratoryOrderOutcomeMapper extends AbstractMapper implements Mapper {
	
	private static final JaxbUtil jaxb = new JaxbUtil(GetLaboratoryOrderOutcomeType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	private static final int MAX_ROWS = 500;

	@Override
	public MuleMessage mapRequest(final MuleMessage message)
			throws MapperException {
		throw new NotImplementedException("Adapter not implemented");
	}

	@Override
	public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
		try {
			final RIV13606REQUESTEHREXTRACTResponseType ehrResponse = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			GetLaboratoryOrderOutcomeResponseType resp = mapResponseType(ehrResponse, message.getUniqueId());
			message.setPayload(marshal(resp));
			return message;
		} catch (Exception err) {
			log.error("Error when transforming LaboratoryOrderOutcome response", err);
			throw new MapperException("Error when transforming LaboratoryOrderOutcome response");
		}
	}
	
	protected GetLaboratoryOrderOutcomeResponseType mapResponseType(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, final String uniqueId) {
		final GetLaboratoryOrderOutcomeResponseType resp = new GetLaboratoryOrderOutcomeResponseType();
		resp.setResult(HealthcondActOutcomeUtil.mapResultType(uniqueId, ehrResp.getResponseDetail()));
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
		final PersonIdType subjectOfCare = HealthcondActOutcomeUtil.mapPersonIdType(ehrExtract.getSubjectOfCare());
		
		for(COMPOSITION comp : ehrResp.getEhrExtract().get(0).getAllCompositions()) {
			final LaboratoryOrderOutcomeType type = new LaboratoryOrderOutcomeType();
			type.setLaboratoryOrderOutcomeHeader(HealthcondActOutcomeUtil.mapHeaderType(comp, systemHSAId, subjectOfCare, orgs, hps));
			IDENTIFIEDHEALTHCAREPROFESSIONAL healthcareProfessional = null;
			if(type.getLaboratoryOrderOutcomeHeader().getAccountableHealthcareProfessional() != null) {
				final String professionalHSAId = type.getLaboratoryOrderOutcomeHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareGiverHSAId();
				if(professionalHSAId != null && hps.containsKey(professionalHSAId)) {
					healthcareProfessional = hps.get(professionalHSAId);
				}
			}
			type.setLaboratoryOrderOutcomeBody(mapBodyType(comp, healthcareProfessional));
			resp.getLaboratoryOrderOutcome().add(type);
		}
		return resp;
	}
	
	/**
	 * TODO: Refactor, too complex.
	 * @param comp
	 * @return
	 */
	protected LaboratoryOrderOutcomeBodyType mapBodyType(final COMPOSITION comp, final IDENTIFIEDHEALTHCAREPROFESSIONAL healthcareProfessional) {
		final LaboratoryOrderOutcomeBodyType type = new LaboratoryOrderOutcomeBodyType();
		if(comp.getCommittal() != null && comp.getCommittal().getTimeCommitted() != null) {
			type.setRegistrationTime(comp.getCommittal().getTimeCommitted().getValue());
		}
		for(CONTENT content : comp.getContent()) {
			if(content instanceof ENTRY) {
				final ENTRY entry = (ENTRY) content;
				for(ITEM item : entry.getItems()) {
					if(item instanceof CLUSTER) {
						CLUSTER cluster = (CLUSTER) item;
						for(ITEM part : cluster.getParts()) {
							if(part instanceof ELEMENT) {
								final ELEMENT elm = (ELEMENT) part;
								if(part.getMeaning() != null) {
									switch(part.getMeaning().getCode()) {
										case "und-und-ure-typ":
											type.setResultType(EHRUtil.getElementTextValue(elm));
											break;
										case "und-kkm-ure-lab":
											type.setDiscipline(EHRUtil.getElementTextValue(elm));
											break;
										case "und-und-ure-utl":
											type.setResultReport(EHRUtil.getElementTextValue(elm));
											break;
										case "und-kkm-ure-kom":
											type.setResultComment(EHRUtil.getElementTextValue(elm));
											break;
									}
								}
							}
						}
					}
				}
			}
		}
		
		type.setAccountableHeathcareProfessional(mapAccountableHealthcareProfessional(healthcareProfessional));
		type.setOrder(mapOrder());
		return type;
	}
	
	protected HealthcareProfessionalType mapAccountableHealthcareProfessional(final IDENTIFIEDHEALTHCAREPROFESSIONAL healthcareProfessional) {
		final HealthcareProfessionalType type = new HealthcareProfessionalType();
		type.setHealthcareProfessionalCareGiverHSAId("TRAFF");
		return type;
	}

	protected OrderType mapOrder() {
		final OrderType type = new OrderType();
		return type;
	}
	
	protected String marshal(final GetLaboratoryOrderOutcomeResponseType response) {
        final JAXBElement<GetLaboratoryOrderOutcomeResponseType> el = objFactory.createGetLaboratoryOrderOutcomeResponse(response);
        return jaxb.marshal(el);
    }

}

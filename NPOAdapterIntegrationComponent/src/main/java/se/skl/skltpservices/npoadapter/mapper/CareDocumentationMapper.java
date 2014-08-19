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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.description._2.CVType;
import riv.clinicalprocess.healthcond.description._2.CareDocumentationBodyType;
import riv.clinicalprocess.healthcond.description._2.CareDocumentationType;
import riv.clinicalprocess.healthcond.description._2.ClinicalDocumentNoteType;
import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.description._2.LegalAuthenticatorType;
import riv.clinicalprocess.healthcond.description._2.OrgUnitType;
import riv.clinicalprocess.healthcond.description._2.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description._2.ResultType;
import riv.clinicalprocess.healthcond.description.enums._2.ClinicalDocumentNoteCodeEnum;
import riv.clinicalprocess.healthcond.description.enums._2.ResultCodeEnum;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.ObjectFactory;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import se.rivta.en13606.ehrextract.v11.AUDITINFO;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.FUNCTIONALROLE;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDHEALTHCAREPROFESSIONAL;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailTypeCodes;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TEL;
import se.rivta.en13606.ehrextract.v11.TELEMAIL;
import se.rivta.en13606.ehrextract.v11.TELPHONE;
import se.rivta.en13606.ehrextract.v11.TS;

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
    
    
    //For Test purpose
    protected void setJaxbUtil(JaxbUtil jaxb) {
    	this.jaxb = jaxb;
    	EHRUtil util = new EHRUtil();
    }
	
	@Override
	public String mapRequest(XMLStreamReader reader) {
		log.debug("Transforming Request");
		GetCareDocumentationType req = unmarshal(reader);
		return riv13606REQUESTEHREXTRACTRequestType(map13606Request(req));
	}

	@Override
	public String mapResponse(XMLStreamReader reader) {
		log.debug("Transforming Response");
		final RIV13606REQUESTEHREXTRACTResponseType resp = riv13606REQUESTEHREXTRACTResponseType(reader);
        final GetCareDocumentationResponseType responseType = mapResponseType(resp);
        if (!resp.getResponseDetail().isEmpty()) {
            responseType.setResult(mapResultType(resp.getResponseDetail()));
        }
		return marshal(responseType);
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
	protected GetCareDocumentationResponseType mapResponseType(final RIV13606REQUESTEHREXTRACTResponseType riv) {
		final GetCareDocumentationResponseType resp = new GetCareDocumentationResponseType();
		if(riv.getEhrExtract().isEmpty()) {
			return resp;
		}
		final EHREXTRACT ehrExtract = riv.getEhrExtract().get(0);
		String systemHsaId = null;
		if(ehrExtract.getEhrSystem() != null) {
			systemHsaId = ehrExtract.getEhrSystem().getExtension();
		}
		final PersonIdType person = mapPersonIdType(ehrExtract.getSubjectOfCare());
		
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
		
		for(COMPOSITION comp : ehrExtract.getAllCompositions()) {
			final CareDocumentationType doc = new CareDocumentationType();
			doc.setCareDocumentationHeader(mapHeaderType(comp, systemHsaId, person, orgs, hps));
			doc.setCareDocumentationBody(mapBodyType(comp));
			resp.getCareDocumentation().add(doc);			
		}		
		return resp;
	}
	
	protected ResultType mapResultType(final List<ResponseDetailType> respDetails) {
		if(respDetails.isEmpty() && respDetails.get(0).getTypeCode() != null) {
			return null;
		}
		final ResponseDetailType resp = respDetails.get(0);
		final ResultType resultType = new ResultType();
		if(resp.getText() != null) {
			resultType.setMessage(resp.getText().getValue());
		}
		//TODO: Add tracablitiy to UUID
		resultType.setLogId(UUID.randomUUID().toString());
		resultType.setResultCode(interpret(resp.getTypeCode()));
		return resultType;
	}
	
	protected ResultCodeEnum interpret(final ResponseDetailTypeCodes code) {
		switch(code) {
		case E:
		case W:
			return ResultCodeEnum.ERROR;
		case I:
			return ResultCodeEnum.INFO;
		default:
			return ResultCodeEnum.OK;
		}
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
	
	protected PatientSummaryHeaderType mapHeaderType(final COMPOSITION comp, final String systemHsaId, 
				final PersonIdType person, final Map<String, ORGANISATION> orgs, final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps) {
		final PatientSummaryHeaderType header = new PatientSummaryHeaderType();
		if(comp.getRcId() != null) {
			header.setDocumentId(comp.getRcId().getExtension());
		}
		header.setSourceSystemHSAId(systemHsaId);
		if(comp.getName() != null) {
			header.setDocumentTitle(comp.getName().getValue());
		}
		//Which time is to be used? time_created on root-level or time on attestations-level
		final ELEMENT time = EHRUtil.findEntryElement(comp.getContent(), TIME_ELEMENT);
		if(time != null && time.getValue() instanceof TS) {
			header.setDocumentTime(((TS)time.getValue()).getValue());
		}
		header.setPatientId(person);
		header.setAccountableHealthcareProfessional(mapHealtcareProfessionalType(comp.getComposer(), orgs, hps, comp.getCommittal()));
		final LegalAuthenticatorType legal = new LegalAuthenticatorType();
		//Only author time exists.
		legal.setLegalAuthenticatorHSAId(UNKNOWN_VALUE);
		legal.setLegalAuthenticatorName(UNKNOWN_VALUE);
		if(header.getAccountableHealthcareProfessional() != null) {
			legal.setSignatureTime(header.getAccountableHealthcareProfessional().getAuthorTime());
		}
		header.setLegalAuthenticator(legal);
		if(!comp.getLinks().isEmpty() && !comp.getLinks().get(0).getTargetId().isEmpty()) {
			header.setCareContactId(comp.getLinks().get(0).getTargetId().get(0).getExtension());
		}
		return header;
	}
	
	//author??
	protected HealthcareProfessionalType mapHealtcareProfessionalType(final FUNCTIONALROLE composer, 
			final Map<String, ORGANISATION> orgs, final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps, final AUDITINFO committal) {
		final HealthcareProfessionalType type = new HealthcareProfessionalType();
		String organisationKey = null;
		String performerKey = null;
		if(composer.getHealthcareFacility() != null) {
			organisationKey = composer.getHealthcareFacility().getExtension();
		}
		if(composer.getPerformer() != null) {
			performerKey = composer.getPerformer().getExtension();
		}
		//TODO: Fix
		type.setHealthcareProfessionalHSAId(performerKey);
		if(organisationKey != null && orgs.containsKey(organisationKey)) {
			final ORGANISATION org = orgs.get(organisationKey);
			type.setHealthcareProfessionalCareUnitHSAId(org.getExtractId().getExtension());
			final OrgUnitType orgUnitType = new OrgUnitType();
			if(!org.getAddr().isEmpty() && !org.getAddr().get(0).getPartOrBrOrAddressLine().isEmpty()) {
				if(org.getName() != null) {
					orgUnitType.setOrgUnitName(org.getName().getValue());
				}
				orgUnitType.setOrgUnitAddress(org.getAddr().get(0).getPartOrBrOrAddressLine().get(0).getContent());
				for(TEL t : org.getTelecom()) {
					if(t instanceof TELEMAIL) {
						orgUnitType.setOrgUnitEmail(((TELEMAIL)t).getValue());
					}
					if(t instanceof TELPHONE) {
						orgUnitType.setOrgUnitTelecom(((TELPHONE)t).getValue());
					}
				}
				orgUnitType.setOrgUnitHSAId(organisationKey);
			}
			type.setHealthcareProfessionalOrgUnit(orgUnitType);
		}
		if(performerKey != null && hps.containsKey(performerKey)) {
			final IDENTIFIEDHEALTHCAREPROFESSIONAL hp = hps.get(performerKey);
			if(committal != null && committal.getTimeCommitted() != null) {
				type.setAuthorTime(committal.getTimeCommitted().getValue());
			}
			type.setHealthcareProfessionalCareGiverHSAId(hp.getExtractId().getExtension());
			if(!hp.getName().isEmpty() && !hp.getName().get(0).getPart().isEmpty()) {
				type.setHealthcareProfessionalName(hp.getName().get(0).getPart().get(0).getValue());
			}
			final CVType cv = new CVType();
			if(!hp.getRole().isEmpty()) {
				if(hp.getRole().get(0).getProfession() != null) {
					final CD cd = hp.getRole().get(0).getProfession();
					cv.setCode(cd.getCode());
					cv.setCodeSystem(cd.getCodeSystem());
				}
			}
			type.setHealthcareProfessionalRoleCode(cv);
		}
		return type;
	}
			
	protected PersonIdType mapPersonIdType(final II elm) {
		final PersonIdType person = new PersonIdType();
		if(elm != null) {
			person.setId(elm.getExtension());
			person.setType(elm.getRoot());
		}
		return person;
	}
	
	protected String marshal(final GetCareDocumentationResponseType response) {
        final JAXBElement<GetCareDocumentationResponseType> el = objFactory.createGetCareDocumentationResponse(response);
        return jaxb.marshal(el);
    }
			
	protected RIV13606REQUESTEHREXTRACTRequestType map13606Request(final GetCareDocumentationType req) {
		final RIV13606REQUESTEHREXTRACTRequestType type = new RIV13606REQUESTEHREXTRACTRequestType();
		type.getMeanings().add(MEANING_VOO);
		type.setMaxRecords(EHRUtil.intType(MAX_ROWS));
		type.setSubjectOfCareId(iiType(req.getPatientId()));
		type.setTimePeriod(IVLTSType(req.getTimePeriod()));
		return type;
	}
	
	protected II iiType(final PersonIdType idType) {
		II ii = new II();
		if(idType != null) {
			ii.setRoot(idType.getType());
			ii.setExtension(idType.getId());
		}
		return ii;
	}
	
	protected IVLTS IVLTSType(final DatePeriodType datePeriod) {
		final IVLTS ivlts = new IVLTS();
		if(datePeriod != null) {
			ivlts.setLow(EHRUtil.tsType(datePeriod.getStart()));
			ivlts.setHigh(EHRUtil.tsType(datePeriod.getEnd()));
		}
		return ivlts;
	}
			
}

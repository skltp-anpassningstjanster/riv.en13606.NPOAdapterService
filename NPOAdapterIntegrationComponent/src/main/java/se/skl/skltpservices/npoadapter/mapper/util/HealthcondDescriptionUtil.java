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
package se.skl.skltpservices.npoadapter.mapper.util;

import java.util.List;
import java.util.Map;

import org.mule.util.StringUtils;

import riv.clinicalprocess.healthcond.description._2.CVType;
import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.description._2.LegalAuthenticatorType;
import riv.clinicalprocess.healthcond.description._2.OrgUnitType;
import riv.clinicalprocess.healthcond.description._2.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description._2.ResultType;
import riv.clinicalprocess.healthcond.description.enums._2.ResultCodeEnum;
import se.rivta.en13606.ehrextract.v11.AUDITINFO;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.FUNCTIONALROLE;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDHEALTHCAREPROFESSIONAL;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.ResponseDetailType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailTypeCodes;
import se.rivta.en13606.ehrextract.v11.TEL;
import se.rivta.en13606.ehrextract.v11.TELEMAIL;
import se.rivta.en13606.ehrextract.v11.TELPHONE;
import se.rivta.en13606.ehrextract.v11.TS;

/**
 * Utility class to create and map elements in the healtcond.description domain.
 * @author torbjorncla
 *
 */
public final class HealthcondDescriptionUtil {
	private HealthcondDescriptionUtil() {
		
	}
	
	public static PatientSummaryHeaderType mapHeaderType(final COMPOSITION comp, final String systemHsaId, 
			final PersonIdType person, final Map<String, ORGANISATION> orgs, final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps, final String TIME_ELEMENT) {
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
		if(header.getAccountableHealthcareProfessional() != null) {
			legal.setSignatureTime(header.getAccountableHealthcareProfessional().getAuthorTime());
		}
		header.setLegalAuthenticator(legal);
		if(!comp.getLinks().isEmpty() && !comp.getLinks().get(0).getTargetId().isEmpty()) {
			header.setCareContactId(comp.getLinks().get(0).getTargetId().get(0).getExtension());
		}
		header.setApprovedForPatient(false);
        header.setNullified(false);
        header.setNullifiedReason(null);
        for(FUNCTIONALROLE careGiver : comp.getOtherParticipations()) {
			if(careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), "iag")) {
				if(careGiver.getPerformer() != null) {
					header.getAccountableHealthcareProfessional().setHealthcareProfessionalCareGiverHSAId(careGiver.getPerformer().getExtension());
				}
				if(careGiver.getHealthcareFacility() != null) {
					header.getAccountableHealthcareProfessional().setHealthcareProfessionalCareUnitHSAId(careGiver.getHealthcareFacility().getExtension());
				}
			}
		}
		return header;
	}
	
	public static HealthcareProfessionalType mapHealtcareProfessionalType(final FUNCTIONALROLE composer, 
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
			//type.setHealthcareProfessionalCareUnitHSAId(org.getExtractId().getExtension());
			final OrgUnitType orgUnitType = new OrgUnitType();
			if(org.getName() != null) {
				orgUnitType.setOrgUnitName(org.getName().getValue());
			}
			for(TEL t : org.getTelecom()) {
				if(t instanceof TELEMAIL) {
					orgUnitType.setOrgUnitEmail(((TELEMAIL)t).getValue());
				}
				if(t instanceof TELPHONE) {
					orgUnitType.setOrgUnitTelecom(((TELPHONE)t).getValue());
				}
			}
			orgUnitType.setOrgUnitHSAId(organisationKey);
			if(!org.getAddr().isEmpty() && !org.getAddr().get(0).getPartOrBrOrAddressLine().isEmpty()) {
				orgUnitType.setOrgUnitAddress(org.getAddr().get(0).getPartOrBrOrAddressLine().get(0).getContent());
			}
			type.setHealthcareProfessionalOrgUnit(orgUnitType);
		}
		if(performerKey != null && hps.containsKey(performerKey)) {
			final IDENTIFIEDHEALTHCAREPROFESSIONAL hp = hps.get(performerKey);
			if(committal != null && committal.getTimeCommitted() != null) {
				type.setAuthorTime(committal.getTimeCommitted().getValue());
			}
			//type.setHealthcareProfessionalCareGiverHSAId(hp.getExtractId().getExtension());
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
	
	public static PersonIdType mapPersonIdType(final II elm) {
		final PersonIdType person = new PersonIdType();
		if(elm != null) {
			person.setId(elm.getExtension());
			person.setType(elm.getRoot());
		}
		return person;
	}
	
	public static II iiType(final PersonIdType idType) {
		II ii = new II();
		if(idType != null) {
			ii.setRoot(idType.getType());
			ii.setExtension(idType.getId());
		}
		return ii;
	}
	
	public static IVLTS IVLTSType(final DatePeriodType datePeriod) {
		final IVLTS ivlts = new IVLTS();
		if(datePeriod != null) {
			ivlts.setLow(EHRUtil.tsType(datePeriod.getStart()));
			ivlts.setHigh(EHRUtil.tsType(datePeriod.getEnd()));
		}
		return ivlts;
	}
	
  	public static ResultType mapResultType(final String uniqueId, final List<ResponseDetailType> respDetails) {
		if(respDetails.isEmpty()) {
			return null;
		}
		final ResponseDetailType resp = respDetails.get(0);
		final ResultType resultType = new ResultType();
		if(resp.getText() != null) {
			resultType.setMessage(resp.getText().getValue());
		}
		resultType.setLogId(uniqueId);
		resultType.setResultCode(interpret(resp.getTypeCode()));
		return resultType;
	}
  	
  	public static ResultCodeEnum interpret(final ResponseDetailTypeCodes code) {
		try {
			switch(code) {
			case E:
			case W:
				return ResultCodeEnum.ERROR;
			case I:
				return ResultCodeEnum.INFO;
			default:
				return ResultCodeEnum.OK;
			}
		} catch (Exception err) {
			return null;
		}
	}
}

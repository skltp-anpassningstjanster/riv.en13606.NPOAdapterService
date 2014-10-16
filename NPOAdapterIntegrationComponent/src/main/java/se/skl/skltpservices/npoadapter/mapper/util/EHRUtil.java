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

import lombok.Data;
import lombok.SneakyThrows;

import org.apache.commons.lang.StringUtils;

import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.AbstractMapper;
import se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper;

import javax.xml.datatype.XMLGregorianCalendar;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Utility class to create and map common EHR types.
 * @author torbjorncla
 *
 */
public final class EHRUtil {

	private static final ParameterType versionParameter = new ParameterType();
	
	static {
		versionParameter.setName(stType("version"));
		versionParameter.setValue(stType("1.1"));
	}
	
    private static ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    //
    public static String formatTimestamp(Date timestamp) {
        return formatter.get().format(timestamp);
    }

    //
    @SneakyThrows
    public static Date parseTimestamp(String timestamp) {
        return formatter.get().parse(timestamp);
    }

    public static String getElementTextValue(final ELEMENT e) {
        if(e != null && e.getValue() instanceof ST) {
            ST text = (ST) e.getValue();
            return text.getValue();
        }
        return null;
    }

    public static String getElementTimeValue(final ELEMENT e) {
        if(e != null && e.getValue() instanceof TS) {
            TS time = (TS) e.getValue();
            return time.getValue();
        }
        return null;
    }
    
    public static ST stType(final String value) {
        if (value == null) {
            return null;
        }
        final ST st = new ST();
        st.setValue(value);
        return st;
    }

    public static TS tsType(final String value) {
        if (value == null) {
            return null;
        }
        final TS ts = new TS();
        ts.setValue(value);
        return ts;
    }

    public static INT intType(final int value) {
        final INT _int = new INT();
        _int.setValue(value);
        return _int;
    }

    public static String getPartValue(final List<EN> names) {
        final EN item = firstItem(names);
        if (item != null) {
            final ENXP part = firstItem(item.getPart());
            return (part == null) ? null : part.getValue();
        }
        return null;
    }

    public static <T> T firstItem(final List<T> list) {
        return (list.size() == 0) ? null : list.get(0);
    }

    public static String getCDCode(final CD cd) {
        return (cd == null) ? null : cd.getCode();
    }

    public static IDENTIFIEDENTITY lookupDemographicIdentity(final List<IDENTIFIEDENTITY> demographics, final String hsaId) {
        for (final IDENTIFIEDENTITY identifiedentity : demographics) {
            if (hsaId.equals(identifiedentity.getExtractId().getExtension())) {
                return identifiedentity;
            }
        }
        return null;
    }

    public static ParameterType createParameter(String name, String value) {
        assert (name != null) && (value != null);
        final ParameterType parameterType = new ParameterType();
        parameterType.setName(stType(name));
        parameterType.setValue(stType(value));
        return parameterType;
    }

    public static ELEMENT findEntryElement(final List<CONTENT> contents, final String type) {
        for(CONTENT content : contents) {
            if(content instanceof ENTRY) {
                ENTRY e = (ENTRY) content;
                for(ITEM item : e.getItems()) {
                    if(item instanceof ELEMENT) {
                        ELEMENT elm = (ELEMENT) item;
                        if(elm.getMeaning() != null && StringUtils.equals(elm.getMeaning().getCode(), type)) {
                            return elm;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Boolean boolValue(final ELEMENT elm) {
        if(elm != null && elm.getValue() instanceof BL) {
            BL bl = (BL) elm.getValue();
            return bl.isValue();
        }
        return null;
    }

    public static String getSystemHSAId(final EHREXTRACT ehrExtract) {
        if(ehrExtract.getEhrSystem() != null) {
            return ehrExtract.getEhrSystem().getExtension();
        }
        return null;
    }

    //
    public static II iiType(final String root, final String extension) {
        final II ii = new II();
        ii.setRoot(root);
        ii.setExtension(extension);
        return ii;
    }

    //
    static II iiType(final PersonId personId) {
        return (personId == null) ? null : iiType(personId.getType(),personId.getId());
    }

    //
    public static II iiType(final Object personIdType) {
        return (personIdType == null) ? null : iiType(XMLBeanMapper.getInstance().map(personIdType, PersonId.class));
    }

    //
    public static <T> T iiType(final II ii, final Class<T> type) {
        return XMLBeanMapper.getInstance().map(ii, type);
    }

    //
    static IVLTS IVLTSType(final DatePeriod datePeriod) {
        if (datePeriod == null) {
            return null;
        }
        final IVLTS ivlts = new IVLTS();
        ivlts.setLow(tsType(datePeriod.getStart()));
        ivlts.setHigh(tsType(datePeriod.getEnd()));
        return ivlts;
    }

    //
    public static IVLTS IVLTSType(final Object datePeriodType) {
        return (datePeriodType == null) ? null : IVLTSType(XMLBeanMapper.getInstance().map(datePeriodType, DatePeriod.class));
    }

    //
    public static <T> T personIdType(final II ii, final Class<T> type) {
        final PersonId personId = personId(ii);
        return (personId == null) ? null : XMLBeanMapper.getInstance().map(personId, type);
    }

    private static PersonId personId(final II ii) {
        if (ii == null) {
            return null;
        }
        final PersonId personId = new PersonId();
        personId.setId(ii.getExtension());
        personId.setType(ii.getRoot());

        return personId;
    }

    public static <T> T datePeriod(final IVLTS ivlts, final Class<T> type) {
        if (ivlts == null) {
            return null;
        }
        final DatePeriod datePeriod = new DatePeriod();

        if (ivlts.getHigh() != null) {
            datePeriod.setEnd(ivlts.getHigh().getValue());
        }
        if (ivlts.getLow() != null) {
            datePeriod.setStart(ivlts.getLow().getValue());
        }

        return XMLBeanMapper.getInstance().map(datePeriod, type);
    }
    
    public static <T> T timePeriod(final IVLTS ivlts, final Class<T> type) {
    	if(ivlts == null) {
    		return null;
    	}
    	final TimePeriod timePeriod = new TimePeriod();

        if (ivlts.getHigh() != null) {
            timePeriod.setEnd(ivlts.getHigh().getValue());
        }
        if (ivlts.getLow() != null) {
            timePeriod.setStart(ivlts.getLow().getValue());
        }

        return XMLBeanMapper.getInstance().map(timePeriod, type);
    }

    //
    public static <T> T resultType(final String logId, final List<ResponseDetailType> details, final Class<T> type) {
        
        final Result result = new Result();
        if (details.isEmpty()) {
            result.setResultCode(ResultCode.OK);
            result.setLogId("0");
        } else {
            final ResponseDetailType resp = details.get(0);
            if (resp.getText() != null) {
                result.setMessage(resp.getText().getValue());
            }
            result.setLogId(logId);
            result.setResultCode(interpret(resp.getTypeCode()));
        }

        // map from result object to a new object of type type
        return XMLBeanMapper.getInstance().map(result, type);
    }

    //
    public static <T> T cvType(final String code, final String codeSystem, final String displayName, Class<T> type) {
        final CV cv = new CV();
        cv.setCode(code);
        cv.setCodeSystem(codeSystem);
        cv.setDisplayName(displayName);
        return XMLBeanMapper.getInstance().map(cv, type);
    }
    
    public static <T> T cvType(final CD cd, Class<T> type) {
    	if(cd == null) {
    		return null;
    	}
    	final CV cv = new CV();
    	cv.setCode(cd.getCode());;
    	cv.setCodeSystem(cd.getCodeSystem());
    	cv.setCodeSystemName(cd.getCodeSystemName());
    	cv.setCodeSystemVersion(cd.getCodeSystemVersion());
    	if(cd.getDisplayName() != null) {
    		cv.setDisplayName(cd.getDisplayName().getValue());
    	}
    	if(cd.getOriginalText() != null) { 
    		cv.setOriginalText(cd.getOriginalText().getValue());
    	}
    	return XMLBeanMapper.getInstance().map(cv, type);
    }
    
    public static <T> T cvTypeWithSTValue(final ELEMENT elm, Class<T> type) {
    	if(elm == null || elm.getMeaning() == null) {
    		return null;
    	}
    	final CV cv = new CV();
    	final CD cd = elm.getMeaning();
    	cv.setCode(cd.getCode());;
    	cv.setCodeSystem(cd.getCodeSystem());
    	cv.setCodeSystemName(cd.getCodeSystemName());
    	cv.setCodeSystemVersion(cd.getCodeSystemVersion());
    	if(cd.getDisplayName() != null) {
    		cv.setDisplayName(cd.getDisplayName().getValue());
    	}
    	if(cd.getOriginalText() != null) { 
    		cv.setOriginalText(cd.getOriginalText().getValue());
    	}
    	//Set OriginalValue
    	if(elm.getValue() != null && elm.getValue() instanceof ST) {
    		cv.setOriginalText(((ST)elm.getValue()).getValue());
    	}
    	return XMLBeanMapper.getInstance().map(cv, type);
    }
    
    public static String linkTargetIdExtension(final List<LINK> links, final String targetTypeCode) {
    	for(LINK link : links) {
    		if(link.getTargetType() != null && StringUtils.equals(link.getTargetType().getCode(), targetTypeCode)) {
    			final II targetId = firstItem(link.getTargetId());
    			if(targetId != null) {
    				return targetId.getExtension();
    			}
    			return null;
    		}
    	}
    	return null;
    }
    
    //
    public static <T> T cvType(final II ii, Class<T> type) {
        if (ii == null) {
            return null;
        }
        return cvType(ii.getExtension(), ii.getRoot(), null, type);
    }


    //
    public static ResultCode interpret(final ResponseDetailTypeCodes code) {
        switch(code) {
            case E:
            case W:
                return ResultCode.ERROR;
            case I:
                return ResultCode.INFO;
            default:
                return ResultCode.OK;
        }
    }
    
    
    public static String careContactId(final List<LINK> links) {
    	for(LINK link : links) {
    		if(link.getTargetType() != null && StringUtils.equals(link.getTargetType().getCode(), AbstractMapper.INFO_VKO)) {
    			final II id = firstItem(link.getTargetId());
    			if(id != null) {
    				return id.getExtension();
    			}
    			return null;
    		}
    	}
    	return null;
    }
    
    public static COMPOSITION findCompositionByLink(final List<COMPOSITION> compositions, final List<LINK> links, final String target) {
    	for(LINK link : links) {
    		if(link.getTargetType() != null && StringUtils.equals(link.getTargetType().getCode(), target)) {
    			final II id = firstItem(link.getTargetId());
    			if(id != null && id.getExtension() != null) {
    				for(COMPOSITION comp : compositions) {
    					if(comp.getRcId() != null && StringUtils.equals(comp.getRcId().getExtension(), id.getExtension())) {
    						return comp;
    					}
    				}
    			}
    		}
    	}
    	return null;
    }
    

    private static HealthcareProfessional healthcareProfessionalType(final FUNCTIONALROLE composer,
                                                                     final Map<String, ORGANISATION> orgs,
                                                                     final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps,
                                                                     final AUDITINFO committal) {
        final HealthcareProfessional professional = new HealthcareProfessional();
        String organisationKey = null;
        String performerKey = null;
        if (composer != null && composer.getHealthcareFacility() != null) {
            organisationKey = composer.getHealthcareFacility().getExtension();
        }
        if (composer!= null && composer.getPerformer() != null) {
            performerKey = composer.getPerformer().getExtension();
        }
        professional.setHealthcareProfessionalHSAId(performerKey);
        if(organisationKey != null && orgs.containsKey(organisationKey)) {
            final ORGANISATION org = orgs.get(organisationKey);
            professional.setHealthcareProfessionalCareUnitHSAId(org.getExtractId().getExtension());
            final OrgUnit orgUnit = new OrgUnit();

            if(org.getName() != null) {
                orgUnit.setOrgUnitName(org.getName().getValue());
            }
            for(final TEL t : org.getTelecom()) {
                if(t instanceof TELEMAIL) {
                    orgUnit.setOrgUnitEmail(removePrefix(t.getValue(), "mailto:"));
                }
                if(t instanceof TELPHONE) {
                    orgUnit.setOrgUnitTelecom(removePrefix(t.getValue(), "tel:"));
                }
            }
            orgUnit.setOrgUnitHSAId(organisationKey);

            mapAddress(orgUnit, org);

            professional.setHealthcareProfessionalOrgUnit(orgUnit);
        }
        if(performerKey != null && hps.containsKey(performerKey)) {
            final IDENTIFIEDHEALTHCAREPROFESSIONAL hp = hps.get(performerKey);
            if(committal != null && committal.getTimeCommitted() != null) {
                professional.setAuthorTime(committal.getTimeCommitted().getValue());
            }
            professional.setHealthcareProfessionalCareGiverHSAId(hp.getExtractId().getExtension());
            if(!hp.getName().isEmpty() && !hp.getName().get(0).getPart().isEmpty()) {
                professional.setHealthcareProfessionalName(hp.getName().get(0).getPart().get(0).getValue());
            }

            final HEALTHCAREPROFESSIONALROLE role = firstItem(hp.getRole());
            if (role != null) {
                professional.setHealthcareProfessionalRoleCode(cvType(role.getProfession(), CV.class));
            }
        }
        return professional;
    }

    //
    protected static OrgUnit mapAddress(final OrgUnit orgUnit, final ORGANISATION organisation) {
        for (final AD ad : organisation.getAddr())
            for (final ADXP adxp : ad.getPartOrBrOrAddressLine()) {
                switch (adxp.getType()) {
                    case AL:
                        orgUnit.setOrgUnitAddress(adxp.getContent());
                        break;
                    case CEN:
                        orgUnit.setOrgUnitLocation(adxp.getContent());
                        break;
                    default:
                        break;
                }
            }
        return orgUnit;
    }



    /**
     * Removes a string prefix on match.
     *
     * @param value the string.
     * @param prefix the prefix to remove.
     * @return the string without prefix, i.e. unchanged if the prefix doesn't match.
     */
    public static String removePrefix(final String value, final String prefix) {
        return (value == null) ? null : value.replaceFirst(prefix, "");
    }

    public static <T> T patientSummaryHeader(final COMPOSITION comp, final SharedHeaderExtract baseHeader, final String timeElement, final Class<T> type) {
    	final PatientSummaryHeader header = new PatientSummaryHeader();
        if(comp.getRcId() != null) {
            header.setDocumentId(comp.getRcId().getExtension());
        }
        header.setSourceSystemHSAId(baseHeader.systemHSAId());
        if(comp.getName() != null) {
            header.setDocumentTitle(comp.getName().getValue());
        }
        if(!comp.getAttestations().isEmpty()) {
            final ATTESTATIONINFO info = comp.getAttestations().get(0);
            if(info.getTime() != null) {
                header.setDocumentTime(info.getTime().getValue());
            }
        }
        if (timeElement != null) {
            final ELEMENT time = EHRUtil.findEntryElement(comp.getContent(), timeElement);
            if (time != null && time.getValue() instanceof TS) {
                header.setDocumentTime(((TS) time.getValue()).getValue());
            } 
        }

        header.setPatientId(personId(baseHeader.subjectOfCare()));
        header.setAccountableHealthcareProfessional(healthcareProfessionalType(comp.getComposer(), baseHeader.organisations(), baseHeader.healthcareProfessionals(), comp.getCommittal()));
        final LegalAuthenticator legal = new LegalAuthenticator();
        
        if(header.getAccountableHealthcareProfessional() != null) {
            legal.setSignatureTime(header.getAccountableHealthcareProfessional().getAuthorTime());
        }
        header.setLegalAuthenticator(legal);

        for (FUNCTIONALROLE careGiver : comp.getOtherParticipations()) {
            if(careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), "iag")) {
                if(careGiver.getPerformer() != null) {
                    header.getAccountableHealthcareProfessional().setHealthcareProfessionalCareGiverHSAId(careGiver.getPerformer().getExtension());
                }
                if(careGiver.getHealthcareFacility() != null) {
                    header.getAccountableHealthcareProfessional().setHealthcareProfessionalCareUnitHSAId(careGiver.getHealthcareFacility().getExtension());
                }
            }
        }
        //Static values.
        header.setApprovedForPatient(false);
        header.setNullified(false);
        header.setNullifiedReason(null);
        return XMLBeanMapper.getInstance().map(header, type);
    }

    //
    public static <T> RIV13606REQUESTEHREXTRACTRequestType requestType(final T rivRequestType, final CD meaning) {
    	final RIV13606REQUESTEHREXTRACTRequestType request = new RIV13606REQUESTEHREXTRACTRequestType();
    	final Request mapperRequest = XMLBeanMapper.getInstance().map(rivRequestType, Request.class);

    	request.getMeanings().add(meaning);
    	request.setSubjectOfCareId(iiType(mapperRequest.getPatientId()));
    	request.setTimePeriod(IVLTSType(mapperRequest.getTimePeriod()));

    	
    	if (mapperRequest.getCareUnitHSAId().size() > 1) {
    		throw new IllegalArgumentException("Only one careUnitHSAId element can be handled");
    	} else if (mapperRequest.getCareUnitHSAId().size() == 1) {
            final ParameterType hsaId = new ParameterType();
            hsaId.setName(stType("hsa_id"));
            hsaId.setValue(stType(mapperRequest.getCareUnitHSAId().get(0)));
            request.getParameters().add(hsaId);
        }
        request.getParameters().add(versionParameter);

        return request;
    }


    /**
     * Returns a {@link Date} date and time representation.
     *
     * @param cal the actual date and time.
     * @return the {@link Date} representation.
     */
    public static Date toDate(XMLGregorianCalendar cal) {
        if (cal != null) {
            final Calendar c = Calendar.getInstance();

            c.set(Calendar.DATE, cal.getDay());
            c.set(Calendar.MONTH, cal.getMonth() - 1);
            c.set(Calendar.YEAR, cal.getYear());
            c.set(Calendar.DAY_OF_MONTH, cal.getDay());
            c.set(Calendar.HOUR_OF_DAY, cal.getHour());
            c.set(Calendar.MINUTE, cal.getMinute());
            c.set(Calendar.SECOND, cal.getSecond());
            c.set(Calendar.MILLISECOND, cal.getMillisecond());

            return c.getTime();
        }
        return null;
    }

    // Generic baseline of data types to be able to convert between schemas (java packages).
    //
    @Data
    public static class Result {
        private ResultCode resultCode;
        private ErrorCode errorCode;
        private String logId;
        private String subCode;
        private String message;
    }

    //
    public static enum ResultCode {
        OK,
        ERROR,
        INFO;
    }

    //
    public static enum ErrorCode {
        INVALID_REQUEST,
        TRANSFORMATION_ERROR,
        APPLICATION_ERROR,
        TECHNICAL_ERROR;
    }
    
    @Data
    public static class TimePeriod {
    	private String start;
    	private String end;
    }
    
    @Data
    public static class Request {
    	private PersonId patientId;
    	private DatePeriod timePeriod;
    	private List<String> careUnitHSAId;

        public List<String> getCareUnitHSAId() {
            if (careUnitHSAId == null) {
                careUnitHSAId = new ArrayList<String>();
            }
            return careUnitHSAId;
        }
    }

    //
    @Data
    public static class DatePeriod {
        private String start;
        private String end;
    }

    //
    @Data
    public static class PersonId {
        private String id;
        private String type;
    }
    
    @Data
    public static class IIType {
    	private String extension;
    	private String root;
    }

    //
    @Data
    public static class CV {
        private String code;
        private String codeSystem;
        private String codeSystemName;
        private String codeSystemVersion;
        private String displayName;
        private String originalText;
    }

    //
    @Data
    public static class HealthcareProfessional {
        private String authorTime;
        private String healthcareProfessionalHSAId;
        private String healthcareProfessionalName;
        private CV healthcareProfessionalRoleCode;
        private OrgUnit healthcareProfessionalOrgUnit;
        private String healthcareProfessionalCareUnitHSAId;
        private String healthcareProfessionalCareGiverHSAId;
    }

    //
    @Data
    public static class OrgUnit {
        private String orgUnitHSAId;
        private String orgUnitName;
        private String orgUnitTelecom;
        private String orgUnitEmail;
        private String orgUnitAddress;
        private String orgUnitLocation;
    }

    //
    @Data
    public static class PatientSummaryHeader {
        private String documentId;
        private String sourceSystemHSAId;
        private String documentTitle;
        private String documentTime;
        private PersonId patientId;
        private HealthcareProfessional accountableHealthcareProfessional;
        private LegalAuthenticator legalAuthenticator;
        private boolean approvedForPatient;
        private String careContactId;
        private Boolean nullified;
        private String nullifiedReason;
    }

    //
    @Data
    public static class LegalAuthenticator {
        private String signatureTime;
        private String legalAuthenticatorHSAId;
        private String legalAuthenticatorName;
        private CV legalAuthenticatorRoleCode;
    }
    
}

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;

import se.rivta.en13606.ehrextract.v11.AD;
import se.rivta.en13606.ehrextract.v11.ADXP;
import se.rivta.en13606.ehrextract.v11.ANY;
import se.rivta.en13606.ehrextract.v11.ATTESTATIONINFO;
import se.rivta.en13606.ehrextract.v11.AUDITINFO;
import se.rivta.en13606.ehrextract.v11.BL;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.EN;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.ENXP;
import se.rivta.en13606.ehrextract.v11.FUNCTIONALROLE;
import se.rivta.en13606.ehrextract.v11.HEALTHCAREPROFESSIONALROLE;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDHEALTHCAREPROFESSIONAL;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.LINK;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.ParameterType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailTypeCodes;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TEL;
import se.rivta.en13606.ehrextract.v11.TELEMAIL;
import se.rivta.en13606.ehrextract.v11.TELPHONE;
import se.rivta.en13606.ehrextract.v11.TS;
import se.skl.skltpservices.npoadapter.mapper.AbstractMapper;
import se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper;

/**
 * Utility class to create and map common EHR types.
 * 
 * @author torbjorncla
 */
public final class EHRUtil {

    public static final String CAREUNITHSAIDS    = "careUnitHsaIds";
    public static final String INFORMATIONSÄGARE = "iag";
    
    
    private static final ParameterType versionParameter = new ParameterType();

    static {
        versionParameter.setName(stType("version"));
        versionParameter.setValue(stType("1.1"));
    }

    private static ThreadLocal<SimpleDateFormat> yyyyMMddFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMdd");
        }
    };

    public static String formatTimestamp(Date timestamp) {
        return yyyyMMddFormatter.get().format(timestamp);
    }
    //
    
    public static Date parseTimePeriod(String dateString) throws ParseException {
        return yyyyMMddFormatter.get().parse(dateString);
    }

    
    public static String getElementTextValue(final ELEMENT e) {
        if (e != null && e.getValue() instanceof ST) {
            ST text = (ST) e.getValue();
            return text.getValue();
        }
        return null;
    }

    public static String getElementTimeValue(final ELEMENT e) {
        if (e != null && e.getValue() instanceof TS) {
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
    
    /**
     * Return TS value from ANY
     * @param obj
     * @return String value, might be null
     */
    public static String getTSValue(final ANY obj) {
    	if(obj instanceof TS && obj != null) {
    		return ((TS)obj).getValue();
    	}
    	return null;
    }
    
    public static String getSTValue(final ANY obj) {
    	if(obj instanceof ST && obj != null) {
    		return ((ST)obj).getValue();
    	}
    	return null;
    }

    public static ELEMENT findEntryElement(final List<CONTENT> contents, final String type) {
        for (CONTENT content : contents) {
            if (content instanceof ENTRY) {
                ENTRY e = (ENTRY) content;
                for (ITEM item : e.getItems()) {
                    if (item instanceof ELEMENT) {
                        ELEMENT elm = (ELEMENT) item;
                        if (elm.getMeaning() != null && StringUtils.equals(elm.getMeaning().getCode(), type)) {
                            return elm;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Boolean boolValue(final ELEMENT elm) {
        if (elm != null && elm.getValue() instanceof BL) {
            BL bl = (BL) elm.getValue();
            return bl.isValue();
        }
        return null;
    }

    public static String getSystemHSAId(final EHREXTRACT ehrExtract) {
        if (ehrExtract.getEhrSystem() != null) {
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
        return (personId == null) ? null : iiType(personId.getType(), personId.getId());
    }

    //
    public static II iiType(final Object personIdType) {
        return (personIdType == null) ? null : iiType(XMLBeanMapper.getInstance().map(personIdType, PersonId.class));
    }

    //
    public static <T> T iiType(final II ii, final Class<T> type) {
        return XMLBeanMapper.getInstance().map(ii, type);
    }

    /**
     * @param datePeriod containing start and end dates as strings - no validation is performed on these parameters.
     * @return new IVLTS
     */
    protected static IVLTS IVLTSType(final DatePeriod datePeriod) {
        if (datePeriod == null) {
            return null;
        }
        final IVLTS ivlts = new IVLTS();
        ivlts.setLow(tsType(datePeriod.getStart()));
        ivlts.setHigh(tsType(datePeriod.getEnd()));
        ivlts.setLowClosed(true);
        ivlts.setHighClosed(true);
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
        if (ivlts == null) {
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
    
    public static Double tsToDouble(final TS ts) {
    	try {
    		if(ts.getValue() != null) {
    			return Double.valueOf(ts.getValue());
    		}
    	} catch (NumberFormatException nfe) {}
    	return null;
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

    /**
     * Convert code object from 13606 to rivta.
     * Automatically handle different package names.
     * @param cd 13606 object of type CD
     * @param type CVType.class for the appropriate package
     * @return new object of type CVType - default to null
     */
    public static <T> T cvTypeFromCD(final CD cd, Class<T> type) {
        if (cd == null) {
            return null;
        }
        final CV cv = new CV();
        cv.setCode(cd.getCode());
        cv.setCodeSystem(cd.getCodeSystem());
        cv.setCodeSystemName(cd.getCodeSystemName());
        cv.setCodeSystemVersion(cd.getCodeSystemVersion());
        if (cd.getDisplayName() != null) {
            cv.setDisplayName(cd.getDisplayName().getValue());
        }
        if (StringUtils.isBlank(cd.getCode()) && cd.getOriginalText() != null) {
            cv.setOriginalText(cd.getOriginalText().getValue());
            cv.setCode(null);
            cv.setCodeSystem(null);
            cv.setCodeSystemName(null);
            cv.setCodeSystemVersion(null);
            cv.setDisplayName(null);
        }
        return XMLBeanMapper.getInstance().map(cv, type);
    }

    /**
     * Convert 13606 element with value attribute to rivta CVType.
     * Automatically handle different package names. 
     * 
     * <items xsi:type="urn:ELEMENT">
     *   <value value="Detta är komm.. .." xsi:type="urn:ST"/>
     * 
     * @param elm 13606 object containing an element 'value' with attribute 'value of type 'ST'.
     * @param type CVType with the appropriate package.
     * @return new object of type CVType - default to null
     */
    public static <T> T cvTypeFromElementWithValueST(final ELEMENT elm, Class<T> type) {
        if (elm == null || elm.getMeaning() == null) {
            return null;
        }
        final CV cv = new CV();
        
        // Set OriginalValue
        if (elm.getValue() != null && elm.getValue() instanceof ST) {
            cv.setOriginalText(((ST) elm.getValue()).getValue());
        }
        return XMLBeanMapper.getInstance().map(cv, type);
    }

    public static String linkTargetIdExtension(final List<LINK> links, final String targetTypeCode) {
        for (LINK link : links) {
            if (link.getTargetType() != null && StringUtils.equals(link.getTargetType().getCode(), targetTypeCode)) {
                final II targetId = firstItem(link.getTargetId());
                if (targetId != null) {
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
        switch (code) {
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
        for (LINK link : links) {
            if (link.getTargetType() != null && StringUtils.equals(link.getTargetType().getCode(), AbstractMapper.INFO_VKO)) {
                final II id = firstItem(link.getTargetId());
                if (id != null) {
                    return id.getExtension();
                }
                return null;
            }
        }
        return null;
    }

    public static COMPOSITION findCompositionByLink(final List<COMPOSITION> compositions, final List<LINK> links, final String target) {
        for (LINK link : links) {
            if (link.getTargetType() != null && StringUtils.equals(link.getTargetType().getCode(), target)) {
                final II id = firstItem(link.getTargetId());
                if (id != null && id.getExtension() != null) {
                    for (COMPOSITION comp : compositions) {
                        if (comp.getRcId() != null && StringUtils.equals(comp.getRcId().getExtension(), id.getExtension())) {
                            return comp;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static HealthcareProfessional healthcareProfessionalType(final FUNCTIONALROLE composer, 
                                                                     final Map<String, ORGANISATION> orgs,
                                                                     final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> healthcareProfessionals, 
                                                                     final AUDITINFO committal) {
        final HealthcareProfessional resultProfessional = new HealthcareProfessional();

        String careGiverHSAId = null;
        if (composer != null && composer.getHealthcareFacility() != null) {
            careGiverHSAId = composer.getHealthcareFacility().getExtension();
        }

        // --- care giver, OrgUnit
        if (careGiverHSAId != null && orgs.containsKey(careGiverHSAId)) {
            final ORGANISATION healthcareFacilityOrganisation = orgs.get(careGiverHSAId);

            resultProfessional.setHealthcareProfessionalCareGiverHSAId(careGiverHSAId);

            final OrgUnit careGiverOrgUnit = new OrgUnit();
            if (healthcareFacilityOrganisation.getName() != null) {
                careGiverOrgUnit.setOrgUnitName(healthcareFacilityOrganisation.getName().getValue());
            }
            for (final TEL t : healthcareFacilityOrganisation.getTelecom()) {
                if (t instanceof TELEMAIL) {
                    careGiverOrgUnit.setOrgUnitEmail(removePrefix(t.getValue(), "mailto:"));
                }
                if (t instanceof TELPHONE) {
                    careGiverOrgUnit.setOrgUnitTelecom(removePrefix(t.getValue(), "tel:"));
                }
            }
            careGiverOrgUnit.setOrgUnitHSAId(careGiverHSAId);

            mapAddress(careGiverOrgUnit, healthcareFacilityOrganisation);

            // ---
            resultProfessional.setHealthcareProfessionalOrgUnit(careGiverOrgUnit);
        }

        //

        String careUnitHSAId = null;
        if (composer != null && composer.getPerformer() != null) {
            careUnitHSAId = composer.getPerformer().getExtension();
        }
        resultProfessional.setHealthcareProfessionalHSAId(careUnitHSAId);

        resultProfessional.setHealthcareProfessionalCareUnitHSAId(careUnitHSAId);

        if (committal != null && committal.getTimeCommitted() != null) {
            resultProfessional.setAuthorTime(committal.getTimeCommitted().getValue());
        }

        if (careUnitHSAId != null && healthcareProfessionals.containsKey(careUnitHSAId)) {
            final IDENTIFIEDHEALTHCAREPROFESSIONAL careUnitProfessional = healthcareProfessionals.get(careUnitHSAId);

            if (!careUnitProfessional.getName().isEmpty() && !careUnitProfessional.getName().get(0).getPart().isEmpty()) {
                resultProfessional.setHealthcareProfessionalName(careUnitProfessional.getName().get(0).getPart().get(0).getValue());
            }

            final HEALTHCAREPROFESSIONALROLE role = firstItem(careUnitProfessional.getRole());
            if (role != null) {
                resultProfessional.setHealthcareProfessionalRoleCode(cvTypeFromCD(role.getProfession(), CV.class));
            }
        }

        // ---
        return resultProfessional;
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
     * @param value
     *            the string.
     * @param prefix
     *            the prefix to remove.
     * @return the string without prefix, i.e. unchanged if the prefix doesn't match.
     */
    public static String removePrefix(final String value, final String prefix) {
        return (value == null) ? null : value.replaceFirst(prefix, "");
    }
    
    
    public static void setDocumentTitle() {
        
    }
    

    public static <T> T patientSummaryHeader(final COMPOSITION comp, 
            final SharedHeaderExtract baseHeader, 
            final String timeElement,
            final Class<T> type,
            final boolean signatureTime) {
        return patientSummaryHeader(comp, baseHeader, timeElement, type, false, false, signatureTime);
    }
    

    /**
     * @param composition
     * @param baseHeader
     * @param timeElement code of the time element
     * @param type of response
     * @param include documentTitle
     * @param include documentTime
     * @param include signatureTime
     * @return a patientSummary of the requested type
     */
    public static <T> T patientSummaryHeader(final COMPOSITION composition, 
                                             final SharedHeaderExtract baseHeader, 
                                             final String timeElement,
                                             final Class<T> type,
                                             final boolean documentTitle,
                                             final boolean documentTime,
                                             final boolean signatureTime) {
        final PatientSummaryHeader header = new PatientSummaryHeader();

        if (composition.getRcId() != null) {
            header.setDocumentId(composition.getRcId().getExtension());
        }

        header.setSourceSystemHSAId(baseHeader.systemHSAId());

        // optional
        if (documentTitle) {
            if (composition.getName() == null || StringUtils.isBlank(composition.getName().getValue())) {
                header.setDocumentTitle(null); // default
            } else {
                header.setDocumentTitle(composition.getName().getValue());
            }
        }

        // optional
        if (documentTime) {
            if (!composition.getAttestations().isEmpty()) {
                final ATTESTATIONINFO info = composition.getAttestations().get(0);
                if (info.getTime() != null) {
                    header.setDocumentTime(info.getTime().getValue());
                }
            }
            if (timeElement != null) {
                final ELEMENT time = EHRUtil.findEntryElement(composition.getContent(), timeElement);
                if (time != null && time.getValue() instanceof TS) {
                    header.setDocumentTime(((TS) time.getValue()).getValue());
                }
            }
        }
        
        header.setPatientId(personId(baseHeader.subjectOfCare()));
        header.setAccountableHealthcareProfessional(healthcareProfessionalType(composition.getComposer(), 
                                                    baseHeader.organisations(),
                                                    baseHeader.healthcareProfessionals(), 
                                                    composition.getCommittal()));

        
        //add signaturetime
        //meaning/attenstation/time@value
        if(signatureTime) {
        	if(!composition.getAttestations().isEmpty()) {
        		final ATTESTATIONINFO info = composition.getAttestations().get(0);
        		if(info.getTime() != null && info.getTime().getValue() != null) {
        			final LegalAuthenticator auth = new LegalAuthenticator();
        			auth.setSignatureTime(info.getTime().getValue());
        			header.setLegalAuthenticator(auth);
        		}
        	}
        }
        
        	

        for (FUNCTIONALROLE careGiver : composition.getOtherParticipations()) {
            if (careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), INFORMATIONSÄGARE)) {
                if (careGiver.getPerformer() != null) {
                    // <performer extension="Enhet" root="1.2.752.129.2.1.4.1"/>
                    header.getAccountableHealthcareProfessional()
                             .setHealthcareProfessionalCareUnitHSAId(careGiver.getPerformer().getExtension());
                }
                if (careGiver.getHealthcareFacility() != null) {
                    // <healthcare_facility extension="Givare" root="1.2.752.129.2.1.4.1"/>
                    header.getAccountableHealthcareProfessional()
                             .setHealthcareProfessionalCareGiverHSAId(careGiver.getHealthcareFacility().getExtension());
                }
            }
        }
        return XMLBeanMapper.getInstance().map(header, type);
    }

    
    /**
     * @return true if this composition should be retained. Otherwise, filter it out.
     */
    public static boolean retain(COMPOSITION composition13606, List<String> careUnitHsaIds, Logger log) {
        
        if (careUnitHsaIds == null || careUnitHsaIds.isEmpty()) {
            log.debug("There no care unit hsa ids in the incoming request to filter against");
            return true;
        } else {
            String compositionCareUnitHsaId = "";
            for (FUNCTIONALROLE careGiver : composition13606.getOtherParticipations()) {
                if (careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), INFORMATIONSÄGARE)) {
                    if (careGiver.getPerformer() != null) {
                        // <performer extension="Enhet" root="1.2.752.129.2.1.4.1"/>
                        if (StringUtils.isNotBlank(careGiver.getPerformer().getExtension())) {
                            compositionCareUnitHsaId = careGiver.getPerformer().getExtension().toUpperCase(); // uppercase
                        }
                    }
                }
            }
            if (StringUtils.isBlank(compositionCareUnitHsaId)) {
                log.debug("There no care unit hsa ids in composition");
                return false;
            } else {
                log.debug("compositionCareUnitHsaId:{}", compositionCareUnitHsaId);
                if (careUnitHsaIds.contains(compositionCareUnitHsaId)) { // uppercase
                    if (composition13606.getRcId() != null) {
                        log.debug("composition with id {} is contained in the incoming list of careUnitHsaId", composition13606.getRcId().getExtension());
                    }
                    return true;
                } else {
                    if (composition13606.getRcId() != null) {
                        log.debug("composition with id {} is not contained in the incoming list of careUnitHsaId", composition13606.getRcId().getExtension());
                    }
                    return false;
                }
            }
        }
    }

    
    /**
     * If the request contains a list of care units, store the values as an invocation parameter list.
     * 
     * Note the 'i' is sometimes lower case (CareDocumentation, ReferralOutcome), otherwise upper case.
     */
    public static <T> void storeCareUnitHsaIdsAsInvocationProperties(final T rivRequestType, MuleMessage message, Logger log) {
        
        final Request mapperRequest = XMLBeanMapper.getInstance().map(rivRequestType, Request.class);

        List<String> careUnitHsaIds = null;
        
        if ( (mapperRequest.getCareUnitHSAid() == null || mapperRequest.getCareUnitHSAid().isEmpty())
            &&
             (mapperRequest.getCareUnitHSAId() == null || mapperRequest.getCareUnitHSAId().isEmpty())
           ) {
            log.debug("Request contains no careUnitHSAid or careUnitHSAId");
        } else if ( (mapperRequest.getCareUnitHSAid() != null && !mapperRequest.getCareUnitHSAid().isEmpty())
                    &&
                    (mapperRequest.getCareUnitHSAId() != null && !mapperRequest.getCareUnitHSAId().isEmpty())
                  ) {
            throw new IllegalArgumentException("Request contains both careUnitHSAid and careUnitHSAId");
        } else if (mapperRequest.getCareUnitHSAid() == null || mapperRequest.getCareUnitHSAid().isEmpty()) {
            careUnitHsaIds = mapperRequest.getCareUnitHSAId();
        } else {
            careUnitHsaIds = mapperRequest.getCareUnitHSAid();
        }
        
        if (careUnitHsaIds != null && !careUnitHsaIds.isEmpty()) {
            // ensure ids are uppercase and unique
            List<String> careUnitHsaIdsUpperCase = new ArrayList<String>();
            for (String careUnitHsaIdUnknownCase : careUnitHsaIds) {
                if (StringUtils.isNotEmpty(careUnitHsaIdUnknownCase)) {
                    careUnitHsaIdsUpperCase.add(careUnitHsaIdUnknownCase.toUpperCase());
                }
            }
            Set<String> uniqueCareUnitHsaIdUpperCase = new LinkedHashSet<String>(careUnitHsaIdsUpperCase);
            careUnitHsaIds = new ArrayList<String>(uniqueCareUnitHsaIdUpperCase);
            if (log.isDebugEnabled()) {
                log.debug("Storing {} careUnitHsaIds from request", careUnitHsaIds.size());
                for (String careUnitHsaId : careUnitHsaIds) {
                    log.info(careUnitHsaId);
                }
            }
            message.setInvocationProperty(CAREUNITHSAIDS, careUnitHsaIds);
        }
    }

    
    @SuppressWarnings("unchecked")
    public static List<String> retrieveCareUnitHsaIdsInvocationProperties(MuleMessage message, Logger log) {
        
        List<String> careUnitHsaIds = new ArrayList<String>();
        
        Object o = message.getInvocationProperty(CAREUNITHSAIDS);
        if (o != null) {
            try {
                careUnitHsaIds = (List<String>)o;
            } catch (ClassCastException c) {
                throw new RuntimeException("invocation parameter careUnitHsaIds - expecting List<String>, received " + o.getClass().getName());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieved {} values for invocation property {}", careUnitHsaIds.size(), CAREUNITHSAIDS);
            for (String careUnitHsaId : careUnitHsaIds) {
                log.debug(careUnitHsaId);
            }
        }
        
        return careUnitHsaIds;
    }

    
    /**
     * Create a 13606 request containing mandatory elements hsa_id, transaction_id, version
     */
    public static <T> RIV13606REQUESTEHREXTRACTRequestType requestType(
            final T rivRequestType, final CD meaning, final String messageId, final Object producerHsaId) {
        
        final RIV13606REQUESTEHREXTRACTRequestType request = new RIV13606REQUESTEHREXTRACTRequestType();
        final Request mapperRequest = XMLBeanMapper.getInstance().map(rivRequestType, Request.class);

        request.getMeanings().add(meaning);
        request.setSubjectOfCareId(iiType(mapperRequest.getPatientId()));
        request.setTimePeriod(IVLTSType(mapperRequest.getTimePeriod()));

        // hsa_id
        final ParameterType hsaId = new ParameterType();
        hsaId.setName(stType("hsa_id"));
        hsaId.setValue(stType(producerHsaId.toString()));
        request.getParameters().add(hsaId);

        // transaction_id
        final ParameterType transactionId = new ParameterType();
        transactionId.setName(stType("transaction_id"));
        transactionId.setValue(stType(messageId));
        request.getParameters().add(transactionId);
        
        // version
        request.getParameters().add(versionParameter);

        return request;
    }

    /**
     * Returns a {@link Date} date and time representation.
     *
     * @param cal
     *            the actual date and time.
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
    
    
    /**
     * Timestamp in format yyyyMMddHHmmss - sometimes needs to be padded with zeros. 
     */
    public static String padTimestampIfNecessary(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(timestamp);
        while (result.length() < "yyyyMMddHHmmss".length()) {
            result.append("0");
        }
        return result.toString();
    }


    // --- --------------------------------------------------------------------
    

    // Generic baseline of data types to be able to convert between schemas (java packages).
    //
    public static class Result {
        private ResultCode resultCode;
        private ErrorCode errorCode;
        private String logId;
        private String subCode;
        private String message;

        public Result() {
            super();
        }

        public Result(ResultCode resultCode, ErrorCode errorCode, String logId, String subCode, String message) {
            super();
            this.resultCode = resultCode;
            this.errorCode = errorCode;
            this.logId = logId;
            this.subCode = subCode;
            this.message = message;
        }

        public ResultCode getResultCode() {
            return resultCode;
        }

        public void setResultCode(ResultCode resultCode) {
            this.resultCode = resultCode;
        }

        public ErrorCode getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        public String getLogId() {
            return logId;
        }

        public void setLogId(String logId) {
            this.logId = logId;
        }

        public String getSubCode() {
            return subCode;
        }

        public void setSubCode(String subCode) {
            this.subCode = subCode;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    //
    public static enum ResultCode {
        OK, ERROR, INFO;
    }

    //
    public static enum ErrorCode {
        INVALID_REQUEST, TRANSFORMATION_ERROR, APPLICATION_ERROR, TECHNICAL_ERROR;
    }

    public static class TimePeriod {
        private String start;
        private String end;

        public TimePeriod() {
            super();
        }

        public TimePeriod(String start, String end) {
            super();
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

    }

    public static class Request {
        //
        private PersonId patientId;

        public PersonId getPatientId() {
            return patientId;
        }

        public void setPatientId(PersonId patientId) {
            this.patientId = patientId;
        }

        //
        private DatePeriod timePeriod;

        public DatePeriod getTimePeriod() {
            return timePeriod;
        }

        public void setTimePeriod(DatePeriod timePeriod) {
            this.timePeriod = timePeriod;
        }
        
        // ImagingOutcome, MedicationHistory, ReferralOutcome
        
        public DatePeriod getDatePeriod() {
            return timePeriod;
        }
        
        public void setDatePeriod(DatePeriod datePeriod) {
            timePeriod = datePeriod;
        }

        // RIV-TA : GetAlertInformation, GetCareContacts, GetDiagnosis, GetImagingOutcome, GetLaboratoryOrderOutcome, GetMedicationHistory
        private List<String> careUnitHSAId;

        public List<String> getCareUnitHSAId() {
            if (careUnitHSAId == null) {
                careUnitHSAId = new ArrayList<String>();
            }
            return careUnitHSAId;
        }

        public void setCareUnitHSAId(List<String> ids) {
            careUnitHSAId = ids;
        }

        // RIV-TA : GetReferralOutcome, GetCareDocumentation
        private List<String> careUnitHSAid;

        public List<String> getCareUnitHSAid() {
            if (careUnitHSAid == null) {
                careUnitHSAid = new ArrayList<String>();
            }
            return careUnitHSAid;
        }

        public void setCareUnitHSAid(List<String> ids) {
            careUnitHSAid = ids;
        }
    }

    //
    public static class DatePeriod {
        private String start;
        private String end;

        public DatePeriod() {
            super();
        }

        public DatePeriod(String start, String end) {
            super();
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

    }

    //
    public static class PersonId {
        private String id;
        private String type;

        public PersonId() {
            super();
        }

        public PersonId(String id, String type) {
            super();
            this.id = id;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class IIType {
        private String extension;
        private String root;

        public IIType() {
            super();
        }

        public IIType(String extension, String root) {
            super();
            this.extension = extension;
            this.root = root;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }
    }

    //
    public static class CV {
        private String code;
        private String codeSystem;
        private String codeSystemName;
        private String codeSystemVersion;
        private String displayName;
        private String originalText;

        public CV() {
            super();
        }

        public CV(String code, String codeSystem, String codeSystemName, String codeSystemVersion, String displayName, String originalText) {
            super();
            this.code = code;
            this.codeSystem = codeSystem;
            this.codeSystemName = codeSystemName;
            this.codeSystemVersion = codeSystemVersion;
            this.displayName = displayName;
            this.originalText = originalText;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCodeSystem() {
            return codeSystem;
        }

        public void setCodeSystem(String codeSystem) {
            this.codeSystem = codeSystem;
        }

        public String getCodeSystemName() {
            return codeSystemName;
        }

        public void setCodeSystemName(String codeSystemName) {
            this.codeSystemName = codeSystemName;
        }

        public String getCodeSystemVersion() {
            return codeSystemVersion;
        }

        public void setCodeSystemVersion(String codeSystemVersion) {
            this.codeSystemVersion = codeSystemVersion;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getOriginalText() {
            return originalText;
        }

        public void setOriginalText(String originalText) {
            this.originalText = originalText;
        }
    }

    //
    public static class HealthcareProfessional {
        private String authorTime;
        private String healthcareProfessionalHSAId;
        private String healthcareProfessionalName;
        private CV healthcareProfessionalRoleCode;
        private OrgUnit healthcareProfessionalOrgUnit;
        private String healthcareProfessionalCareUnitHSAId;
        private String healthcareProfessionalCareGiverHSAId;

        public HealthcareProfessional() {
            super();
        }

        public HealthcareProfessional(String authorTime, String healthcareProfessionalHSAId, String healthcareProfessionalName,
                CV healthcareProfessionalRoleCode, OrgUnit healthcareProfessionalOrgUnit, String healthcareProfessionalCareUnitHSAId,
                String healthcareProfessionalCareGiverHSAId) {
            super();
            this.authorTime = authorTime;
            this.healthcareProfessionalHSAId = healthcareProfessionalHSAId;
            this.healthcareProfessionalName = healthcareProfessionalName;
            this.healthcareProfessionalRoleCode = healthcareProfessionalRoleCode;
            this.healthcareProfessionalOrgUnit = healthcareProfessionalOrgUnit;
            this.healthcareProfessionalCareUnitHSAId = healthcareProfessionalCareUnitHSAId;
            this.healthcareProfessionalCareGiverHSAId = healthcareProfessionalCareGiverHSAId;
        }

        public String getAuthorTime() {
            return authorTime;
        }

        public void setAuthorTime(String authorTime) {
            this.authorTime = authorTime;
        }

        public String getHealthcareProfessionalHSAId() {
            return healthcareProfessionalHSAId;
        }

        public void setHealthcareProfessionalHSAId(String healthcareProfessionalHSAId) {
            this.healthcareProfessionalHSAId = healthcareProfessionalHSAId;
        }

        public String getHealthcareProfessionalName() {
            return healthcareProfessionalName;
        }

        public void setHealthcareProfessionalName(String healthcareProfessionalName) {
            this.healthcareProfessionalName = healthcareProfessionalName;
        }

        public CV getHealthcareProfessionalRoleCode() {
            return healthcareProfessionalRoleCode;
        }

        public void setHealthcareProfessionalRoleCode(CV healthcareProfessionalRoleCode) {
            this.healthcareProfessionalRoleCode = healthcareProfessionalRoleCode;
        }

        public OrgUnit getHealthcareProfessionalOrgUnit() {
            return healthcareProfessionalOrgUnit;
        }

        public void setHealthcareProfessionalOrgUnit(OrgUnit healthcareProfessionalOrgUnit) {
            this.healthcareProfessionalOrgUnit = healthcareProfessionalOrgUnit;
        }

        public String getHealthcareProfessionalCareUnitHSAId() {
            return healthcareProfessionalCareUnitHSAId;
        }

        public void setHealthcareProfessionalCareUnitHSAId(String healthcareProfessionalCareUnitHSAId) {
            this.healthcareProfessionalCareUnitHSAId = healthcareProfessionalCareUnitHSAId;
        }

        public String getHealthcareProfessionalCareGiverHSAId() {
            return healthcareProfessionalCareGiverHSAId;
        }

        public void setHealthcareProfessionalCareGiverHSAId(String healthcareProfessionalCareGiverHSAId) {
            this.healthcareProfessionalCareGiverHSAId = healthcareProfessionalCareGiverHSAId;
        }

    }

    //
    public static class OrgUnit {
        private String orgUnitHSAId;
        private String orgUnitName;
        private String orgUnitTelecom;
        private String orgUnitEmail;
        private String orgUnitAddress;
        private String orgUnitLocation;

        public OrgUnit() {
            super();
        }

        public OrgUnit(String orgUnitHSAId, String orgUnitName, String orgUnitTelecom, String orgUnitEmail, String orgUnitAddress,
                String orgUnitLocation) {
            super();
            this.orgUnitHSAId = orgUnitHSAId;
            this.orgUnitName = orgUnitName;
            this.orgUnitTelecom = orgUnitTelecom;
            this.orgUnitEmail = orgUnitEmail;
            this.orgUnitAddress = orgUnitAddress;
            this.orgUnitLocation = orgUnitLocation;
        }

        public String getOrgUnitHSAId() {
            return orgUnitHSAId;
        }

        public void setOrgUnitHSAId(String orgUnitHSAId) {
            this.orgUnitHSAId = orgUnitHSAId;
        }

        public String getOrgUnitName() {
            return orgUnitName;
        }

        public void setOrgUnitName(String orgUnitName) {
            this.orgUnitName = orgUnitName;
        }

        public String getOrgUnitTelecom() {
            return orgUnitTelecom;
        }

        public void setOrgUnitTelecom(String orgUnitTelecom) {
            this.orgUnitTelecom = orgUnitTelecom;
        }

        public String getOrgUnitEmail() {
            return orgUnitEmail;
        }

        public void setOrgUnitEmail(String orgUnitEmail) {
            this.orgUnitEmail = orgUnitEmail;
        }

        public String getOrgUnitAddress() {
            return orgUnitAddress;
        }

        public void setOrgUnitAddress(String orgUnitAddress) {
            this.orgUnitAddress = orgUnitAddress;
        }

        public String getOrgUnitLocation() {
            return orgUnitLocation;
        }

        public void setOrgUnitLocation(String orgUnitLocation) {
            this.orgUnitLocation = orgUnitLocation;
        }

    }

    //
    public static class PatientSummaryHeader {
        private String documentId;
        private String sourceSystemHSAId;
        private String documentTitle;
        private String documentTime;
        private PersonId patientId;
        private HealthcareProfessional accountableHealthcareProfessional;
        private LegalAuthenticator legalAuthenticator;
        private boolean approvedForPatient = false;
        private String careContactId;

        public String getDocumentId() {
            return documentId;
        }

        private void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getSourceSystemHSAId() {
            return sourceSystemHSAId;
        }

        private void setSourceSystemHSAId(String sourceSystemHSAId) {
            this.sourceSystemHSAId = sourceSystemHSAId;
        }

        public String getDocumentTitle() {
            return documentTitle;
        }

        private void setDocumentTitle(String documentTitle) {
            this.documentTitle = documentTitle;
        }

        public String getDocumentTime() {
            return documentTime;
        }

        private void setDocumentTime(String documentTime) {
            this.documentTime = documentTime;
        }

        public PersonId getPatientId() {
            return patientId;
        }

        private void setPatientId(PersonId patientId) {
            this.patientId = patientId;
        }

        public HealthcareProfessional getAccountableHealthcareProfessional() {
            return accountableHealthcareProfessional;
        }

        private void setAccountableHealthcareProfessional(HealthcareProfessional accountableHealthcareProfessional) {
            this.accountableHealthcareProfessional = accountableHealthcareProfessional;
        }

        public LegalAuthenticator getLegalAuthenticator() {
            return legalAuthenticator;
        }

        private void setLegalAuthenticator(LegalAuthenticator legalAuthenticator) {
            this.legalAuthenticator = legalAuthenticator;
        }

        public boolean isApprovedForPatient() {
            return approvedForPatient;
        }

        public String getCareContactId() {
            return careContactId;
        }
    }

    //
    public static class LegalAuthenticator {
        private String signatureTime;
        private String legalAuthenticatorHSAId;
        private String legalAuthenticatorName;
        private CV legalAuthenticatorRoleCode;

        public LegalAuthenticator() {
            super();
        }

        public LegalAuthenticator(String signatureTime, String legalAuthenticatorHSAId, String legalAuthenticatorName, CV legalAuthenticatorRoleCode) {
            super();
            this.signatureTime = signatureTime;
            this.legalAuthenticatorHSAId = legalAuthenticatorHSAId;
            this.legalAuthenticatorName = legalAuthenticatorName;
            this.legalAuthenticatorRoleCode = legalAuthenticatorRoleCode;
        }

        public String getSignatureTime() {
            return signatureTime;
        }

        public void setSignatureTime(String signatureTime) {
            this.signatureTime = signatureTime;
        }

        public String getLegalAuthenticatorHSAId() {
            return legalAuthenticatorHSAId;
        }

        public void setLegalAuthenticatorHSAId(String legalAuthenticatorHSAId) {
            this.legalAuthenticatorHSAId = legalAuthenticatorHSAId;
        }

        public String getLegalAuthenticatorName() {
            return legalAuthenticatorName;
        }

        public void setLegalAuthenticatorName(String legalAuthenticatorName) {
            this.legalAuthenticatorName = legalAuthenticatorName;
        }

        public CV getLegalAuthenticatorRoleCode() {
            return legalAuthenticatorRoleCode;
        }

        public void setLegalAuthenticatorRoleCode(CV legalAuthenticatorRoleCode) {
            this.legalAuthenticatorRoleCode = legalAuthenticatorRoleCode;
        }
    }
}

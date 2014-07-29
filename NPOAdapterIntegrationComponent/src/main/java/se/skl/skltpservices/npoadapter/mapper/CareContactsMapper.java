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

import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType;
import se.rivta.clinicalprocess.logistics.logistics.v2.*;
import se.rivta.en13606.ehrextract.v11.*;

import java.util.List;

/**
 * Maps from EHR_EXTRACT (vko v1.1) to RIV GetCareContactsResponseType v2.0. <p>
 *
 * Riv contract spec (TKB): "http://rivta.se/downloads/ServiceContracts_clinicalpocess_logistics_logistics_2.0.0.zip"
 *
 * @author Peter
 */
public class CareContactsMapper {

    /**
     * Maps from EHR_EXTRACT (vko) to GetCareContactsResponseType.
     *
     * @param ehrExtract the EHR_EXTRACT XML Java bean.
     * @return the corresponding {@link se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType} response type
     */
    public GetCareContactsResponseType map(final EHREXTRACT ehrExtract) {

        final GetCareContactsResponseType responseType = new GetCareContactsResponseType();

        final PersonIdType personIdType = new PersonIdType();
        personIdType.setId(ehrExtract.getSubjectOfCare().getExtension());
        personIdType.setType(ehrExtract.getSubjectOfCare().getRoot());

        for (int i = 0; i < ehrExtract.getAllCompositions().size(); i++) {
            final CareContactType contactType = new CareContactType();
            contactType.setCareContactHeader(mapHeader(ehrExtract, i));
            contactType.setCareContactBody(mapBody(ehrExtract, i));
            responseType.getCareContact().add(contactType);
        }

        return responseType;
    }

    /**
     * Maps contact header information.
     *
     * @param ehrExtract the extract.
     * @param compositionIndex the actual composition in the list.
     * @return the target header information.
     */
    protected PatientSummaryHeaderType mapHeader(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);

        final PatientSummaryHeaderType headerType = new PatientSummaryHeaderType();

        headerType.setDocumentId(composition.getRcId().getExtension());
        headerType.setSourceSystemHSAId(ehrExtract.getEhrSystem().getExtension());
        headerType.setPatientId(mapPersonId(ehrExtract.getSubjectOfCare()));

        headerType.setAccountableHealthcareProfessional(mapProfessional(composition, ehrExtract.getDemographicExtract()));

        // Missing input data (default values have to be used)
        headerType.setApprovedForPatient(true);
        headerType.setNullified(false);
        headerType.setNullifiedReason(null);

        return headerType;

    }


    /**
     * Maps contact body information.
     *
     * @param ehrExtract the extract to map from.
     * @param compositionIndex the actual composition in the list.
     * @return the target body information.
     */
    protected CareContactBodyType mapBody(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);

        final CareContactBodyType bodyType = new CareContactBodyType();

        for (final CONTENT content : composition.getContent()) {
            for (final ITEM item : ((ENTRY) content).getItems()) {
                final String meaningCode = item.getMeaning().getCode();

                if ("vko-vko-typ".equals(meaningCode)) {
                    bodyType.setCareContactCode(ContactCodes.map.rget(getSTValue((ELEMENT) item)));
                } else if ("vko-vko-ors".equals(meaningCode)) {
                    bodyType.setCareContactReason(getSTValue((ELEMENT) item));
                } else if ("vko-vko-sta".equals(meaningCode)) {
                    bodyType.setCareContactStatus(ContactStatus.map.rget(getSTValue((ELEMENT) item)));
                }

                // Executing unit
                for (final FUNCTIONALROLE role : composition.getOtherParticipations()) {
                    if ("ute".equals(getCDCode(role.getFunction()))) {
                        final String hsaId = role.getPerformer().getExtension();
                        bodyType.setCareContactOrgUnit(mapOrgUnit(ehrExtract.getDemographicExtract(), hsaId));
                    }
                }

                bodyType.setCareContactTimePeriod(mapTimePeriod(composition.getSessionTime()));
            }
        }
        return bodyType;
    }

    /**
     * Maps from {@link IVLTS} to {@link se.rivta.clinicalprocess.logistics.logistics.v2.TimePeriodType}
     *
     * @param sessionTime the source session time.
     * @return the target time period type.
     */
    private TimePeriodType mapTimePeriod(final IVLTS sessionTime) {
        final TimePeriodType timePeriodType = new TimePeriodType();

        timePeriodType.setStart(sessionTime.getLow().getValue());
        timePeriodType.setEnd(sessionTime.getHigh().getValue());

        return timePeriodType;
    }

    //
    protected String getSTValue(final ELEMENT element) {
        final ST st = (ST) element.getValue();
        return (st == null) ? null : st.getValue();
    }

    //
    protected PersonIdType mapPersonId(final II subjectOfCare) {
        final PersonIdType personIdType = new PersonIdType();
        personIdType.setId(subjectOfCare.getExtension());
        personIdType.setType(subjectOfCare.getRoot());
        return personIdType;
    }

    //
    protected <T> T firstItem(List<T> list) {
        return (list.size() == 0) ? null : list.get(0);
    }

    //
    protected String getCDCode(final CD cd) {
        return (cd == null) ? null : cd.getCode();
    }

    //
    protected String getPartValue(final List<EN> names) {
        final EN item = firstItem(names);
        if (item != null) {
            final ENXP part = firstItem(item.getPart());
            return (part == null) ? null : part.getValue();
        }
        return null;
    }

    //
    protected IDENTIFIEDENTITY lookupDemographicIdentity(final List<IDENTIFIEDENTITY> demographics, final String hsaId) {
        for (final IDENTIFIEDENTITY identifiedentity : demographics) {
            if (hsaId.equals(identifiedentity.getExtractId().getExtension())) {
                return identifiedentity;
            }
        }
        return null;
    }

    //
    protected HealthcareProfessionalType mapProfessional(final COMPOSITION composition, final List<IDENTIFIEDENTITY> demographics) {
        final HealthcareProfessionalType professionalType = new HealthcareProfessionalType();
        professionalType.setAuthorTime(composition.getCommittal().getTimeCommitted().getValue());
        professionalType.setHealthcareProfessionalHSAId(composition.getComposer().getPerformer().getExtension());

        final IDENTIFIEDHEALTHCAREPROFESSIONAL professional = (IDENTIFIEDHEALTHCAREPROFESSIONAL) lookupDemographicIdentity(demographics, professionalType.getHealthcareProfessionalHSAId());
        if (professional != null) {
            professionalType.setHealthcareProfessionalName(getPartValue(professional.getName()));
            final HEALTHCAREPROFESSIONALROLE role = firstItem(professional.getRole());
            if (role != null && role.getProfession() != null) {
                final CVType cvType = new CVType();
                cvType.setCode(getCDCode(role.getProfession()));
                cvType.setCodeSystem(role.getProfession().getCodeSystem());
                cvType.setDisplayName(role.getProfession().getDisplayName().getValue());
                professionalType.setHealthcareProfessionalRoleCode(cvType);
            }
        }

        professionalType.setHealthcareProfessionalOrgUnit(mapOrgUnit(demographics, composition.getComposer().getHealthcareFacility().getExtension()));

        return professionalType;
    }

    //
    protected OrgUnitType mapTel(final OrgUnitType orgUnitType, final ORGANISATION organisation) {
        for (final TEL item : organisation.getTelecom()) {
            if (item instanceof TELEMAIL) {
                orgUnitType.setOrgUnitEmail(item.getValue());
            } else if (item instanceof TELPHONE) {
                orgUnitType.setOrgUnitTelecom(item.getValue());
            }
        }
        return orgUnitType;
    }

    //
    protected OrgUnitType mapAddress(final OrgUnitType orgUnitType, final ORGANISATION organisation) {
        for (final AD ad : organisation.getAddr())
            for (final ADXP adxp : ad.getPartOrBrOrAddressLine()) {
                switch (adxp.getType()) {
                    case AL:
                        orgUnitType.setOrgUnitAddress(adxp.getContent());
                        break;
                    case CEN:
                        orgUnitType.setOrgUnitLocation(adxp.getContent());
                        break;
                    default:
                        break;
                }
            }
        return orgUnitType;
    }

    //
    protected OrgUnitType mapOrgUnit(final List<IDENTIFIEDENTITY> demographics, final String hsaId) {
        final OrgUnitType orgUnitType = new OrgUnitType();
        orgUnitType.setOrgUnitHSAId(hsaId);

        final ORGANISATION organisation = (ORGANISATION) lookupDemographicIdentity(demographics, hsaId);
        if (organisation != null) {
            orgUnitType.setOrgUnitName(organisation.getName().getValue());
            mapTel(orgUnitType, organisation);
            mapAddress(orgUnitType, organisation);
        }
        return orgUnitType;
    }
}

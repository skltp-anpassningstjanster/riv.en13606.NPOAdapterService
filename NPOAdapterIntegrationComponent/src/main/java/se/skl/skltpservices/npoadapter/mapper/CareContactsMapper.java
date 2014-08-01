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

import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsType;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.ObjectFactory;
import se.rivta.clinicalprocess.logistics.logistics.v2.*;
import se.rivta.en13606.ehrextract.v11.*;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;
import java.util.List;

/**
 * Maps from EHR_EXTRACT (vko v1.1) to RIV GetCareContactsResponseType v2.0. <p>
 *
 * Riv contract spec (TKB): "http://rivta.se/downloads/ServiceContracts_clinicalpocess_logistics_logistics_2.0.0.zip"
 *
 * @author Peter
 */
public class CareContactsMapper extends AbstractMapper implements Mapper {

    public static final CD MEANING_VKO = new CD();
    static {
        MEANING_VKO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VKO.setCode("vko");
    }

    private static final JaxbUtil jaxbRequest = new JaxbUtil(GetCareContactsType.class);
    private static final JaxbUtil jaxbResponse = new JaxbUtil(GetCareContactsType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();



    @Override
    public String mapResponse(XMLStreamReader reader) {
        final RIV13606REQUESTEHREXTRACTResponseType response = unmarshalEHRResponse(reader);
        return marshal(map(response.getEhrExtract().get(0)));
    }

    @Override
    public String mapRequest(XMLStreamReader reader) {
        final GetCareContactsType request = unmarshal(reader);
        return marshalEHRRequest(map(request));
    }



    /**
     * Maps from EHR_EXTRACT (vko) to GetCareContactsResponseType.
     *
     * @param ehrExtract the EHR_EXTRACT XML Java bean.
     * @return the corresponding {@link se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType} response type
     */
    protected GetCareContactsResponseType map(final EHREXTRACT ehrExtract) {

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
     * Maps from GetCareContacsRequestType to EHR_EXTRACT request.
     */
    public RIV13606REQUESTEHREXTRACTRequestType map(final GetCareContactsType careContactsType) {
        final RIV13606REQUESTEHREXTRACTRequestType targetRequest = new RIV13606REQUESTEHREXTRACTRequestType();
        targetRequest.setMaxRecords(map(500));
        targetRequest.setSubjectOfCareId(map(careContactsType.getPatientId()));
        targetRequest.getMeanings().add(MEANING_VKO);
        targetRequest.setTimePeriod(map(careContactsType.getTimePeriod()));
        // FIXME: get real hsa_id
        targetRequest.getParameters().add(createParameter("hsa_id", "DUMMY-TEST"));
        return targetRequest;
    }

    //
    protected GetCareContactsType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetCareContactsType) jaxbRequest.unmarshal(reader);
        } finally {
            close(reader);
        }
    }


    protected String marshal(final GetCareContactsResponseType response) {
        final JAXBElement<GetCareContactsResponseType> el = objectFactory.createGetCareContactsResponse(response);
        return jaxbResponse.marshal(el);
    }


    protected ParameterType createParameter(String name, String value) {
        assert (name != null) && (value != null);
        final ParameterType parameterType = new ParameterType();
        parameterType.setName(createST(name));
        parameterType.setValue(createST(value));
        return parameterType;
    }

    protected IVLTS map(final DatePeriodType datePeriodType) {
        if (datePeriodType == null) {
            return null;
        }
        final IVLTS value = new IVLTS();
        value.setLow(createTS(datePeriodType.getStart()));
        value.setHigh(createTS(datePeriodType.getEnd()));
        return value;
    }

    protected ST createST(final String value) {
        if (value == null) {
            return null;
        }
        final ST st = new ST();
        st.setValue(value);
        return st;
    }

    protected TS createTS(final String value) {
        if (value == null) {
            return null;
        }
        final TS ts = new TS();
        ts.setValue(value);
        return ts;
    }

    protected INT map(final int n) {
        final INT value = new INT();
        value.setValue(n);
        return value;
    }

    protected II map(final PersonIdType personIdType) {
        final II value= new II();
        value.setRoot(personIdType.getType());
        value.setExtension(personIdType.getId());
        return value;
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

        // FIXME: Missing input data (default values have to be used, validation is required)
        headerType.setApprovedForPatient(true);
        headerType.setNullified(false);
        headerType.setNullifiedReason(null);

        return headerType;

    }


    /**
     * Maps contact body information.
     *
     * @param ehrExtract the extract to createTS from.
     * @param compositionIndex the actual composition in the list.
     * @return the target body information.
     */
    protected CareContactBodyType mapBody(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);

        final CareContactBodyType bodyType = new CareContactBodyType();

        for (final CONTENT content : composition.getContent()) {
            for (final ITEM item : ((ENTRY) content).getItems()) {
                switch (item.getMeaning().getCode()) {
                    case "vko-vko-typ":
                        bodyType.setCareContactCode(ContactCodes.map.code(getSTValue((ELEMENT) item)));
                        break;
                    case "vko-vko-ors":
                        bodyType.setCareContactReason(getSTValue((ELEMENT) item));
                        break;
                    case "vko-vko-sta":
                        bodyType.setCareContactStatus(ContactStatus.map.code(getSTValue((ELEMENT) item)));
                        break;
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

        // FIXME: Missing HSAIds have to be validated!
        // professionalType.setHealthcareProfessionalCareGiverHSAId();
        // professionalType.setHealthcareProfessionalCareUnitHSAId();

        professionalType.setHealthcareProfessionalOrgUnit(mapOrgUnit(demographics, composition.getComposer().getHealthcareFacility().getExtension()));

        return professionalType;
    }

    /**
     * Removes a string prefix on match.
     *
     * @param value the string.
     * @param prefix the prefix to remove.
     * @return the string without prefix, i.e. unchanged if the prefix doesn't match.
     */
    protected String removePrefix(final String value, final String prefix) {
        return (value == null) ? null : value.replaceFirst(prefix, "");
    }

    //
    protected OrgUnitType mapTel(final OrgUnitType orgUnitType, final ORGANISATION organisation) {
        for (final TEL item : organisation.getTelecom()) {
            if (item instanceof TELEMAIL) {
                orgUnitType.setOrgUnitEmail(removePrefix(item.getValue(), "mailto:"));
            } else if (item instanceof TELPHONE) {
                orgUnitType.setOrgUnitTelecom(removePrefix(item.getValue(), "tel:"));
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

    /**
     * Contact codes.
     *
     * @author Peter
     */
    public static class ContactCodes extends AbstarctCodeMapper<Integer, String> {
        public static ContactCodes map = new ContactCodes();

        static {
            map.add(1, "Besök");
            map.add(2, "Telefon");
            map.add(3, "Vårdtillfälle");
            map.add(4, "Dagsjukvård");
            map.add(5, "Annan");
        }

        public String text(final Integer key) {
            return super.value(key, "Annan");
        }

        public Integer code(final String key) {
            return super.key(key, 5);
        }
    }

    /**
     * Contact status.
     *
     * @author Peter
     */
    public static class ContactStatus extends AbstarctCodeMapper<Integer, String> {
        public static ContactStatus map = new ContactStatus();

        static {
            map.add(1, "Ej påbörjad");
            map.add(2, "Inställd");
            map.add(3, "Pågående");
            map.add(4, "Avbruten");
            map.add(5, "Avslutad");
        }

        public String text(final Integer key) {
            return super.value(key, "Ej påbörjad");
        }

        public Integer code(final String key) {
            return super.key(key, 1);
        }
    }
}

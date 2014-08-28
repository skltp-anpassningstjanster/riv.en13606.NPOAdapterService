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
import org.mule.util.StringUtils;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.logistics.logistics._2.*;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;

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
@Slf4j
public class CareContactsMapper extends AbstractMapper implements Mapper {

    public static final CD MEANING_VKO = new CD();
    static {
        MEANING_VKO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VKO.setCode("vko");
    }

    private static final JaxbUtil jaxb = new JaxbUtil(GetCareContactsType.class, GetCareContactsResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();


    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType response = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
    		if(response.getEhrExtract().isEmpty()) {
    			throw new MapperException("No EHREXTRACT returned from source system");
    		}
    		message.setPayload(marshal(map(response.getEhrExtract().get(0))));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping EHREXTRACT  response", err);
    	}
    }

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
    	try {
    		final GetCareContactsType request = unmarshal(payloadAsXMLStreamReader(message));
        	message.setPayload(riv13606REQUESTEHREXTRACTRequestType(map(request, message)));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping EHREXTRACT request", err);
    	}
    }



    /**
     * Maps from EHR_EXTRACT (vko) to GetCareContactsResponseType.
     *
     * @param ehrExtract the EHR_EXTRACT XML Java bean.
     * @return the corresponding {@link riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType} response type
     */
    protected GetCareContactsResponseType map(final EHREXTRACT ehrExtract) {

        final GetCareContactsResponseType responseType = new GetCareContactsResponseType();

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
    protected RIV13606REQUESTEHREXTRACTRequestType map(final GetCareContactsType careContactsType, final MuleMessage message) {
        final RIV13606REQUESTEHREXTRACTRequestType targetRequest = new RIV13606REQUESTEHREXTRACTRequestType();
        targetRequest.setMaxRecords(EHRUtil.intType(maxEhrExtractRecords(message)));
        targetRequest.setSubjectOfCareId(map(careContactsType.getPatientId()));
        targetRequest.getMeanings().add(MEANING_VKO);
        targetRequest.setTimePeriod(EHRUtil.IVLTSType(careContactsType.getTimePeriod()));
        final List<String> ids = careContactsType.getCareUnitHSAId();
        if (ids.size() == 1) {
            targetRequest.getParameters().add(EHRUtil.createParameter("hsa_id", EHRUtil.firstItem(ids)));
        } else if (ids.size() > 1) {
            throw new IllegalArgumentException("Request includes several care units (HSAId search criteria), but only 1 is allowed by the source system: " + ids);
        }

        targetRequest.getParameters().add(EHRUtil.createParameter("version", "1.1"));
        return targetRequest;
    }

    //
    protected GetCareContactsType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetCareContactsType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }


    protected String marshal(final GetCareContactsResponseType response) {
        final JAXBElement<GetCareContactsResponseType> el = objectFactory.createGetCareContactsResponse(response);
        return jaxb.marshal(el);
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
        headerType.setPatientId(EHRUtil.personIdType(ehrExtract.getSubjectOfCare(), PersonIdType.class));

        headerType.setAccountableHealthcareProfessional(mapProfessional(composition, ehrExtract.getDemographicExtract()));

        headerType.setApprovedForPatient(false);
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
                        bodyType.setCareContactCode(ContactCodes.map.code(EHRUtil.getElementTextValue((ELEMENT) item)));
                        break;
                    case "vko-vko-ors":
                        bodyType.setCareContactReason(EHRUtil.getElementTextValue((ELEMENT) item));
                        break;
                    case "vko-vko-sta":
                        bodyType.setCareContactStatus(ContactStatus.map.code(EHRUtil.getElementTextValue((ELEMENT) item)));
                        break;
                }

                // Executing unit
                for (final FUNCTIONALROLE role : composition.getOtherParticipations()) {
                    if ("ute".equals(EHRUtil.getCDCode(role.getFunction()))) {
                        final String hsaId = role.getPerformer().getExtension();
                        bodyType.setCareContactOrgUnit(mapOrgUnit(ehrExtract.getDemographicExtract(), hsaId));
                    }
                }

                bodyType.setCareContactTimePeriod(EHRUtil.datePeriod(composition.getSessionTime(), TimePeriodType.class));
            }
        }
        return bodyType;
    }
    
    protected II map(final PersonIdType personIdType) {
        final II value= new II();
        value.setRoot(personIdType.getType());
        value.setExtension(personIdType.getId());
        return value;
    }


    //
    protected HealthcareProfessionalType mapProfessional(final COMPOSITION composition, final List<IDENTIFIEDENTITY> demographics) {
        final HealthcareProfessionalType professionalType = new HealthcareProfessionalType();
        professionalType.setAuthorTime(composition.getCommittal().getTimeCommitted().getValue());
        professionalType.setHealthcareProfessionalHSAId(composition.getComposer().getPerformer().getExtension());

        final IDENTIFIEDHEALTHCAREPROFESSIONAL professional = (IDENTIFIEDHEALTHCAREPROFESSIONAL) EHRUtil.lookupDemographicIdentity(demographics, professionalType.getHealthcareProfessionalHSAId());
        if (professional != null) {
            professionalType.setHealthcareProfessionalName(EHRUtil.getPartValue(professional.getName()));
            final HEALTHCAREPROFESSIONALROLE role = EHRUtil.firstItem(professional.getRole());
            if (role != null) {
               final CVType cvType = EHRUtil.cvType(role.getProfession(), CVType.class);
                professionalType.setHealthcareProfessionalRoleCode(cvType);
            }
        }

        for (final FUNCTIONALROLE careGiver : composition.getOtherParticipations()) {
			if (careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), "iag")) {
				if (careGiver.getPerformer() != null) {
					professionalType.setHealthcareProfessionalCareGiverHSAId(careGiver.getPerformer().getExtension());
				}
				if (careGiver.getHealthcareFacility() != null) {
					professionalType.setHealthcareProfessionalCareUnitHSAId(careGiver.getHealthcareFacility().getExtension());
				}
			}
		}

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

        final ORGANISATION organisation = (ORGANISATION) EHRUtil.lookupDemographicIdentity(demographics, hsaId);
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
    public static class ContactCodes extends AbstractCodeMapper<Integer, String> {
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
    public static class ContactStatus extends AbstractCodeMapper<Integer, String> {
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

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
import org.mule.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.logistics.logistics._2.*;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsType;
import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

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

    protected static final Logger log = LoggerFactory.getLogger(CareContactsMapper.class);
    
    public static final CD MEANING_VKO = new CD();
    static {
        MEANING_VKO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VKO.setCode(INFO_VKO);
    }

    private static final JaxbUtil jaxb = new JaxbUtil(GetCareContactsType.class, GetCareContactsResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();

    public CareContactsMapper() {
        schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-CARECONTACTS"));
        log.debug("schema validation is activated? " + schemaValidationActivated);
        
        initialiseValidator("/core_components/clinicalprocess_logistics_logistics_enum_2.0.xsd", 
                            "/core_components/clinicalprocess_logistics_logistics_2.0.xsd",
                            "/interactions/GetCareContactsInteraction/GetCareContactsResponder_2.0.xsd");
    }


    
    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        try {
            final GetCareContactsType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_VKO, message.getUniqueId(), message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }


    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType response = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
            checkContinuation(log, response);
            message.setPayload(marshal(mapResponse(response, message)));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
    	}
    }


    /**
     * Maps from EHR_EXTRACT (vko) to GetCareContactsResponseType.
     *
     * @param response the EHR_EXTRACT XML Java bean.
     * @return the corresponding riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsResponseType response type
     */
    protected GetCareContactsResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType response, MuleMessage message) {
        checkContinuation(log, response);

        final GetCareContactsResponseType responseType = new GetCareContactsResponseType();

        if (!response.getEhrExtract().isEmpty()) {
            
            List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);
            
            final EHREXTRACT ehrExtract = response.getEhrExtract().get(0);
            for (int i = 0; i < ehrExtract.getAllCompositions().size(); i++) {
                log.debug("processing {} of {}",i+1, ehrExtract.getAllCompositions().size());
                if (EHRUtil.retain(ehrExtract.getAllCompositions().get(i), careUnitHsaIds, log)) {
                    final CareContactType contactType = new CareContactType();
                    contactType.setCareContactHeader(mapHeader(ehrExtract, i));
                    contactType.setCareContactBody(mapBody(ehrExtract, i));
                    responseType.getCareContact().add(contactType);
                } else {
                    log.debug("Not retaining composition {}", ehrExtract.getRmId());
                }
            }
        }

        return responseType;
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
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, schemaValidator, log);
        return xml;
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

                // care contact org unit - retrieve data from demographic_extract for the hsaId for "utförande enhet"
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
                final CVType cvType = EHRUtil.cvTypeFromCD(role.getProfession(), CVType.class);
                professionalType.setHealthcareProfessionalRoleCode(cvType);
            }
        }

        for (final FUNCTIONALROLE careGiver : composition.getOtherParticipations()) {
			if (careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), "iag")) {
				if (careGiver.getPerformer() != null) {
					professionalType.setHealthcareProfessionalCareUnitHSAId(careGiver.getPerformer().getExtension());
				}
				if (careGiver.getHealthcareFacility() != null) {
					professionalType.setHealthcareProfessionalCareGiverHSAId(careGiver.getHealthcareFacility().getExtension());
				}
			}
		}

        professionalType.setHealthcareProfessionalOrgUnit(mapOrgUnit(demographics, composition.getComposer().getHealthcareFacility().getExtension()));

        return professionalType;
    }

    //
    protected OrgUnitType mapTel(final OrgUnitType orgUnitType, final ORGANISATION organisation) {

        for (final TEL item : organisation.getTelecom()) {
            if (item instanceof TELEMAIL) {
                orgUnitType.setOrgUnitEmail(EHRUtil.removePrefix(item.getValue(), "mailto:"));
            } else if (item instanceof TELPHONE) {
                orgUnitType.setOrgUnitTelecom(EHRUtil.removePrefix(item.getValue(), "tel:"));
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

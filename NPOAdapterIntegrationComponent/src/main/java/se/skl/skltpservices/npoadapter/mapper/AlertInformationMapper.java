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

import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.description._2.AlertInformationBodyType;
import riv.clinicalprocess.healthcond.description._2.AlertInformationType;
import riv.clinicalprocess.healthcond.description._2.CVType;
import riv.clinicalprocess.healthcond.description._2.CommunicableDiseaseType;
import riv.clinicalprocess.healthcond.description._2.HyperSensitivityType;
import riv.clinicalprocess.healthcond.description._2.OtherHypersensitivityType;
import riv.clinicalprocess.healthcond.description._2.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.description._2.PharmaceuticalHypersensitivityType;
import riv.clinicalprocess.healthcond.description._2.RelatedAlertInformationType;
import riv.clinicalprocess.healthcond.description._2.RestrictionOfCareType;
import riv.clinicalprocess.healthcond.description._2.ResultType;
import riv.clinicalprocess.healthcond.description._2.SeriousDiseaseType;
import riv.clinicalprocess.healthcond.description._2.TimePeriodType;
import riv.clinicalprocess.healthcond.description._2.TreatmentType;
import riv.clinicalprocess.healthcond.description._2.UnstructuredAlertInformationType;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationResponseType;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.GetAlertInformationType;
import riv.clinicalprocess.healthcond.description.getalertinformationresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.LINK;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

/**
 * Mapper for AlertInformationInteraction from 13606 upp.
 * 
 * @author torbjorncla
 */
public class AlertInformationMapper extends AbstractMapper implements Mapper {

    protected static final Logger log = LoggerFactory.getLogger(AlertInformationMapper.class);
    
    private static final JaxbUtil jaxb = new JaxbUtil(GetAlertInformationType.class);
    private static final ObjectFactory objFactory = new ObjectFactory();

    public static final CD MEANING_UPP = new CD();
    static {
        MEANING_UPP.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_UPP.setCode(INFO_UPP);
    }

    /**
     * UPP Informationsobjekt
     */
    private final static String INAKTUELL_TIDPUNKT = "upp-upp-itp";
    private final static String KOM_INAKTUELL_TIDPUNKT = "upp-upp-kin";
    private final static String KONST_DATUM = "upp-upp-kdt";
    private final static String VERIFIERAD_TIDPUNKT = "upp-upp-vtp";
    private final static String GILTLIGHETSTID = "upp-upp-ght";
    private final static String KOM_UPPMARKSAMHET = "upp-upp-kom";
    private final static String KOM_SAMBAND = "upp-upp-ksb";

    private final static String UPPMARKSAMMAD_OVERKANSLIGHET = "upp-okh";
    private final static String TYP_AV_OVERKANSLIGHET = "upp-okh-typ";
    private final static String ALLVARLIGHETSGRAD = "upp-okh-avg";
    private final static String VISSHETSGRAD = "upp-okh-vhg";
    private final static String LAKEMEDEL_OVERKANSLIGHET = "upp-okh-lmo";
    private final static String SUBSTANS = "upp-okh-lmo-sub";
    private final static String SUBSTANS_EJ_ATC = "upp-okh-lmo-sea";
    private final static String EJ_ATC_KOM = "upp-okh-lmo-eak";
    private final static String LAKEMDELELPRODUKT = "upp-okh-lmo-lmp";
    private final static String ANNAN_OVERKANSLIGHET = "upp-okh-aok";
    private final static String AGENS_OVERKANSLIGHET = "upp-okh-aok-age";
    private final static String AGENS_OVERKANSLIGHET_KOD = "upp-okh-aok-agk";

    private final static String UPPMARKSAMMAD_ALVARLIG_SJUKDOM = "upp-uas";
    private final static String SJUKDOM = "upp-uas-sjd";

    private final static String UPPMARKSAMMAD_BEHANLDING = "upp-ube";
    private final static String BEHANDLING = "upp-ube-beh";
    private final static String LAKEMDELSBEHANDLIG = "upp-ube-lbe";
    private final static String BEHANDLINGSKOD = "upp-ube-kod";

    private final static String UPPMARKSAMMAD_VARDBEGRANSNING = "upp-vbe";
    private final static String VARDBEGRANSINING = "upp-vbe-vbe";

    private final static String UPPMARKSAMMAD_EJ_STRUKTURERAD_VARNING = "upp-est";
    private final static String EJ_STRUKTURERAD_VARNING_RUBRIK = "upp-est-rub";
    private final static String EJ_STRUKTURERAD_VARNING_INNEHALL = "upp-est-inh";

    private final static String UPPMARKSAMMAD_ARBETSMILJORISK = "upp-arb";
    @SuppressWarnings("unused")
    private final static String SMITTFORANDE = "upp-arb-smf";
    private final static String SMITTVAG = "upp-arb-smf-vag";
    private final static String SMITTSAM_SJUKDOM = "upp-arb-smf-sjd";

    public AlertInformationMapper() {
        schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-ALERTINFORMATION"));
        log.debug("schema validation is activated? " + schemaValidationActivated);
        
        initialiseValidator("/core_components/clinicalprocess_healthcond_description_enum_2.1.xsd",
                            "/core_components/clinicalprocess_healthcond_description_2.1.xsd",
                            "/interactions/GetAlertInformationInteraction/GetAlertInformationResponder_2.0.xsd");
    }

    
    @Override
    public MuleMessage mapRequest(MuleMessage message) throws MapperException {
        try {
            final GetAlertInformationType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_UPP, message.getUniqueId(),
                    message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    
    @Override
    public MuleMessage mapResponse(MuleMessage message) throws MapperException {
        try {
            final RIV13606REQUESTEHREXTRACTResponseType ehrResp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
            final GetAlertInformationResponseType response = mapResponse(ehrResp, message);
            message.setPayload(marshal(response));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
        }
    }


    protected String marshal(final GetAlertInformationResponseType resp) {
        final JAXBElement<GetAlertInformationResponseType> el = objFactory.createGetAlertInformationResponse(resp);
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, log);
        return xml;
    }

    protected GetAlertInformationType unmarshal(final XMLStreamReader reader) {
        try {
            return (GetAlertInformationType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    /**
     * Create response.
     * 
     * @param ehrResp
     *            response to be loaded into soap-payload.
     * @param uniqueId
     *            mule-message uniqueId.
     * @return a alertinformation response.
     */
    protected GetAlertInformationResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, MuleMessage message) {
        checkContinuation(log, ehrResp);
        final GetAlertInformationResponseType response = new GetAlertInformationResponseType();

        response.setResult(EHRUtil.resultType(message.getUniqueId(), ehrResp.getResponseDetail(), ResultType.class));
        if (ehrResp.getEhrExtract().isEmpty()) {
            return response;
        }

        final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);

        List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);

        /**
         * TODO: This might need to be change to be able to handle references to other upp-objects.
         */
        for (COMPOSITION composition13606 : ehrExtract.getAllCompositions()) {

            if (EHRUtil.retain(composition13606, careUnitHsaIds, log)) {
                final AlertInformationType alertInformation = new AlertInformationType();

                alertInformation.setAlertInformationHeader(EHRUtil.patientSummaryHeader(composition13606, sharedHeaderExtract, null,
                        PatientSummaryHeaderType.class, false, false, true));
                alertInformation.setAlertInformationBody(mapBodyType(composition13606));
                response.getAlertInformation().add(alertInformation);
            }
        }
        return response;
    }

    /**
     * Maps AlertInformationBodyType per composition.
     * 
     * @param comp
     *            , entity from all_compositions.
     * @return AlertInformationBodyType-element JAXB entity.
     */
    protected AlertInformationBodyType mapBodyType(final COMPOSITION comp) {
        final AlertInformationBodyType type = new AlertInformationBodyType();
        addReleatedAlertInformation(comp, type);
        for (CONTENT content : comp.getContent()) {
            if (content instanceof ENTRY) {
                final ENTRY entry = (ENTRY) content;
                type.setTypeOfAlertInformation(EHRUtil.cvTypeFromCD(entry.getMeaning(), CVType.class));
                for (ITEM item : entry.getItems()) {
                    final String meaning = EHRUtil.getCDCode(item.getMeaning());
                    switch (meaning) {
                    case KONST_DATUM:
                        if (item instanceof ELEMENT) {
                            setAscertainedDate(type, EHRUtil.getElementTimeValue((ELEMENT) item));
                        }
                        break;
                    case VERIFIERAD_TIDPUNKT:
                        if (item instanceof ELEMENT) {
                            type.setVerifiedTime(EHRUtil.getElementTimeValue((ELEMENT) item));
                        }
                        break;
                    case GILTLIGHETSTID:
                        if (item instanceof ELEMENT) {
                            final ELEMENT elm = (ELEMENT) item;
                            if (elm.getValue() instanceof IVLTS) {
                                type.setValidityTimePeriod(EHRUtil.timePeriod((IVLTS) elm.getValue(), TimePeriodType.class));
                                if (StringUtils.isBlank(type.getAscertainedDate())) {
                                    setAscertainedDate(type, type.getValidityTimePeriod().getStart()); // default
                                }
                            }
                        }
                        break;
                    case KOM_UPPMARKSAMHET:
                        if (item instanceof ELEMENT) {
                            type.setAlertInformationComment(EHRUtil.getElementTextValue((ELEMENT) item));
                        }
                        break;
                    case KOM_SAMBAND:
                        if (item instanceof ELEMENT) {
                            for (RelatedAlertInformationType rai : type.getRelatedAlertInformation()) {
                                rai.setRelationComment(EHRUtil.getElementTextValue((ELEMENT) item));
                            }
                        }
                        break;
                    case INAKTUELL_TIDPUNKT:
                        if (item instanceof ELEMENT) {
                            type.setObsoleteTime(EHRUtil.getElementTimeValue((ELEMENT) item));
                        }
                        break;
                    case KOM_INAKTUELL_TIDPUNKT:
                        if (item instanceof ELEMENT) {
                            type.setObsoleteComment(EHRUtil.getElementTimeValue((ELEMENT) item));
                        }
                        break;
                    default:
                        if (type.getTypeOfAlertInformation() != null) {
                            switch (type.getTypeOfAlertInformation().getCode()) {
                            case UPPMARKSAMMAD_OVERKANSLIGHET:
                                mapHypersensitivity(type, item, meaning);
                                break;
                            case UPPMARKSAMMAD_ALVARLIG_SJUKDOM:
                                mapSeriousDisease(type, item, meaning);
                                break;
                            case UPPMARKSAMMAD_BEHANLDING:
                                mapTreatment(type, item, meaning);
                                break;
                            case UPPMARKSAMMAD_ARBETSMILJORISK:
                                mapCommunicableDisease(type, item);
                                break;
                            case UPPMARKSAMMAD_VARDBEGRANSNING:
                                mapRestrictionOfCare(type, item);
                                break;
                            case UPPMARKSAMMAD_EJ_STRUKTURERAD_VARNING:
                                mapUnstructuredAlertInformation(type, item);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
        return type;
    }

    private void setAscertainedDate(AlertInformationBodyType type, String date) {
        type.setAscertainedDate(date);
        if (StringUtils.isNotBlank(type.getAscertainedDate())) {
            // SERVICE-323
            if (type.getAscertainedDate().length() > "yyyyMMdd".length()) {
                type.setAscertainedDate(type.getAscertainedDate().substring(0, "yyyyMMdd".length()));
            }
        }
    }


    protected void addReleatedAlertInformation(final COMPOSITION comp, final AlertInformationBodyType body) {
        for (LINK link : comp.getLinks()) {
            final RelatedAlertInformationType type = new RelatedAlertInformationType();
            type.setTypeOfAlertInformationRelationship(EHRUtil.cvTypeFromCD(link.getRole(), CVType.class));
            switch (type.getTypeOfAlertInformationRelationship().getCode()) {
            case "ers" : type.getTypeOfAlertInformationRelationship().setDisplayName("Ersätter"); break;
            case "uto" : type.getTypeOfAlertInformationRelationship().setDisplayName("Utökar"); break;
            default : log.error("Unrecognised role code {} - unable to set displayName", 
                    type.getTypeOfAlertInformationRelationship().getCode()); break;            
            }
            for (II id : link.getTargetId()) {
                type.getDocumentId().add(id.getExtension());
            }
            body.getRelatedAlertInformation().add(type);
        }
    }

    protected void mapUnstructuredAlertInformation(final AlertInformationBodyType bodyType, final ITEM item) {
        if (bodyType.getUnstructuredAlertInformation() == null) {
            bodyType.setUnstructuredAlertInformation(new UnstructuredAlertInformationType());
        }
        if (item instanceof ELEMENT && item.getMeaning() != null) {
            final ELEMENT elm = (ELEMENT) item;
            switch (elm.getMeaning().getCode()) {
            case EJ_STRUKTURERAD_VARNING_RUBRIK:
                bodyType.getUnstructuredAlertInformation().setUnstructuredAlertInformationHeading(EHRUtil.getElementTextValue(elm));
                break;
            case EJ_STRUKTURERAD_VARNING_INNEHALL:
                bodyType.getUnstructuredAlertInformation().setUnstructuredAlertInformationContent(EHRUtil.getElementTextValue(elm));
                break;
            }
        }
    }

    /**
     * Maps restrictionOfCare element.
     * 
     * @param bodyType
     *            AlertInformationBodyType to append restrictionOfCare entity too.
     * @param item
     *            item from Entry-list in composition.
     */
    protected void mapRestrictionOfCare(final AlertInformationBodyType bodyType, final ITEM item) {
        if (bodyType.getRestrictionOfCare() == null) {
            bodyType.setRestrictionOfCare(new RestrictionOfCareType());
        }
        if (item instanceof ELEMENT && item.getMeaning() != null) {
            final ELEMENT elm = (ELEMENT) item;
            switch (elm.getMeaning().getCode()) {
            case VARDBEGRANSINING:
                bodyType.getRestrictionOfCare().setRestrictionOfCareComment(EHRUtil.getElementTextValue(elm));
                break;
            }
        }
    }

    /**
     * Maps CommuncicalbeDisease element.
     * 
     * @param bodyType
     *            AlertInformationBodyType to append communcialDiseas entity too.
     * @param item
     *            item from Entry-list in composition.
     * @param meaning
     *            OID-code.
     */
    protected void mapCommunicableDisease(final AlertInformationBodyType bodyType, final ITEM item) {
        if (item instanceof CLUSTER) {
            if (bodyType.getCommunicableDisease() == null) {
                bodyType.setCommunicableDisease(new CommunicableDiseaseType());
            }
            final CLUSTER cluster = (CLUSTER) item;
            for (ITEM innerItem : cluster.getParts()) {
                if (innerItem instanceof ELEMENT && innerItem.getMeaning() != null) {
                    final ELEMENT elm = (ELEMENT) innerItem;
                    switch (innerItem.getMeaning().getCode()) {
                    case SMITTSAM_SJUKDOM:
                        bodyType.getCommunicableDisease().setCommunicableDiseaseCode(EHRUtil.cvTypeFromElementWithValueST(elm, CVType.class));
                        break;
                    case SMITTVAG:
                        bodyType.getCommunicableDisease().setRouteOfTransmission(EHRUtil.cvTypeFromElementWithValueST(elm, CVType.class));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Maps Treatment element.
     * 
     * @param bodyType
     *            AlertInformationBodyType to append treatement entity too.
     * @param item
     *            item from Entry-list in composition.
     * @param meaning
     *            OID-code.
     */
    protected void mapTreatment(final AlertInformationBodyType bodyType, final ITEM item, final String meaning) {
        if (bodyType.getTreatment() == null) {
            bodyType.setTreatment(new TreatmentType());
        }
        if (item instanceof ELEMENT) {
            switch (meaning) {
            case BEHANDLING:
                bodyType.getTreatment().setTreatmentDescription(EHRUtil.getElementTextValue((ELEMENT) item));
                break;
            case LAKEMDELSBEHANDLIG:
                final ELEMENT elm = (ELEMENT) item;
                if (elm.getValue() != null && elm.getValue() instanceof CD) {
                    bodyType.getTreatment().setPharmaceuticalTreatment(EHRUtil.cvTypeFromCD((CD) elm.getValue(), CVType.class));
                }
                break;
            case BEHANDLINGSKOD:
                bodyType.getTreatment().setTreatmentCode(EHRUtil.cvTypeFromElementWithValueST((ELEMENT) item, CVType.class));
                break;
            }
        }
    }

    /**
     * Maps SeriousDisease element.
     * 
     * @param bodyType
     *            AlertInformationBodyType to append seriousDesease entity too.
     * @param item
     *            item from Entry-list in compositions.
     * @param meaning
     *            OID-code.
     */
    protected void mapSeriousDisease(final AlertInformationBodyType bodyType, final ITEM item, final String meaning) {
        if (bodyType.getSeriousDisease() == null) {
            bodyType.setSeriousDisease(new SeriousDiseaseType());
        }
        switch (meaning) {
        case SJUKDOM:
            if (item instanceof ELEMENT) {
                bodyType.getSeriousDisease().setDisease(EHRUtil.cvTypeFromElementWithValueST((ELEMENT) item, CVType.class));
            }
            break;
        }
    }

    /**
     * Maps Hypersensitivity element.
     * 
     * @param bodyType
     *            AlertInformationBodyType to append hypersensitivity entity too.
     * @param item
     *            item from Entry-list in compositions.
     * @param meaning
     *            OID-code.
     */
    protected void mapHypersensitivity(final AlertInformationBodyType bodyType, final ITEM item, final String meaning) {
        if (bodyType.getHypersensitivity() == null) {
            bodyType.setHypersensitivity(new HyperSensitivityType());
        }
        switch (meaning) {
        case TYP_AV_OVERKANSLIGHET:
            if (item instanceof ELEMENT) {
                bodyType.getHypersensitivity().setTypeOfHypersensitivity(EHRUtil.cvTypeFromElementWithValueST((ELEMENT) item, CVType.class));
            }
            break;
        case ALLVARLIGHETSGRAD:
            if (item instanceof ELEMENT) {
                bodyType.getHypersensitivity().setDegreeOfSeverity(EHRUtil.cvTypeFromElementWithValueST((ELEMENT) item, CVType.class));
            }
            break;
        case VISSHETSGRAD:
            if (item instanceof ELEMENT) {
                bodyType.getHypersensitivity().setDegreeOfCertainty(EHRUtil.cvTypeFromElementWithValueST((ELEMENT) item, CVType.class));
            }
            break;
        case LAKEMEDEL_OVERKANSLIGHET:
            if (item instanceof CLUSTER) {
                bodyType.getHypersensitivity().setPharmaceuticalHypersensitivity(mapPharmaceuticalHypersensitivity((CLUSTER) item));
            }
            break;
        case ANNAN_OVERKANSLIGHET:
            if (item instanceof CLUSTER) {
                bodyType.getHypersensitivity().setOtherHypersensitivity(mapOtherHypersensititivyType((CLUSTER) item));
            }
            break;
        }
    }

    /**
     * Maps PharmaceuticalHypersensitiviy element.
     * 
     * @param cluster
     *            CLUSTER from Items-list.
     * @return JAXB entity for PharmaceuticalHypersensitiviy element.
     */
    protected PharmaceuticalHypersensitivityType mapPharmaceuticalHypersensitivity(final CLUSTER cluster) {
        final PharmaceuticalHypersensitivityType type = new PharmaceuticalHypersensitivityType();
        type.setNonATCSubstanceComment(" "); // default
        for (ITEM innerItem : cluster.getParts()) {
            if (innerItem instanceof ELEMENT) {
                final ELEMENT elm = (ELEMENT) innerItem;
                if (elm.getMeaning() != null) {
                    switch (elm.getMeaning().getCode()) {
                    case SUBSTANS:
                        if (elm.getValue() instanceof CD) {
                            type.setAtcSubstance(EHRUtil.cvTypeFromCD((CD) elm.getValue(), CVType.class));
                        }
                        break;
                    case SUBSTANS_EJ_ATC:
                        if (StringUtils.isNotBlank(EHRUtil.getElementTextValue(elm))) {
                            type.setNonATCSubstance(EHRUtil.getElementTextValue(elm));
                        }
                        break;
                    case EJ_ATC_KOM:
                        type.setNonATCSubstanceComment(EHRUtil.getElementTextValue(elm));
                        break;
                    case LAKEMDELELPRODUKT:
                        type.getPharmaceuticalProductId().add(EHRUtil.cvTypeFromCD((CD) elm.getValue(), CVType.class));
                        break;
                    }
                }
            }
        }
        return type;
    }

    /**
     * Maps OtherHypersensitivity element.
     * 
     * @param cluster
     *            Cluster from Items-list.
     * @return JAXB entity of OtherHypersensitivity element.
     */
    protected OtherHypersensitivityType mapOtherHypersensititivyType(final CLUSTER cluster) {
        final OtherHypersensitivityType type = new OtherHypersensitivityType();
        for (ITEM innerItem : cluster.getParts()) {
            if (innerItem instanceof ELEMENT) {
                final ELEMENT elm = (ELEMENT) innerItem;
                if (elm.getMeaning() != null) {
                    switch (elm.getMeaning().getCode()) {
                    case AGENS_OVERKANSLIGHET:
                        type.setHypersensitivityAgent(EHRUtil.getElementTextValue(elm));
                        break;
                    case AGENS_OVERKANSLIGHET_KOD:
                        type.setHypersensitivityAgentCode(EHRUtil.cvTypeFromElementWithValueST(elm, CVType.class));
                        break;
                    }
                }
            }
        }
        return type;
    }
}

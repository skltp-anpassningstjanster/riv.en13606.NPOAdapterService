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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.actoutcome._3.*;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.ResultCodeEnum;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

/**
 * Maps from EHR_EXTRACT (und-kkm-kli v1.1) to RIV GetLaboratoryOrderOutcomeResponseType
 * <p>
 *
 * @author torbjorncla
 */
public class LaboratoryOrderOutcomeMapper extends AbstractMapper implements Mapper {

    protected static final Logger log = LoggerFactory.getLogger(LaboratoryOrderOutcomeMapper.class);
    
    private static final JaxbUtil jaxb = new JaxbUtil(GetLaboratoryOrderOutcomeType.class);
    private static final ObjectFactory objFactory = new ObjectFactory();

    public static final CD MEANING = new CD();
    static {
        MEANING.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING.setCode(INFO_UND_KKM_KLI);
    }

    public LaboratoryOrderOutcomeMapper() {
        schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-LABORATORYORDEROUTCOME"));
        log.debug("schema validation is activated? " + schemaValidationActivated);
        
        initialiseValidator("/core_components/clinicalprocess_healthcond_actoutcome_enum_3.1.xsd",
                            "/core_components/clinicalprocess_healthcond_actoutcome_3.1_ext.xsd",
                            "/core_components/clinicalprocess_healthcond_actoutcome_3.1.xsd",
                            "/interactions/GetLaboratoryOrderOutcomeInteraction/GetLaboratoryOrderOutcomeResponder_3.1.xsd");
    }

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        try {
            final GetLaboratoryOrderOutcomeType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING, message.getUniqueId(),
                    message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
        try {
            final RIV13606REQUESTEHREXTRACTResponseType ehrResponse = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
            GetLaboratoryOrderOutcomeResponseType resp = mapResponse(ehrResponse, message);
            message.setPayload(marshal(resp));
            return message;
        } catch (Exception err) {
            throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
        }
    }

    protected GetLaboratoryOrderOutcomeResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType response13606, MuleMessage message) {
        final GetLaboratoryOrderOutcomeResponseType responseRivta = new GetLaboratoryOrderOutcomeResponseType();
        responseRivta.setResult(EHRUtil.resultType(message.getUniqueId(), response13606.getResponseDetail(), ResultType.class));
        if (response13606.getEhrExtract().isEmpty()) {
            return responseRivta;
        }

        final EHREXTRACT ehrExtract = response13606.getEhrExtract().get(0);
        SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);

        String sourceSystemHSAId = "";
        if (ehrExtract.getEhrSystem() != null) {
            sourceSystemHSAId = ehrExtract.getEhrSystem().getExtension();
        }

        for (COMPOSITION composition13606 : ehrExtract.getAllCompositions()) {
            if (composition13606.getMeaning() != null) {
                if (StringUtils.equals(composition13606.getMeaning().getCode(), "und")) {
                    if (EHRUtil.retain(composition13606, careUnitHsaIds, log)) {
                        final COMPOSITION und = composition13606;
                        final COMPOSITION vbe = EHRUtil.findCompositionByLink(ehrExtract.getAllCompositions(), EHRUtil.firstItem(und.getContent()).getLinks(), "vbe");
                        final LaboratoryOrderOutcomeType laboratoryOrderOutcome = new LaboratoryOrderOutcomeType();

                        // header
                        laboratoryOrderOutcome.setLaboratoryOrderOutcomeHeader(
                                EHRUtil.patientSummaryHeader(vbe, sharedHeaderExtract, null, PatientSummaryHeaderType.class, false, true, true));
                        laboratoryOrderOutcome.getLaboratoryOrderOutcomeHeader().setSourceSystemHSAId(sourceSystemHSAId);
                        laboratoryOrderOutcome.getLaboratoryOrderOutcomeHeader().setCareContactId(EHRUtil.careContactId(vbe.getLinks()));
                        if (und.getRcId() != null) {
                            laboratoryOrderOutcome.getLaboratoryOrderOutcomeHeader().setDocumentId(und.getRcId().getExtension());
                        }
                        
                         
                        IDENTIFIEDHEALTHCAREPROFESSIONAL firstProfessional = sharedHeaderExtract.getFirstProfessional();    
                        if (firstProfessional != null) {
                            if (firstProfessional.getId() != null && !firstProfessional.getId().isEmpty()) {
                            	laboratoryOrderOutcome.getLaboratoryOrderOutcomeHeader().setLegalAuthenticator(new LegalAuthenticatorType());
                                laboratoryOrderOutcome.getLaboratoryOrderOutcomeHeader().getLegalAuthenticator().setLegalAuthenticatorHSAId(
                                    firstProfessional.getId().get(0).getExtension());
                            }
                            
                            laboratoryOrderOutcome.getLaboratoryOrderOutcomeHeader().getLegalAuthenticator().setLegalAuthenticatorName(
                                EHRUtil.getPartValue(firstProfessional.getName()));
                        }
  
                        // body
                        laboratoryOrderOutcome.setLaboratoryOrderOutcomeBody(mapBodyType(und, vbe, sharedHeaderExtract.healthcareProfessionals(),
                                sharedHeaderExtract.organisations()));

                        responseRivta.getLaboratoryOrderOutcome().add(laboratoryOrderOutcome);
                    }
                }
            }
        }

        if (responseRivta.getResult() == null) {
            final ResultType resultType = new ResultType();
            resultType.setResultCode(ResultCodeEnum.OK);
            resultType.setLogId(message.getUniqueId());
            responseRivta.setResult(resultType);
        }
        return responseRivta;
    }

    // TODO: Refactor, too complex.
    protected LaboratoryOrderOutcomeBodyType mapBodyType(final COMPOSITION und, final COMPOSITION vbe,
            final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps, final Map<String, ORGANISATION> orgs) {
        final LaboratoryOrderOutcomeBodyType type = new LaboratoryOrderOutcomeBodyType();

        // Undersokningsresultat.har ansvarig
        if (und.getComposer() != null) {
            final EHRUtil.HealthcareProfessional hp = EHRUtil.healthcareProfessionalType(und.getComposer(), orgs, hps, und.getCommittal());
            final HealthcareProfessionalType healthcareProfessional = new HealthcareProfessionalType();

            // Vard- och omsorgspersonal.personal-id
            healthcareProfessional.setHealthcareProfessionalHSAId(hp.getHealthcareProfessionalHSAId());
            // Vard- och omsorgspersonal.namn
            healthcareProfessional.setHealthcareProfessionalName(hp.getHealthcareProfessionalName());

            if (hp.getHealthcareProfessionalOrgUnit() != null) {
                final OrgUnitType orgType = new OrgUnitType();
                orgType.setOrgUnitAddress(hp.getHealthcareProfessionalOrgUnit().getOrgUnitAddress());
                orgType.setOrgUnitEmail(hp.getHealthcareProfessionalOrgUnit().getOrgUnitEmail());
                orgType.setOrgUnitHSAId(hp.getHealthcareProfessionalOrgUnit().getOrgUnitHSAId());
                orgType.setOrgUnitLocation(hp.getHealthcareProfessionalOrgUnit().getOrgUnitLocation());
                orgType.setOrgUnitName(hp.getHealthcareProfessionalOrgUnit().getOrgUnitName());
                orgType.setOrgUnitTelecom(hp.getHealthcareProfessionalOrgUnit().getOrgUnitTelecom());
                healthcareProfessional.setHealthcareProfessionalOrgUnit(orgType);
            }

            if (hp.getHealthcareProfessionalRoleCode() != null) {
                final CVType cvType = new CVType();
                cvType.setCode(hp.getHealthcareProfessionalRoleCode().getCode());
                cvType.setCodeSystem(hp.getHealthcareProfessionalRoleCode().getCodeSystem());
                cvType.setCodeSystemName(hp.getHealthcareProfessionalRoleCode().getCodeSystemName());
                cvType.setCodeSystemVersion(hp.getHealthcareProfessionalRoleCode().getCodeSystemVersion());
                cvType.setDisplayName(hp.getHealthcareProfessionalRoleCode().getDisplayName());
                cvType.setOriginalText(hp.getHealthcareProfessionalRoleCode().getOriginalText());
                healthcareProfessional.setHealthcareProfessionalRoleCode(cvType);
            }

            // Overwrite
            for (FUNCTIONALROLE careGiver : und.getOtherParticipations()) {
                if (careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), "iag")) {
                    if (careGiver.getPerformer() != null) {
                        healthcareProfessional.setHealthcareProfessionalCareUnitHSAId(careGiver.getPerformer().getExtension());
                    }
                    if (careGiver.getHealthcareFacility() != null) {
                        healthcareProfessional.setHealthcareProfessionalCareGiverHSAId(careGiver.getHealthcareFacility().getExtension());
                    }
                }
            }

            type.setAccountableHealthcareProfessional(healthcareProfessional);
        }

        if (und.getCommittal() != null) {
            if (und.getCommittal().getTimeCommitted() != null) {
                type.setRegistrationTime(EHRUtil.padTimestampIfNecessary(und.getCommittal().getTimeCommitted().getValue()));
            }
        }

        for (CONTENT content : und.getContent()) {
            if (content instanceof ENTRY) {
                final ENTRY entry = (ENTRY) content;
                for (ITEM item : entry.getItems()) {
                    if (item instanceof CLUSTER) {
                        CLUSTER cluster = (CLUSTER) item;
                        for (ITEM part : cluster.getParts()) {

                            // element
                            if (part instanceof ELEMENT) {
                                final ELEMENT elm = (ELEMENT) part;
                                if (part.getMeaning() != null) {
                                    switch (part.getMeaning().getCode()) {
                                    case "und-und-ure-typ":
                                        type.setResultType(EHRUtil.getElementTextValue(elm));
                                        break;
                                    case "und-kkm-ure-lab":
                                        type.setDiscipline(EHRUtil.getElementTextValue(elm));
                                        if (StringUtils.isBlank(type.getDiscipline())) {
                                            type.setDiscipline("Klinisk kemi"); // default
                                        }
                                        break;
                                    case "und-und-ure-utl":
                                        type.setResultReport(EHRUtil.getElementTextValue(elm));
                                        break;
                                    case "und-kkm-ure-kom":
                                        type.setResultComment(EHRUtil.getElementTextValue(elm));
                                        break;
                                    case "und-und-ure-stp":
                                        type.getAccountableHealthcareProfessional().setAuthorTime(EHRUtil.padTimestampIfNecessary(EHRUtil.getElementTimeValue(elm)));
                                        break;
                                    }
                                }
                            }

                            // cluster
                            if (part instanceof CLUSTER) {
                                final CLUSTER analys = (CLUSTER) part;
                                if (analys.getMeaning() != null) {
                                    switch (analys.getMeaning().getCode()) {
                                    case "und-kkm-uat":
                                        type.getAnalysis().add(mapAnalysis(analys));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        type.setOrder(mapOrder(vbe));
        return type;
    }

    protected OrderType mapOrder(final COMPOSITION vbe) {
        if (vbe == null) {
            return null;
        }
        final OrderType type = new OrderType();
        if (vbe.getRcId() != null) {
            type.setOrderId(vbe.getRcId().getExtension());
        }
        for (CONTENT content : vbe.getContent()) {
            if (content.getMeaning() != null && StringUtils.equalsIgnoreCase(content.getMeaning().getCode(), "vbe-vbe")) {
                if (content instanceof ENTRY) {
                    final ENTRY vbeEntry = (ENTRY) content;
                    for (ITEM vbeItem : vbeEntry.getItems()) {
                        if (vbeItem.getMeaning() != null && StringUtils.equalsIgnoreCase(vbeItem.getMeaning().getCode(), "vbe-vbe-fst")) {
                            if (vbeItem instanceof ELEMENT) {
                                type.setOrderReason(EHRUtil.getElementTextValue((ELEMENT) vbeItem));
                            }
                        }
                    }
                }
            }
        }
        return type;
    }

    //
    protected AnalysisType mapAnalysis(final CLUSTER analys) {
        final AnalysisType type = new AnalysisType();
        type.setAnalysisId(EHRUtil.iiType(analys.getRcId(), IIType.class));
        for (LINK link : analys.getLinks()) {
            if (!link.getTargetId().isEmpty()) {
                final RelationToAnalysisType rel = new RelationToAnalysisType();
                final II ii = link.getTargetId().get(0);
                rel.setAnalysisId(EHRUtil.iiType(ii, IIType.class));
                type.getRelationToAnalysis().add(rel);
            }
        }
        for (ITEM uatItem : analys.getParts()) {
            if (uatItem instanceof ELEMENT) {
                final ELEMENT uatElm = (ELEMENT) uatItem;
                if (uatElm.getMeaning() != null) {
                    switch (uatElm.getMeaning().getCode()) {
                    case "und-kkm-uat-kod":
                        if (uatElm.getObsTime() != null) {
                            type.setAnalysisTime(EHRUtil.datePeriod(uatElm.getObsTime(), TimePeriodType.class));
                            if (uatElm.getValue() instanceof CD) {
                                type.setAnalysisCode(EHRUtil.cvType((CD) uatElm.getValue(), CVType.class));
                            }
                        }
                        break;
                    case "und-kkm-uat-txt":
                        if (uatElm.getObsTime() != null) {
                            type.setAnalysisTime(EHRUtil.datePeriod(uatElm.getObsTime(), TimePeriodType.class));
                            type.setAnalysisText(EHRUtil.getElementTextValue(uatElm));
                        }
                        break;
                    case "und-kkm-uat-sta":
                        type.setAnalysisStatus(EHRUtil.getElementTextValue(uatElm));
                        break;
                    case "und-kkm-uat-kom":
                        type.setAnalysisComment(EHRUtil.getElementTextValue(uatElm));
                        break;
                    case "und-kkm-uat-mat":
                        type.setSpecimen(EHRUtil.getElementTextValue(uatElm));
                        break;
                    case "und-kkm-uat-met":
                        type.setMethod(EHRUtil.getElementTextValue(uatElm));
                        break;
                    }
                }
            }
            if (uatItem instanceof CLUSTER) {
                type.setAnalysisOutcome(mapAnalysisOutcome((CLUSTER) uatItem));
            }
        }
        return type;
    }

    protected AnalysisOutcomeType mapAnalysisOutcome(final CLUSTER utfCluster) {
        final AnalysisOutcomeType type = new AnalysisOutcomeType();
        if (utfCluster.getMeaning() != null && utfCluster.getMeaning().getCode() != null) {
            if (utfCluster.getMeaning().getCode().equalsIgnoreCase("und-kkm-utf")) {
                for (ITEM utfItem : utfCluster.getParts()) {
                    if (utfItem instanceof ELEMENT) {
                        final ELEMENT utfElm = (ELEMENT) utfItem;
                        if (utfElm.getMeaning() != null) {
                            switch (utfElm.getMeaning().getCode()) {
                            case "und-kkm-utf-var":
                                type.setOutcomeValue(EHRUtil.getElementTextValue(utfElm));
                                if (utfElm.getObsTime() != null && utfElm.getObsTime().getLow() != null) {
                                    type.setObservationTime(utfElm.getObsTime().getLow().getValue());
                                }
                                break;
                            case "und-kkm-utf-vae":
                                type.setOutcomeUnit(EHRUtil.getElementTextValue(utfElm));
                                break;
                            case "und-kkm-utf-pat":
                                final Boolean flag = EHRUtil.boolValue(utfElm);
                                if (flag != null) {
                                    type.setPathologicalFlag(flag);
                                }
                                break;
                            case "und-kkm-utf-bes":
                                type.setOutcomeDescription(EHRUtil.getElementTextValue(utfElm));
                                break;
                            case "und-kkm-utf-ref":
                                type.setReferenceInterval(EHRUtil.getElementTextValue(utfElm));
                                break;
                            case "und-kkm-utf-pop":
                                type.setReferencePopulation(EHRUtil.getElementTextValue(utfElm));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return type;
    }

    protected String marshal(final GetLaboratoryOrderOutcomeResponseType response) {
        final JAXBElement<GetLaboratoryOrderOutcomeResponseType> el = objFactory.createGetLaboratoryOrderOutcomeResponse(response);
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, schemaValidator, log);
        return xml;
    }

    protected GetLaboratoryOrderOutcomeType unmarshal(final XMLStreamReader reader) {
        try {
            return (GetLaboratoryOrderOutcomeType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

}

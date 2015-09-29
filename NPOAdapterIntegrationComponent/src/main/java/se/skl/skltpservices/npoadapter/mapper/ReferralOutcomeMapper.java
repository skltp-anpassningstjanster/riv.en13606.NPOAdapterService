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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.actoutcome._3.ActCodeType;
import riv.clinicalprocess.healthcond.actoutcome._3.ActType;
import riv.clinicalprocess.healthcond.actoutcome._3.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.actoutcome._3.MultimediaType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeBodyType;
import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome._3.ReferralType;
import riv.clinicalprocess.healthcond.actoutcome._3.ResultType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.MediaTypeEnum;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.ReferralOutcomeTypeCodeEnum;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.GetReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getreferraloutcomeresponder._3.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.ANY;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TS;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil.HealthcareProfessional;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

/**
 * Input und-kon Undersökning resultat
 * 
 * Output riv:clinicalprocess:activityprescription:actoutcome GetReferralOutcome
 * 
 * Maps from EHR_EXTRACT to RIV GetReferralOutcomeResponseType.
 * 
 * @author Martin Flower
 */
public class ReferralOutcomeMapper extends AbstractMapper implements Mapper {

    private static final Logger log = LoggerFactory.getLogger(ReferralOutcomeMapper.class);

    public static final CD MEANING_UND = new CD();

    static {
        MEANING_UND.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_UND.setCode(INFO_UND_KON);
    }

    private static final JaxbUtil jaxb = new JaxbUtil(GetReferralOutcomeType.class, GetReferralOutcomeResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private static final String EHR13606_DEF = "DEF";
    private static final String EHR13606_TILL = "TILL";
    private static final String SAKNAS = "saknas";

    public ReferralOutcomeMapper() {
        schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-REFERRALOUTCOME"));
        log.debug("schema validation is activated? " + schemaValidationActivated);

        initialiseValidator("/core_components/clinicalprocess_healthcond_actoutcome_enum_3.1.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1_ext.xsd",
                "/core_components/clinicalprocess_healthcond_actoutcome_3.1.xsd",
                "/interactions/GetReferralOutcomeInteraction/GetReferralOutcomeResponder_3.1.xsd");
    }

    // unmarshall xml stream into a GetReferralOutcomeType
    protected GetReferralOutcomeType unmarshal(final XMLStreamReader reader) {
        log.debug("unmarshal");
        try {
            return (GetReferralOutcomeType) jaxb.unmarshal(reader);
        } catch (NullPointerException n) {
            // Cannot see how to intercept payloads with null xml message.
            // Throwing a new exception is better than returning a NullPointerException.
            throw new IllegalArgumentException("Payload contains a null xml message");
        } finally {
            close(reader);
        }
    }

    protected String marshal(final GetReferralOutcomeResponseType response) {
        final JAXBElement<GetReferralOutcomeResponseType> el = objectFactory.createGetReferralOutcomeResponse(response);
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, schemaValidator, log);
        return xml;
    }

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        try {
            final GetReferralOutcomeType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_UND, message.getUniqueId(),
                    message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
        try {
            final RIV13606REQUESTEHREXTRACTResponseType ehrResponse = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));

            GetReferralOutcomeResponseType rivtaResponse = mapResponse(ehrResponse, message);

            message.setPayload(marshal(rivtaResponse));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping response - " + err.getLocalizedMessage(), err, Ehr13606AdapterError.MAPRESPONSE);
        }
    }

    /**
     * Maps from EHR_EXTRACT (und-kon) to GetReferralOutcomeResponseType.
     *
     * @return GetReferralOutcomeResponseType response type
     */
    protected GetReferralOutcomeResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType ehrResponse, MuleMessage message) {
        final List<EHREXTRACT> ehrExtractList = ehrResponse.getEhrExtract();
        GetReferralOutcomeResponseType responseType = mapEhrExtract(ehrExtractList, message);
        responseType.setResult(EHRUtil.resultType(message.getUniqueId(), ehrResponse.getResponseDetail(), ResultType.class));
        return responseType;
    }

    protected GetReferralOutcomeResponseType mapEhrExtract(List<EHREXTRACT> ehrExtractList, MuleMessage message) {

        GetReferralOutcomeResponseType responseType = new GetReferralOutcomeResponseType();
        List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);

        if (ehrExtractList == null || ehrExtractList.isEmpty()) {
            throw new RuntimeException("ehrExtractList is null or empty - malformed 13606 message");
        }
        if (ehrExtractList.size() != 1) {
            log.warn("ehrExtractList size {} - first EHREXTRACT will be processed, others will be ignored - is this a malformed 13606 message?",
                    ehrExtractList.size());
        }

        EHREXTRACT ehrExtract = ehrExtractList.get(0);
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        log.debug("sharedHeaderExtract {}", sharedHeaderExtract.toString());

        // retrieve all separate und and vbe compostions, index by rcId
        // a 'und' will link from composer/performer to a 'vbe'
        // several 'und' can link to the same vbe
        Map<String, COMPOSITION> unds = new HashMap<String, COMPOSITION>();
        Map<String, COMPOSITION> vbes = new HashMap<String, COMPOSITION>();
        populateUndsAndVbes(ehrExtract, unds, vbes);

        for (COMPOSITION und : unds.values()) {
            if (EHRUtil.retain(und, careUnitHsaIds, log)) {
                final ReferralOutcomeType referralOutcome = new ReferralOutcomeType();
                COMPOSITION vbe = getLinkedVbeFromUnd(und, vbes);
                referralOutcome.setReferralOutcomeHeader(mapHeader(und, sharedHeaderExtract));
                
//              referralOutcome.getReferralOutcomeHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalHSAId()
                
                
                
                referralOutcome.setReferralOutcomeBody(mapBody(und, vbe, sharedHeaderExtract));
                responseType.getReferralOutcome().add(referralOutcome);
            }
        }
        return responseType;
    }

    /**
     * @param und
     *            - current und composition
     * @param vbes
     *            - all vbe compositions
     * @return linked vbe - default to null
     */
    private COMPOSITION getLinkedVbeFromUnd(COMPOSITION und, Map<String, COMPOSITION> vbes) {
        if (und != null) {
            if (und.getContent() != null) {
                if (und.getContent().size() > 0) {
                    if (und.getContent().get(0).getLinks() != null) {
                        if (und.getContent().get(0).getLinks().size() > 0) {
                            if (und.getContent().get(0).getLinks().get(0).getFollowLink() != null) {
                                if (Boolean.TRUE == und.getContent().get(0).getLinks().get(0).getFollowLink().isValue()) {
                                    if (und.getContent().get(0).getLinks().get(0).getTargetType() != null) {
                                        if ("vbe".equals(und.getContent().get(0).getLinks().get(0).getTargetType().getCode())) {
                                            if (und.getContent().get(0).getLinks().get(0).getTargetId() != null) {
                                                if (und.getContent().get(0).getLinks().get(0).getTargetId() != null) {
                                                    if (und.getContent().get(0).getLinks().get(0).getTargetId().size() > 0) {
                                                        if (und.getContent().get(0).getLinks().get(0).getTargetId().get(0).getExtension() != null) {
                                                            String vbeRcId = und.getContent().get(0).getLinks().get(0).getTargetId().get(0)
                                                                    .getExtension();
                                                            if (vbes != null) {
                                                                if (vbes.keySet().size() > 0) {
                                                                    return vbes.get(vbeRcId);
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void populateUndsAndVbes(EHREXTRACT ehrExtract, Map<String, COMPOSITION> unds, Map<String, COMPOSITION> vbes) {
        for (COMPOSITION composition : ehrExtract.getAllCompositions()) {
            if (composition.getRcId() != null && composition.getRcId().getExtension() != null) {
                String rcId = composition.getRcId().getExtension();

                if (StringUtils.isBlank(rcId)) {
                    log.error("composition contains a blank rcId");
                } else {
                    if (composition.getMeaning() != null && composition.getMeaning().getCode() != null) {
                        switch (composition.getMeaning().getCode()) {
                        case "und":
                            unds.put(rcId, composition);
                            break;
                        case "vbe":
                            vbes.put(rcId, composition);
                            break;
                        default:
                            log.warn("Unrecognised meaning code {} in message", composition.getMeaning().getCode());
                        }
                    }
                }
            }
        }
    }

    /**
     * Maps contact header information.
     *
     * @param ehrExtract
     *            the extract.
     * @return the target header information.
     */
    private PatientSummaryHeaderType mapHeader(COMPOSITION und, SharedHeaderExtract sharedHeaderExtract) {

        log.debug("populating header using und composition");
        // the accountableHealthcareProfessional in the header is the person who created the information in the document
        PatientSummaryHeaderType patient = (PatientSummaryHeaderType) EHRUtil.patientSummaryHeader(und, sharedHeaderExtract, "und-und-ure-stp",
                PatientSummaryHeaderType.class, false, false, true);
        
        // 'Tidpunkt då dokument skapades' - TKB clinicalprocess healthcond actoutcome
        if (StringUtils.isBlank(patient.getDocumentTime())) {
            if (und.getCommittal() != null) {
                if (und.getCommittal().getTimeCommitted() != null) {
                    if (StringUtils.isNotBlank(und.getCommittal().getTimeCommitted().getValue())) {
                        patient.setDocumentTime(und.getCommittal().getTimeCommitted().getValue());
                    }
                }
            }
        }
        return patient;
    }

    /**
     * Create a ReferralOutcomeRecord using the information in the current und and vbe compositions
     * @param healthcareProfessional 
     *
     * @return a new ReferralOutcomeBodyTypeRecord
     */
    private ReferralOutcomeBodyType mapBody(COMPOSITION und, COMPOSITION vbe, SharedHeaderExtract sharedHeaderExtract) {

        Map<String, String> ehr13606values = new LinkedHashMap<String, String>();

        // parse the two compositions into values stored in a Map
        retrieveUndValues(und, ehr13606values);
        retrieveVbeValues(vbe, ehr13606values);

        if (log.isDebugEnabled()) {
            Iterator<String> it = ehr13606values.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                log.debug("|" + key + "|" + ehr13606values.get(key) + "|");
            }
        }

        // use the ehr values to build a referral outcome body
        return buildBody(ehr13606values, vbe, sharedHeaderExtract);
    }

    /*
     * Create a ReferralOutcomeRecord for outgoing message. Use values from ehr 13606 incoming message. Populate values in outgoing message
     * if there is data in the incoming message.
     * 
     * Attempt to avoid empty elements in the outgoing message (this is why we check for data before creating outgoing objects).
     */
    private ReferralOutcomeBodyType buildBody(Map<String, String> ehr13606values, COMPOSITION vbe, SharedHeaderExtract sharedHeaderExtract) {

        final ReferralOutcomeBodyType bodyType = new ReferralOutcomeBodyType();

        bodyType.setReferralOutcomeTypeCode(interpretOutcomeType(ehr13606values.get("und-und-ure-typ")));
        if (bodyType.getReferralOutcomeTypeCode() == null) {
            log.warn("Missing mandatory field ReferralOutcomeTypeCode");
        }
        bodyType.setReferralOutcomeTitle(ehr13606values.get("und-kon-ure-kty"));
        if (StringUtils.isBlank(bodyType.getReferralOutcomeTitle())) {
            bodyType.setReferralOutcomeTitle(SAKNAS);
        }
        bodyType.setReferralOutcomeText(ehr13606values.get("und-und-ure-utl"));
        if (StringUtils.isBlank(bodyType.getReferralOutcomeText())) {
            bodyType.setReferralOutcomeText(SAKNAS);
        }

        //

        bodyType.getClinicalInformation(); // nothing to do here

        //

        bodyType.getAct().add(new ActType());
        if (ehr13606values.containsKey("und-und-uat-kod")) {
            bodyType.getAct().get(0).setActCode(new ActCodeType());
            bodyType.getAct().get(0).getActCode().setCode(ehr13606values.get("und-und-uat-kod"));
            bodyType.getAct().get(0).getActCode().setCodeSystem(ehr13606values.get("und-und-uat-kod-system"));
        }

        // bodyType.getAct().get(0).setActId(ehr13606values.get("vbe-rc-id"));
        bodyType.getAct().get(0).setActText(ehr13606values.get("und-und-uat-txt")); // und-kon-ure-kty
        if (StringUtils.isBlank(bodyType.getAct().get(0).getActText())) {
            bodyType.getAct().get(0).setActText(SAKNAS);
        }
        if (ehr13606values.containsKey("und-und-res-und")) {
            // There is no data in qa for und-und-res-und need to confirm that element contains text
            bodyType.getAct().get(0).getActResult().add(new MultimediaType());
            bodyType.getAct().get(0).getActResult().get(0).setMediaType(MediaTypeEnum.TEXT_PLAIN);
            bodyType.getAct().get(0).getActResult().get(0).setValue(ehr13606values.get("und-und-res-und").getBytes(Charset.forName("UTF-8")));
        }

        bodyType.getAct().get(0).setActTime(ehr13606values.get("und-und-uat-txt-high"));

        //

        ReferralType rt = new ReferralType();

        rt.setReferralId(ehr13606values.get("und-kon-ure-id"));
        if (StringUtils.isBlank(rt.getReferralId())) {
            rt.setReferralId(SAKNAS);
        }
        rt.setReferralReason(ehr13606values.get("vbe-vbe-fst"));
        if (StringUtils.isBlank(rt.getReferralReason())) {
            rt.setReferralReason(SAKNAS);
        }

        rt.setReferralTime(ehr13606values.get("vbe-committal-timecommitted"));

        // populate healthcareProfessional using vbe composition
        HealthcareProfessional h = EHRUtil.healthcareProfessionalType(vbe.getComposer(), 
                                                                      sharedHeaderExtract.organisations(),
                                                                      sharedHeaderExtract.healthcareProfessionals(), 
                                                                      vbe.getCommittal());
        HealthcareProfessionalType o = XMLBeanMapper.getInstance().map(h, HealthcareProfessionalType.class);
        rt.setReferralAuthor(o);
        
        // tkb - 'Skall ej anges'
        rt.getReferralAuthor().setHealthcareProfessionalCareGiverHSAId(null);
        // tkb - 'Skall ej anges'
        rt.getReferralAuthor().setHealthcareProfessionalCareUnitHSAId(null);
        
        rt.getReferralAuthor().setAuthorTime(ehr13606values.get("vbe-committal-timecommitted"));
        if (StringUtils.isBlank(rt.getReferralAuthor().getAuthorTime())) {
            rt.getReferralAuthor().setAuthorTime(SAKNAS);
        }
        bodyType.setReferral(rt);

        //
        return bodyType;
    }

    // Retrieve ehr values from und composition and store in a map
    private void retrieveUndValues(COMPOSITION composition, Map<String, String> values) {
        for (final CONTENT content : composition.getContent()) {
            retrieveContentValue(content, values);
        }
    }

    private void retrieveContentValue(CONTENT content, Map<String, String> values) {

        for (final ITEM item : ((ENTRY) content).getItems()) {
            retrieveItemValue(item, values);
        }

        // links contains und-kon-ure-id
        if ("und-kon-ure".equals(content.getMeaning().getCode())) {
            String undKonUreId = "";
            for (se.rivta.en13606.ehrextract.v11.LINK link : content.getLinks()) {
                if (link.getTargetId() != null) {
                    for (se.rivta.en13606.ehrextract.v11.II ii : link.getTargetId()) {
                        if (StringUtils.isNotBlank(ii.getExtension())) {
                            undKonUreId = ii.getExtension();
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(undKonUreId)) {
                values.put("und-kon-ure-id", undKonUreId);
            }
        }

    }

    // Retrieve ehr values from vbe composition and store in a map
    private void retrieveVbeValues(COMPOSITION composition, Map<String, String> values) {

        for (final CONTENT content : composition.getContent()) {
            retrieveContentValue(content, values);
        }

        if (composition.getRcId() != null) {
            if (StringUtils.isNotBlank(composition.getRcId().getRoot())) {
                values.put("vbe-rc-id", composition.getRcId().getRoot());
            }
        }

        if (composition.getCommittal() != null) {
            if (composition.getCommittal().getTimeCommitted() != null) {
                if (StringUtils.isNotBlank(composition.getCommittal().getTimeCommitted().getValue())) {
                    values.put("vbe-committal-timecommitted", composition.getCommittal().getTimeCommitted().getValue());
                }
            }
        }

        if (composition.getComposer() != null) {
            if (composition.getComposer().getPerformer() != null) {
                values.put("vbe-composer-performer-extension", composition.getComposer().getPerformer().getExtension());
            }
        }
    }

    /*
     * Retrieve item values from incoming message and store as Strings in a Map. Basing processing on Strings means converting numerics and
     * objects to String. This makes processing slightly less efficient, but simplifies coding in parent methods.
     */
    private void retrieveItemValue(ITEM item, Map<String, String> values) {

        if (item.getMeaning() != null) {
            String code = item.getMeaning().getCode();
            if (StringUtils.isNotBlank(code)) {

                if (item instanceof ELEMENT) {
                    String text = "";
                    ANY value = ((ELEMENT) item).getValue();
                    if (value != null) {
                        if (value instanceof ST) {
                            text = ((ST) value).getValue();
                            if ("und-und-uat-kod".equals(item.getMeaning().getCode())) {
                                String codeSystem = item.getMeaning().getCodeSystem();
                                if (StringUtils.isNotBlank(codeSystem)) {
                                    values.put("und-und-uat-kod-system", codeSystem);
                                }
                            }
                        } else if (value instanceof TS) {
                            text = ((TS) value).getValue();
                        } else {
                            log.error("Code " + code + " has unexpected value type " + value.getClass().getCanonicalName());
                        }

                        if (StringUtils.isNotBlank(text)) {
                            values.put(code, text);
                        }

                        // ----------

                        // obs time requires special handling
                        if (((ELEMENT) item).getObsTime() != null) {
                            IVLTS ivlts = ((ELEMENT) item).getObsTime();
                            if (ivlts != null) {
                                TS tsLow = ivlts.getLow();
                                if (StringUtils.isNotBlank(tsLow.getValue())) {
                                    values.put(code + "-low", tsLow.getValue());
                                }
                                TS tsHigh = ivlts.getHigh();
                                if (StringUtils.isNotBlank(tsHigh.getValue())) {
                                    values.put(code + "-high", tsHigh.getValue());
                                }
                                if (ivlts.isLowClosed() != null) {
                                    values.put(code + "-lowClosed", ivlts.isLowClosed().toString());
                                }
                                if (ivlts.isHighClosed() != null) {
                                    values.put(code + "-highClosed", ivlts.isHighClosed().toString());
                                }
                            }
                        }
                    }

                } else if (item instanceof CLUSTER) {
                    CLUSTER cluster = (CLUSTER) item;
                    for (ITEM childItem : cluster.getParts()) {
                        retrieveItemValue(childItem, values);
                    }
                } else {
                    log.error("ITEM is neither an ELEMENT nor a CLUSTER:" + item.getMeaning().getCode());
                }
            }
        }
    }

    protected ReferralOutcomeTypeCodeEnum interpretOutcomeType(final String value) {
        if (value == null)
            return null;
        switch (value) {
        case EHR13606_DEF:
            return ReferralOutcomeTypeCodeEnum.SS;
        case EHR13606_TILL:
            return ReferralOutcomeTypeCodeEnum.SR;
        default:
            return null;
        }
    }

}

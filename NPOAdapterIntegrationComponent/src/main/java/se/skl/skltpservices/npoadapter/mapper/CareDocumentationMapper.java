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

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.description._2.*;
import riv.clinicalprocess.healthcond.description.enums._2.ClinicalDocumentNoteCodeEnum;
import riv.clinicalprocess.healthcond.description.enums._2.ClinicalDocumentTypeCodeEnum;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationResponseType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

/**
 * Maps from EHR_EXTRACT (voo v1.1) to RIV GetCareDocumentationResponseType
 * <p>
 *
 * @author torbjorncla
 */
public class CareDocumentationMapper extends AbstractMapper implements Mapper {

    private static final Logger log = LoggerFactory.getLogger(CareDocumentationMapper.class);

    private static JaxbUtil jaxb = new JaxbUtil(GetCareDocumentationType.class);
    private static final ObjectFactory objFactory = new ObjectFactory();

    private static final String TIME_ELEMENT = "voo-voo-tid";
    private static final String TEXT_ELEMENT = "voo-voo-txt";

    public static final CD MEANING_VOO = new CD();
    static {
        MEANING_VOO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VOO.setCode(INFO_VOO);
    }

    
    public CareDocumentationMapper() {
        schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-CAREDOCUMENTATION"));
        log.debug("schema validation is activated? " + schemaValidationActivated);
        
        initialiseValidator("/core_components/clinicalprocess_healthcond_description_enum_2.1.xsd", 
                            "/core_components/clinicalprocess_healthcond_description_2.1_ext.xsd",
                            "/core_components/clinicalprocess_healthcond_description_2.1.xsd",
                            "/interactions/GetCareDocumentationInteraction/GetCareDocumentationResponder_2.1.xsd");
    }

    
    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        log.debug("Transforming request");
        try {
            final GetCareDocumentationType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_VOO, message.getUniqueId(),
                    message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Exception when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
        log.debug("Transforming response - start");
        try {
            final RIV13606REQUESTEHREXTRACTResponseType response = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
            final GetCareDocumentationResponseType responseType = mapResponse(response, message);
            message.setPayload(marshal(responseType));
            log.debug("Transformed response - end");
            return message;
        } catch (Exception err) {
            throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
        }
    }

    protected GetCareDocumentationType unmarshal(final XMLStreamReader reader) {
        try {
            return (GetCareDocumentationType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    /**
     * Maps EHREXTRACT from RIV13606REQUESTEHREXTRACT to GetCareDocumentationResponse
     * <p/>
     *
     * @param ehrResp
     *            subset from RIV136060REQUESTEHREXTRACT.
     * @return GetCareDocumentationType for marshaling.
     */
    protected GetCareDocumentationResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, MuleMessage message) {
        checkContinuation(log, ehrResp);
        log.debug("Populating GetCareDocumentationResponse using ehrResp (" + ehrResp.getClass().getName() + ")");
        final GetCareDocumentationResponseType getCareDocumentationResponse = new GetCareDocumentationResponseType();
        getCareDocumentationResponse.setResult(EHRUtil.resultType(message.getUniqueId(), ehrResp.getResponseDetail(), Result.class));

        if (ehrResp.getEhrExtract().isEmpty()) {
            return getCareDocumentationResponse;
        }

        final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);

        for (COMPOSITION composition13606 : ehrExtract.getAllCompositions()) {
            if (EHRUtil.retain(composition13606, careUnitHsaIds, log)) {
                final CareDocumentationType doc = new CareDocumentationType();
                doc.setCareDocumentationHeader(EHRUtil.patientSummaryHeader(composition13606, sharedHeaderExtract, TIME_ELEMENT,
                        CPatientSummaryHeaderType.class, false, false, true));
                doc.getCareDocumentationHeader().setCareContactId(EHRUtil.careContactId(composition13606.getLinks()));
                doc.getCareDocumentationHeader().setSourceSystemHSAid(EHRUtil.getSystemHSAId(ehrExtract));
                doc.getCareDocumentationHeader().setDocumentTime(EHRUtil.padTimestampIfNecessary(doc.getCareDocumentationHeader().getDocumentTime()));
                doc.setCareDocumentationBody(mapBodyType(composition13606));
                getCareDocumentationResponse.getCareDocumentation().add(doc);
            }
        }
        log.debug("Finished populating GetCareDocumentationResponse");
        return getCareDocumentationResponse;
    }

    protected CareDocumentationBodyType mapBodyType(final COMPOSITION composition) {
        final CareDocumentationBodyType body = new CareDocumentationBodyType();
        final ClinicalDocumentNoteType clinicalDocumentNote = new ClinicalDocumentNoteType();
        body.setClinicalDocumentNote(clinicalDocumentNote);

        if (composition.getMeaning() != null) {
            final String code = composition.getMeaning().getCode();

            // --- note
            if (isClinicalDocumentNoteCode(code)) {
                clinicalDocumentNote.setClinicalDocumentNoteCode(ClinicalDocumentNoteCodeEnum.fromValue(code));
            // --- type    
            } else if (isClinicalDocumentTypeCode(code)) {
                clinicalDocumentNote.setClinicalDocumentTypeCode(ClinicalDocumentTypeCodeEnum.fromValue(code));
            } else {
                log.warn("Not able to map documentcode:{}", code);
            }
            
            if (StringUtils.isNotBlank(code)) {
                if (isClinicalDocumentTypeCode(code) || "spe".equals(code)) {
                    if (composition.getName() != null) {
                        clinicalDocumentNote.setClinicalDocumentNoteTitle(composition.getName().getValue());
                    }
                }
            }
        }

        // Only txt is supported.
        final ELEMENT txt = EHRUtil.findEntryElement(composition.getContent(), TEXT_ELEMENT);
        if (txt != null) {
            String textWithDollarSigns = EHRUtil.getElementTextValue(txt);
            // SERVICE-371 - 13606 new line characters
            clinicalDocumentNote.setClinicalDocumentNoteText(textWithDollarSigns.replace("$$NL$$", "\r\n"));
        }
        return body;
    }

    protected String marshal(final GetCareDocumentationResponseType response) {
        final JAXBElement<GetCareDocumentationResponseType> el = objFactory.createGetCareDocumentationResponse(response);
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, schemaValidator, log);
        return xml;
    }

    protected boolean isClinicalDocumentTypeCode(final String code) {
        try {
            ClinicalDocumentTypeCodeEnum.fromValue(code);
            return true;
        } catch (IllegalArgumentException err) {
            return false;
        }
    }

    protected boolean isClinicalDocumentNoteCode(final String code) {
        try {
            ClinicalDocumentNoteCodeEnum.fromValue(code);
            return true;
        } catch (IllegalArgumentException err) {
            return false;
        }
    }
}

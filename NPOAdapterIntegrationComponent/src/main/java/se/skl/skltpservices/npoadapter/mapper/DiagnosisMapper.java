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
import riv.clinicalprocess.healthcond.description.enums._2.DiagnosisTypeEnum;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisResponseType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisType;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

/**
 * Maps from EHR_EXTRACT (dia v1.1) to RIV GetDiagnosisResponseType
 * <p>
 *
 * @author torbjorncla
 */
public class DiagnosisMapper extends AbstractMapper implements Mapper {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisMapper.class);

    private static final JaxbUtil jaxb = new JaxbUtil(GetDiagnosisType.class);
    private static final ObjectFactory objFactory = new ObjectFactory();

    public static final CD MEANING_DIA = new CD();
    static {
        MEANING_DIA.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_DIA.setCode(INFO_DIA);
    }

    protected static final String TIME_ELEMENT = "dia-dia-tid";
    protected static final String CODE_ELEMENT = "dia-dia-kod";
    protected static final String TYPE_ELEMENT = "dia-dia-typ";
    protected static final String DIA_DIA      = "dia-dia";
    protected static final String DIA          = "dia";
    protected static final String CHRONIC_DIAGNOSIS = "Kronisk diagnos";

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        try {
            final GetDiagnosisType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_DIA, message.getUniqueId(),
                    message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Exception when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
        try {
            final RIV13606REQUESTEHREXTRACTResponseType ehrResp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
            final GetDiagnosisResponseType resp = mapResponse(ehrResp, message);
            message.setPayload(marshal(resp));
            return message;
        } catch (Exception err) {
            throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
        }
    }

    protected String marshal(final GetDiagnosisResponseType response) {
        final JAXBElement<GetDiagnosisResponseType> el = objFactory.createGetDiagnosisResponse(response);
        return jaxb.marshal(el);
    }

    protected GetDiagnosisType unmarshal(final XMLStreamReader reader) {
        try {
            return (GetDiagnosisType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    /**
     * Create response. Collects organisation and healthcare-professional into maps with HSAId as key. So other functions dont need to
     * itterat over document each time.
     * 
     * @param ehrResp
     *            response to be loaded into soap-payload.
     * @param uniqueId
     *            mule-message uniqueId.
     * @return a diagnosis response.
     */
    protected GetDiagnosisResponseType mapResponse(RIV13606REQUESTEHREXTRACTResponseType ehrResp, MuleMessage message) {
        final GetDiagnosisResponseType resp = new GetDiagnosisResponseType();
        resp.setResult(EHRUtil.resultType(message.getUniqueId(), ehrResp.getResponseDetail(), ResultType.class));
        if (ehrResp.getEhrExtract().isEmpty()) {
            return resp;
        }

        final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);

        for (COMPOSITION composition13606 : ehrExtract.getAllCompositions()) {
            if (EHRUtil.retain(composition13606, careUnitHsaIds, log)) {
                final DiagnosisType type = new DiagnosisType();
                type.setDiagnosisHeader(EHRUtil.patientSummaryHeader(composition13606, sharedHeaderExtract, TIME_ELEMENT,
                        PatientSummaryHeaderType.class));
                type.getDiagnosisHeader().setCareContactId(getCareContactId(composition13606));
                type.getDiagnosisHeader().setDocumentTime(EHRUtil.padTimestampIfNecessary(type.getDiagnosisHeader().getDocumentTime()));
                type.setDiagnosisBody(mapDiagnosisBodyType(composition13606));
                resp.getDiagnosis().add(type);
            }
        }

        return resp;
    }

    /**
     * Diagnosis specific method because of careContactId location.
     * 
     * @param comp
     * @return careContactId.
     */
    protected String getCareContactId(final COMPOSITION comp) {
        for (CONTENT content : comp.getContent()) {
            if (content instanceof ENTRY) {
                final ENTRY entry = (ENTRY) content;
                return EHRUtil.careContactId(entry.getLinks());
            }
        }
        return null;
    }

    //
    protected DiagnosisBodyType mapDiagnosisBodyType(final COMPOSITION composition) {
        final DiagnosisBodyType type = new DiagnosisBodyType();
        for (CONTENT content : composition.getContent()) {
            if (content instanceof ENTRY) {
                ENTRY e = (ENTRY) content;

                // related diagnosis
                
                if (e.getMeaning() != null && e.getMeaning().getCode() != null) {
                    if (DIA_DIA.equals(e.getMeaning().getCode())) {
                        if (e.getLinks() != null) {
                            List<LINK> links = e.getLinks();
                            for (LINK link : links) {
                                if (link.getTargetType() != null) {
                                    if (DIA.equalsIgnoreCase(link.getTargetType().getCode())) {
                                        if (link.getTargetId() != null) {
                                            for (II ii : link.getTargetId()) {
                                                if (StringUtils.isNotBlank(ii.getExtension())) {
                                                    RelatedDiagnosisType relatedDiagnosis = new RelatedDiagnosisType();
                                                    relatedDiagnosis.setDocumentId(ii.getExtension());
                                                    type.getRelatedDiagnosis().add(relatedDiagnosis);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                
                for (ITEM item : e.getItems()) {
                    if (item instanceof ELEMENT) {
                        ELEMENT elm = (ELEMENT) item;
                        if (elm.getValue() != null && elm.getMeaning() != null && elm.getMeaning().getCode() != null) {
                            
                            // --- process this meaning
                            
                            switch (elm.getMeaning().getCode()) {
                            
                            // dia-dia-tid
                            case TIME_ELEMENT:
                                if (elm.getValue() instanceof TS) {
                                    type.setDiagnosisTime(((TS) elm.getValue()).getValue());
                                    type.setDiagnosisTime(EHRUtil.padTimestampIfNecessary(type.getDiagnosisTime()));
                                }
                                break;
                            
                            // dia-dia-typ    
                            case TYPE_ELEMENT:
                                if (elm.getValue() instanceof ST) {
                                    final ST simpleText = (ST) elm.getValue();
                                    if (StringUtils.equalsIgnoreCase(simpleText.getValue(), CHRONIC_DIAGNOSIS)) {
                                        type.setTypeOfDiagnosis(DiagnosisTypeEnum.HUVUDDIAGNOS);
                                        type.setChronicDiagnosis(true);
                                    } else {
                                        type.setTypeOfDiagnosis(interpret(simpleText.getValue()));
                                    }
                                }
                                break;
                                
                            // dia-dia-kod    
                            case CODE_ELEMENT:
                                if (elm.getValue() instanceof CD) {
                                    type.setDiagnosisCode(EHRUtil.cvType((CD) elm.getValue(), CVType.class));
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        return type;
    }

    /**
     * Maps enum between different domains.
     * 
     * @param diagnosisType
     * @return the actual enum, or null if none matches.
     */
    protected DiagnosisTypeEnum interpret(final String diagnosisType) {
        try {
            return DiagnosisTypeEnum.fromValue(diagnosisType);
        } catch (Exception err) {
            log.warn(String.format("Could not map DiagnosisType of value: %s", diagnosisType));
            return null;
        }
    }
}

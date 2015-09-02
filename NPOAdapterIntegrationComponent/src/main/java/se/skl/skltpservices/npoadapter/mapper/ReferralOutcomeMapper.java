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
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;


/**
 * Input
 *  und-kon
 *   Undersökning resultat
 *   
 * Output
 *  riv:clinicalprocess:activityprescription:actoutcome
 *   GetReferralOutcome
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
    
    private static final String EHR13606_DEF  = "DEF";
    private static final String EHR13606_TILL = "TILL";
    private static final String SAKNAS        = "saknas";

    // unmarshall xml stream into a GetReferralOutcomeType
    protected GetReferralOutcomeType unmarshal(final XMLStreamReader reader) {
        log.debug("unmarshal");
        try {
            return  (GetReferralOutcomeType) jaxb.unmarshal(reader);
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
        return jaxb.marshal(el);
    }
    

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        try {
            final GetReferralOutcomeType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_UND, message.getUniqueId(), message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    
    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType ehrResponse 
    		   = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
    		
    		GetReferralOutcomeResponseType rivtaResponse = mapResponse(ehrResponse, message);
    		
            message.setPayload(marshal(rivtaResponse));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
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
        if (!ehrExtractList.isEmpty()) {
            final EHREXTRACT ehrExtract = ehrExtractList.get(0);
            
            List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);
            if (EHRUtil.retain(ehrExtract.getAllCompositions().get(0), careUnitHsaIds, log)) {
                final ReferralOutcomeType referralOutcomeRecordType = new ReferralOutcomeType();
                referralOutcomeRecordType.setReferralOutcomeHeader(mapHeader(ehrExtract));
                referralOutcomeRecordType.setReferralOutcomeBody(mapBody(ehrExtract));
                responseType.getReferralOutcome().add(referralOutcomeRecordType);
            }
        }
        return responseType;
    }


    /**
     * Maps contact header information.
     *
     * @param ehrExtract the extract.
     * @return the target header information.
     */
    private PatientSummaryHeaderType mapHeader(final EHREXTRACT ehrExtract) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(0);
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        PatientSummaryHeaderType patient = (PatientSummaryHeaderType)EHRUtil.patientSummaryHeader(composition, sharedHeaderExtract, "und-und-ure-stp", PatientSummaryHeaderType.class, true, true);
        
        // 'Tidpunkt då dokument skapades' - TKB clinicalprocess healthcond actoutcome
        if ("" == patient.getDocumentTime() || null == patient.getDocumentTime()) {
            TS timestamp = ehrExtract.getTimeCreated();
            if (timestamp != null && StringUtils.isNotBlank(timestamp.getValue())) {
                patient.setDocumentTime(ehrExtract.getTimeCreated().getValue());
            } else {
                patient.setDocumentTime("19000101000000"); // default timestamp
            }
        }
        return patient;
    }
    
    
    /**
     * Create a ReferralOutcomeRecord using the information
     * in the first and second compositions.
     *
     * @param ehrExtract the extract containing the current composition
     * @return a new ReferralOutcomeBodyTypeRecord
     */
    private ReferralOutcomeBodyType mapBody(final EHREXTRACT ehrExtract) {
        
        Map<String,String> ehr13606values = new LinkedHashMap<String,String>();
        
        if (ehrExtract.getAllCompositions().size() > 0) {
            final COMPOSITION composition0 = ehrExtract.getAllCompositions().get(0);
            if (composition0 != null) {
                // parse this composition into values stored in a Map
                retrieveValues(composition0, ehr13606values);
            
                if (ehrExtract.getAllCompositions().size() > 1) {
                    final COMPOSITION composition1 = ehrExtract.getAllCompositions().get(1);
                    retrieveValues(composition1, ehr13606values);
                }
                
                if (log.isDebugEnabled()) {
                    Iterator<String> it = ehr13606values.keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        log.debug("|" + key + "|" + ehr13606values.get(key) + "|");
                    }
                }
            }
        }
        
        // use the ehr values to build a referral outcome body
        return buildBody(ehr13606values);
    }

   /* 
    * Create a ReferralOutcomeRecord for outgoing message.
    * Use values from ehr 13606 incoming message.
    * Populate values in outgoing message if there is data in the incoming message.
    * 
    * Attempt to avoid empty elements in the outgoing message
    * (this is why we check for data before creating outgoing objects).
    */
    private ReferralOutcomeBodyType buildBody (Map<String, String> ehr13606values) {
        
        final ReferralOutcomeBodyType bodyType = new ReferralOutcomeBodyType();
        
        bodyType.setReferralOutcomeTypeCode(interpretOutcomeType(ehr13606values.get("und-und-ure-typ")));
        if(bodyType.getReferralOutcomeTypeCode() == null) {
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
        
//      bodyType.getAct().get(0).setActId(ehr13606values.get("vbe-rc-id"));
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
        
        rt.setReferralAuthor(new HealthcareProfessionalType());
        
        rt.getReferralAuthor().setHealthcareProfessionalName((ehr13606values.get("vbe-composer-performer-extension")));
        rt.getReferralAuthor().setAuthorTime(ehr13606values.get("vbe-committal-timecommitted"));
        if (StringUtils.isBlank(rt.getReferralAuthor().getAuthorTime())) {
            rt.getReferralAuthor().setAuthorTime(SAKNAS);
        }
        bodyType.setReferral(rt);
        
        //
        return bodyType;
    }

    
    // Retrieve ehr values from message and store in a map
    private void retrieveValues(COMPOSITION composition, Map<String,String> values) {
        for (final CONTENT content : composition.getContent()) {
            retrieveContentValue(content, values);
        }
        
        if ("vbe".equals(composition.getMeaning().getCode())) {
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


    /*
     * Retrieve item values from incoming message and store 
     * as Strings in a Map.
     * Basing processing on Strings means converting numerics
     * and objects to String. This makes processing slightly less
     * efficient, but simplifies coding in parent methods.
     */
    private void retrieveItemValue(ITEM item, Map<String,String> values) {
        
        if (item.getMeaning() != null) {
            String code = item.getMeaning().getCode();
            if (StringUtils.isNotBlank(code)) {
                
                if (item instanceof ELEMENT) {
                    String text = "";    
                    ANY value = ((ELEMENT)item).getValue();
                    if (value != null) {
                               if (value instanceof ST) {
                            text = ((ST)value).getValue();
                            if ("und-und-uat-kod".equals(item.getMeaning().getCode())) {
                                String codeSystem = item.getMeaning().getCodeSystem();
                                if (StringUtils.isNotBlank(codeSystem)) {
                                    values.put("und-und-uat-kod-system", codeSystem);
                                }
                            }
                        } else if (value instanceof TS) {
                            text = ((TS)value).getValue();
                        } else {
                            log.error("Code " + code + " has unexpected value type " + value.getClass().getCanonicalName());
                        }
                               
                        if (StringUtils.isNotBlank(text)) {
                           values.put(code, text);
                        }
                        
                        // ----------
                        
                        // obs time requires special handling
                        if (((ELEMENT)item).getObsTime() != null) {
                            IVLTS ivlts = ((ELEMENT)item).getObsTime();
                            if (ivlts != null) {
                                TS tsLow  = ivlts.getLow();
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
                    CLUSTER cluster = (CLUSTER)item;
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
    	if(value == null) return null;
    	switch(value) {
    		case EHR13606_DEF: return ReferralOutcomeTypeCodeEnum.SS;
    		case EHR13606_TILL: return ReferralOutcomeTypeCodeEnum.SR;
    		default: return null;
    	}
    }
    
}

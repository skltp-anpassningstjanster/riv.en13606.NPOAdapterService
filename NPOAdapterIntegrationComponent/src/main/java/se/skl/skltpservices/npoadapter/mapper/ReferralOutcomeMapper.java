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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.actoutcome._3.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeBodyType;
import riv.clinicalprocess.healthcond.actoutcome._3.ReferralOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome._3.ReferralType;
import riv.clinicalprocess.healthcond.actoutcome._3.ResultType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.ResultCodeEnum;
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
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;


/**
 * Input
 *  lkm
 *  lko
 *   Läkemedel ordination
 *  lkf
 *   Läkemedel förskrivning
 *   
 * Output
 *  riv:clinicalprocess:activityprescription:actoutcome
 *   GetReferralOutcome
 * 
 * Maps from EHR_EXTRACT to RIV GetReferralOutcomeResponseType v2.0. 
 * <p>
 * Riv contract spec : 
 * http://rivta.se/downloads/ServiceContracts_clinicalpocess_activityprescription_actoutcome_3.0_RC1.zip
 *
 * @author Martin
 */
@Slf4j
public class ReferralOutcomeMapper extends AbstractMapper implements Mapper {

    public static final CD MEANING_UND = new CD();
    
    static {
        MEANING_UND.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_UND.setCode(INFO_LKM);
    }

    private static final JaxbUtil jaxb 
      = new JaxbUtil(GetReferralOutcomeType.class, GetReferralOutcomeResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();

    protected GetReferralOutcomeType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetReferralOutcomeType) jaxb.unmarshal(reader);
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
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_UND)));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err);
        }
    }

    
    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType ehrResponse 
    		   = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
    		
    		GetReferralOutcomeResponseType rivtaResponse = map(ehrResponse, message.getUniqueId());
    		
            message.setPayload(marshal(rivtaResponse));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping response", err);
    	}
    }

    
    /**
     * Maps from EHR_EXTRACT (lko/lkf) to GetReferralOutcomeResponseType.
     *
     * @param ehrExtractList the EHR_EXTRACT XML Java bean.
     * @return GetReferralOutcomeResponseType response type
     */
    protected GetReferralOutcomeResponseType map(final RIV13606REQUESTEHREXTRACTResponseType ehrResponse, String uniqueId) {

        final List<EHREXTRACT> ehrExtractList = ehrResponse.getEhrExtract();
        log.debug("list of EHREXTRACT - " + ehrExtractList.size());
        GetReferralOutcomeResponseType responseType = mapEhrExtract(ehrExtractList);
        
        responseType.setResult(EHRUtil.resultType(uniqueId, ehrResponse.getResponseDetail(), ResultType.class));
        
        // TODO - investigate why this code is necessary - if EHRUtil.resultType cannot handle this message,
        // then maybe we shouldn't be trying to do any extra processing here.
        if (responseType.getResult() == null) {
            responseType.setResult(new ResultType());
        }
        if (responseType.getResult().getResultCode() == null) {
            responseType.getResult().setResultCode(ResultCodeEnum.OK); // TODO ok?
        }
        if (StringUtils.isEmpty(responseType.getResult().getLogId())) {
            responseType.getResult().setLogId("TODO log id");
        }
        return responseType;
    }
    
    
    protected GetReferralOutcomeResponseType mapEhrExtract(List<EHREXTRACT> ehrExtractList) {
        GetReferralOutcomeResponseType responseType = new GetReferralOutcomeResponseType();
        if (!ehrExtractList.isEmpty()) {
            final EHREXTRACT ehrExtract = ehrExtractList.get(0);
            for (int i = 0; i < ehrExtract.getAllCompositions().size(); i++) {
                final ReferralOutcomeType referralOutcomeRecordType = new ReferralOutcomeType();
                referralOutcomeRecordType.setReferralOutcomeHeader(mapHeader(ehrExtract, i));
                referralOutcomeRecordType.setReferralOutcomeBody(mapBody(ehrExtract, i));
                responseType.getReferralOutcome().add(referralOutcomeRecordType);
            }
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
    private PatientSummaryHeaderType mapHeader(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);
        if (composition.getComposer() == null) {
            log.warn("composition " + compositionIndex + " has a null composer"); // TODO lkf has no composer
        }
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        
        PatientSummaryHeaderType patient = (PatientSummaryHeaderType)EHRUtil.patientSummaryHeader(composition, sharedHeaderExtract, "und-und-ure-stp", PatientSummaryHeaderType.class);
        if (StringUtils.isBlank(patient.getAccountableHealthcareProfessional().getAuthorTime())) {
            patient.getAccountableHealthcareProfessional().setAuthorTime("TODO - author time");   
        }
        
        if (StringUtils.isBlank(patient.getLegalAuthenticator().getSignatureTime())) {
            patient.getLegalAuthenticator().setSignatureTime("TODO - signature time");
        }
        return patient;
    }
    
    
    /**
     * Create a ReferralOutcomeRecord using the information
     * in the current ehr13606 composition.
     *
     * @param ehrExtract the extract containing the current composition
     * @param compositionIndex the actual composition in the list.
     * @return a new ReferralOutcomeRecord
     */
    private ReferralOutcomeBodyType mapBody(final EHREXTRACT ehrExtract, final int compositionIndex) {
    
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);
        
        // parse this composition into values stored in a Map
        Map<String,String> ehr13606values = retrieveValues(composition, compositionIndex);
        
        // use the ehr values to build a medication medical history record body
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
        
        ReferralType mpt = new ReferralType();
        
        mpt.setCareContactId("TODO care contact id");
        mpt.setReferralAuthor(new HealthcareProfessionalType());
        mpt.setReferralId("TODO referral id");
        mpt.setReferralReason("TODO referral reason");
        mpt.setReferralTime("TODO referral time");
        
        // ---

        final ReferralOutcomeBodyType bodyType = new ReferralOutcomeBodyType();
        bodyType.setReferral(mpt);
        return bodyType;
    }

    
    // Retrieve ehr values from message and store in a map
    private Map<String,String> retrieveValues(COMPOSITION composition, int compositionIndex) {
        
        Map<String,String> values = new LinkedHashMap<String,String>(); // Linked in order to preserve order of insertion
        for (final CONTENT content : composition.getContent()) {
            for (final ITEM item : ((ENTRY) content).getItems()) {
                retrieveItemValue(item, values);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("incoming composition : " + compositionIndex);
            Iterator<String> it = values.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                log.debug("|" + key + "|" + values.get(key) + "|");
            }
        }
        
        return values;
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
/*                      } else if (value instanceof PQ) {
                            text = ((PQ)value).getValue().toString();
                        } else if (value instanceof INT) {
                            text = ((INT)value).getValue().toString();
                        } else if (value instanceof TS) {
                            text = ((TS)value).getValue();
                        } else if (value instanceof BL) {
                            text = ((BL)value).isValue().toString();
                        } else if (value instanceof CD) {
                            text = ((CD)value).getCode();
                        } else if (value instanceof II) {
                            text = ((II)value).getRoot();
                        } else if (value instanceof IVLTS) {
                            // lkm-dst-bet is more complex
                            // split into two String values
                            // lkm-dst-bet-low, lkm-dst-bet-hiugh
                            String low = ((IVLTS)value).getLow().getValue();
                            if (StringUtils.isNotBlank(low)) {
                                values.put(code + "-low", low);
                            }
                            if (((IVLTS)value).getHigh().getNullFlavor() == null ) {
                                String high = ((IVLTS)value).getHigh().getValue();
                                if (StringUtils.isNotBlank(high)) {
                                    values.put(code + "-high", high);
                                }
                            }
*/                      } else {
                            log.error("Code " + code + " has unknown value type " + value.getClass().getCanonicalName());
                        }
                               
                        if (StringUtils.isNotBlank(text)) {
                           values.put(code, text);
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
}

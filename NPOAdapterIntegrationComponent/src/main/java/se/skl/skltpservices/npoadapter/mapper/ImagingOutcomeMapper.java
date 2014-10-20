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
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.actoutcome._3.CVType;
import riv.clinicalprocess.healthcond.actoutcome._3.ECGReferralType;
import riv.clinicalprocess.healthcond.actoutcome._3.HealthcareProfessionalType;
import riv.clinicalprocess.healthcond.actoutcome._3.IIType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImageDataType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImageRecordingType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImageStructuredDataType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImagingBodyType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._3.ResultType;
import riv.clinicalprocess.healthcond.actoutcome._3.TimePeriodType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.TypeOfResultCodeEnum;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.ObjectFactory;
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
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;

/**
 * Transformer for RIV-TA GetImagingOutcome -> EN13606 Informationsmangd UND-BDI
 * @author torbjorncla
 *
 */
@Slf4j
public class ImagingOutcomeMapper extends AbstractMapper implements Mapper {

	private static final JaxbUtil jaxb = new JaxbUtil(GetImagingOutcomeType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	
	private static final CD MEANING_UND_BDI = new CD();
	static {
		MEANING_UND_BDI.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_UND_BDI.setCode(INFO_UND_BDI);
	}
	
	private static final String VARDBEGARAN = "vbe";
	private static final String UNDERSOKNINGS_RESULTAT = "und";
	private static final String UND_SVARSTIDPUNKT = "und-und-ure-stp";
	
	
	@Override
	public MuleMessage mapRequest(MuleMessage message) throws MapperException {
		try {
			final GetImagingOutcomeType req = unmarshall(payloadAsXMLStreamReader(message));
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(req, MEANING_UND_BDI)));
			return message;
		}
		catch (Exception err) {
			throw new MapperException("Exception when mapping request", err);
		}
	}
	

	@Override
	public MuleMessage mapResponse(MuleMessage message) throws MapperException {
		try {
			final RIV13606REQUESTEHREXTRACTResponseType ehrResp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			final GetImagingOutcomeResponseType resp = mapResponseType(ehrResp, message.getUniqueId());
			message.setPayload(marshal(resp));
			return message;
		} catch (Exception err) {
			throw new MapperException("Exception when mapping response", err);
		}
	}
	
	public GetImagingOutcomeResponseType mapResponseType(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, final String uniqueId) {
		final GetImagingOutcomeResponseType resp = new GetImagingOutcomeResponseType();
		resp.setResult(EHRUtil.resultType(uniqueId, ehrResp.getResponseDetail(), ResultType.class));
		if (ehrResp.getEhrExtract().isEmpty()) {
			return resp;
		}
		final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
		final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
		
		for (COMPOSITION comp : ehrExtract.getAllCompositions()) {
			if (StringUtils.equals(EHRUtil.getCDCode(comp.getMeaning()), UNDERSOKNINGS_RESULTAT)) {
				final COMPOSITION und = comp;
				final COMPOSITION vbe = EHRUtil.findCompositionByLink(ehrExtract.getAllCompositions(), EHRUtil.firstItem(und.getContent()).getLinks(), VARDBEGARAN);
				final ImagingOutcomeType type = new ImagingOutcomeType();

				type.setImagingOutcomeHeader(EHRUtil.patientSummaryHeader(und, sharedHeaderExtract, UND_SVARSTIDPUNKT, PatientSummaryHeaderType.class));
				if (StringUtils.isEmpty(type.getImagingOutcomeHeader().getAccountableHealthcareProfessional().getAuthorTime())) {
				    type.getImagingOutcomeHeader().getAccountableHealthcareProfessional().setAuthorTime("TODO");
				}
		        
				Map<String,String> ehr13606values = getEhr13606values(und,vbe);
		        type.setImagingOutcomeBody(mapBody(ehr13606values));
				
		        resp.getImagingOutcome().add(type);
			}
		}
        return resp;
	}
	
	
    private ImagingBodyType mapBody(Map<String, String> ehr13606values) {

        ImagingBodyType body = new ImagingBodyType();

        if (ehr13606values.containsKey("und-und-ure-typ")) {
            try {
                body.setTypeOfResult(TypeOfResultCodeEnum.valueOf(ehr13606values.get("und-und-ure-typ")));
            } catch (IllegalArgumentException iae) {
                log.error("Received unexpected Svarstype und-und-ure-typ:" + ehr13606values.get("und-und-ure-typ"));
            }
        }
        
        body.setResultTime(ehr13606values.get("und-und-ure-stp"));
        
        body.setResultReport(ehr13606values.get("und-und-ure-utl"));

        // ---
        
        body.getImageRecording().add(new ImageRecordingType());
        body.getImageRecording().get(0).setRecordingId(new IIType());
        body.getImageRecording().get(0).getRecordingId().setRoot(ehr13606values.get("vbe-rc-id"));
        
        body.getImageRecording().get(0).setExaminationActivity(new CVType());
        body.getImageRecording().get(0).getExaminationActivity().setCode(ehr13606values.get("und-und-uat-txt"));

        body.getImageRecording().get(0).setExaminationTimePeriod(new TimePeriodType());
        body.getImageRecording().get(0).getExaminationTimePeriod().setStart(ehr13606values.get("und-und-uat-txt-low"));
        body.getImageRecording().get(0).getExaminationTimePeriod().setStart(ehr13606values.get("und-und-uat-txt-high"));
        
        body.getImageRecording().get(0).setExaminationUnit(ehr13606values.get("und-bdi-ure-lab"));
        
        body.getImageRecording().get(0).setAccountableHealthcareProfessional(new HealthcareProfessionalType());
        body.getImageRecording().get(0).getAccountableHealthcareProfessional().setAuthorTime("TODO");

        body.getImageRecording().get(0).getImageStructuredData().add(new ImageStructuredDataType());
        body.getImageRecording().get(0).getImageStructuredData().get(0).setImageData(new ImageDataType());
        body.getImageRecording().get(0).getImageStructuredData().get(0).getImageData().setReference(ehr13606values.get("und-und-res-und")); // TODO
        body.getImageRecording().get(0).getImageStructuredData().get(0).getImageData().setMediaType("TODO");
        
        // ---
        
        body.setReferral(new ECGReferralType());
        body.getReferral().setReferralId(ehr13606values.get("vbe-rc-id"));
        body.getReferral().setReferralReason(ehr13606values.get("vbe-vbe-fst"));
        body.getReferral().setCareContactId("TODO");

        body.getReferral().setAccountableHealthcareProfessional(new HealthcareProfessionalType());
        body.getReferral().getAccountableHealthcareProfessional().setAuthorTime(ehr13606values.get("vbe-committal-timecommitted"));
        if (StringUtils.isBlank(body.getReferral().getAccountableHealthcareProfessional().getAuthorTime())) {
            body.getReferral().getAccountableHealthcareProfessional().setAuthorTime("TODO");
        }

        // --- 
        
        return body;
    }

    private Map<String, String> getEhr13606values(COMPOSITION und, COMPOSITION vbe) {

        Map<String,String> ehr13606values = new LinkedHashMap<String,String>();
        
        // parse this composition into values stored in a Map
        retrieveValues(und, ehr13606values);
        retrieveValues(vbe, ehr13606values);
        
        if (log.isDebugEnabled()) {
            Iterator<String> it = ehr13606values.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                log.debug("|" + key + "|" + ehr13606values.get(key) + "|");
            }
        }
        return ehr13606values;
    }

    // Retrieve ehr values from message and store in a map
    private void retrieveValues(COMPOSITION composition, Map<String,String> values) {

        if (composition != null) {
            for (final CONTENT content : composition.getContent()) {
                for (final ITEM item : ((ENTRY) content).getItems()) {
                    retrieveItemValue(item, values);
                }
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
                        values.put("vbe-composer-performer-root", composition.getComposer().getPerformer().getRoot());
                    }
                }
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
                        } else if (value instanceof TS) {
                            text = ((TS)value).getValue();
                        } else {
                            log.error("Code " + code + " has unknown value type " + value.getClass().getCanonicalName());
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
	
	
	protected String marshal(final GetImagingOutcomeResponseType resp) {
		final JAXBElement<GetImagingOutcomeResponseType> el = objFactory.createGetImagingOutcomeResponse(resp);
		return jaxb.marshal(el);
	}
	
	protected GetImagingOutcomeType unmarshall(final XMLStreamReader reader) {
		try {
			return (GetImagingOutcomeType) jaxb.unmarshal(reader);
		} finally {
			close(reader);
		}
	}

}

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

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import riv.clinicalprocess.healthcond.actoutcome._3.OrgUnitType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._3.ResultType;
import riv.clinicalprocess.healthcond.actoutcome._3.TimePeriodType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.MediaTypeEnum;
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
import se.rivta.en13606.ehrextract.v11.LINK;
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
 * Transformer for RIV-TA GetImagingOutcome -> EN13606 Informationsmangd UND-BDI
 * 
 * @author torbjorncla
 */
public class ImagingOutcomeMapper extends AbstractMapper implements Mapper {
	
	private static final Logger log = LoggerFactory.getLogger(ImagingOutcomeMapper.class);

	private static final JaxbUtil jaxb = new JaxbUtil(GetImagingOutcomeType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	
	protected static final CD MEANING_UND_BDI = new CD();
	static {
		MEANING_UND_BDI.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_UND_BDI.setCode(INFO_UND_BDI);
	}
	
	private static final String VARDBEGARAN = "vbe";
	private static final String UNDERSOKNINGS_RESULTAT = "und";
	private static final String UND_SVARSTIDPUNKT = "und-und-ure-stp";
	
    public ImagingOutcomeMapper() {
    	schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-IMAGINGOUTCOME"));
        log.debug("schema validation is activated? " + schemaValidationActivated);
        
        initialiseValidator("/core_components/clinicalprocess_healthcond_actoutcome_enum_3.1.xsd",
                            "/core_components/clinicalprocess_healthcond_actoutcome_3.1_ext.xsd",
                            "/core_components/clinicalprocess_healthcond_actoutcome_3.1.xsd",
                            "/interactions/GetImagingOutcomeInteraction/GetImagingOutcomeResponder_1.0.xsd");
    }


	
	protected String marshal(final GetImagingOutcomeResponseType resp) {
		final JAXBElement<GetImagingOutcomeResponseType> el = objFactory.createGetImagingOutcomeResponse(resp);
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, schemaValidator, log);
        return xml;
	}

	
	protected GetImagingOutcomeType unmarshal(final XMLStreamReader reader) {
		try {
			return (GetImagingOutcomeType) jaxb.unmarshal(reader);
		} finally {
			close(reader);
		}
	}


	@Override
	public MuleMessage mapRequest(MuleMessage message) throws MapperException {
		try {
			final GetImagingOutcomeType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_UND_BDI, message.getUniqueId(), message.getInvocationProperty("route-logical-address"))));
			return message;
		} catch (Exception err) {
			if (err instanceof MapperException) {
				throw err;
			} else {
				throw new MapperException("Exception when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
			}
		}
	}
	

	@Override
	public MuleMessage mapResponse(MuleMessage message) throws MapperException {
		try {
			final RIV13606REQUESTEHREXTRACTResponseType ehrResp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			final GetImagingOutcomeResponseType resp = mapResponse(ehrResp, message);
			message.setPayload(marshal(resp));
			return message;
		} catch (Exception err) {
			if (err instanceof MapperException) {
				throw err;
			} else {
				throw new MapperException("Exception when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
			}
		}
	}
	
	public GetImagingOutcomeResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, MuleMessage message) {
		final GetImagingOutcomeResponseType resp = new GetImagingOutcomeResponseType();
		resp.setResult(EHRUtil.resultType(message.getUniqueId(), ehrResp.getResponseDetail(), ResultType.class));
		
		if (ehrResp.getEhrExtract().isEmpty()) {
			log.debug("Empty ehrResp");
			return resp;
		}
		final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
		final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);
		
		// Process a list of compositions.
		// Compositions come in pairs ("und","vbe")
		for (COMPOSITION composition13606 : ehrExtract.getAllCompositions()) {
			if (StringUtils.equals(EHRUtil.getCDCode(composition13606.getMeaning()), UNDERSOKNINGS_RESULTAT)) {
	            if (EHRUtil.retain(composition13606, careUnitHsaIds, log)) {
			    
    				final COMPOSITION und = composition13606;
    				final COMPOSITION vbe = EHRUtil.findCompositionByLink(ehrExtract.getAllCompositions(), EHRUtil.firstItem(und.getContent()).getLinks(), VARDBEGARAN);
    				final ImagingOutcomeType type = new ImagingOutcomeType();
    
    				type.setImagingOutcomeHeader(EHRUtil.patientSummaryHeader(und, sharedHeaderExtract, UND_SVARSTIDPUNKT, PatientSummaryHeaderType.class, false, true, true));
    				Map<String,String> ehr13606values = getEhr13606values(und,vbe);
    				
    				
    		        type.setImagingOutcomeBody(mapBody(ehr13606values, und));
    		        
    		        //Map VBE healthcarepro
    		        if(vbe.getComposer() != null) {
    		        		final HealthcareProfessional hp = EHRUtil.healthcareProfessionalType(vbe.getComposer(), 
    		        				sharedHeaderExtract.organisations(), sharedHeaderExtract.healthcareProfessionals(), null);
    		        		if(type.getImagingOutcomeBody().getReferral() != null) {
    		        			final HealthcareProfessionalType htp = new HealthcareProfessionalType();
    		        			
    		        			//htp.setAuthorTime(value);
    		        			htp.setHealthcareProfessionalCareGiverHSAId(hp.getHealthcareProfessionalCareGiverHSAId());
    		        			htp.setHealthcareProfessionalCareUnitHSAId(hp.getHealthcareProfessionalCareUnitHSAId());
    		        			htp.setHealthcareProfessionalHSAId(hp.getHealthcareProfessionalHSAId());
    		        			htp.setHealthcareProfessionalName(hp.getHealthcareProfessionalName());
    		        			
    		        			if(hp.getHealthcareProfessionalOrgUnit() != null) {
    		        				htp.setHealthcareProfessionalOrgUnit(new OrgUnitType());
    		        				htp.getHealthcareProfessionalOrgUnit().setOrgUnitAddress(hp.getHealthcareProfessionalOrgUnit().getOrgUnitAddress());
    		        				htp.getHealthcareProfessionalOrgUnit().setOrgUnitEmail(hp.getHealthcareProfessionalOrgUnit().getOrgUnitEmail());
    		        				htp.getHealthcareProfessionalOrgUnit().setOrgUnitHSAId(hp.getHealthcareProfessionalOrgUnit().getOrgUnitHSAId());
    		        				htp.getHealthcareProfessionalOrgUnit().setOrgUnitLocation(hp.getHealthcareProfessionalOrgUnit().getOrgUnitLocation());
    		        				htp.getHealthcareProfessionalOrgUnit().setOrgUnitName(hp.getHealthcareProfessionalOrgUnit().getOrgUnitName());
    		        				htp.getHealthcareProfessionalOrgUnit().setOrgUnitTelecom(hp.getHealthcareProfessionalOrgUnit().getOrgUnitTelecom());
    		        			}
    		        			
    		        			if(hp.getHealthcareProfessionalRoleCode() != null) {
    		        				htp.setHealthcareProfessionalRoleCode(new CVType());
    		        				htp.getHealthcareProfessionalRoleCode().setCode(hp.getHealthcareProfessionalRoleCode().getCode());
    		        				htp.getHealthcareProfessionalRoleCode().setCodeSystem(hp.getHealthcareProfessionalRoleCode().getCodeSystem());
    		        				htp.getHealthcareProfessionalRoleCode().setCodeSystemName(hp.getHealthcareProfessionalRoleCode().getCodeSystemName());
    		        				htp.getHealthcareProfessionalRoleCode().setCodeSystemVersion(hp.getHealthcareProfessionalRoleCode().getCodeSystemVersion());
    		        				htp.getHealthcareProfessionalRoleCode().setDisplayName(hp.getHealthcareProfessionalRoleCode().getDisplayName());
    		        				htp.getHealthcareProfessionalRoleCode().setOriginalText(hp.getHealthcareProfessionalRoleCode().getOriginalText());
    		        			}
    		        			
    		        			type.getImagingOutcomeBody().getReferral().setAccountableHealthcareProfessional(htp);
    		        			
    		        			//Author time und-und-ure-stp from und
    		        			type.getImagingOutcomeBody().getReferral().getAccountableHealthcareProfessional().setAuthorTime(type.getImagingOutcomeBody().getResultTime());
    		        		}
    		        } 
    		        
    		        resp.getImagingOutcome().add(type);
	            }
			}
		}
        return resp;
	}
	
	
    private ImagingBodyType mapBody(Map<String, String> ehr13606values, final COMPOSITION und) {
    	
        ImagingBodyType body = new ImagingBodyType();

        if (ehr13606values.containsKey("und-und-ure-typ")) {
            try {
                body.setTypeOfResult(TypeOfResultCodeEnum.valueOf(ehr13606values.get("und-und-ure-typ")));
            } catch (IllegalArgumentException iae) {
                log.error("Received unexpected Svarstyp und-und-ure-typ:" + ehr13606values.get("und-und-ure-typ"));
            }
        } 
        
        body.setResultTime(ehr13606values.get("und-und-ure-stp"));
        
        body.setResultReport(ehr13606values.get("und-und-ure-utl"));

        // ---
        
        body.getImageRecording().add(new ImageRecordingType());
        body.getImageRecording().get(0).setRecordingId(new IIType());
        body.getImageRecording().get(0).getRecordingId().setExtension(und.getRcId().getExtension());
        body.getImageRecording().get(0).getRecordingId().setRoot(und.getRcId().getRoot());
        
        body.getImageRecording().get(0).setExaminationActivity(new CVType());
        body.getImageRecording().get(0).getExaminationActivity().setCode(ehr13606values.get("und-und-uat-kod"));

        body.getImageRecording().get(0).setExaminationTimePeriod(new TimePeriodType());
        body.getImageRecording().get(0).getExaminationTimePeriod().setStart(ehr13606values.get("und-und-uat-txt-low"));
        if (StringUtils.isNoneBlank(ehr13606values.get("und-und-uat-txt-high"))) {
            body.getImageRecording().get(0).getExaminationTimePeriod().setEnd(ehr13606values.get("und-und-uat-txt-high"));
        }
        
        body.getImageRecording().get(0).setExaminationUnit(ehr13606values.get("und-bdi-ure-lab"));

        if (StringUtils.isNotBlank(ehr13606values.get("und-und-res-und"))) {
            body.getImageRecording().get(0).getImageStructuredData().add(new ImageStructuredDataType());
            body.getImageRecording().get(0).getImageStructuredData().get(0).setImageData(new ImageDataType());
            // Assumption is that we receive text data - this will need to be confirmed by sample data
            body.getImageRecording().get(0).getImageStructuredData().get(0).getImageData().setMediaType(MediaTypeEnum.TEXT_PLAIN);
            body.getImageRecording().get(0).getImageStructuredData().get(0).getImageData().setValue(ehr13606values.get("und-und-res-und").getBytes(Charset.forName("UTF-8")));
        }
        
        // ---
        
        body.setReferral(new ECGReferralType());
        body.getReferral().setReferralId(ehr13606values.get("vbe-rc-id"));
        
        body.getReferral().setReferralReason(ehr13606values.get("vbe-vbe-fst"));

        body.getReferral().setAccountableHealthcareProfessional(new HealthcareProfessionalType());
        body.getReferral().getAccountableHealthcareProfessional().setAuthorTime(ehr13606values.get("vbe-committal-timecommitted"));

        // --- 
        
        return body;
    }

    
    // Retrieve values from the two compositions and store in a Map
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

    
    // Retrieve ehr values from this composition and store in a map
    private void retrieveValues(COMPOSITION composition, Map<String,String> values) {

        if (composition != null) {
            for (final CONTENT content : composition.getContent()) {
                for (final ITEM item : ((ENTRY) content).getItems()) {
                    retrieveItemValue(item, values);
                }
            }
            
            if ("vbe".equals(composition.getMeaning().getCode())) {
                // Following are three 'synthetic' keys for retrieving String values 
                // in incoming objects
                if (composition.getRcId() != null) {
                    if (StringUtils.isNotBlank(composition.getRcId().getExtension())) {
                        values.put("vbe-rc-id", composition.getRcId().getExtension());
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
                        values.put("vbe-composer-performer-root", composition.getComposer().getPerformer().getExtension());
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
                            // There are many other subtypes of Any, but we restrict ourselves to the ones we expect
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
}

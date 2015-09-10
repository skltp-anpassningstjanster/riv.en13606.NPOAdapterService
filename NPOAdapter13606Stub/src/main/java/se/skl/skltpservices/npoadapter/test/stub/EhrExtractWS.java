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
package se.skl.skltpservices.npoadapter.test.stub;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ParameterType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTCONTINUATIONRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTPortType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailType;
import se.rivta.en13606.ehrextract.v11.ResponseDetailTypeCodes;
import se.rivta.en13606.ehrextract.v11.ST;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.test.Util;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

/**
 * Test stub always returning a fix response.
 */
@WebService(serviceName = "RIV13606REQUEST_EHR_EXTRACT_Service",
        endpointInterface = "se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTPortType",
        portName = "RIV13606REQUEST_EHR_EXTRACT_Port",
        targetNamespace = "urn:riv13606:v1.1")
public class EhrExtractWS implements RIV13606REQUESTEHREXTRACTPortType {
	
	private static final Logger log = LoggerFactory.getLogger(EhrExtractWS.class);
	
	

    private static final String VKO = "vko";
    private static final String VOO = "voo";
    private static final String DIA = "dia";
    private static final String UND_KKM_KLI = "und-kkm-kli";
    private static final String UPP = "upp";
    private static final String LKM = "lkm-ord";
    private static final String UND_KON = "und-kon";
    private static final String UND_BDI = "und-bdi";
    
    //Public accessible for testing.
    public static final String NOT_IMPLEMENTED_YET_TEXT = "This function is not yet implemented";
    public static final String INTEGRATION_TEST_ERROR_TEXT = "This is an error message";
    public static final String PATIENT_ID_TRIGGER_ERROR = "triggerError";
    public static final String PATIENT_ID_TRIGGER_WARNING = "triggerWarning";
    public static final String PATIENT_ID_TRIGGER_INFO = "triggerInfo";
    
    private static final String RESET_CACHE_SSN= "reset-test-data";

    
    static Map<String, EHREXTRACT> responseCache = Collections.synchronizedMap(new HashMap<String, EHREXTRACT>());
        
    @PostConstruct
    public void loadTestData() {
    	log.info("Load test data");
    	try {
			resetCache();
		} catch (Exception e) {
			log.error("Error loading testdata", e);
		}
    }
    
    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTCONTINUATION(RIV13606REQUESTEHREXTRACTCONTINUATIONRequestType request) {
        return null;
    }

    // Adapter response timeout - defined in NPOAdapter-config.properties
    private long responseTimeout = 20000;
    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }
    
    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACT(RIV13606REQUESTEHREXTRACTRequestType request) {
    	if(request.getSubjectOfCareId() != null) {
    		if(StringUtils.equals(request.getSubjectOfCareId().getExtension(), RESET_CACHE_SSN)) {
    			final RIV13606REQUESTEHREXTRACTResponseType resp = new RIV13606REQUESTEHREXTRACTResponseType();
    			final ResponseDetailType detail = new ResponseDetailType();
    			final ST st = new ST();
    			try {
    				resetCache();
    				detail.setTypeCode(ResponseDetailTypeCodes.I);
        			st.setValue("testdata cache successfully reset");
    			} catch (Exception err) {
    				detail.setTypeCode(ResponseDetailTypeCodes.E);
    				st.setValue("failed to reset testdata cache, reason: " + err.getMessage());
    				log.error("Failed to reset cache: ", err);
    			}
    			
    			
    			detail.setText(st);
    			resp.getResponseDetail().add(detail);
    			return resp;
    		}
    	}
    	
    	
        final String hsaId = validate(request);
        try {
        // sleep between 5 and 10 seconds
        if ("slow".equals(hsaId)) {
            final long t = 5000 + (long)(Math.random() * 5000);
            log.info("Slow response, sleep for {} millis", t);
            Thread.sleep(t);
        }
        
        // sleep between 5 and 10 seconds
        if ("delayWithoutTimeout".equals(hsaId)) {

            // given a timeout of 20000, add a delay between 15000 and 18000 milliseconds
            
            long delayMilliseconds = (long) (responseTimeout * 0.75);
            delayMilliseconds = (long) (delayMilliseconds 
                                + (
                                    Math.random() 
                                     * 
                                    ((responseTimeout * 0.9) - (responseTimeout * 0.75)) 
                                  ));
            
            log.info("delayWithoutTimeout, sleep for {} millis", delayMilliseconds);
            Thread.sleep(delayMilliseconds);
        }
        
        // Sleep for longer than Adapter timeout.
        // Response will be sent to Adapter after Adapter has timedout
        if ("respondAfterAdapterTimeout".equals(hsaId)) {

            // given a timeout of 20000, add a delay between 21000 and 23000 milliseconds
            long delayMilliseconds = (long) (responseTimeout + 1000 + (Math.random() * 2000));
            
            log.info("respondAfterAdapterTimeout, sleep for {} millis", delayMilliseconds);
            Thread.sleep(delayMilliseconds);
        }
        
    	//See if error flow is triggered.
    	switch(request.getSubjectOfCareId().getExtension()) {
    	case PATIENT_ID_TRIGGER_ERROR:
    		return createAlternativeResponse(ResponseDetailTypeCodes.E, INTEGRATION_TEST_ERROR_TEXT);
    	case PATIENT_ID_TRIGGER_WARNING:
    		return createAlternativeResponse(ResponseDetailTypeCodes.W, NOT_IMPLEMENTED_YET_TEXT);
    	case PATIENT_ID_TRIGGER_INFO:
    		return createAlternativeResponse(ResponseDetailTypeCodes.I, NOT_IMPLEMENTED_YET_TEXT);
    	}


    	// Return testdata
    	final RIV13606REQUESTEHREXTRACTResponseType responseType = new RIV13606REQUESTEHREXTRACTResponseType();
    	/** Removed to support new dynamic testdata
    	if (!request.getSubjectOfCareId().getExtension().startsWith("19")) {
            log.info("SubjectOfCareId doesn't start with 19, simulate not found and return an empty response...");
            return responseType;
        }
        **/

        if (request.getTimePeriod() != null) {
            final Date ts = EHRUtil.parseTimePeriod(request.getTimePeriod().getLow().getValue());
            if (ts.after(new Date())) {
                log.info("Start time after current time, simulate not found and return an empty response...");
                return responseType;
            }
        }

        switch(request.getMeanings().get(0).getCode()) {
        case VKO:
        	log.info("Received vko request");
        	final String vkoKey = cacheKey(request, VKO);
        	log.info("Query cache for: " + vkoKey);
        	if(responseCache.containsKey(vkoKey)) {
        		responseType.getEhrExtract().add(responseCache.get(vkoKey));
        		log.info("Loaded dyanmictest data: " + vkoKey);
        	} else {
        		responseType.getEhrExtract().add(getTestData(Util.CARECONTACS_TEST_FILE));
        	}
        	break;
        case VOO:
        	log.info("Received voo request");
        	final String vooKey = cacheKey(request, VOO);
        	log.info("Query cache for: " + vooKey);
        	if(responseCache.containsKey(vooKey)) {
        		responseType.getEhrExtract().add(responseCache.get(vooKey));
        		log.info("Loaded dynamictest data: " + vooKey);
        	} else {
        		responseType.getEhrExtract().add(getTestData(Util.CAREDOCUMENTATION_TEST_FILE));
        	}
        	break;
        case DIA:
        	log.info("Received dia request");
        	final String diaKey = cacheKey(request, DIA);
        	log.info("Query cache for: " + diaKey);
        	if(responseCache.containsKey(diaKey)) {
        		responseType.getEhrExtract().add(responseCache.get(diaKey));
        		log.info("Loaded dynamictest data: " + diaKey);
        	} else {
        		responseType.getEhrExtract().add(getTestData(Util.DIAGNOSIS_TEST_FILE));
        	}
        	break;
        case UND_KKM_KLI:
        	log.info("Received und-kkm-kli request");
        	final String undKkmKey = cacheKey(request, UND_KKM_KLI);
        	log.info("Query cache for: " + undKkmKey);
        	if(responseCache.containsKey(undKkmKey)) {
        		responseType.getEhrExtract().add(responseCache.get(undKkmKey));
        	} else {
        		responseType.getEhrExtract().add(getTestData(Util.LAB_TEST_FILE));
        	}
        	break;
        case UPP:
        	log.info("Received upp request");
        	final String uppKey = cacheKey(request, UPP);
        	log.info("Query cache for: " + uppKey);
        	if(responseCache.containsKey(uppKey)) {
        		responseType.getEhrExtract().add(responseCache.get(uppKey));
        		log.info("Loaded dynamictest data: " + uppKey);
        	} else {
        		responseType.getEhrExtract().add(getTestData(Util.ALERT_TEST_FILE));        		
        	}
        	break;
        case LKM:
            log.info("Received lkm request");
            final String lkmKey = cacheKey(request, LKM);
            log.info("Query cache for: " + lkmKey);
            if(responseCache.containsKey(lkmKey)) {
            	responseType.getEhrExtract().add(responseCache.get(lkmKey));
            	log.info("Loaded dynamictest data: " + lkmKey);
            } else {
            	responseType.getEhrExtract().add(getTestData(Util.MEDICALHISTORY_TEST_FILE));
            }
            break;
        case UND_KON:
            log.info("Received und-kon request");
            final String undKonKey = cacheKey(request, UND_KON);
            log.info("Query cache for: " + undKonKey);
            if(responseCache.containsKey(undKonKey)) {
            	responseType.getEhrExtract().add(responseCache.get(undKonKey));
            	log.info("Loaded dynamictest data: " + undKonKey);
            } else {
            	responseType.getEhrExtract().add(getTestData(Util.REFERRALOUTCOME_TEST_FILE));
            }
            break;
        case UND_BDI:
            log.info("Received und-bdi request");
            final String undBdiKey = cacheKey(request, UND_BDI);
            log.info("Query cache for: " + undBdiKey);
            if(responseCache.containsKey(undBdiKey)) {
            	responseType.getEhrExtract().add(responseCache.get(undBdiKey));
            	log.info("Loaded dynamictest data: " + undBdiKey);
            } else {
            	if ("ExtraLarge".equals(hsaId)) {
            		// 1MB response - performance test case TP2
            		responseType.getEhrExtract().add(getTestData(Util.IMAGINGOUTCOME1MB_TEST_FILE));
            	} else {
            		responseType.getEhrExtract().add(getTestData(Util.IMAGINGOUTCOME_TEST_FILE));
            	}            	
            }
            break;
        default:
            log.error("Received unexpected request " + request.getMeanings().get(0).getCode());
        	return createAlternativeResponse(ResponseDetailTypeCodes.E, NOT_IMPLEMENTED_YET_TEXT);
        }
        
        return responseType;
        } catch (JAXBException err) {
        	log.error("Error parsing", err);
        } catch (InterruptedException in) {
        	log.error("Thread sleep error", in);
        } catch (java.text.ParseException e) {
        	log.error("Parse date error", e);
		}
        return createAlternativeResponse(ResponseDetailTypeCodes.E, "Error creating response");
    }

    /**
     * Validates request.
     * @param request the request.
     *
     * @return the HSA_ID if any.
     */
    private String validate(final RIV13606REQUESTEHREXTRACTRequestType request) {
        String version = null;
        String hsaId = null;
        for (final ParameterType param : request.getParameters()) {
            log.debug("RIV13606REQUESTEHREXTRACTRequestType.parameter {}: {}", param.getName().getValue(), param.getValue().getValue());
            if ("version".equals(param.getName().getValue())) {
                version = param.getValue().getValue();
            }
            if ("hsa_id".equals(param.getName().getValue())) {
                if (hsaId == null) {
                    hsaId = param.getValue().getValue();
                }
            }
        }
        if (request.getSubjectOfCareId() == null) {
            throw new IllegalArgumentException("Subject of care must be defined");
        }
        if (!"1.1".equals(version)) {
            throw new IllegalArgumentException("Invalid version parameter: " + version);
        }
        if (request.getMeanings().size() != 1) {
            throw new IllegalArgumentException("Invalid size of meanings list (exactly 1 meaning must be defined): " + request.getMeanings());
        }
        if (request.getMaxRecords() != null) {
            throw new IllegalArgumentException("Max records shall not be defined");
        }

        log.debug("hsaId:" + hsaId);
        
        return hsaId;
    }

    //
    protected EHREXTRACT getTestData(final String path) throws JAXBException {
        EHREXTRACT ehrextract = responseCache.get(path);
        if (ehrextract == null) {
            ehrextract = Util.loadEhrTestData(path);
            responseCache.put(path, ehrextract);
        }
    	return ehrextract;
    }
        
    protected void resetCache() throws Exception {
    	final String path = SpringPropertiesUtil.getProperty("EHR_TESTDATA_PATH");
    	log.info("Reset testdata cache, load from: " + path);
    	responseCache.clear();
    	Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				try {
					responseCache.put(file.getFileName().toString(), Util.loadDynamicTestData(file));
					log.info("Cached file: " + file.getFileName().toString());
				} catch (JAXBException err) {
					log.error("File: " + file.toString(), err);
				}
				return super.visitFile(file, attrs);
			}
		});
    }

    //
    protected RIV13606REQUESTEHREXTRACTResponseType createAlternativeResponse(final ResponseDetailTypeCodes code, final String msg) {
        log.info("Trigger detected to return an alternative response {}: {}", code, msg);
        final RIV13606REQUESTEHREXTRACTResponseType resp = new RIV13606REQUESTEHREXTRACTResponseType();
    	final ResponseDetailType detail = new ResponseDetailType();
    	final ST st = new ST();
    	final CD cd = new CD();
    	st.setValue(msg);
    	detail.setTypeCode(code);
    	detail.setCode(cd);
    	detail.setText(st);
    	resp.getResponseDetail().add(detail);
    	return resp;
    }
    
    protected RIV13606REQUESTEHREXTRACTResponseType missingDataResponse(final String ssn, final String info) {
    	log.error("Missing test-data-file: %s-%s.xml", ssn, info);
        final RIV13606REQUESTEHREXTRACTResponseType resp = new RIV13606REQUESTEHREXTRACTResponseType();
    	final ResponseDetailType detail = new ResponseDetailType();
    	final ST st = new ST();
    	st.setValue(String.format("Missing test-data-file: %s-%s.xml", ssn, info));
    	detail.setTypeCode(ResponseDetailTypeCodes.E);
    	detail.setText(st);
    	resp.getResponseDetail().add(detail);
    	return resp;
    }
    
    protected String cacheKey(final RIV13606REQUESTEHREXTRACTRequestType req, final String info) {
    	return String.format("%s-%s.xml", req.getSubjectOfCareId().getExtension(), info);
    }
}

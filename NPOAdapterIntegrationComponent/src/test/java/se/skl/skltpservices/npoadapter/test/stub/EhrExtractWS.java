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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.jws.WebService;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/*
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
   <RIV13606REQUEST_EHR_EXTRACT_request xmlns="urn:riv13606:v1.1">
    <subject_of_care_id extension="191212121212" root="1.2.752.129.2.1.3"/>
    <meanings codeSystem="1.2.752.129.2.2.2.1" code="dia"/>
    <parameters>
     <name value="hsa_id"/>
     <value value="something"/>
    </parameters>
    <parameters>
     <name value="transaction_id"/>
     <value value="something else"/>
    </parameters>
    <parameters>
     <name value="version"/>
     <value value="1.1"/>
    </parameters>
   </RIV13606REQUEST_EHR_EXTRACT_request>
  </soap:Body>
 </soap:Envelope>"
 */

/**
 * Test stub returns fixed responses for given parameters.
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

    // Public accessible for testing.
    public static final String NOT_IMPLEMENTED_YET_TEXT    = "This function is not yet implemented";
    public static final String INTEGRATION_TEST_ERROR_TEXT = "This is an error message";
    public static final String PATIENT_ID_TRIGGER_ERROR    = "triggerError"; // TF4
    public static final String PATIENT_ID_TRIGGER_WARNING  = "triggerWarning";
    public static final String PATIENT_ID_TRIGGER_INFO     = "triggerInfo";

    public static final String PATIENT_ID_SLOW             = "191212120001";
    public static final String PATIENT_ID_DELAY_NO_TIMEOUT = "191212120002";
    public static final String PATIENT_ID_DELAY_TIMEOUT    = "191212120003";
    public static final String PATIENT_ID_EXTRA_LARGE      = "191212120004";
    public static final String PATIENT_ID_NOT_FOUND        = "191212120005"; // TF2
    
    public static final String PATIENT_ID_RESET_CACHE      = "191212120099";
    

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

 
    // --- ---
    // Adapter response timeout - defined in NPOAdapter-config.properties - SERVICE_TIMEOUT_MS=30000

    // default
    private long responseTimeout = 31000;

    // can be overriden in -config-override.properties
    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }
    

    @Override
    public RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACT(RIV13606REQUESTEHREXTRACTRequestType request) {

        if (request.getTimePeriod() == null) {
            log.info("received request without timePeriod");
        } else {
            log.info("received request timePeriod.low :" + request.getTimePeriod().getLow().getValue());
            log.info("received request timePeriod.high:" + request.getTimePeriod().getHigh().getValue());

            try {
                final Date ts = EHRUtil.parseTimePeriod(request.getTimePeriod().getLow().getValue());
                if (ts.after(new Date())) {
                    log.info("Start time after current time, simulate not found and return an empty response..."); // TF3
                    return new RIV13606REQUESTEHREXTRACTResponseType();
                }
            } catch (ParseException p) {
                throw new IllegalArgumentException("Invalid date " + request.getTimePeriod().getLow().getValue()); // TF7
            }
        }
        
        
        // final String hsaId = validate(request);
        final String subjectOfCareId = validateAndReturnSubjectOfCareId(request);

        if (request.getSubjectOfCareId() != null) {
            if (PATIENT_ID_RESET_CACHE.equals(subjectOfCareId)) {
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
        
        try {
            // sleep between 5 and 10 seconds
            if (PATIENT_ID_SLOW.equals(subjectOfCareId)) {
                final long t = 5000 + (long) (Math.random() * 5000);
                log.info("Slow response, sleep for {} millis", t);
                Thread.sleep(t);
            }

            // sleep between 5 and 10 seconds
            if (PATIENT_ID_DELAY_NO_TIMEOUT.equals(subjectOfCareId)) {

                // given a timeout of 20000, add a delay between 15000 and 18000 milliseconds

                long delayMilliseconds = (long) (responseTimeout * 0.75);
                delayMilliseconds = (long) (delayMilliseconds + (Math.random() * ((responseTimeout * 0.9) - (responseTimeout * 0.75))));

                log.info("delayWithoutTimeout, sleep for {} millis", delayMilliseconds);
                Thread.sleep(delayMilliseconds);
            }

            // Sleep for longer than Adapter timeout.
            // Response will be sent to Adapter after Adapter has timed-out
            if (PATIENT_ID_DELAY_TIMEOUT.equals(subjectOfCareId)) {

                // given a timeout of 20000, add a delay between 21000 and 23000 milliseconds
                long delayMilliseconds = (long) (responseTimeout + 1000 + (Math.random() * 2000));

                log.info("respondAfterAdapterTimeout, sleep for {} millis", delayMilliseconds);
                Thread.sleep(delayMilliseconds);
            }

            switch (subjectOfCareId) {
                case PATIENT_ID_TRIGGER_ERROR:
                    return createResponseWithCodeAndMessage(ResponseDetailTypeCodes.E, INTEGRATION_TEST_ERROR_TEXT); // TF4
                case PATIENT_ID_TRIGGER_WARNING:
                    return createResponseWithCodeAndMessage(ResponseDetailTypeCodes.W, NOT_IMPLEMENTED_YET_TEXT);
                case PATIENT_ID_TRIGGER_INFO:
                    return createResponseWithCodeAndMessage(ResponseDetailTypeCodes.I, NOT_IMPLEMENTED_YET_TEXT);
                case PATIENT_ID_NOT_FOUND:
                    log.debug("patient id not found");
                    return new RIV13606REQUESTEHREXTRACTResponseType();
            }


            final RIV13606REQUESTEHREXTRACTResponseType responseType = new RIV13606REQUESTEHREXTRACTResponseType();
            switch(request.getMeanings().get(0).getCode()) {
            case VKO:
                log.info("Received vko request");
                final String vkoKey = cacheKey(request, VKO);
                log.info("Query cache for: " + vkoKey);
                if(responseCache.containsKey(vkoKey)) {
                    responseType.getEhrExtract().add(responseCache.get(vkoKey));
                    log.info("Loaded dyanmictest data: " + vkoKey);
                } else {
                    responseType.getEhrExtract().add(getTestData(Util.CARECONTACTS_TEST_FILE_1, subjectOfCareId));
                }
                responseType.setContinuationToken(new ST());
                responseType.getContinuationToken().setValue("stub");
                break;
            case VOO:
                log.info("Received voo request");
                final String vooKey = cacheKey(request, VOO);
                log.info("Query cache for: " + vooKey);
                if(responseCache.containsKey(vooKey)) {
                    responseType.getEhrExtract().add(responseCache.get(vooKey));
                    log.info("Loaded dynamictest data: " + vooKey);
                } else {
                    responseType.getEhrExtract().add(getTestData(Util.CAREDOCUMENTATION_TEST_FILE, subjectOfCareId));
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
                    responseType.getEhrExtract().add(getTestData(Util.DIAGNOSIS_TEST_FILE, subjectOfCareId));
                }
                break;
            case UND_KKM_KLI:
                log.info("Received und-kkm-kli request");
                final String undKkmKey = cacheKey(request, UND_KKM_KLI);
                log.info("Query cache for: " + undKkmKey);
                if(responseCache.containsKey(undKkmKey)) {
                    responseType.getEhrExtract().add(responseCache.get(undKkmKey));
                } else {
                    responseType.getEhrExtract().add(getTestData(Util.LAB_TEST_FILE_1, subjectOfCareId));
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
                    responseType.getEhrExtract().add(getTestData(Util.ALERT_TEST_FILE, subjectOfCareId));                
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
                    responseType.getEhrExtract().add(getTestData(Util.MEDICATIONHISTORY_TEST_FILE_1, subjectOfCareId));
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
                    responseType.getEhrExtract().add(getTestData(Util.REFERRALOUTCOME_TEST_FILE_1, subjectOfCareId));
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
                    if (PATIENT_ID_EXTRA_LARGE.equals(subjectOfCareId)) {
                        // 1MB response - performance test case TP2
                        log.info("Received UND-BDI Request extra large");
                        responseType.getEhrExtract().add(getTestData(Util.IMAGINGOUTCOME1MB_TEST_FILE, subjectOfCareId));
                    } else {
                        log.info("Received UND-BDI Request");
                        responseType.getEhrExtract().add(getTestData(Util.IMAGINGOUTCOME_TEST_FILE, subjectOfCareId));
                    }
                }
                break;
                
            default:
                log.error("Received unexpected request " + request.getMeanings().get(0).getCode());
                return createResponseWithCodeAndMessage(ResponseDetailTypeCodes.E, NOT_IMPLEMENTED_YET_TEXT);
            }

            return responseType;
        } catch (JAXBException err) {
            log.error("Error parsing", err);
        } catch (InterruptedException in) {
            log.error("Thread sleep error", in);
        }
        return createResponseWithCodeAndMessage(ResponseDetailTypeCodes.E, "Error creating response");
    }


    /**
     * @return subjectOfCareId
     * @throws exception if request does not follow stub contract
     */
    private String validateAndReturnSubjectOfCareId(final RIV13606REQUESTEHREXTRACTRequestType request) {
        String version = null;
        for (final ParameterType param : request.getParameters()) {
            log.debug("RIV13606REQUESTEHREXTRACTRequestType.parameter {}: {}", param.getName().getValue(), param.getValue().getValue());
            if ("version".equals(param.getName().getValue())) {
                version = param.getValue().getValue();
            }
        }
        if (request.getSubjectOfCareId() == null) {
            throw new IllegalArgumentException("Subject of care must be defined");
        }
        if (StringUtils.isBlank(request.getSubjectOfCareId().getExtension())) {
            throw new IllegalArgumentException("Subject of care must not be blank");
        }
        if (!"1.1".equals(version)) {
            throw new IllegalArgumentException("Invalid version parameter: " + version);
        }
        if (request.getMeanings().size() != 1) {
            throw new IllegalArgumentException("Invalid size of meanings list (exactly 1 meaning must be defined): " + request.getMeanings());
        }
        if (request.getMaxRecords() != null) {
            throw new IllegalArgumentException("Max records must not be defined");
        }

        return request.getSubjectOfCareId().getExtension();
    }


    //
    private EHREXTRACT getTestData(final String path, final String subjectOfCareId) throws JAXBException {
        EHREXTRACT ehrExtract = responseCache.get(path);
        if (ehrExtract == null) {
            ehrExtract = Util.loadEhrTestData(path);
            responseCache.put(path, ehrExtract);
        }
        
        if (StringUtils.isNotBlank(subjectOfCareId)) {
            if (ehrExtract != null) {
                if (ehrExtract.getSubjectOfCare() != null) {
                    ehrExtract.getSubjectOfCare().setExtension(subjectOfCareId);
                }
            }
        }
        return ehrExtract;
    }

    //
    protected void resetCache() throws Exception {
        log.info("reset testdata cache");
        responseCache.clear();
        
        final String path = SpringPropertiesUtil.getProperty("EHR_TESTDATA_PATH");
        if (StringUtils.isBlank(path)) {
            log.warn("No value for property EHR_TESTDATA_PATH");
        } else {
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
            log.info("Finished loading testdata cache from: " + path);
        }
    }

    //
    protected RIV13606REQUESTEHREXTRACTResponseType createResponseWithCodeAndMessage(final ResponseDetailTypeCodes code, final String msg) {
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

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

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstracts all mapper implementations.
 *
 * @see se.skl.skltpservices.npoadapter.mapper.Mapper
 * @see se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper
 *
 * @author Peter
 */
@Slf4j
public abstract class AbstractMapper {

    // context for baseline (en 13606)
    private static final JaxbUtil enEhrExtractTypeJaxbUtil = new JaxbUtil("se.rivta.en13606.ehrextract.v11");
    private static final ObjectFactory enObjectFactory = new ObjectFactory();

    // context for the riv alternative
    private static final JaxbUtil rivEhrExtractTypeJaxbUtil = new JaxbUtil("riv.itintegration.registry._1:riv.ehr.patientsummary._1:riv.ehr.patientsummary.getehrextractresponder._1");
    private static final riv.ehr.patientsummary.getehrextractresponder._1.ObjectFactory rivEhrExtractTypeObjectFactory = new riv.ehr.patientsummary.getehrextractresponder._1.ObjectFactory();

    // main supported information types,
    public static final String INFO_VKO = "vko";
    public static final String INFO_VOO = "voo";
    public static final String INFO_DIA = "dia";
    public static final String INFO_LKF = "lkf";
    public static final String INFO_LKM = "lkm";
    public static final String INFO_LKO = "lko";
    public static final String INFO_UND_KKM_KLI = "und-kkm-kli";
    public static final String INFO_UND_BDI = "und-bdi";
    public static final String INFO_UND_KON = "und-kon";
    public static final String INFO_UPP = "upp";

    static final String NS_CARECONTACTS_2 = "urn:riv:clinicalprocess:logistics:logistics:GetCareContacts:2:rivtabp21";
    static final String NS_CAREDOCUMENTATION_2 = "urn:riv:clinicalprocess:healthcond:description:GetCareDocumentation:2:rivtabp21";
    static final String NS_DIAGNOSIS_2 = "urn:riv:clinicalprocess:healthcond:description:GetDiagnosis:2:rivtabp21";
    static final String NS_EN_EXTRACT = "urn:riv13606:v1.1:RIV13606REQUEST_EHR_EXTRACT";
    static final String NS_RIV_EXTRACT = "urn:riv:ehr:patientsummary:GetEhrExtractResponder:1:GetEhrExtract:rivtabp21";
    static final String NS_LABORATORY_3 = "urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcome:3:rivtabp21";
    static final String NS_IMAGING_1 = "urn:riv:clinicalprocess:healthcond:actoutcome:GetImagingOutcome:1:rivtabp21";
    static final String NS_ALERT_2 = "urn:riv:clinicalprocess:healthcond:description:GetAlertInformation:2:rivtabp21";
    static final String NS_MEDICATIONHISTORY = "urn:riv:clinicalprocess:activityprescription:actoutcome:GetMedicationHistory:2:rivtabp21";
    static final String NS_REFERRALOUTCOME = "urn:riv:clinicalprocess:healthcond:actoutcome:GetReferralOutcome:3:rivtabp21";

    
    // mapper implementation hash map with RIV service contract operation names (from WSDL) as a key
    private static final HashMap<String, Mapper> map = new HashMap<String, Mapper>();
    static {
        // contacts
        map.put(mapperKey(NS_EN_EXTRACT, NS_CARECONTACTS_2), new CareContactsMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_CARECONTACTS_2), new RIVCareContactsMapper());

        // docs
        map.put(mapperKey(NS_EN_EXTRACT, NS_CAREDOCUMENTATION_2), new CareDocumentationMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_CAREDOCUMENTATION_2), new RIVCareDocumentationMapper());
        
        //dia
        map.put(mapperKey(NS_EN_EXTRACT, NS_DIAGNOSIS_2), new DiagnosisMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_DIAGNOSIS_2), new RIVDiagnosisMapper());
        
        //lab
        map.put(mapperKey(NS_EN_EXTRACT, NS_LABORATORY_3), new LaboratoryOrderOutcomeMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_LABORATORY_3), new RIVLaboratoryOrderOutcomeMapper());
        
        //imaging
        map.put(mapperKey(NS_EN_EXTRACT, NS_IMAGING_1), new ImagingOutcomeMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_IMAGING_1), new RIVImagingOutcomeMapper());
        
        //alertinfo
        map.put(mapperKey(NS_EN_EXTRACT, NS_ALERT_2), new AlertInformationMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_ALERT_2), new RIVAlertInformationMapper());
        
        //medicationhistory
        map.put(mapperKey(NS_EN_EXTRACT, NS_MEDICATIONHISTORY), new MedicationHistoryMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_MEDICATIONHISTORY), new RIVMedicationHistoryMapper());
        
        //referraloutcome
        map.put(mapperKey(NS_EN_EXTRACT, NS_REFERRALOUTCOME), new ReferralOutcomeMapper());
        map.put(mapperKey(NS_RIV_EXTRACT, NS_REFERRALOUTCOME), new RIVReferralOutcomeMapper());
    }


    /**
     * Returns the actual mapper instance by the name of the (inbound SOAP) service operation.
     *
     * @param sourceNS the source service contract namespace.
     * @param  targetNS the target service contract namespace.
     * @return the corresponding mapper.
     * @throws java.lang.IllegalStateException when no mapper matches the name of the operation.
     */
    public static Mapper getInstance(final String sourceNS, final String targetNS) {
        assert (sourceNS != null) && (targetNS != null);
        final String key = mapperKey(sourceNS, targetNS);
        final Mapper mapper = map.get(key);
        log.debug("Lookup mapper for key: \"{}\" -> {}", key, mapper);
        if (mapper == null) {
            throw new IllegalStateException("Unable to lookup mapper for operation: \"" + key + "\"");
        }
        return mapper;
    }

    /**
     * Returns the {@link javax.xml.stream.XMLStreamReader} from the message.
     *
     * @param message the message.
     * @return the payload as the expected reader.
     */
    protected XMLStreamReader payloadAsXMLStreamReader(final MuleMessage message) {
        if (message.getPayload() instanceof Object[]) {
            final Object[] payload = (Object[]) message.getPayload();
            if (payload.length > 1 && payload[1] instanceof XMLStreamReader) {
                return (XMLStreamReader) payload[1];
            }
        } else if (message.getPayload() instanceof  XMLStreamReader) {
            return (XMLStreamReader) message.getPayload();
        }
        throw new IllegalArgumentException("Unexpected type of message payload (an Object[] with XMLStreamReader was expected): " + message.getPayload());
    }


    //
    private static String mapperKey(final String src, final String dst) {
        return src + "-" + dst;
    }

    //
    protected RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType(final XMLStreamReader reader) {
        try {
            return (RIV13606REQUESTEHREXTRACTResponseType) enEhrExtractTypeJaxbUtil.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    //
    protected String riv13606REQUESTEHREXTRACTRequestType(final RIV13606REQUESTEHREXTRACTRequestType request) {
        final JAXBElement<RIV13606REQUESTEHREXTRACTRequestType> el = enObjectFactory.createRIV13606REQUESTEHREXTRACTRequest(request);
        return enEhrExtractTypeJaxbUtil.marshal(el);
    }

    //
    protected GetEhrExtractResponseType ehrExtractResponseType(final XMLStreamReader reader) {
        try {
            return (GetEhrExtractResponseType) rivEhrExtractTypeJaxbUtil.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    //
    protected String ehrExtractType(final GetEhrExtractType request) {
        final JAXBElement<GetEhrExtractType> el = rivEhrExtractTypeObjectFactory.createGetEhrExtract(request);
        return rivEhrExtractTypeJaxbUtil.marshal(el);
    }


    //
    protected void close(final XMLStreamReader reader) {
        try {
            reader.close();
        } catch (XMLStreamException | NullPointerException e) {
            ;
        }
    }
    
    protected SharedHeaderExtract extractInformation(final EHREXTRACT ehrExtract) {
    	final Map<String, ORGANISATION> orgs = new HashMap<String, ORGANISATION>();
		final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps = new HashMap<String, IDENTIFIEDHEALTHCAREPROFESSIONAL>();
		
		for(IDENTIFIEDENTITY entity : ehrExtract.getDemographicExtract()) {
			if(entity instanceof ORGANISATION) {
				final ORGANISATION org = (ORGANISATION) entity;
				if(org.getExtractId() != null) {
					orgs.put(org.getExtractId().getExtension(), org);
				}
			}
			if(entity instanceof IDENTIFIEDHEALTHCAREPROFESSIONAL) {
				final IDENTIFIEDHEALTHCAREPROFESSIONAL hp = (IDENTIFIEDHEALTHCAREPROFESSIONAL) entity;
				if(hp.getExtractId() != null) {
					hps.put(hp.getExtractId().getExtension(), hp);
				}
			}
		}
		return new SharedHeaderExtract(orgs, hps, EHRUtil.getSystemHSAId(ehrExtract), ehrExtract.getSubjectOfCare());
    }

}

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
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.*;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;

/**
 * Abstracts all @{link Mapper} implementations.
 *
 * @see {@link se.skl.skltpservices.npoadapter.mapper.XMLBeanMapper} for bean namespace/package mapping.
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


    static final String NS_CARECONTACTS_2 = "urn:riv:clinicalprocess:logistics:logistics:GetCareContacts:2:rivtabp21";
    static final String NS_CAREDOCUMENTATION_2 = "urn:riv:clinicalprocess:healthcond:description:GetCareDocumentation:2:rivtabp21";
    static final String NS_DIAGNOSIS_2 = "urn:riv:clinicalprocess:healthcond:description:GetDiagnosis:2:rivtabp21";
    static final String NS_EN_EXTRACT = "urn:riv13606:v1.1:RIV13606REQUEST_EHR_EXTRACT";
    static final String NS_RIV_EXTRACT = "urn:riv:ehr:patientsummary:GetEhrExtractResponder:1:GetEhrExtract:rivtabp21";
    static final String NS_LABORATORY_3 = "urn:riv:clinicalprocess:healthcond:actoutcome:GetLaboratoryOrderOutcome:3:rivtabp21";

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
            throw new IllegalStateException("NPOAdapter: Unable to lookup mapper for operation: \"" + key + "\"");
        }
        return mapper;
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

}

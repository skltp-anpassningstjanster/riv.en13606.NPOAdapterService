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

import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.dozer.loader.api.FieldDefinition;
import org.dozer.loader.api.TypeMappingBuilder;
import org.dozer.loader.api.TypeMappingOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import se.rivta.en13606.ehrextract.v11.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;

import static org.dozer.loader.api.TypeMappingOptions.*;

/**
 * Abstracts all @{link Mapper} implementations.
 *
 * @author Peter
 */
public abstract class AbstractMapper {

    //
    static DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    static {
        final BeanMappingBuilder builder = new BeanMappingBuilder() {


            @Override
            protected void configure() {

                typeMappingBuilder(
                        RIV13606REQUESTEHREXTRACTRequestType.class,
                        riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType.class,
                        new String[] { "meanings", "parameters", "archetypeIds", "rcIds" });

                typeMappingBuilder(
                        riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType.class,
                        RIV13606REQUESTEHREXTRACTResponseType.class,
                        new String[] { "ehrExtract", "responseDetail" });

                typeMappingBuilder(
                        riv.ehr.patientsummary._1.EHREXTRACT.class,
                        se.rivta.en13606.ehrextract.v11.EHREXTRACT.class,
                        new String[] { "allCompositions", "criteria", "folders", "demographicExtract" });

                typeMappingBuilder(
                        riv.ehr.patientsummary._1.COMPOSITION.class,
                        se.rivta.en13606.ehrextract.v11.COMPOSITION.class,
                        new String[] { "attestations", "otherParticipations", "content" });

                typeMappingBuilder(
                        riv.ehr.patientsummary._1.ATTESTATIONINFO.class,
                        se.rivta.en13606.ehrextract.v11.ATTESTATIONINFO.class,
                        new String[] { "targetRcId" });

                typeMappingBuilder(
                        riv.ehr.patientsummary._1.EXTRACTCRITERIA.class,
                        se.rivta.en13606.ehrextract.v11.EXTRACTCRITERIA.class,
                        new String[] { "archetypeIds" });

                typeMappingBuilder(
                        riv.ehr.patientsummary._1.FOLDER.class,
                        se.rivta.en13606.ehrextract.v11.FOLDER.class,
                        new String[] { "subFolders" , "attestations", "compositionRcIds" });

                typeMappingBuilder(
                        riv.ehr.patientsummary._1.IDENTIFIEDENTITY.class,
                        se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY.class,
                        new String[] { "id" , "telecom" });
            }

            /** Makes a mapping and ensures private list fields are traversed during mapping. <p/>
             *
             * Since no set method exists we need to se accessible on private list fields.
             */
            TypeMappingBuilder typeMappingBuilder(Class<?> src, Class<?> dst, String[] listFields) {
                final TypeMappingBuilder m = mapping(
                        type(src),
                        type(dst),
                        TypeMappingOptions.oneWay(),
                        mapNull(false));

                // accessible makes the trick
                for (final String field : listFields) {
                    final FieldDefinition f = field(field).accessible();
                    m.fields(f, f);
                }
                return m;
            }
        };

        dozerBeanMapper.addMapping(builder);
    }

    // log
    static final Logger log = LoggerFactory.getLogger(AbstractMapper.class);

    // context
    private static final JaxbUtil jaxb = new JaxbUtil("se.rivta.en13606.ehrextract.v11");
    private static final ObjectFactory objectFactory = new ObjectFactory();

    // mapper implementation hash map with RIV service contract operation names (from WSDL) as a key
    private static final HashMap<String, Mapper> map = new HashMap<String, Mapper>();
    static {
        map.put("GetCareContacts", new CareContactsMapper());
        map.put("GetCareDocumentation", new CareDocumentationMapper());
    }

    /**
     * Returns the actual mapper instance by the name of the (inbound SOAP) service operation.
     *
     * @param operation the operation name, i.e. from WSDL. Must be not null.
     * @return the corresponding mapper.
     * @throws java.lang.IllegalStateException when no mapper matches the name of the operation.
     */
    public static Mapper getInstance(String operation) {
        assert operation != null;
        log.debug("Lookup mapper for operation: \"" + operation + "\"");
        final Mapper mapper = map.get(operation);
        if (mapper == null) {
            throw new IllegalStateException("NPOAdapter: Unable to lookup mapper for operation: \"" + operation+ "\"");
        }
        return mapper;
    }

    //
    protected RIV13606REQUESTEHREXTRACTResponseType unmarshalEHRResponse(final XMLStreamReader reader) {
        try {
            return (RIV13606REQUESTEHREXTRACTResponseType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    //
    protected String marshalEHRRequest(final RIV13606REQUESTEHREXTRACTRequestType request) {
        final JAXBElement<RIV13606REQUESTEHREXTRACTRequestType> el = objectFactory.createRIV13606REQUESTEHREXTRACTRequest(request);
        return jaxb.marshal(el);
    }

    //
    protected void close(final XMLStreamReader reader) {
        try {
            reader.close();
        } catch (XMLStreamException | NullPointerException e) {
            ;
        }
    }

    //
    protected riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType map(final RIV13606REQUESTEHREXTRACTRequestType ehrRequest) {
        final riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType ehrExtractType = dozerBeanMapper.map(ehrRequest, riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType.class);
        return ehrExtractType;
    }

    //
    protected RIV13606REQUESTEHREXTRACTResponseType map(final riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType ehrExtractResponseType) {
        final RIV13606REQUESTEHREXTRACTResponseType responseType = dozerBeanMapper.map(ehrExtractResponseType, RIV13606REQUESTEHREXTRACTResponseType.class);
        return responseType;
    }

}

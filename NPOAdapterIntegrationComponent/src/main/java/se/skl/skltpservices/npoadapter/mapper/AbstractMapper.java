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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.dozer.loader.api.BeanMappingBuilder;
import org.dozer.loader.api.FieldDefinition;
import org.dozer.loader.api.TypeMappingBuilder;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import se.rivta.en13606.ehrextract.v11.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.dozer.loader.api.TypeMappingOptions.*;

/**
 * Abstracts all @{link Mapper} implementations.
 *
 * @author Peter
 */
@Slf4j
public abstract class AbstractMapper {


    static final String[] B_PKGS = { "riv.ehr.patientsummary._1.", "riv.ehr.patientsummary.getehrextractresponder._1." };

    //
    static DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    static {

        /**
         * Configures Dozer to map XmlType beans from baseline package "se.rivta.en13606.ehrextract.v11"
         * to and from corresponding RIV domain schemas defined above {@link B_PKGS}. <p/>
         *
         * The schemas are similar but not exactly the same so the mapping code has to
         * check both sides (a and b).
         */
        final BeanMappingBuilder builder = new BeanMappingBuilder() {


            @Override
            protected void configure() {

                for (final Class<?> c : findCandidates("se.rivta.en13606.ehrextract.v11")) {
                    typeMappingBuilder(c, classB(c.getSimpleName()), getInheritedFields(c));
                }

            }

            /** Makes a mapping and ensures private list fields are traversed during mapping.
             * Ans also checks that the actual field exists in the destination class.  <p/>
             *
             * Since no set method exists we need to se accessible on private list fields.
             */
            TypeMappingBuilder typeMappingBuilder(Class<?> src, Class<?> dst, List<String> listFields) {
                final TypeMappingBuilder m = mapping(
                        type(src),
                        type(dst),
                        mapNull(false));

                // accessible makes the trick
                for (final String field : listFields) {
                    if (getInheritedFields(dst).contains(field)) {
                        final FieldDefinition f = field(field).accessible();
                        m.fields(f, f);
                    }
                }
                return m;
            }
        };

        dozerBeanMapper.addMapping(builder);
    }

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


    public static List<String> getInheritedFields(Class<?> type) {
        final List<String> fields = new ArrayList<String>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (final Field f : c.getDeclaredFields()) {
                fields.add(f.getName());
            }
        }
        return fields;
    }

    /**
     * Finds mapping candidates for mapping between baseline schema to/from the corresponding RIV schema.
     *
     * @param basePackage the base package (baseline).
     * @return the list of candidates.
     */
    @SneakyThrows
    private static List<Class<?>> findCandidates(String basePackage)
    {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        List<Class<?>> candidates = new ArrayList<Class<?>>(200);
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + "/" + "**/*.class";
        Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (isCandidate(metadataReader)) {
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        }

        log.info("Found " + candidates.size() + " candidates for XML Bean doamin schema mapping");

        return candidates;
    }

    /**
     * Resolves base package name.
     *
     * @param basePackage the base package name.
     * @return the resource path.
     */
    private static String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * Returns if a classpath resource is a relevant class XML Bean candidate for Dozer mapping.
     * @param metadataReader the metadataReader.
     * @return true if the class is candidate, otherwise false.
     */
    private static boolean isCandidate(MetadataReader metadataReader)
    {
        try {
            Class a = Class.forName(metadataReader.getClassMetadata().getClassName());
            if (!Modifier.isAbstract(a.getModifiers())
                    && a.getAnnotation(XmlType.class) != null
                    && classB(a.getSimpleName()) != null) {
                return true;
            }
        } catch(Throwable e) {}

        return false;
    }

    /**
     * Returns the b-class (map destination) if it exists.
     *
     * @param aName the name of the a-class.
     * @return the b-class or null if no such class exists.
     */
    private static Class<?> classB(String aName) {
        if ("RIV13606REQUESTEHREXTRACTRequestType".equals(aName)) {
            aName = "GetEhrExtractType";
        } else if ("RIV13606REQUESTEHREXTRACTResponseType".equals(aName)) {
            aName = "GetEhrExtractResponseType";
        }
        Class<?> b = null;
        for (int i = 0; (i < B_PKGS.length && b == null); i++) {
            b = classForName(B_PKGS[i] + aName);
        }
        if (b == null) {
            log.warn("No b-class found for a-class when configuring Dozer XMLBean mapping between domain namespaces: " + aName);
        }
        return b;
    }

    private static Class<?> classForName(final String name) {
        try {
            return Class.forName(name);
        } catch (Throwable e) {
        }
        return null;
    }

}

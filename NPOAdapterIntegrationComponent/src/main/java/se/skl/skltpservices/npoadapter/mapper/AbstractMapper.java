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
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.dozer.loader.api.FieldsMappingOptions.*;
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

    /** Special hints needed for the ELEMENT.ANY value */
    static Class<?>[] ANY_ELEMENT_VALUE_HINTS = { ST.class, TS.class };
    /** Special hints needed for the IDENTIFEDENTITY.telecom List value */
    static Class<?>[] TEL_IDENTIFIEDENTITY_VALUE_HINTS = { TELEMAIL.class, TELPHONE.class };

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
                    typeMappingBuilder(c, classB(c.getSimpleName()), getAllFields(c));
                }
            }

            /**
             * Makes a mapping and ensures private list fields are traversed during mapping.
             * Ans also checks that the actual field exists in the destination class.  <p/>
             *
             * Since no set method exists we need to se accessible on private list fields.
             */
            TypeMappingBuilder typeMappingBuilder(final Class<?> src, final Class<?> dst, final List<Field> fields) {

                // log.debug("setup mapping for: {}", src.getSimpleName());

                final TypeMappingBuilder m = mapping(
                        type(src),
                        type(dst),
                        mapNull(false));


                // list fields.
                for (final Field field : fields) {
                    if (contains(getAllFields(dst), field.getName())) {
                        final FieldDefinition f = field(field.getName()).accessible();
                        if (List.class.isAssignableFrom(field.getType())) {
                            if (IDENTIFIEDENTITY.class.isAssignableFrom(src) && "telecom".equals(field.getName())) {
                                m.fields(f, f, hintA(TEL_IDENTIFIEDENTITY_VALUE_HINTS), hintB(toArray(classB(Arrays.asList(TEL_IDENTIFIEDENTITY_VALUE_HINTS)))));
                            } else {
                                m.fields(f, f);
                            }
                        }

                        // handle exceptions from generic configuration
                        if (src.equals(ELEMENT.class) && "value".equals(field.getName())) {
                            m.fields(f, f, hintA(ANY_ELEMENT_VALUE_HINTS), hintB(toArray(classB(Arrays.asList(ANY_ELEMENT_VALUE_HINTS)))));
                        }

                        // Boolean exception handling
                        if (field.getType().equals(Boolean.class)) {
                            m.fields(f, f, copyByReference());
                        }

                    } else {
                        log.warn("Missing mapping field at destination class: {}.{}", dst.getName(), field.getName());
                    }
                }
                return m;
            }

        };

        dozerBeanMapper.addMapping(builder);
    }

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
        map.put(NS_EN_EXTRACT + "-" + NS_CARECONTACTS_2, new CareContactsMapper());
        map.put(NS_RIV_EXTRACT + "-" + NS_CARECONTACTS_2, new RIVCareContactsMapper());

        // docs
        map.put(NS_EN_EXTRACT + "-" + NS_CAREDOCUMENTATION_2, new CareDocumentationMapper());
        
        //dia
        map.put(NS_EN_EXTRACT + "-" + NS_DIAGNOSIS_2, new DiagnosisMapper());
        
        //lab
        map.put(NS_EN_EXTRACT + "-" + NS_LABORATORY_3, new LaboratoryOrderOutcomeMapper());
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
        final String key = sourceNS + "-" + targetNS;
        final Mapper mapper = map.get(key);
        log.debug("Lookup mapper for key: \"{}\" -> {}", key, mapper);
        if (mapper == null) {
            throw new IllegalStateException("NPOAdapter: Unable to lookup mapper for operation: \"" + key + "\"");
        }
        return mapper;
    }


    //
    private static Class<?>[] toArray(final List<Class<?>> list) {
        return list.toArray(new Class<?>[0]);
    }

    //
    protected static boolean contains(final List<Field> list, final String name) {
        for (final Field field : list) {
            if (field.getName().equals((name))) {
                return true;
            }
        }
        return false;
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

    //
    protected static riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType map(final RIV13606REQUESTEHREXTRACTRequestType ehrRequest) {
        final riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType ehrExtractType = dozerBeanMapper.map(ehrRequest, riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType.class);
        return ehrExtractType;
    }

    //
    protected static RIV13606REQUESTEHREXTRACTResponseType map(final riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType ehrExtractResponseType) {
        final RIV13606REQUESTEHREXTRACTResponseType responseType = dozerBeanMapper.map(ehrExtractResponseType, RIV13606REQUESTEHREXTRACTResponseType.class);
        return responseType;
    }


    //
    public static DozerBeanMapper getDozerBeanMapper() {
        return dozerBeanMapper;
    }

    /**
     * Returns all {@link java.util.List} fields for any given class.
     *
     * @param type the input class.
     * @return all list field names.
     */
    private static List<Field> getAllFields(final Class<?> type) {
        final List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (final Field f : c.getDeclaredFields()) {
                fields.add(f);
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
    private static List<Class<?>> findCandidates(final String basePackage)
    {
        final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

        final List<Class<?>> candidates = new ArrayList<Class<?>>(200);
        final String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + "/" + "**/*.class";
        final Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
        for (final Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (isCandidate(metadataReader)) {
                    candidates.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
                }
            }
        }

        log.info("Found " + candidates.size() + " XML Bean candidates for domain schema mapping in baseline package \"" + basePackage + "\"");

        return candidates;
    }

    /**
     * Resolves base package name.
     *
     * @param basePackage the base package name.
     * @return the resource path.
     */
    private static String resolveBasePackage(final String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    /**
     * Returns if a classpath resource is a relevant class XML Bean candidate for Dozer mapping.
     * @param metadataReader the metadataReader.
     * @return true if the class is candidate, otherwise false.
     */
    private static boolean isCandidate(final MetadataReader metadataReader)
    {
        try {
            final Class a = Class.forName(metadataReader.getClassMetadata().getClassName());
            if (!Modifier.isAbstract(a.getModifiers())
                    && a.getAnnotation(XmlType.class) != null
                    && classB(a.getSimpleName()) != null) {
                return true;
            }
        } catch(Throwable e) {}

        return false;
    }

    //
    private static List<Class<?>> classB(final List<Class<?>> classAList) {
        final List<Class<?>> classBList = new ArrayList<Class<?>>(classAList.size());
        for (final Class<?> a : classAList) {
            final Class<?> b = classB(a.getSimpleName());
            if (b != null) {
                classBList.add(b);
            }
        }
        return classBList;
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
        for (int i = 0; (i < B_PKGS.length) && (b == null); i++) {
            b = classForName(B_PKGS[i] + aName);
        }
        if (b == null) {
            log.warn("No destination (b-class) found for source \"" + aName + "\" when configuring Dozer XMLBean namespace mapping");
        }
        return b;
    }

    /**
     * Resolves a name to the actual class.
     *
     * @param name the class name.
     * @return the class or null if no such class exists in classpath.
     */
    private static Class<?> classForName(final String name) {
        try {
            return Class.forName(name);
        } catch (Throwable e) {
        }
        return null;
    }

}

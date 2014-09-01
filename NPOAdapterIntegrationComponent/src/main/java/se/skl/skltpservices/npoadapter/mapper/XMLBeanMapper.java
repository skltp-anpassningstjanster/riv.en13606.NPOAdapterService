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
import org.dozer.loader.api.TypeMappingOptions;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import riv.clinicalprocess.healthcond.description.getcaredocumentationresponder._2.GetCareDocumentationType;
import se.rivta.en13606.ehrextract.v11.*;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;

import javax.xml.bind.annotation.XmlType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.dozer.loader.api.FieldsMappingOptions.*;
import static org.dozer.loader.api.TypeMappingOptions.mapNull;

/**
 * Based on Dozer mapper, and especially configures Dozer to map XMLBeans between the different schema
 * locations. <p/>
 *
 * Baseline source schema package is "se.rivta.en13606.ehrextract.v11" (A) and target is "riv.ehr.patientsummary...." (B). <p/>
 *
 * See also "http://dozer.sourceforge.net"
 */
@Slf4j
public class XMLBeanMapper {

    //
    static final String SRC_PKG = "se.rivta.en13606.ehrextract.v11";
    static final String[] DST_PKGS = { "riv.ehr.patientsummary._1", "riv.ehr.patientsummary.getehrextractresponder._1" };

    //
    static DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();

    /** Special hints needed for the ELEMENT.ANY value */
    static Class<?>[] ANY_ELEMENT_VALUE_HINTS = { ST.class, TS.class, CD.class, BL.class };
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

                for (final Class<?> c : findCandidates(SRC_PKG)) {
                    typeMappingBuilder(c, dstClass(c.getSimpleName()), getAllFields(c));
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

                        // Correct mapping of Lists (unconventional method names)
                        if (List.class.isAssignableFrom(field.getType())) {
                            // Correct mapping of IDENTIFIEDENTITY.telecom TEL type (instantiate correct type)
                            if (IDENTIFIEDENTITY.class.isAssignableFrom(src) && "telecom".equals(field.getName())) {
                                m.fields(f, f, hintA(TEL_IDENTIFIEDENTITY_VALUE_HINTS), hintB(toArray(dstClass(Arrays.asList(TEL_IDENTIFIEDENTITY_VALUE_HINTS)))));
                            } else {
                                m.fields(f, f);
                            }
                        }

                        // Correct mapping of ELEMENT.value ANY type (instantiate correct type)
                        if (src.equals(ELEMENT.class) && "value".equals(field.getName())) {
                            m.fields(f, f, hintA(ANY_ELEMENT_VALUE_HINTS), hintB(toArray(dstClass(Arrays.asList(ANY_ELEMENT_VALUE_HINTS)))));
                        }

                        // Correct mapping of Boolean type (use field mapping since getter method might have an unconventional name)
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
        
        
        /**
         * Used since their are type-o's in the schemas.
         */
        final BeanMappingBuilder diffMappingBuilder = new BeanMappingBuilder() {
			@Override
			protected void configure() {
				mapping(EHRUtil.Request.class, GetCareDocumentationType.class, mapNull(true)).fields("careUnitHSAId", "careUnitHSAid");
			}
        	
        };
        
        dozerBeanMapper.addMapping(builder);
        dozerBeanMapper.addMapping(diffMappingBuilder);
    }

    /**
     * Returns the bean mapper.
     *
     * @return the XML bean mapper single instance.
     */
    public static DozerBeanMapper getInstance() {
        return dozerBeanMapper;
    }

    /**
     * Maps from baseline to destination namespaces.
     *
     * @param ehrRequest the request.
     * @return the corresponding in destination (RIV) namespace.
     */
    public static riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType map(final RIV13606REQUESTEHREXTRACTRequestType ehrRequest) {
        final riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType ehrExtractType = dozerBeanMapper.map(ehrRequest, riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType.class);
        return ehrExtractType;
    }

    /**
     * Maps from destination (RIV) namespace to baseline.
     *
     * @param ehrExtractResponseType the response.
     * @return the corresponding response in baseline namespace.
     */
    public static RIV13606REQUESTEHREXTRACTResponseType map(final riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType ehrExtractResponseType) {
        final RIV13606REQUESTEHREXTRACTResponseType responseType = dozerBeanMapper.map(ehrExtractResponseType, RIV13606REQUESTEHREXTRACTResponseType.class);
        return responseType;
    }

    /**
     * Returns a class array.
     *
     * @param list the list of classes.
     * @return the array of classes.
     */
    private static Class<?>[] toArray(final List<Class<?>> list) {
        return list.toArray(new Class<?>[0]);
    }

    /**
     * Returns if a @{link Field} list contains a field with a given name.
     *
     * @param list the list.
     * @param name the field name.
     * @return true if the list contains such a field, otherwise false.
     */
    protected static boolean contains(final List<Field> list, final String name) {
        for (final Field field : list) {
            if (field.getName().equals((name))) {
                return true;
            }
        }
        return false;
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
                final Class<?> a = candidate(metadataReader);
                if (a != null) {
                    candidates.add(a);
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
     * @return the class if the class is an candidate, otherwise null.
     */
    private static Class<?> candidate(final MetadataReader metadataReader) {
        final Class<?> a = classForName(metadataReader.getClassMetadata().getClassName());
        if (a != null
                && !Modifier.isAbstract(a.getModifiers())
                && a.getAnnotation(XmlType.class) != null
                && dstClass(a.getSimpleName()) != null) {
            return a;
        }
        return null;
    }

    /**
     * Returns a list of corresponding destination classes.
     *
     * @param srcClassList the source class list.
     * @return the list of existing destination classes.
     */
    private static List<Class<?>> dstClass(final List<Class<?>> srcClassList) {
        final List<Class<?>> dstClassList = new ArrayList<Class<?>>(srcClassList.size());
        for (final Class<?> a : srcClassList) {
            final Class<?> b = dstClass(a.getSimpleName());
            if (b != null) {
                dstClassList.add(b);
            }
        }
        return dstClassList;
    }

    /**
     * Returns the corresponding map destination class (b-class) if any exists.
     *
     * @param aName the name of the source class (a-class).
     * @return the destination class (b-class) or null if no such class exists.
     */
    private static Class<?> dstClass(String aName) {
        if ("RIV13606REQUESTEHREXTRACTRequestType".equals(aName)) {
            aName = "GetEhrExtractType";
        } else if ("RIV13606REQUESTEHREXTRACTResponseType".equals(aName)) {
            aName = "GetEhrExtractResponseType";
        }
        Class<?> b = null;
        for (int i = 0; (i < DST_PKGS.length) && (b == null); i++) {
            b = classForName(DST_PKGS[i] + "." + aName);
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

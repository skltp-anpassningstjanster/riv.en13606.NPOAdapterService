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
package se.skl.skltpservices.npoadapter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Overrides Spring configuration to be able to programmatically lookup the actual configuration. <p/>
 *
 * Please note: property values are unresolved.
 *
 * @author Peter
 */
public class SpringPropertiesUtil extends PropertyPlaceholderConfigurer {

    protected static final Logger log = LoggerFactory.getLogger(SpringPropertiesUtil.class);
    
    private static Map<String, String> propertiesMap;
    // Default as in PropertyPlaceholderConfigurer
    private int springSystemPropertiesMode = SYSTEM_PROPERTIES_MODE_NEVER;

    @Override
    public void setSystemPropertiesMode(int systemPropertiesMode) {
        super.setSystemPropertiesMode(systemPropertiesMode);
        springSystemPropertiesMode = systemPropertiesMode;
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        super.processProperties(beanFactory, props);
        propertiesMap = new HashMap<String, String>();
        for (final Object key : props.keySet()) {
            final String keyStr = key.toString();
            final String valueStr = resolvePlaceholder(keyStr, props, springSystemPropertiesMode);
            propertiesMap.put(keyStr, valueStr);
        }
    }

    /**
     * Return null if property is not resolved 
     */
    public static String getProperty(String name) {
        if (propertiesMap == null) {
            log.error("propertiesMap is null");
            return null;
//          throw new RuntimeException("propertiesMap is null - incorrectly initialised");
        }
        return propertiesMap.get(name);
    }

    //
    public static Map<String, String> getProperties() {
        return propertiesMap;
    }
}
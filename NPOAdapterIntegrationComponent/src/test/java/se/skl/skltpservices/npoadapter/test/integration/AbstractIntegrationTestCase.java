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
package se.skl.skltpservices.npoadapter.test.integration;

import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;

/**
 * Created by Peter on 2014-08-14.
 */
public abstract class AbstractIntegrationTestCase extends AbstractTestCase {

    public AbstractIntegrationTestCase() {
        super();
    }

    @Override
    protected String getConfigResources() {
        return "soitoolkit-mule-jms-connector-activemq-embedded.xml"
                + ",NPOAdapter-common.xml"
                + ",NPOAdapter-config.xml"
                + ",teststub-services/ehrextract-teststub-service.xml"
                + ",teststub-services/getcarecontacts-teststub-service.xml"
                + ",teststub-services/takvagval-teststub-service.xml";
    }
}

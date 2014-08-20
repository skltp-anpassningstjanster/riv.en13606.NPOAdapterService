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
package se.skl.skltpservices.npoadapter.router;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Peter on 2014-08-12.
 */
public class RouteDataTest {

    static final String LOGICAL_ADDRESS1 = "SE123456";
    static final String LOGICAL_ADDRESS2 = "DE123456";
    static final String SERVICE_NAMESPACE = "urn:riv:patientsummary";
    static final String SERVICE_NAMESPACE_2 = "http://nationellpatientoversikt.se:SendStatus";

    static final String TEMP_URL1 = "https://tempurl1.xxx";
    static final String TEMP_URL2 = "https://tempurl2.xxx";


    static RouteData createTestRouteData() {
        final RouteData routeData = new RouteData();

        routeData.setRoute(LOGICAL_ADDRESS1, RouteData.route(SERVICE_NAMESPACE, TEMP_URL1));
        routeData.setRoute(LOGICAL_ADDRESS2, RouteData.route(SERVICE_NAMESPACE, TEMP_URL2));
        routeData.setRoute(LOGICAL_ADDRESS1, RouteData.route(SERVICE_NAMESPACE_2, TEMP_URL2));

        return routeData;
    }

    @Test
    public void testRoute() {
        final RouteData source = createTestRouteData();

        assertTrue(source.getRoute(LOGICAL_ADDRESS1, true).isCallback());
        assertFalse(source.getRoute(LOGICAL_ADDRESS1, false).isCallback());

        assertEquals(source.getRoute(LOGICAL_ADDRESS1, false).getUrl(), TEMP_URL1);
        assertEquals(source.getRoute(LOGICAL_ADDRESS1, true).getUrl(), TEMP_URL2);
    }

    @Test
    public void testSave() {
        final RouteData source = createTestRouteData();

        final File tempFile = getTempFile();

        RouteData.save(source, tempFile.getAbsolutePath());

        final RouteData target = RouteData.load(tempFile.getAbsolutePath());

        assertEquals(source.getRoute(LOGICAL_ADDRESS1).getUrl(), target.getRoute(LOGICAL_ADDRESS1).getUrl());
        assertEquals(source.getRoute(LOGICAL_ADDRESS2).getUrl(), target.getRoute(LOGICAL_ADDRESS2).getUrl());
        assertEquals(source.getRoute(LOGICAL_ADDRESS2).getSoapAction(), target.getRoute(LOGICAL_ADDRESS2).getSoapAction());
    }

    @Test
    public void testNoLocalFile() {
        final File tempFile = getTempFile();

        final RouteData routeData = RouteData.load(tempFile.getAbsolutePath());

        assertNull(routeData);
    }

    static File getTempFile() {
        try {
            final File file = File.createTempFile("testroute", ".tmp");
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

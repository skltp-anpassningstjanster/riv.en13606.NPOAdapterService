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

import static org.junit.Assert.assertEquals;

/**
 * Created by Peter on 2014-08-12.
 */
public class RouterTest {

    static final String TEMP_WSDL = RouteDataTest.TEMP_URL1 + "?wsdl";

    @Test
    public void testFallbackOnLocalCache() {
        final Router router = new Router();
        router.setTakWSDL(TEMP_WSDL);

        final File file = RouteDataTest.getTempFile();

        router.setTakCacheFilename(file.getAbsolutePath());

        final RouteData routeDataSource = RouteDataTest.createTestRouteData();

        RouteData.save(routeDataSource, router.getTakCacheFilename());

        router.reloadRoutingData0();

        final RouteData.Route route = router.getRoute(RouteDataTest.LOGICAL_ADDRESS1);

        assertEquals(route.getUrl(), routeDataSource.getRoute(RouteDataTest.LOGICAL_ADDRESS1).getUrl());
    }

    @Test
    public void testSetTakWSDL() {
        final Router router = new Router();
        router.setTakWSDL(TEMP_WSDL);

        assertEquals(TEMP_WSDL, router.getTakWSDL().toString());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidTakWSDL() {
        final Router router = new Router();
        router.setTakWSDL("invalid url");
    }
}

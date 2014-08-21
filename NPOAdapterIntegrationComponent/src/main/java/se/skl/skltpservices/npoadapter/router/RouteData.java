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

import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.HashMap;

/**
 * Keeps routing information, main routing is from northbound NPO consumer to southbound care system.
 * Though, a callback flag indicates that itäs about an internal NPO specific route, i.e. the same logical
 * address can be used for 2 different kinds of routes (a main consumer-producer route, and a callback route). <p/>
 *
 * Route data is serialized to a file store.
 *
 * @author Peter
 */
@Slf4j
public class RouteData implements Serializable {

    static final long serialVersionUID = 1L;
    public static final String CALLBACK_PREFIX = "callback:";
    public static final String NPO_NS = "http://nationellpatientoversikt.se";

    //
    private HashMap<String, Route> map = new HashMap<String, Route>();

    @Data
    public static class Route implements Serializable {
        static final long serialVersionUID = 1L;
        private String soapAction;
        private String url;
        private boolean callback;

        public String key(final String key) {
            return key(key, isCallback());
        }

        public static String key(final String key, boolean callback) {
            return (callback) ? (CALLBACK_PREFIX + key) : key;
        }
    }


    //
    public static Route route(final String serviceContract, final String url) {
        final Route route = new Route();
        route.setSoapAction(serviceContract);
        route.setUrl(url);
        if (serviceContract.startsWith(NPO_NS)) {
            route.setCallback(true);
        }
        return route;
    }


    //
    public Route getRoute(final String logicalAddress) {
        return getRoute(logicalAddress, false);
    }


    //
    public Route getRoute(final String logicalAddress, final boolean callbackRoute)  {
        final Route route = map.get(Route.key(logicalAddress, callbackRoute));
        if (route == null) {
            log.error("Routing information not found for logical address: {}, callback: {}", logicalAddress, callbackRoute);
            log.debug("Route map: {}", map);
        }
        return route;
    }

    //
    public void setRoute(final String logicalAddress, final Route route) {
        if (map.containsKey(route.key(logicalAddress))) {
            log.error("NPOAdapter: Duplicate routes exists for: " + logicalAddress + ",and " + route + " has been ignored/skipped");
            log.info("NPOAdapter: Current route for " + logicalAddress + " is " + getRoute(logicalAddress, route.isCallback()));
        } else {
            map.put(route.key(logicalAddress), route);
        }
    }


    public static boolean save(final RouteData routingData, final String fileName) {
        boolean rc = false;
        try {
            @Cleanup ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(fileName));
            os.writeObject(routingData);
            os.flush();
            rc = true;
            log.debug("NPOAdapter: Successfully saved route data to file: " + fileName);
        } catch (Exception e) {
            log.error("NPOAdapter: Unable to save route data to local file: " + fileName, e);
        }
        return rc;
    }

    //
    public static RouteData load(final String fileName) {
        RouteData routingData = null;
        try {
            @Cleanup ObjectInputStream is = new ObjectInputStream(new FileInputStream(fileName));
            routingData = (RouteData) is.readObject();
            log.debug("NPOAdapter: Successfully loaded route data from file: " + fileName);
        } catch (Exception e) {
            log.error("NPOAdapter: Unable to load route data from local file: " + fileName, e);
        }
        return routingData;
    }

}

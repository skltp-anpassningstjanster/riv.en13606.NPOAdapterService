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

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.HashMap;

/**
 * Created by Peter on 2014-08-12.
 */
public class RouteData implements Serializable {

    static final long serialVersionUID = 1L;
    static final Logger log = LoggerFactory.getLogger(RouteData.class);

    //
    private HashMap<String, Route> map = new HashMap<String, Route>();

    @Data
    public static class Route implements Serializable {
        static final long serialVersionUID = 1L;
        private String serviceNamespace;
        private String url;
    }


    //
    public static Route route(final String serviceNamespace, final String url) {
        final Route route = new Route();
        route.setServiceNamespace(serviceNamespace);
        route.setUrl(url);
        return route;
    }

    //
    public Route getRoute(final String logicalAddress) {
        return map.get(logicalAddress);
    }

    //
    public void setRoute(final String logicalAddress, final Route route) {
        if (map.containsKey(logicalAddress)) {
            log.error("NPOAdapter: Duplicate routes exists for: " + logicalAddress + ",and " + route + " has been ignored/skipped");
            log.info("NPOAdapter: Current route for " + logicalAddress + " is " + getRoute(logicalAddress));
        } else {
            map.put(logicalAddress, route);
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

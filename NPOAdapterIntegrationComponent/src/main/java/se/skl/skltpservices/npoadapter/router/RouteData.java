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

import java.io.*;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores routing information. 
 * Main routing is from NPOv2 consumer to NPOv1 producer (care system).
 * Second routing for callback from NPOAdapter to NPOv1 producer (care system).
 * <p/>
 *
 * Route data is serialized to a file store.
 *
 * @author Peter
 */
public class RouteData implements Serializable {
	
	private static final Logger log = LoggerFactory.getLogger(RouteData.class);

    static final long serialVersionUID = 1L;
    public static final String CALLBACK_PREFIX = "callback:";

    //
    private HashMap<String, Route> map = new HashMap<String, Route>();

    public static class Route implements Serializable {
        static final long serialVersionUID = 1L;
        private String soapAction;
        private String url;
        private boolean callback;
        
        public Route() {
        	super();
        }
        
		public String getSoapAction() {
			return soapAction;
		}

		private void setSoapAction(String soapAction) {
			this.soapAction = soapAction;
		}

		public String getUrl() {
			return url;
		}

		private void setUrl(String url) {
			this.url = url;
		}

		public boolean isCallback() {
			return callback;
		}

		private void setCallback(boolean callback) {
			this.callback = callback;
		}

		public String key(final String key) {
            return key(key, isCallback());
        }

        public static String key(final String key, boolean callback) {
            return (callback) ? (CALLBACK_PREFIX + key) : key;
        }
        
        public String toString() {
            return "soapAction:" + soapAction + " url:" + url;
        }
    }


    // Factory method for creating a Route
    static Route route(final String serviceContract, final String url) {
        final Route route = new Route();
        route.setSoapAction(serviceContract);
        route.setUrl(url);
        if (serviceContract.startsWith(Router.NAMESPACE_CALLBACK)) {
            route.setCallback(true);
        } else {
            route.setCallback(false);
        }
        return route;
    }


    //
    Route getRoute(final String logicalAddress) {
        return getRoute(logicalAddress, false);
    }


    //
    Route getRoute(final String logicalAddress, final boolean callbackRoute)  {
        final Route route = map.get(Route.key(logicalAddress, callbackRoute));
        if (route == null) {
            log.error("Routing information not found for logical address: {}, callback: {}", logicalAddress, callbackRoute);
            log.debug("Route map: {}", map);
        }
        return route;
    }

    //
    void setRoute(final String logicalAddress, final Route route) {
        if (map.containsKey(route.key(logicalAddress))) {
            log.error("NPOAdapter: Duplicate routes exists for: " + logicalAddress + ". " + route + " has been ignored/skipped");
            log.info("NPOAdapter: Current route for " + logicalAddress + " is " + getRoute(logicalAddress, route.isCallback()));
        } else {
            map.put(route.key(logicalAddress), route);
        }
    }

    static boolean save(final RouteData routingData, final String fileName) {
        boolean rc = false;
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new FileOutputStream(fileName));
            os.writeObject(routingData);
            os.flush();
            rc = true;
            log.debug("NPOAdapter: Successfully saved {} routes to file:{}", routingData.map.keySet().size(), fileName);
        } catch (Exception e) {
            log.error("NPOAdapter: Unable to save route data to local file: " + fileName, e);
        } finally {
        	if(os != null) {
        		try {
        			os.close();
        		} catch (Exception err) {
        			log.error("Could not close stream", err);
        		}
        	}
        }
        return rc;
    }

    //
    static RouteData load(final String fileName) {
        RouteData routingData = null;
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(new FileInputStream(fileName));
            routingData = (RouteData) is.readObject();
            log.debug("NPOAdapter: Successfully loaded route data from file: " + fileName);
        } catch (Exception e) {
            log.error("NPOAdapter: Unable to load route data from local file: " + fileName, e);
        } finally {
        	if(is != null) {
        		try {
        			is.close();
        		} catch (Exception err) {
        			log.error("Could not close input stream", err);
        		}
        	}
        }
        return routingData;
    }

}

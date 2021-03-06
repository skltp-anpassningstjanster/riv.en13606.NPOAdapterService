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

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.mapper.AbstractMapper;
import skl.tp.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import skl.tp.vagvalsinfo.v2.SokVagvalsServiceSoap11LitDocService;
import skl.tp.vagvalsinfo.v2.VirtualiseringsInfoType;

import javax.xml.datatype.XMLGregorianCalendar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Peter on 2014-08-08.
 */
public class Router implements MuleContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(Router.class);
	
	private final Object $lock = new Object[0];

    static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    
    public static final String NAMESPACE_CALLBACK = "http://nationellpatientoversikt.se";
    public static final String CONTRACT_CALLBACK  = "http://nationellpatientoversikt.se:SendStatus";
    
    static final List<String> CONTRACTS = Arrays.asList(
             AbstractMapper.NS_RIV_EXTRACT,
            "urn:riv13606:v1.1:RIV13606REQUEST_EHR_EXTRACT",
            CONTRACT_CALLBACK);

    private URL takWSDL;
    private String takCacheFilename;
    private RouteData routeData;

    //
    String getTakCacheFilename() {
        return takCacheFilename;
    }

    //
    public void setTakCacheFilename(String takCacheFilename) {
        this.takCacheFilename = takCacheFilename;
    }

    /**
     * @param logicalAddress - producer's hsaId
     * @return route - returns null if no route found 
     */
    public RouteData.Route getRoute(final String logicalAddress) {
        return getRoute(logicalAddress, false);
    }

    /**
     * @param logicalAddress - producer's hsaId
     * @return route - returns null if no route found 
     */
    public RouteData.Route getCallbackRoute(final String logicalAddress) {
        return getRoute(logicalAddress, true);
    }

    //
    private RouteData.Route getRoute(final String logicalAddress, final boolean callbackRoute) {
        return getRouteData().getRoute(logicalAddress, callbackRoute);
    }

    /**
     * Used by a mule flow to periodically schedule updates of the routing data cache.
     *
     * Checkout flow spec in file "update-tak-cache-service.xml"
     */
    public void reloadRoutingData() {
        log.info("NPOAdapter: Received routing data relaod event");
        reloadRoutingData0();
    }

    //
    void reloadRoutingData0() {
        try {
            log.info("NPOAdapter: loading routing data from TAK");
            final HamtaAllaVirtualiseringarResponseType data = getRoutingDataFromSource();
            log.info("retrieved {} VirtualiseringsInfo", data.getVirtualiseringsInfo().size());
            final RouteData routeData = toRouteData(data);
            RouteData.save(routeData, takCacheFilename);
            setRouteData(routeData);
        } catch (Throwable e) {
            log.error("NPOAdapter: Unable to get routing data from TAK", e);
            log.info("NPOAdapter: Trying with locally stored cache: " + takCacheFilename);
            final RouteData routeData = RouteData.load(takCacheFilename);
            if (routeData == null) {
                log.error("NPOAdapter: FATAL ERROR, Can't get routing data from TAK or local cache file");
            } else {
                setRouteData(routeData);
            }
        }
    }

    //
    private RouteData toRouteData(final HamtaAllaVirtualiseringarResponseType data) {
        final RouteData routeData = new RouteData();
        final Calendar now = Calendar.getInstance();
        for (final VirtualiseringsInfoType infoType : data.getVirtualiseringsInfo()) {
            if (isActive(now, infoType) && isTargetContract(infoType.getTjansteKontrakt())) {
                routeData.setRoute(infoType.getReceiverId(), RouteData.route(infoType.getTjansteKontrakt(), infoType.getAdress()));
            }
        }
        return routeData;
    }

    //
    private boolean isTargetContract(final String namespace) {
        return (namespace == null) ? false : CONTRACTS.contains(namespace);
    }

    //
    private boolean isActive(final Calendar time, final VirtualiseringsInfoType infoType) {
        final Calendar from = floorDate(toDate(infoType.getFromTidpunkt()));
        final Calendar to = ceilDate(toDate(infoType.getTomTidpunkt()));
        return (time.after(from) && time.before(to));
    }

    /**
     * Returns a {@link Date} date and time representation.
     *
     * @param cal the actual date and time.
     * @return the {@link Date} representation.
     */
    private Calendar toDate(XMLGregorianCalendar cal) {
        if (cal != null) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.DATE, cal.getDay());
            c.set(Calendar.MONTH, cal.getMonth() - 1);
            c.set(Calendar.YEAR, cal.getYear());
            return c;
        }
        return null;
    }

    // truncates a date to 00:00:00
    private static Calendar floorDate(Calendar cal) {
        if (cal == null) {
            cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    // increases a date to 23:59:59
    private static Calendar ceilDate(Calendar cal) {
        if (cal == null) {
            cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, 2999);
            cal.set(Calendar.MONTH, 12);
            cal.set(Calendar.DAY_OF_MONTH, 31);
        }
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal;
    }


    //
    private HamtaAllaVirtualiseringarResponseType getRoutingDataFromSource() {
        final SokVagvalsServiceSoap11LitDocService client = new SokVagvalsServiceSoap11LitDocService(getTakWSDL());
        final HamtaAllaVirtualiseringarResponseType data = client.getSokVagvalsSoap11LitDocPort().hamtaAllaVirtualiseringar(null);
        return data;
    }

    //
    public void setTakWSDL(final String takWSDL) {
        try {
            setTakWSDL(new URL(takWSDL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    //
    public void setTakWSDL(final URL takWSDL) {
        this.takWSDL = takWSDL;
    }

    //
    URL getTakWSDL() {
        return this.takWSDL;
    }

    //
    public void setRouteData(final RouteData routeData) {
    	synchronized (this.$lock) {
    		this.routeData = routeData;			
		}
    }

    //
    RouteData getRouteData() {
    	synchronized (this.$lock) {
    		if (this.routeData == null) {
    			reloadRoutingData0();
    		}
    		return this.routeData;
		}
    }

    @Override
    public void setMuleContext(MuleContext context) {
        log.info("NPOAdapter: Mule context ready, schedule pre-loading of routing data");
        worker.schedule(new Runnable() {
            @Override
            public void run() {
                log.info("NPOAdapter: Pre-load (initialize) routing data");
                getRouteData();
            }
        }, 10, TimeUnit.SECONDS);
    }
}

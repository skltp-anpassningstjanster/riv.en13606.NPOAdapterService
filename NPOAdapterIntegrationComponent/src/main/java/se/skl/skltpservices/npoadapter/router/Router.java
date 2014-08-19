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

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;
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
@Slf4j
public class Router implements MuleContextAware {

    static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    static final List<String> CONTRACTS = Arrays.asList("urn:riv:ehr:patientsummary:GetEhrExtractResponder:1:GetEhrExtract:rivtabp21",
            "urn:riv13606:v1.1:RIV13606REQUEST_EHR_EXTRACT");

    private URL takWSDL;
    private String takCacheFilename;
    private RouteData routeData;

    //
    public String getTakCacheFilename() {
        return takCacheFilename;
    }

    //
    public void setTakCacheFilename(String takCacheFilename) {
        this.takCacheFilename = takCacheFilename;
    }

    //
    public RouteData.Route getRoute(final String logicalAddress) {
        return getRouteData().getRoute(logicalAddress);
    }

    /**
     * Used by a mule flow to periodically schedule updates of the routing data cache.
     *
     * Checkout flow spec in file "update-tak-cache-service.xml"
     */
    public void updateRoutingDataScheduledEvent() {
        log.info("NPOAdapter: Received scheduled routing data update event");
        updateRoutingData0();
    }

    //
    protected void updateRoutingData0() {
        try {
            log.info("NPOAdapter: Load routing data from TAK");
            final HamtaAllaVirtualiseringarResponseType data = getRoutingDataFromSource();
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
    protected RouteData toRouteData(final HamtaAllaVirtualiseringarResponseType data) {
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
    protected boolean isTargetContract(final String namespace) {
        return (namespace == null) ? false : CONTRACTS.contains(namespace);
    }

    //
    protected boolean isActive(final Calendar time, final VirtualiseringsInfoType infoType) {
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
    protected Calendar toDate(XMLGregorianCalendar cal) {
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
    protected static Calendar floorDate(Calendar cal) {
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
    protected static Calendar ceilDate(Calendar cal) {
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
    protected HamtaAllaVirtualiseringarResponseType getRoutingDataFromSource() {
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
    public URL getTakWSDL() {
        return this.takWSDL;
    }

    //
    @Synchronized
    public void setRouteData(final RouteData routeData) {
        this.routeData = routeData;
    }

    //
    @Synchronized
    public RouteData getRouteData() {
        if (this.routeData == null) {
            updateRoutingData0();
        }
        return this.routeData;
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

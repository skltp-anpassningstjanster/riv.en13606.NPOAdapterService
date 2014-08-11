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

import skl.tp.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import skl.tp.vagvalsinfo.v2.SokVagvalsServiceSoap11LitDocService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Peter on 2014-08-08.
 */
public class Router {

    private URL endpoint;
    private HamtaAllaVirtualiseringarResponseType routingData;

    //
    public String getEndpoint(final String logicalAddress) {
        return "";
    }

    //
    public void load() {
        final HamtaAllaVirtualiseringarResponseType data = getRoutingDataFromSource();
        setRoutingData(data);
    }

    //
    public HamtaAllaVirtualiseringarResponseType getRoutingDataFromSource() {
        final SokVagvalsServiceSoap11LitDocService client = new SokVagvalsServiceSoap11LitDocService(endpoint);
        final HamtaAllaVirtualiseringarResponseType data = client.getSokVagvalsSoap11LitDocPort().hamtaAllaVirtualiseringar(null);
        return data;
    }

    //
    public void setSource(final String endpoint) {
        try {
            setSource(new URL(endpoint));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    //
    public void setSource(final URL endpoint) {
        this.endpoint = endpoint;
    }

    //
    public URL getSource() {
        return this.endpoint;
    }

    //
    public synchronized void setRoutingData(final HamtaAllaVirtualiseringarResponseType routingData) {
        this.routingData = routingData;
    }

    //
    public synchronized  HamtaAllaVirtualiseringarResponseType getRoutingData() {
        return this.routingData;
    }
}

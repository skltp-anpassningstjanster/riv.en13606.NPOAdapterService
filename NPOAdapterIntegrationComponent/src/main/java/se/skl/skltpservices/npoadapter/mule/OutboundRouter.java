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
package se.skl.skltpservices.npoadapter.mule;

import org.mule.api.MuleEvent;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.routing.outbound.AbstractRecipientList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by Peter on 2014-07-30.
 */
public class OutboundRouter extends AbstractRecipientList {

    //
    static final Logger log = LoggerFactory.getLogger(OutboundRouter.class);

    @Override
    protected List<Object> getRecipients(MuleEvent event) throws CouldNotRouteOutboundMessageException {
        try {
            final List<Object> route = Collections.singletonList(getRoute());
            log.debug("route: " + route.get(0));
            return route;
        } catch (Throwable e) {
            throw new CouldNotRouteOutboundMessageException(event, this, e);
        }
    }

    /**
     * Returns the route.
     *
     * @return the URL (as a string) to the outbound endpoint.
     */
    protected Object getRoute() {
        return "http://localhost:11000/npoadapter/ehrextract/stub";
    }

    public void setVpInstanceId(String vpInstanceId) {
        //this.vpInstanceId = vpInstanceId;
    }

    public void setResponseTimeout(final int responseTimeout) {
        //this.responseTimeout = responseTimeout;
    }

}

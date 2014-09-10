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
package se.skl.skltpservices.npoadapter.ws;

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleContext;
import org.mule.api.annotations.expressions.Lookup;
import org.mule.api.construct.FlowConstruct;
import org.mule.management.stats.FlowConstructStatistics;

import riv.ehr.patientsummary._1.EHREXTRACT;
import se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface;
import se.riv.itintegration.monitoring.v1.ConfigurationType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationResponseType;
import se.riv.itintegration.monitoring.v1.PingForConfigurationType;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.util.Samples;
import se.skl.skltpservices.npoadapter.util.Sample;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;

import javax.jws.WebService;

import java.util.Date;
import java.util.Map;

/**
 * Implements standard interface <p/>
 *
 * The current configuration is also returned.
 *
 * @author Peter
 */
@Slf4j
@WebService(serviceName = "PingForConfigurationResponderService",
        endpointInterface = "se.riv.itintegration.monitoring.rivtabp21.v1.PingForConfigurationResponderInterface",
        portName = "PingForConfigurationResponderPort",
        targetNamespace = "urn:riv:itintegration:monitoring:PingForConfiguration:1:rivtabp21")
public class PingForConfigurationWS implements PingForConfigurationResponderInterface {

    @Lookup
    private MuleContext muleContext;

    @Override
    public PingForConfigurationResponseType pingForConfiguration(String logicalAddress, PingForConfigurationType request) {
        log.debug("Called with logical address {}", logicalAddress);

        final PingForConfigurationResponseType response = new PingForConfigurationResponseType();

        response.setVersion(EHREXTRACT.class.getPackage().getImplementationVersion());
        response.setPingDateTime(EHRUtil.formatTimestamp(new Date()));


        // instrumentation.
        for (final Samples samples: Sample.samples()) {
            samples.recalc();
            response.getConfiguration().add(property(samples.name(), samples.toString()));
        }

        // services
        for (final FlowConstruct flow: muleContext.getRegistry().lookupFlowConstructs()) {
            response.getConfiguration().add(property(flow.getName(), stats(flow)));

        }

        // config
        for (final Map.Entry<String, String> entry : SpringPropertiesUtil.getProperties().entrySet()) {
            response.getConfiguration().add(property(entry.getKey(), entry.getValue()));
        }


        return response;
    }

    //
    static String stats(FlowConstruct flow) {
        final FlowConstructStatistics stats = flow.getStatistics();
        final StringBuilder sb = new StringBuilder();
        sb.append("enabled=").append(stats.isEnabled())
                .append(", num_requests=").append(stats.getProcessedEvents())
                .append(", num_avg_queuesz=").append(stats.getAverageQueueSize())
                .append(", num_exec_errors=").append(stats.getExecutionErrors())
                .append(", num_fatal_errors=").append(stats.getFatalErrors())
                .append(", time_avg=").append(stats.getAverageProcessingTime())
                .append(", time_max=").append(stats.getMaxProcessingTime())
                .append(", time_min=").append(stats.getMinProcessingTime())
                .append(", time_tot=").append(stats.getTotalProcessingTime());
        return sb.toString();
    }

    //
    static ConfigurationType property(final String name, final String value) {
        final ConfigurationType p = new ConfigurationType();
        p.setName(name);
        p.setValue(name.toUpperCase().contains("PASSWORD") ? "********" : value);
        return p;
    }
}

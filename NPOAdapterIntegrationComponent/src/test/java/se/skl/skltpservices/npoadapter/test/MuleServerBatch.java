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
package se.skl.skltpservices.npoadapter.test;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.soitoolkit.commons.mule.test.StandaloneMuleServer;

/**
 * Created by Peter on 2014-09-04.
 */
@Slf4j
public class MuleServerBatch {
    public static void main(String[] args) throws Exception {
        new StandaloneMuleServer("NPOAdapterIntegrationComponent", true, true) {
            @Override
            @SneakyThrows
            public void run() {
                // Start me up...
                log.info("Startup...");
                start();
                synchronized(this) {
                    while (true) {
                        this.wait();
                    }
                }
            }
        }.run();
    }

}

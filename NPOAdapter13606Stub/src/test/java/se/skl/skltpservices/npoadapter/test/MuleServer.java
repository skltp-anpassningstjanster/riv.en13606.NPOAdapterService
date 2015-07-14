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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.StandaloneMuleServer;

/**
 * SOI toolkit embedded test mule server. <p/>
 *
 * Set system property batchMode to true in order to run in batch mode, default is console mode
 * and to stop server by pressing any key.
 *
 */
public class MuleServer {
	
	private static final Logger log = LoggerFactory.getLogger(MuleServer.class);
	
    //
    public static void main(String[] args) throws Exception {
        final boolean batchMode =  Boolean.valueOf(System.getProperty("batchMode"));
        new StandaloneMuleServer("NPOAdapterIntegrationComponent", true, true) {
            @Override
            public void run() throws InterruptedException, Exception {
                if (batchMode) {
                    // Start me up...
                    log.info("Starting up in batch mode...");
                    start();
                    // wait until interrupted
                    synchronized (this) {
                        wait();
                    }
                } else {
                    log.info("Starting up in console mode...");
                    super.run();
                }
            }
        }.run();
    }
}

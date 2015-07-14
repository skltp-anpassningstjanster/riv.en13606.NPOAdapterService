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
package se.skl.skltpservices.npoadapter.util;

import java.util.*;

/**
 * Instruments points of interests, and samples timed statistics.
 *
 * @author Peter
 */
public class Sample {
    private long createdTimestamp;
    private Samples samples;
    private static Map<String, Samples> samplesMap = Collections.synchronizedMap(new HashMap<String, Samples>());

    /**
     * Creates and starts a sample.
     *
     * @param name the sample name.
     */
    public Sample(final String name) {
        this.createdTimestamp = System.currentTimeMillis();
        this.samples = samples(name);
    }

    //
    private Samples samples(final String name) {
        Samples s = samplesMap.get(name);
        if (s == null) {
            s = new Samples(name, 1000);
            samplesMap.put(name, s);
        }
        return s;
    }

    //
    public String name() {
        return samples.name();
    }

    //
    public static Collection<Samples> samples() {
        return samplesMap.values();
    }

    /**
     * Returns message and marks sample as successful.
     * @param message the message.
     *
     * @return the message.
     */
    public <T> T ok(final T message) {
        this.samples.ok();
        return message;
    }

    /**
     * Ends sample.
     *
     * @return the elapsed time in millis.
     */
    public long end() {
        final long time = (System.currentTimeMillis() - createdTimestamp);
        final long elapsed = (time < 0) ? 0 : time;
        this.samples.add(elapsed);
        return elapsed;
    }
}

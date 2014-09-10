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
 * Instruments points of interests, sample timing statistics.
 *
 * @author Peter
 */
public class Sample {
    private long createdTimestamp;
    private HistoryTimer timer;
    private static Map<String, HistoryTimer> timerMap = Collections.synchronizedMap(new HashMap<String, HistoryTimer>());

    /**
     * Creates and starts a sample.
     *
     * @param name the sample name.
     */
    public Sample(final String name) {
        this.createdTimestamp = System.currentTimeMillis();
        this.timer = timer(name);
    }

    //
    private HistoryTimer timer(final String name) {
        HistoryTimer timer = timerMap.get(name);
        if (timer == null) {
            timer = new HistoryTimer(name, 1000);
            timerMap.put(name, timer);
        }
        return timer;
    }

    //
    public String name() {
        return timer.name();
    }

    //
    public static Collection<HistoryTimer> timers() {
        return timerMap.values();
    }

    /**
     * Returns message and marks sample as successful.
     * @param message the message.
     *
     * @return the message.
     */
    public <T> T ok(final T message) {
        this.timer.ok();
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
        this.timer.add(elapsed);
        return elapsed;
    }
}

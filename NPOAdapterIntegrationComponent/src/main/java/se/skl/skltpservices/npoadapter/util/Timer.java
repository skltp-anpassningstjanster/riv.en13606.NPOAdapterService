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

import java.io.Serializable;

/**
 * Keeps timed statistics.
 *
 * @author Peter
 */
public class Timer implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Number of requests.
     * @serial
     */
    private long n;
    /**
     * Min time in millis.
     * @serial
     */
    private long min;
    /**
     * Max time in millis.
     * @serial
     */
    private long max;
    /**
     * Total time in millis.
     * @serial
     */
    private long sum;
    /**
     * Name of this timer.
     * @serial
     */
    private String name;

    public Timer(String name) {
        this.name = name;
        reset();
    }

    //
    public String name() {
        return name;
    }


    //
    public void add(long t) {
        sum += t;
        min = Math.min(min, t);
        max = Math.max(max, t);
        n++;
    }

    //
    protected void reset() {
        n   = 0L;
        sum = 0L;
        min = Long.MAX_VALUE;
        max = Long.MIN_VALUE;
    }


    public long min() {
        return (min == Long.MAX_VALUE) ? 0 : min;
    }

    //
    public long max() {
        return (max == Long.MIN_VALUE) ? 0 : max;
    }

    //
    public long avg() {
        return (n == 0) ? 0 : (sum / n);
    }

    //
    public long n() {
        return n;
    }

    @Override
    public String toString() {
        return String.format("\"%s\": {n: %d, min: %d, max: %d, avg: %d}", name(), n(), min(), max(), avg());
    }
}

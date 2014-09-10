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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps stats and timing history.
 *
 * @author Peter
 */
public class Samples extends Timer {
    private static final long serialVersionUID = 1L;
    private int len;
    private int ofs = 0;
    private long[] history;
    private AtomicLong total = new AtomicLong (0);
    private AtomicLong success = new AtomicLong (0);

    //
    public Samples(String name, int len) {
        super(name);
        this.len = len;
        this.history = new long[len];
        Arrays.fill(history, -1);
    }

    //
    public void ok() {
        success.getAndIncrement();
    }

    //
    public void add(long t) {
        synchronized (this) {
            if (ofs >= len) {
                ofs = 0;
            }
            history[ofs++] = t;
        }
        total.getAndIncrement();
    }

    //
    public synchronized void recalc() {
        reset();
        for (int i = 0; i < len && history[i] >= 0; i++) {
            super.add(history[i]);
        }
    }

    @Override
    public synchronized String toString() {
        final long num = total.get();
        final long err = num - success.get();
        return String.format("num_req=%d, num_err=%d, timed_stats=(history=%d, time_avg=%d, time_max=%d, time_min=%d)",
                num, err, n(), avg(), max(), min());
    }
}

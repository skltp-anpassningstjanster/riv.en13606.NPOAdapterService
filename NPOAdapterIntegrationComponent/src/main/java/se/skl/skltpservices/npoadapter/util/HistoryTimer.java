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

/**
 * Keeps timing history.
 *
 * @author Peter
 */
public class HistoryTimer extends Timer {
    private static final long serialVersionUID = 1L;
    private int len;
    private int ofs = 0;
    private long[] history;

    //
    public HistoryTimer(String name, int len) {
        super(name);
        this.len = len;
        this.history = new long[len];
        Arrays.fill(history, -1);
    }

    //
    public void add(long t) {
        if (ofs >= len) {
            ofs = 0;
        }
        history[ofs++] = t;
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
        return String.format("samples=%d, time_avg=%d, time_max=%d, time_min=%d", n(), avg(), max(), min());
    }
}

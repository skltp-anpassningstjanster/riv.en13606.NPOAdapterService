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
package se.skl.skltpservices.npoadapter.mapper;

import java.util.HashMap;

/**
 * Abstract code (key) to value mapper (tuples) with reverse index.
 *
 * @author Peter
 */
public abstract class AbstarctCodeMapper<K, V> {
    private HashMap<K, V> map = new HashMap<K, V>();
    private HashMap<V, K> reverseMap = new HashMap<V, K>();

    protected void add(K key, V value) {
        map.put(key, value);
        reverseMap.put(value, key);
    }

    protected V value(K key, V defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        final V value = map.get(key);
        return (value == null) ? defaultValue : value;
    }

    protected K key(V key, K defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        final K value = reverseMap.get(key);
        return (value == null) ? defaultValue : value;
    }
}

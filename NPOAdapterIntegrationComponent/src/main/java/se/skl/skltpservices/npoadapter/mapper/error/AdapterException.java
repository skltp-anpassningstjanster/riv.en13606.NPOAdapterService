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
package se.skl.skltpservices.npoadapter.mapper.error;

import se.skl.skltpservices.npoadapter.mule.Ehr13606AdapterError;

public abstract class AdapterException extends Exception {
	private static final long serialVersionUID = 1L;
	private final Ehr13606AdapterError error;
	
	public AdapterException(final String message) {
		super(message);
		this.error = Ehr13606AdapterError.UNDEFINED;
	}
	public AdapterException(final String message, final Exception cause) {
		super(message, cause);
		this.error = Ehr13606AdapterError.UNDEFINED;;
	}
	public AdapterException(final String message, final Ehr13606AdapterError errorCode) {
		super(message);
		this.error = errorCode;
	}
	public AdapterException(final String message, final Exception cause, final Ehr13606AdapterError errorCode) {
		super(message, cause);
		this.error = errorCode;
	}
	public Ehr13606AdapterError getEhr13606AdapterError() {
		return error;
	}
}

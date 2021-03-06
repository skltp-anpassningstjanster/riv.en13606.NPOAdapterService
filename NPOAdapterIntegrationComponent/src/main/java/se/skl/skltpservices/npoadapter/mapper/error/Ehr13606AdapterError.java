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

/**
 * Error codes that can be returned in a soap fault.
 * 
 * @author Martin Flower
 */
public enum Ehr13606AdapterError {
	
	NOERROR                       ("0000"),
	UNDEFINED                     ("1000"),
	MAPREQUEST                    ("2001"),
	MAPRESPONSE                   ("2011"),
	MAPRIVREQUEST                 ("2002"),
	MAPRIVRESPONSE                ("2012"),
	INDEXUPDATE                   ("3001"),
	INDEXUPDATE_MESSAGE_TYPE      ("3002"),
	INDEXUPDATE_MISSING_TYPE      ("3003"),
    INDEXUPDATE_SENDSTATUS        ("3004"),
	ROUTE		                  ("4001"),
    ROUTE_CALLBACK_MISSING        ("4001"),
    ROUTE_CALLBACK_URL_BLANK      ("4002"),
	UNSUPPORTED                   ("5000"),
	MISSINGDATA                   ("6000"),
	INVALIDATA                    ("6001"),
    OUTBOUNDRESPONSE_UNRECOGNISED ("7001"),
    OUTBOUNDRESPONSE_NULL         ("7002");
    
	private String errorCode;
	
	Ehr13606AdapterError(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public boolean isError() {
	    return !(this.equals(NOERROR));
	}
    public boolean isDefinedError() {
        return !(this.equals(NOERROR) || this.equals(UNDEFINED));
    }
}

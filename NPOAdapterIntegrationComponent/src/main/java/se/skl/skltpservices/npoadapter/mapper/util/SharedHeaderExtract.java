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
package se.skl.skltpservices.npoadapter.mapper.util;

import java.util.Collections;
import java.util.Map;

import se.rivta.en13606.ehrextract.v11.*;

public class SharedHeaderExtract {
	
	private final String systemHSAId;
	private final II subjectOfCare;
	
	private final Map<String, ORGANISATION> organisations;
	private final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> healthcareProfessionals;
	
	public SharedHeaderExtract(final Map<String, ORGANISATION> orgs, 
								final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps, 
								final String systemHSAId,
								final II subjectOfCare) {
		this.organisations = Collections.unmodifiableMap(orgs);
		this.healthcareProfessionals = Collections.unmodifiableMap(hps);
		this.systemHSAId = systemHSAId;
		this.subjectOfCare = subjectOfCare;
	}
	
	public Map<String, ORGANISATION> organisations() {
		return this.organisations;
	}
	
	public Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> healthcareProfessionals() {
		return this.healthcareProfessionals;
	}
	
	public String systemHSAId() {
		return this.systemHSAId;
	}
		
	public II subjectOfCare() {
		return this.subjectOfCare;
	}
}

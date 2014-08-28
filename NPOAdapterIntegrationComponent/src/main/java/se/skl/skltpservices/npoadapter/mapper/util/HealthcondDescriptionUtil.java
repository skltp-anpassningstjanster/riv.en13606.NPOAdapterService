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

import java.util.List;
import java.util.Map;

import riv.clinicalprocess.healthcond.description._2.DatePeriodType;
import riv.clinicalprocess.healthcond.description._2.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.description._2.PersonIdType;
import riv.clinicalprocess.healthcond.description._2.ResultType;
import se.rivta.en13606.ehrextract.v11.*;

/**
 * Utility class to create and map elements in the healtcond.description domain.
 * @author torbjorncla
 *
 */
public final class HealthcondDescriptionUtil {
    private HealthcondDescriptionUtil() {

    }

    //
    public static PatientSummaryHeaderType mapHeaderType(final COMPOSITION comp,
                                                         final String systemHsaId,
                                                         final II subjectOfCare,
                                                         final Map<String, ORGANISATION> orgs,
                                                         final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps,
                                                         final String timeElement) {
        return EHRUtil.patientSummaryHeader(comp, systemHsaId, subjectOfCare, orgs, hps, timeElement, PatientSummaryHeaderType.class);
    }


    public static PersonIdType mapPersonIdType(final II elm) {
        return EHRUtil.personIdType(elm, PersonIdType.class);
    }

    public static II iiType(final PersonIdType idType) {
        return EHRUtil.iiType(idType);
    }

    public static IVLTS IVLTSType(final DatePeriodType datePeriod) {
        return EHRUtil.IVLTSType(datePeriod);
    }

    public static ResultType mapResultType(final String uniqueId, final List<ResponseDetailType> respDetails) {
        return EHRUtil.resultType(uniqueId, respDetails, ResultType.class);
    }
}

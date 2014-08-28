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

import riv.clinicalprocess.healthcond.actoutcome._3.CVType;
import riv.clinicalprocess.healthcond.actoutcome._3.IIType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._3.PersonIdType;
import riv.clinicalprocess.healthcond.actoutcome._3.ResultType;
import riv.clinicalprocess.healthcond.actoutcome._3.TimePeriodType;
import se.rivta.en13606.ehrextract.v11.*;

/**
 * Helper util for the healthcond.actoutcome domain
 *
 * @author torbjorncla
 *
 */
public final class HealthcondActOutcomeUtil {


    private HealthcondActOutcomeUtil() {
    }

    //
    public static PatientSummaryHeaderType mapHeaderType(final COMPOSITION comp, final String systemHsaId,
                                                         final II subjectOfCare,
                                                         final Map<String, ORGANISATION> orgs,
                                                         final Map<String, IDENTIFIEDHEALTHCAREPROFESSIONAL> hps) {

        return EHRUtil.patientSummaryHeader(comp, systemHsaId, subjectOfCare, orgs, hps, null, PatientSummaryHeaderType.class);
    }

    //
    public static PersonIdType mapPersonIdType(final II elm) {
        return EHRUtil.personIdType(elm, PersonIdType.class);
    }

    //
    public static IIType mapIIType(final II ii) {
        return EHRUtil.iiType(ii, IIType.class);
    }

    public static IIType mapIIType(final String extension, final String root) {
        return EHRUtil.iiType(EHRUtil.iiType(root, extension), IIType.class);
    }

    public static CVType mapCVType(final II id) {
        return EHRUtil.cvType(id, CVType.class);
    }

    public static CVType mapCVType(final String code, final String codeSystem) {
        return EHRUtil.cvType(code, codeSystem, null, CVType.class);
    }

    public static CVType mapCVType(final CD cd) {
        return EHRUtil.cvType(cd, CVType.class);
    }

    public static TimePeriodType mapTimePeriodType(final IVLTS ivlts) {
        return EHRUtil.datePeriod(ivlts, TimePeriodType.class);
    }


    public static ResultType mapResultType(final String uniqueId, final List<ResponseDetailType> respDetails) {
        return EHRUtil.resultType(uniqueId, respDetails, ResultType.class);
    }

}

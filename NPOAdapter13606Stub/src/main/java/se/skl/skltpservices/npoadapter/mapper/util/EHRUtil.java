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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class EHRUtil {


    private static ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };

    public static String formatTimestamp(Date timestamp) {
        return formatter.get().format(timestamp);
    }
    //
    
    public static Date parseTimestamp(String timestamp) throws ParseException {
        return formatter.get().parse(timestamp);
    }

    public static class Request {
        //
        private PersonId patientId;

        public PersonId getPatientId() {
            return patientId;
        }

        public void setPatientId(PersonId patientId) {
            this.patientId = patientId;
        }

        //
        private DatePeriod timePeriod;

        public DatePeriod getTimePeriod() {
            return timePeriod;
        }

        public void setTimePeriod(DatePeriod timePeriod) {
            this.timePeriod = timePeriod;
        }

        // RIV-TA : GetAlertInformation, GetCareContacts, GetDiagnosis, GetImagingOutcome, GetLaboratoryOrderOutcome, GetMedicationHistory
        private List<String> careUnitHSAId;

        public List<String> getCareUnitHSAId() {
            if (careUnitHSAId == null) {
                careUnitHSAId = new ArrayList<String>();
            }
            return careUnitHSAId;
        }

        public void setCareUnitHSAId(List<String> ids) {
            careUnitHSAId = ids;
        }

        // RIV-TA : GetReferralOutcome, GetCareDocumentation
        private List<String> careUnitHSAid;

        public List<String> getCareUnitHSAid() {
            if (careUnitHSAid == null) {
                careUnitHSAid = new ArrayList<String>();
            }
            return careUnitHSAid;
        }

        public void setCareUnitHSAid(List<String> ids) {
            careUnitHSAid = ids;
        }
    }
    
    //
    public static class DatePeriod {
        private String start;
        private String end;

        public DatePeriod() {
            super();
        }

        public DatePeriod(String start, String end) {
            super();
            this.start = start;
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

    }
    
    //
    public static class PersonId {
        private String id;
        private String type;

        public PersonId() {
            super();
        }

        public PersonId(String id, String type) {
            super();
            this.id = id;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }


}

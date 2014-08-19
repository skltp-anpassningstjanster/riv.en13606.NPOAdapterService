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

import riv.clinicalprocess.logistics.logistics.getcarecontactsresponder._2.GetCareContactsType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractResponseType;
import riv.ehr.patientsummary.getehrextractresponder._1.GetEhrExtractType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;

import javax.xml.stream.XMLStreamReader;

/**
 * Created by Peter on 2014-08-14.
 */
public class RIVCareContactsMapper extends CareContactsMapper {

    @Override
    public String mapResponse(final XMLStreamReader reader) {
        final GetEhrExtractResponseType ehrExtractResponseType = ehrExtractResponseType(reader);

        // map to baseline model
        final RIV13606REQUESTEHREXTRACTResponseType riv13606REQUESTEHREXTRACTResponseType = map(ehrExtractResponseType);

        return marshal(map(riv13606REQUESTEHREXTRACTResponseType.getEhrExtract().get(0)));
    }

    @Override
    public String mapRequest(final XMLStreamReader reader) {
        final GetCareContactsType request = unmarshal(reader);

        // map to baseline model
        final RIV13606REQUESTEHREXTRACTRequestType ehrRequest = map(request);

        final GetEhrExtractType ehrExtractType = map(ehrRequest);

        return ehrExtractType(ehrExtractType);
    }
}

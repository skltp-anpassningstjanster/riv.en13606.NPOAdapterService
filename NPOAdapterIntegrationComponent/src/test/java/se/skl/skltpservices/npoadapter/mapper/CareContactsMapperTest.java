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

import static junit.framework.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import se.rivta.clinicalprocess.logistics.logistics.getcarecontacts.v2.GetCareContactsResponseType;
import se.rivta.clinicalprocess.logistics.logistics.v2.*;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Peter on 2014-07-28.
 */
public class CareContactsMapperTest {


    private static SimpleDateFormat timeStampFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
    static {
        timeStampFormatter.setLenient(false);
    }

    private static EHREXTRACT ehrextract;

    @BeforeClass
    public static void init() throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance("se.rivta.en13606.ehrextract.v11");
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final JAXBElement<EHREXTRACT> root = (JAXBElement<EHREXTRACT>) unmarshaller.unmarshal(CareContactsMapperTest.class.getResourceAsStream("/CareContacts_SSEN13606-2.1.1.xml"));
        ehrextract = root.getValue();
    }

    // Make it easy to dump the resulting response after map (for dev purposes only)
    @XmlRootElement
    static class Root {
       @XmlElement
       private GetCareContactsResponseType type;
    }

    //
    private void dump(GetCareContactsResponseType responseType) throws JAXBException {
        final JAXBContext context = JAXBContext.newInstance(Root.class);
        Root root = new Root();
        root.type = responseType;
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(root, System.out);
    }


    @Test
    public void testMapFromEhrToCareContratcs() {
        CareContactsMapper mapper = new CareContactsMapper();
        GetCareContactsResponseType responseType = mapper.map(ehrextract);
        assertNotNull(responseType);

        assertNotNull(responseType.getCareContact());
        assertEquals(4, responseType.getCareContact().size());

        for (final CareContactType careContactType : responseType.getCareContact()) {
            verifyCareContactHeader(careContactType.getCareContactHeader());
            verifyCareContactBody(careContactType.getCareContactBody());
        }
    }


    private void verifyCareContactHeader(PatientSummaryHeaderType careContactHeader) {
        assertNotNull(careContactHeader.getDocumentId());
        assertNotNull(careContactHeader.getSourceSystemHSAId());
        verifyAccountableHealthcareProfessional(careContactHeader.getAccountableHealthcareProfessional());

        assertNotNull(careContactHeader.getPatientId());
        assertTrue(careContactHeader.isApprovedForPatient());
        assertFalse(careContactHeader.isNullified());
        assertNull(careContactHeader.getNullifiedReason());
    }

    private void verifyAccountableHealthcareProfessional(HealthcareProfessionalType accountableHealthcareProfessional) {
        assertNotNull(accountableHealthcareProfessional);
        verifyTimeStampType(accountableHealthcareProfessional.getAuthorTime(), false);
        assertNotNull(accountableHealthcareProfessional.getHealthcareProfessionalHSAId());
        assertNotNull(accountableHealthcareProfessional.getHealthcareProfessionalName());
        verifyOrgUnit(accountableHealthcareProfessional.getHealthcareProfessionalOrgUnit());

        assertNull(accountableHealthcareProfessional.getHealthcareProfessionalRoleCode());

        // FIXME: According to spec (TKB) these 2 are optional för HealthcareProfessionalType, but mandatory in responseType.
        assertNull(accountableHealthcareProfessional.getHealthcareProfessionalCareGiverHSAId());
        assertNull(accountableHealthcareProfessional.getHealthcareProfessionalCareUnitHSAId());
    }

    private void verifyOrgUnit(OrgUnitType healthcareProfessionalOrgUnit) {
        assertNotNull(healthcareProfessionalOrgUnit);
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitEmail());
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitAddress());
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitName());
        assertNull(healthcareProfessionalOrgUnit.getOrgUnitLocation());
        assertNotNull(healthcareProfessionalOrgUnit.getOrgUnitTelecom());
    }


    private void verifyCareContactBody(CareContactBodyType careContactBody) {
        assertNotNull(careContactBody.getCareContactCode());
        assertTrue(1 <= careContactBody.getCareContactCode() && 5 >= careContactBody.getCareContactCode());
        assertNull(careContactBody.getCareContactReason());
        assertNotNull(careContactBody.getCareContactStatus());
        assertTrue(1 <= careContactBody.getCareContactStatus() && 5 >= careContactBody.getCareContactStatus());

        verifyOrgUnit(careContactBody.getCareContactOrgUnit());

        assertNotNull(careContactBody.getCareContactTimePeriod());

        verifyTimeStampType(careContactBody.getCareContactTimePeriod().getStart(), false);
        verifyTimeStampType(careContactBody.getCareContactTimePeriod().getEnd(), true);
     }

    private void verifyTimeStampType(String timestamp, boolean nullable) {
        if (!nullable) {
            assertNotNull(timestamp);
        }
        if (timestamp != null) {
            try {
                timeStampFormatter.parse(timestamp);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

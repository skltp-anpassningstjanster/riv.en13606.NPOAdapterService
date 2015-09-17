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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.api.MuleMessage;

import riv.clinicalprocess.activityprescription.actoutcome._2.DispensationAuthorizationType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DosageType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugArticleType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugChoiceType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugType;
import riv.clinicalprocess.activityprescription.actoutcome._2.HealthcareProfessionalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationPrescriptionType;
import riv.clinicalprocess.activityprescription.actoutcome._2.OrgUnitType;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.PrescriptionStatusEnum;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin Flower
 */
public class MedicationHistoryMapperTest {

    private static EHREXTRACT ehrextract;
    
    private static GetMedicationHistoryResponseType resp;
    
    private static final String DOC_ID_1 = "SE1623210002198208149297ordination106";
    private static final String DOC_ID_2 = "SE1623210002198208149297ordination109";
    private static final String DOC_ID_3 = "SE1623210002198208149297ordination111";
    private static final String DOC_ID_4 = "SE1623210002198208149297ordination113";
    
    private static final HashMap<String, MedicationMedicalRecordType> records = new HashMap<String, MedicationMedicalRecordType>();

    @BeforeClass
    public static void init() throws JAXBException {
        ehrextract = Util.loadEhrTestData(Util.MEDICALHISTORY_TEST_FILE);
        
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        MedicationHistoryMapper mapper = (MedicationHistoryMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_MEDICATIONHISTORY);        
        resp = mapper.mapEhrExtract(Arrays.asList(ehrextract), mockMessage);
        
        for(MedicationMedicalRecordType rec : resp.getMedicationMedicalRecord()) {
        	records.put(rec.getMedicationMedicalRecordHeader().getDocumentId(), rec);
        }
        
        
        assertTrue(records.containsKey(DOC_ID_1));
        assertTrue(records.containsKey(DOC_ID_2));
        assertTrue(records.containsKey(DOC_ID_3));
        assertTrue(records.containsKey(DOC_ID_4));
    }

    // Make it easy to dump the resulting response after createTS
    @XmlRootElement
    static class Root {
        @XmlElement
        private GetMedicationHistoryResponseType type;
    }

    private void dump(final GetMedicationHistoryResponseType responseType) {
        Root root = new Root();
        root.type = responseType;
        try {
            Util.dump(root);
        } catch (JAXBException j) {
            fail(j.getLocalizedMessage());
        }
    }
    
    @Test
    public void testHeader() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_1);
    	assertNotNull(rec);
    	assertNotNull(rec.getMedicationMedicalRecordHeader());
    	
    	assertEquals(rec.getMedicationMedicalRecordHeader().getDocumentId(), DOC_ID_1);
    	assertEquals(rec.getMedicationMedicalRecordHeader().getSourceSystemHSAId(), "SE162321000230-0011");
    	assertEquals(rec.getMedicationMedicalRecordHeader().getPatientId().getId(), "198208149297");
    	assertEquals(rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getAuthorTime(), "20150305000000");
    	assertEquals(rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalHSAId(), "SE2321000230-102X");
    	assertEquals(rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalName(), "PascalLars, Gustafsson");
    	final riv.clinicalprocess.activityprescription.actoutcome._2.CVType role = rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalRoleCode();
    	assertEquals(role.getDisplayName(), "Läkare");
    	assertEquals(role.getCode(), "Läkare");
    	final OrgUnitType org = rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalOrgUnit();
    	assertEquals(org.getOrgUnitAddress(), "Sunderby sjukhus  97180 LULEÅ");
    	assertEquals("test@test.se", org.getOrgUnitEmail());
    	assertEquals("SE2321000230-LuBo-Kir", org.getOrgUnitHSAId());
    	assertEquals("0920-282000", org.getOrgUnitTelecom());
    	assertEquals("LULEÅ", org.getOrgUnitLocation());
    	
    	assertEquals("SE2321000230-1016", rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareGiverHSAId());
    	assertEquals("SE2321000230-1019", rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalCareUnitHSAId());
    
    	
    }
    
    @Test
    public void testPrescription() {
    	final MedicationMedicalRecordType rec1 = records.get(DOC_ID_1);
    	final MedicationMedicalRecordType rec2 = records.get(DOC_ID_2);
    	
    	
    	final MedicationPrescriptionType mpt1 = rec1.getMedicationMedicalRecordBody().getMedicationPrescription();
    	final MedicationPrescriptionType mpt2 = rec2.getMedicationMedicalRecordBody().getMedicationPrescription();
    	assertEquals("SE1623210002198208149297ordination106", mpt1.getPrescriptionId().getExtension());
    	assertEquals("notat", mpt2.getPrescriptionNote());
    	assertEquals("19800101000000", mpt2.getEvaluationTime());
    	assertEquals("SE1623210002198208149297ordination109", mpt2.getPrescriptionChainId().getExtension());
    	assertEquals(PrescriptionStatusEnum.ACTIVE, mpt1.getPrescriptionStatus());
    	assertEquals(PrescriptionStatusEnum.ACTIVE, mpt2.getPrescriptionStatus());
    }
    
    @Test
    public void testPrescriber() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_1);
    	final HealthcareProfessionalType hp = rec.getMedicationMedicalRecordBody().getMedicationPrescription().getPrescriber();
    	assertEquals("20150305000000", hp.getAuthorTime());
    	assertEquals("SE2321000230-102X", hp.getHealthcareProfessionalHSAId());
    }
    
    @Test
    public void testDrug() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_1);
    	final DrugChoiceType drug = rec.getMedicationMedicalRecordBody().getMedicationPrescription().getDrug();
    	assertEquals("EXTEMPORE E-FÖRSKRIVNING", drug.getUnstructuredDrugInformation().getUnstructuredInformation());
    }
    
    @Test
    public void testArticleDrug() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_2);
    	final DrugArticleType dat = rec.getMedicationMedicalRecordBody().getMedicationPrescription().getDrug().getDrugArticle();
    	assertEquals("19861001100155", dat.getNplPackId().getDisplayName());
    }
    
    @Test
    public void testDrugDrug() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_2);
    	final DrugType drug = rec.getMedicationMedicalRecordBody().getMedicationPrescription().getDrug().getDrug();
    	assertEquals("J01FA01", drug.getAtcCode().getCode());
    	assertEquals("1.2.752.129.2.2.3.1.1", drug.getAtcCode().getCodeSystem());
    	assertEquals("Erytromycin", drug.getAtcCode().getDisplayName());
    	
    	assertEquals("Tablett", drug.getPharmaceuticalForm());
    	assertEquals(Double.valueOf("500"), drug.getStrength());
    	assertEquals("MG", drug.getStrengthUnit());
    }
    
    @Test
    public void testDosage() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_1);
    	assertNotNull(rec.getMedicationMedicalRecordBody().getMedicationPrescription().getDrug());
    	final DrugChoiceType drug = rec.getMedicationMedicalRecordBody().getMedicationPrescription().getDrug();
    	assertEquals(1, drug.getDosage().size());
    	final DosageType dos = drug.getDosage().get(0);
    	assertEquals(Double.valueOf("20150305000000"), dos.getLengthOfTreatment().getTreatmentInterval().getLow());
    	assertNull(dos.getLengthOfTreatment().getTreatmentInterval().getHigh());
    	assertTrue(dos.getLengthOfTreatment().isIsMaximumTreatmentTime());
    	assertEquals("Putar med magen", dos.getDosageInstruction());
    	assertEquals("eo", dos.getShortNotation());
    }
    
    @Test
    public void testDispensationAuth() {
    	final MedicationMedicalRecordType rec = records.get(DOC_ID_1);
    	final DispensationAuthorizationType d = rec.getMedicationMedicalRecordBody().getMedicationPrescription().getDispensationAuthorization();
    	assertNotNull(d);
    	assertEquals("SE1623210002198208149297recept1061", d.getDispensationAuthorizationId().getExtension());
    	assertEquals(Double.valueOf("0"), d.getTotalAmount());
    	assertEquals("styck", d.getPackageUnit());
    	assertEquals("Apotek", d.getDistributionMethod());
    	assertEquals("20150305000000", d.getDispensationAuthorizer().getAuthorTime());
    	assertEquals("PascalLars, Gustafsson", d.getDispensationAuthorizer().getHealthcareProfessionalName());
    	assertEquals("SE2321000230-102X", d.getDispensationAuthorizer().getHealthcareProfessionalHSAId());
    }

    
}

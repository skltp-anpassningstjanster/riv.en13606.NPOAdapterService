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

import riv.clinicalprocess.activityprescription.actoutcome._2.CVType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DispensationAuthorizationType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DosageType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugArticleType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugChoiceType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugType;
import riv.clinicalprocess.activityprescription.actoutcome._2.GenericsType;
import riv.clinicalprocess.activityprescription.actoutcome._2.HealthcareProfessionalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationPrescriptionType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MerchandiseType;
import riv.clinicalprocess.activityprescription.actoutcome._2.OrgUnitType;
import riv.clinicalprocess.activityprescription.actoutcome._2.UnstructuredDrugInformationType;
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
    
    private static MedicationHistoryMapper mapper;
    
    private static final HashMap<String, MedicationMedicalRecordType> records = new HashMap<String, MedicationMedicalRecordType>();

    @BeforeClass
    public static void init() throws JAXBException {
        ehrextract = Util.loadEhrTestData(Util.MEDICALHISTORY_TEST_FILE);
        
        MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        mapper = (MedicationHistoryMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_MEDICATIONHISTORY);        
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
    	
    	assertEquals(DOC_ID_1, rec.getMedicationMedicalRecordHeader().getDocumentId());
    	assertEquals("SE162321000230-0011",rec.getMedicationMedicalRecordHeader().getSourceSystemHSAId());
    	assertEquals("198208149297", rec.getMedicationMedicalRecordHeader().getPatientId().getId());
    	assertEquals("20150305000000", rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getAuthorTime());
    	assertNull(rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalHSAId());
    	assertNull(rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalName());
    	final riv.clinicalprocess.activityprescription.actoutcome._2.CVType role = rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalRoleCode();
    	assertNull(role);
    	final OrgUnitType org = rec.getMedicationMedicalRecordHeader().getAccountableHealthcareProfessional().getHealthcareProfessionalOrgUnit();
    	assertNull(org);
    	
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
    	assertEquals("20150305000001", hp.getAuthorTime());
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

    /**
     * Rule "only one is allowed"
     * Priority
     * 1. drugArticle
     * 2. drug
     * 3. merchandise
     * 4. generics
     * 5. unstructuredDrugInformation
     */
	@Test
	public void testApplyAdapterSpecificRules() throws Exception {
		DrugChoiceType testObject = new DrugChoiceType();
		
		final DrugArticleType drugArticle = new DrugArticleType();
		final DrugType drug = new DrugType();
		final MerchandiseType merchandise = new MerchandiseType();
		final GenericsType generics = new GenericsType();
		final UnstructuredDrugInformationType unstructuredDrugInfo = new UnstructuredDrugInformationType();
		
		//All populated, only drugArticle should remain
		testObject.setDrug(drug);
		testObject.setDrugArticle(drugArticle);
		testObject.setGenerics(generics);
		testObject.setMerchandise(merchandise);
		testObject.setUnstructuredDrugInformation(unstructuredDrugInfo);
		mapper.applyAdapterSpecificRules(testObject);
		assertNotNull(testObject.getDrugArticle());
		assertNull(testObject.getDrug());
		assertNull(testObject.getMerchandise());
		assertNull(testObject.getGenerics());
		assertNull(testObject.getUnstructuredDrugInformation());
		
		//Only generics populated, only generics should remain
		testObject = new DrugChoiceType();
		testObject.setGenerics(generics);
		mapper.applyAdapterSpecificRules(testObject);
		assertNull(testObject.getDrugArticle());
		assertNull(testObject.getDrug());
		assertNull(testObject.getMerchandise());
		assertNotNull(testObject.getGenerics());
		assertNull(testObject.getUnstructuredDrugInformation());
		
		//Unstructured and drug populated, only drug should remain
		testObject = new DrugChoiceType();
		testObject.setUnstructuredDrugInformation(unstructuredDrugInfo);
		testObject.setDrug(drug);
		mapper.applyAdapterSpecificRules(testObject);
		assertNull(testObject.getDrugArticle());
		assertNotNull(testObject.getDrug());
		assertNull(testObject.getMerchandise());
		assertNull(testObject.getGenerics());
		assertNull(testObject.getUnstructuredDrugInformation());
		
		//None populated, none should remain
		testObject = new DrugChoiceType();
		mapper.applyAdapterSpecificRules(testObject);
		assertNull(testObject.getDrugArticle());
		assertNull(testObject.getDrug());
		assertNull(testObject.getMerchandise());
		assertNull(testObject.getGenerics());
		assertNull(testObject.getUnstructuredDrugInformation());
		
	}
}

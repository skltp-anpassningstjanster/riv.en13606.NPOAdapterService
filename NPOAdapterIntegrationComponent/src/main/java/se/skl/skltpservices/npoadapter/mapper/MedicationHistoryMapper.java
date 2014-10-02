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

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.activityprescription.actoutcome.enums._2.ResultCodeEnum;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.TypeOfPrescriptionEnum;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.ObjectFactory;
import riv.clinicalprocess.activityprescription.actoutcome._2.AdditionalPatientInformationType;
import riv.clinicalprocess.activityprescription.actoutcome._2.CVType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DispensationAuthorizationType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugChoiceType;
import riv.clinicalprocess.activityprescription.actoutcome._2.IIType;
import riv.clinicalprocess.activityprescription.actoutcome._2.LegalAuthenticatorType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordBodyType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationPrescriptionType;
import riv.clinicalprocess.activityprescription.actoutcome._2.OrgUnitType;
import riv.clinicalprocess.activityprescription.actoutcome._2.HealthcareProfessionalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PatientSummaryHeaderType;



import riv.clinicalprocess.activityprescription.actoutcome._2.PersonIdType;
import riv.clinicalprocess.activityprescription.actoutcome._2.ResultType;
import se.rivta.en13606.ehrextract.v11.*;

import se.rivta.en13606.ehrextract.v11.CLUSTER;

import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import java.util.Date;
import java.util.List;






/**
 * Input
 *  lkf
 *  Läkemedel förskrivning
 * Output
 *  riv:clinicalprocess:activityprescription:actoutcome
 *  GetMedicationHistory
 * 
 * Maps from EHR_EXTRACT to RIV GetMedicationHistoryResponseType v2.0. 
 * <p>
 * Riv contract spec : "http://rivta.se/downloads/ServiceContracts_clinicalpocess_activityprescription_actoutcome_2.0_RC1.zip"
 *
 * @author Martin
 */
@Slf4j
public class MedicationHistoryMapper extends AbstractMapper implements Mapper {

    public static final CD MEANING_VKO = new CD();
    static {
        MEANING_VKO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_VKO.setCode(INFO_VKO);
    }

    private static final JaxbUtil jaxb = new JaxbUtil(GetMedicationHistoryType.class, GetMedicationHistoryResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();


    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType response 
    		   = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
            message.setPayload(marshal(map(response.getEhrExtract())));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping response", err);
    	}
    }

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
    	try {
    		final GetMedicationHistoryType request 
    		  = unmarshal(payloadAsXMLStreamReader(message));
        	message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_VKO)));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping request", err);
    	}
    }

    /**
     * Maps from EHR_EXTRACT (lko) to GetMedicationHistoryResponseType.
     *
     * @param ehrExtractList the EHR_EXTRACT XML Java bean.
     * @return riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType response type
     */
    protected GetMedicationHistoryResponseType map(final List<EHREXTRACT> ehrExtractList) {

        final GetMedicationHistoryResponseType responseType = new GetMedicationHistoryResponseType();
        
        ResultType resultType = new ResultType();
        resultType.setMessage("Hello World");
        resultType.setResultCode(ResultCodeEnum.OK);
        resultType.setSubCode("SubCode123");
        
        responseType.setResult(resultType);
        
        log.debug("list of EHREXTRACT - " + ehrExtractList.size());

        if (!ehrExtractList.isEmpty()) {
            final EHREXTRACT ehrExtract = ehrExtractList.get(0);
            for (int i = 0; i < ehrExtract.getAllCompositions().size(); i++) {
                final MedicationMedicalRecordType medicationMedicalRecordType = new MedicationMedicalRecordType();
                medicationMedicalRecordType.setMedicationMedicalRecordHeader(mapHeader(ehrExtract, i));
                medicationMedicalRecordType.setMedicationMedicalRecordBody(mapBody(ehrExtract, i));
                responseType.getMedicationMedicalRecord().add(medicationMedicalRecordType);
            }
        }
        return responseType;
    }

    //
    protected GetMedicationHistoryType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetMedicationHistoryType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    protected String marshal(final GetMedicationHistoryResponseType response) {
        final JAXBElement<GetMedicationHistoryResponseType> el = objectFactory.createGetMedicationHistoryResponse(response);
        return jaxb.marshal(el);
    }

    /**
     * Maps contact header information.
     *
     * @param ehrExtract the extract.
     * @param compositionIndex the actual composition in the list.
     * @return the target header information.
     */
    protected PatientSummaryHeaderType mapHeader(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);

        final PatientSummaryHeaderType headerType = new PatientSummaryHeaderType();
        
        headerType.setAccountableHealthcareProfessional(mapProfessional(composition, ehrExtract.getDemographicExtract()));
        headerType.setApprovedForPatient(false);
        headerType.setCareContactId("friday");
        headerType.setDocumentId(composition.getRcId().getExtension());
        headerType.setDocumentTime("10:59");
        headerType.setDocumentTitle("The title of this document");
        headerType.setLegalAuthenticator(new LegalAuthenticatorType());
        headerType.setNullified(false);
        headerType.setNullifiedReason(null);
        headerType.setPatientId(EHRUtil.personIdType(ehrExtract.getSubjectOfCare(), PersonIdType.class));
        headerType.setSourceSystemHSAId(ehrExtract.getEhrSystem().getExtension());
        
        return headerType;
    }
    
    /**
     * Maps contact body information.
     *
     * @param ehrExtract the extract to createTS from.
     * @param compositionIndex the actual composition in the list.
     * @return the target body information.
     */
    protected MedicationMedicalRecordBodyType mapBody(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);

        final MedicationMedicalRecordBodyType bodyType = new MedicationMedicalRecordBodyType();
        
        
        AdditionalPatientInformationType apit = new AdditionalPatientInformationType();
        apit.setDateOfBirth("2010-01-31");
        
        CVType gender = new CVType();
        gender.setCode("abc");
        gender.setCodeSystem("def");
        gender.setDisplayName("ghi");
        gender.setOriginalText("jkl");
        apit.setGender(gender);
        
        bodyType.setAdditionalPatientInformation(apit);
        
        MedicationPrescriptionType mpt = new MedicationPrescriptionType();
        
        mpt.setDispensationAuthorization(new DispensationAuthorizationType());
        mpt.setEndOfTreatment("2014-12-31");
        mpt.setEndOfTreatmentReason(new CVType());
        mpt.setEvaluator(new HealthcareProfessionalType());
        mpt.setPrecedingPrescriptionId(new IIType());
        mpt.setPrescriber(new HealthcareProfessionalType());
        mpt.setPrescriptionChainId(new IIType());
        mpt.setPrescriptionId(new IIType());
        mpt.setPrescriptionStatus(new CVType());
        mpt.setSelfMedication(false);
        mpt.setStartOfFirstTreatment("2014-01-31");
        mpt.setSucceedingPrescriptionId(new IIType());
        mpt.setTypeOfPrescription(TypeOfPrescriptionEnum.INSÄTTNING);
        
        bodyType.setMedicationPrescription(mpt);

        for (final CONTENT content : composition.getContent()) {
            for (final ITEM item : ((ENTRY) content).getItems()) {
            	
            	
            	log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + (item instanceof ELEMENT ? "ELEMENT" : "CLUSTER"));

                switch (item.getMeaning().getCode()) {
                    case "lkm-ord-tid":   // Ordinationstidpunkt
                        mpt.setStartOfTreatment("2014-02-28");
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-ord-not":   // Notat
                        mpt.setPrescriptionNote("Here is a note");
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-ord-utv":   // Utvärderingstidpunkt
                        mpt.setEvaluationTime("123");
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-ord-and":   // Ändamål
                        mpt.setTreatmentPurpose("There is a purpose to this treatment");
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                        

                    case "lkm-dos"    :   // Dosering
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue());
                    	CLUSTER c = (CLUSTER)item;
                    	List<ITEM> parts = c.getParts();
                    	for (ITEM part : parts) {
                        	log.debug(part.getMeaning().getCode());
                            if ("lkm-dst".equals(part.getMeaning().getCode())) {
                                c = (CLUSTER)part;
                                
                                List<ITEM> dosparts = c.getParts();
                                for (ITEM dospart : dosparts) {
                                    log.debug(dospart.getMeaning().getCode() + " " + dospart.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)dospart));
                                }
                            }
                        }
                        break;
                        
                    case "lkm-lva"    :   // Läkemedelsval
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue());
                        c = (CLUSTER)item;
                        parts = c.getParts();
                        for (ITEM part : parts) {
                            log.debug(part.getMeaning().getCode());
                            
                            switch (item.getMeaning().getCode()) {
                            case "lkm-lva-kom" :
                                log.debug(part.getMeaning().getCode() + " " + part.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)part));
                                break;
                            case "lkm-lva-typ" :
                                log.debug(part.getMeaning().getCode() + " " + part.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)part));
                                break;
                            case "lkm-lva-ext" :
                                log.debug(part.getMeaning().getCode() + " " + part.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)part));
                                break;
                            case "lkm-lkm-lva":
                                log.debug(part.getMeaning().getCode() + " " + part.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)part));
                                break;
                            }
                        }
                        // mpt.setDrug(new DrugChoiceType());
                        // EHRUtil.getElementTextValue((ELEMENT)item);
                        break;
                        
                        
                    case "lkm-for-tid":   // Förskrivningstidpunkt
                        mpt.setStartOfTreatment("2014-02-28");
                        break;
                    case "lkm-for-uiv":   // Utlämningsintervall
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-for-mpt":   // Mängd per tillfälle
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-for-tot":   // Totalmängd
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-for-fpe":   // Förpackningsenhet
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                    case "lkm-for-dbs":   // Distributionssätt
                        log.debug(item.getMeaning().getCode() + " " + item.getMeaning().getDisplayName().getValue() + " " + EHRUtil.getElementTextValue((ELEMENT)item));
                        break;
                }
            }
        }
        return bodyType;
    }
                
                
                // Executing unit
                /*
                for (final FUNCTIONALROLE role : composition.getOtherParticipations()) {
                    if ("ute".equals(EHRUtil.getCDCode(role.getFunction()))) {
                        final String hsaId = role.getPerformer().getExtension();
                        
                    }
                }
                */
        

    //
    
    protected HealthcareProfessionalType mapProfessional(final COMPOSITION composition, final List<IDENTIFIEDENTITY> demographics) {
    	
        final HealthcareProfessionalType professionalType = new HealthcareProfessionalType();
        professionalType.setAuthorTime(composition.getCommittal().getTimeCommitted().getValue());
		professionalType.setHealthcareProfessionalHSAId("Not known");
        FUNCTIONALROLE fr = composition.getComposer();
        if (fr != null) {
        	II ii = fr.getPerformer();
        	if (ii != null) {
        		professionalType.setHealthcareProfessionalHSAId(ii.getExtension());
        	}
        }
        

        final IDENTIFIEDHEALTHCAREPROFESSIONAL professional = (IDENTIFIEDHEALTHCAREPROFESSIONAL) EHRUtil.lookupDemographicIdentity(demographics, professionalType.getHealthcareProfessionalHSAId());
        if (professional != null) {
            professionalType.setHealthcareProfessionalName(EHRUtil.getPartValue(professional.getName()));
            final HEALTHCAREPROFESSIONALROLE role = EHRUtil.firstItem(professional.getRole());
            if (role != null) {
               final CVType cvType = EHRUtil.cvType(role.getProfession(), CVType.class);
                professionalType.setHealthcareProfessionalRoleCode(cvType);
            }
        }

        for (final FUNCTIONALROLE careGiver : composition.getOtherParticipations()) {
			if (careGiver.getFunction() != null && StringUtils.equalsIgnoreCase(careGiver.getFunction().getCode(), "iag")) {
				if (careGiver.getPerformer() != null) {
					professionalType.setHealthcareProfessionalCareGiverHSAId(careGiver.getPerformer().getExtension());
				}
				if (careGiver.getHealthcareFacility() != null) {
					professionalType.setHealthcareProfessionalCareUnitHSAId(careGiver.getHealthcareFacility().getExtension());
				}
			}
		}

        professionalType.setHealthcareProfessionalOrgUnit(null);
        FUNCTIONALROLE fro = composition.getComposer();
        if (fro != null) {
        	II hcf = fro.getHealthcareFacility();
        	if (hcf != null) {
        		professionalType.setHealthcareProfessionalOrgUnit(mapOrgUnit(demographics, hcf.getExtension()));
        	}
        }

        return professionalType;
    }


    //
    protected OrgUnitType mapTel(final OrgUnitType orgUnitType, final ORGANISATION organisation) {

        for (final TEL item : organisation.getTelecom()) {
            if (item instanceof TELEMAIL) {
                orgUnitType.setOrgUnitEmail(EHRUtil.removePrefix(item.getValue(), "mailto:"));
            } else if (item instanceof TELPHONE) {
                orgUnitType.setOrgUnitTelecom(EHRUtil.removePrefix(item.getValue(), "tel:"));
            }
        }
        return orgUnitType;
    }

    //
    protected OrgUnitType mapAddress(final OrgUnitType orgUnitType, final ORGANISATION organisation) {
        for (final AD ad : organisation.getAddr())
            for (final ADXP adxp : ad.getPartOrBrOrAddressLine()) {
                switch (adxp.getType()) {
                    case AL:
                        orgUnitType.setOrgUnitAddress(adxp.getContent());
                        break;
                    case CEN:
                        orgUnitType.setOrgUnitLocation(adxp.getContent());
                        break;
                    default:
                        break;
                }
            }
        return orgUnitType;
    }

    //
    protected OrgUnitType mapOrgUnit(final List<IDENTIFIEDENTITY> demographics, final String hsaId) {
        final OrgUnitType orgUnitType = new OrgUnitType();
        orgUnitType.setOrgUnitHSAId(hsaId);

        final ORGANISATION organisation = (ORGANISATION) EHRUtil.lookupDemographicIdentity(demographics, hsaId);
        if (organisation != null) {
            orgUnitType.setOrgUnitName(organisation.getName().getValue());
            mapTel(orgUnitType, organisation);
            mapAddress(orgUnitType, organisation);
        }
        return orgUnitType;
    }

    /**
     * Contact codes.
     *
     * @author Peter
     */
    public static class ContactCodes extends AbstractCodeMapper<Integer, String> {
        public static ContactCodes map = new ContactCodes();

        static {
            map.add(1, "Besök");
            map.add(2, "Telefon");
            map.add(3, "Vårdtillfälle");
            map.add(4, "Dagsjukvård");
            map.add(5, "Annan");
        }

        public String text(final Integer key) {
            return super.value(key, "Annan");
        }

        public Integer code(final String key) {
            return super.key(key, 5);
        }
    }

    /**
     * Contact status.
     *
     * @author Peter
     */
    public static class ContactStatus extends AbstractCodeMapper<Integer, String> {
        public static ContactStatus map = new ContactStatus();

        static {
            map.add(1, "Ej påbörjad");
            map.add(2, "Inställd");
            map.add(3, "Pågående");
            map.add(4, "Avbruten");
            map.add(5, "Avslutad");
        }

        public String text(final Integer key) {
            return super.value(key, "Ej påbörjad");
        }

        public Integer code(final String key) {
            return super.key(key, 1);
        }
    }
}

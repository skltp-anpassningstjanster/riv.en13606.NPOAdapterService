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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.activityprescription.actoutcome._2.CVType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DispensationAuthorizationType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DosageType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugArticleType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugChoiceType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugType;
import riv.clinicalprocess.activityprescription.actoutcome._2.GenericsType;
import riv.clinicalprocess.activityprescription.actoutcome._2.HealthcareProfessionalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.IIType;
import riv.clinicalprocess.activityprescription.actoutcome._2.LengthOfTreatmentType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordBodyType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationPrescriptionType;
import riv.clinicalprocess.activityprescription.actoutcome._2.OrgUnitType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PQIntervalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PQType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PatientSummaryHeaderType;
import riv.clinicalprocess.activityprescription.actoutcome._2.ResultType;
import riv.clinicalprocess.activityprescription.actoutcome._2.UnstructuredDrugInformationType;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.PrescriptionStatusEnum;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.TypeOfPrescriptionEnum;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.BL;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.FUNCTIONALROLE;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.LINK;
import se.rivta.en13606.ehrextract.v11.PQ;
import se.rivta.en13606.ehrextract.v11.PQTIME;
import se.rivta.en13606.ehrextract.v11.QTY;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil.HealthcareProfessional;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.skl.skltpservices.npoadapter.util.SpringPropertiesUtil;


/**
 * Input
 *  lkm
 *  lko
 *   Läkemedel ordination
 *  lkf
 *   Läkemedel förskrivning
 *   
 * Output
 *  riv:clinicalprocess:activityprescription:actoutcome
 *   GetMedicationHistory
 * 
 * Maps from EHR_EXTRACT to RIV GetMedicationHistoryResponseType 
 *
 * @author Martin
 */
public class MedicationHistoryMapper extends AbstractMapper implements Mapper {

	private static final Logger log = LoggerFactory.getLogger(MedicationHistoryMapper.class);
	
    public static final CD MEANING_LKM_ORD = new CD();
    
    protected static final String TIME_ELEMENT = "lkm-ord-tid";
    
    /**
     * TODO: OOIDER
     */
    private static final String INFORMATIONSMANGD_LAKEMEDEL_ORDINATION = "lko";
    private static final String INFORMATIONSMANGD_LAKEMEDEL_FORSKRIVNING = "lkf";
    private static final String INFORMATIONSMANGD_VARDKONTAKT = "vko";
    
    private static final String DELTAGARE_UTVARDERAS_AV = "utv";
    
    private static final String LAKEMEDELS_ORDINATION = "lkm-ord";
    private static final String LAKEMEDELS_ORDINATION_NOT = "lkm-ord-not";
    private static final String LAKEMEDELS_ORDINATION_UTVARDERINGSTIDPUNKT = "lkm-ord-utv";
    private static final String LAKEMEDELS_ORDINATION_ANDAMAL = "lkm-ord-and";
    private static final String LAKEMEDELS_ORDINATION_ORDINATIONS_KEDJA = "lkm-ord-oki";
    private static final String LAKEMEDELS_ORDINATION_TIDPUNKT = "lkm-ord-tid";
    
    private static final String LAKEMDELSVAL = "lkm-lva";
    private static final String LAKEMDELSVAL_KOMMENTAR = "lkm-lva-kom";
    private static final String EXTEMPORERINGSBEREDNING = "lkm-lva-ext";
    
    private static final String LAKEMEDELSVARA = "lkm-lkm-lva";
    private static final String LAKEMEDELSVARA_NPL_PACKID = "lkm-lkm-lva-npl";
    
    private static final String LAKEMEDELSPRODUKT = "lkm-lkm-lpr";
    private static final String LAKEMEDELSPRODUKT_NPLID = "lkm-lkm-lpr-npl";
    private static final String LAKEMEDELSPRODUKT_ATC = "lkm-lkm-lpr-atc";
    private static final String LAKEMEDELSPRODUKT_BEREDNINGSFORM = "lkm-lkm-lpr-ber";
    private static final String LAKEMEDELSPRODUKT_PRODUKT_STYRKA = "lkm-lkm-lpr-prs";
    private static final String LAKEMEDELSPRODUKT_PRODUKT_STYRKA_ENHET = "lkm-lkm-lpr-pre";
    
    private static final String UTBYTESGRUPP = "lkm-lva-ubg";
    private static final String UTBYTESGRUPP_STYRKEGRUPPNAMN = "lkm-lva-ubg-sty";
    private static final String UTBYTESGRUPP_SUBSTANSGRUPPNAMN = "lkm-lva-ubg-sub";
    private static final String UTBYTESGRUPP_LAKMEDELSFORMNAMN = "lkm-lva-ubg-lfn";
    
    private static final String LAKEMEDELDOSERING = "lkm-dos";
    private static final String DOSERINGSSTEG_BEHANDLINGSTID = "lkm-dst-bet";
    private static final String DOSERINGSSTEG_MAXTID = "lkm-dst-max";
    private static final String DOSERINGSSTEG_DOSERINGSANVISNING = "lkm-dst-dan";
    private static final String DOSERINGSSTEG_DOSERINGSENHET = "lkm-dst-den";
    private static final String DOSERINGSSTEG_KORTNOTATION = "lkm-dst-kno";
    
    private static final String FORSKRIVNING = "lkm-for";
    private static final String FORSKRIVNING_UTLAMMNIGS_INTERVAL = "lkm-for-uiv";
    private static final String FORSKRIVNING_TOTALMAGNG = "lkm-for-tot";
    private static final String FORSKRIVNING_FORPACKNINGSENHET = "lkm-for-fpe";
    private static final String FORSKRIVNING_DISTRIBUTIONSMETOD = "lkm-for-dbs";
    private static final String FORSKRIVNING_FORSKRIVNINGSTIDPUNKT = "lkm-for-tid";
    
    static {
        MEANING_LKM_ORD.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_LKM_ORD.setCode(INFO_LKM_ORD);
    }

    private static final JaxbUtil jaxb 
      = new JaxbUtil(GetMedicationHistoryType.class, GetMedicationHistoryResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();

    
    private static final DateFormat dateformatTS = new SimpleDateFormat("yyyyMMddHHmmss");
    
    
    public MedicationHistoryMapper() {
        schemaValidationActivated = new Boolean(SpringPropertiesUtil.getProperty("SCHEMAVALIDATION-MEDICATIONHISTORY"));
        log.debug("schema validation is activated? " + schemaValidationActivated);
        
        initialiseValidator("/core_components/clinicalprocess_activityprescription_actoutcome_enum_2.0.xsd",
                            "/core_components/clinicalprocess_activityprescription_actoutcome_2.0.xsd",
                            "/interactions/GetMedicationHistoryInteraction/GetMedicationHistoryResponder_2.0.xsd");
    }


    protected GetMedicationHistoryType unmarshal(final XMLStreamReader reader) {
        try {
            return  (GetMedicationHistoryType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }

    
    protected String marshal(final GetMedicationHistoryResponseType response) {
        final JAXBElement<GetMedicationHistoryResponseType> el = objectFactory.createGetMedicationHistoryResponse(response);
        String xml = jaxb.marshal(el);
        validateXmlAgainstSchema(xml, schemaValidator, log);
        return xml;
    }
    

    @Override
    public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
        try {
            final GetMedicationHistoryType request = unmarshal(payloadAsXMLStreamReader(message));
            EHRUtil.storeCareUnitHsaIdsAsInvocationProperties(request, message, log);
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_LKM_ORD, message.getUniqueId(), message.getInvocationProperty("route-logical-address"))));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err, Ehr13606AdapterError.MAPREQUEST);
        }
    }

    
    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType ehrResponse 
    		   = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
    		
    		GetMedicationHistoryResponseType rivtaResponse = mapResponse(ehrResponse, message);
    		
            message.setPayload(marshal(rivtaResponse));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping response", err, Ehr13606AdapterError.MAPRESPONSE);
    	}
    }

    
    /**
     * Maps from EHR_EXTRACT (lko/lkf) to GetMedicationHistoryResponseType.
     *
     * @param ehrExtractList the EHR_EXTRACT XML Java bean.
     * @return GetMedicationHistoryResponseType response type
     */
    protected GetMedicationHistoryResponseType mapResponse(final RIV13606REQUESTEHREXTRACTResponseType ehrResponse, MuleMessage message) {
        final List<EHREXTRACT> ehrExtractList = ehrResponse.getEhrExtract();
        log.debug("list of EHREXTRACT - " + ehrExtractList.size());
        GetMedicationHistoryResponseType responseType = mapEhrExtract(ehrExtractList, message);
        responseType.setResult(EHRUtil.resultType(message.getUniqueId(), ehrResponse.getResponseDetail(), ResultType.class));
        return responseType;
    }
    
    
    protected GetMedicationHistoryResponseType mapEhrExtract(List<EHREXTRACT> ehrExtractList, MuleMessage message) {
        final GetMedicationHistoryResponseType responseType = new GetMedicationHistoryResponseType();
        if(!ehrExtractList.isEmpty()) {
        	final EHREXTRACT ehrExtract = ehrExtractList.get(0);
        	final Map<String, COMPOSITION> lkfs = new HashMap<String, COMPOSITION>();
        	final Map<String, COMPOSITION> lkos = new HashMap<String, COMPOSITION>();
        	
        	//Sort all compositions in maps so that they easly can be obtained by rc_id
        	sortCompositions(ehrExtract.getAllCompositions(), lkos, lkfs);
        	
        	for(COMPOSITION lko : lkos.values()) {
        		final MedicationMedicalRecordType record = new MedicationMedicalRecordType();
        		
        		final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
                final PatientSummaryHeaderType patientSummaryHeader = 
                		(PatientSummaryHeaderType)EHRUtil.patientSummaryHeader(lko, sharedHeaderExtract, "not used", PatientSummaryHeaderType.class, false, false, false);
                
                //JIRA: SERVICE-340
                final HealthcareProfessionalType prescriber = patientSummaryHeader.getAccountableHealthcareProfessional();
                patientSummaryHeader.setAccountableHealthcareProfessional(new HealthcareProfessionalType());
                patientSummaryHeader.getAccountableHealthcareProfessional().setAuthorTime(prescriber.getAuthorTime());
                patientSummaryHeader.getAccountableHealthcareProfessional().
                		setHealthcareProfessionalCareGiverHSAId(prescriber.getHealthcareProfessionalCareGiverHSAId());
                patientSummaryHeader.getAccountableHealthcareProfessional().
                		setHealthcareProfessionalCareUnitHSAId(prescriber.getHealthcareProfessionalCareUnitHSAId());
                
                //Apply specific rules to header for this TK
                patientSummaryHeader.setLegalAuthenticator(null); 
                //careContent found in lkm-ord -> links, to keep itterations down set this value when mapping body
                
                //Map body, Content 1..1
                final MedicationMedicalRecordBodyType body = new MedicationMedicalRecordBodyType();
                
                
                //Map utv
                HealthcareProfessional utvProfessional = null;
                for(FUNCTIONALROLE fr : lko.getOtherParticipations()) {
                	if(fr.getFunction() != null && StringUtils.equals(DELTAGARE_UTVARDERAS_AV, fr.getFunction().getCode())) {
                		utvProfessional = 
                				EHRUtil.healthcareProfessionalType(fr, sharedHeaderExtract.organisations(), 
                						sharedHeaderExtract.healthcareProfessionals(), null);
                	}
                }
                
                if(!lko.getContent().isEmpty() && lko.getContent().get(0) instanceof ENTRY) {
                	final ENTRY content = (ENTRY) lko.getContent().get(0);
                	if(content.getMeaning() != null 
                			&& StringUtils.equals(content.getMeaning().getCode(), LAKEMEDELS_ORDINATION)) {
                		//Set careContent header value and find lkf-id
                		IIType lkfId = null;
                		for(LINK link : content.getLinks()) {
                			if(link.getTargetType() != null && link.getTargetType().getCode() != null) {
                				switch (link.getTargetType().getCode()) {
                				case INFORMATIONSMANGD_LAKEMEDEL_FORSKRIVNING:
                					if(!link.getTargetId().isEmpty()) {
                						lkfId = EHRUtil.iiType(link.getTargetId().get(0), IIType.class);
                					}
                					break;
                				case INFORMATIONSMANGD_VARDKONTAKT:
                					if(!link.getTargetId().isEmpty()) {
                						final II careContactId = link.getTargetId().get(0);
                						patientSummaryHeader.setCareContactId(careContactId.getExtension());
                					}
                					break;
                				}
                			}
                		}
                		//Continue build body
                		//Forskrivning // Ordination
                		final MedicationPrescriptionType prescription = new MedicationPrescriptionType();
                		
                		if(lko.getRcId() != null) {
                			final IIType lkoIIType = new IIType();
                			lkoIIType.setExtension(lko.getRcId().getExtension());
                			lkoIIType.setRoot(lko.getRcId().getRoot());
                			prescription.setPrescriptionId(lkoIIType);
                		}
                		
                		//Forskrivning
                		if(lkfId != null) {
                			final DispensationAuthorizationType dispensationAuth = new DispensationAuthorizationType();
                			prescription.setDispensationAuthorization(dispensationAuth);
                			dispensationAuth.setDispensationAuthorizationId(lkfId);
                			
                			if(lkfs.containsKey(lkfId.getExtension())) {
                				final COMPOSITION lkf = lkfs.get(lkfId.getExtension());
                                               				
                				//Map lkf 
                				for(CONTENT lkfContent : lkf.getContent()) {
                					if(lkfContent.getMeaning() != null && StringUtils.equals(lkfContent.getMeaning().getCode(), FORSKRIVNING)) {
                						if(lkfContent instanceof ENTRY) {
                							final ENTRY lkfEntry = (ENTRY) lkfContent;
                							
                							//Map lkf healthcare pro
                							final FUNCTIONALROLE lkfFunc = lkfEntry.getInfoProvider();
                							HealthcareProfessional lkfPro = EHRUtil.healthcareProfessionalType(lkfFunc, 
                									sharedHeaderExtract.organisations(), 
                            						sharedHeaderExtract.healthcareProfessionals(), null);
                							
                							dispensationAuth.setDispensationAuthorizer(new HealthcareProfessionalType());
                							dispensationAuth.getDispensationAuthorizer().setHealthcareProfessionalHSAId(lkfPro.getHealthcareProfessionalHSAId());
                							dispensationAuth.getDispensationAuthorizer().setHealthcareProfessionalName(lkfPro.getHealthcareProfessionalName());
                							
                							final CVType lkfCv = new CVType();
                                			lkfCv.setCode(lkfPro.getHealthcareProfessionalRoleCode().getCode());
                                			lkfCv.setCodeSystem(lkfPro.getHealthcareProfessionalRoleCode().getCodeSystem());
                                			lkfCv.setCodeSystemName(lkfPro.getHealthcareProfessionalRoleCode().getCodeSystemName());
                                			lkfCv.setCodeSystemVersion(lkfPro.getHealthcareProfessionalRoleCode().getCodeSystemVersion());
                                			lkfCv.setDisplayName(lkfPro.getHealthcareProfessionalRoleCode().getDisplayName());
                                			lkfCv.setOriginalText(lkfPro.getHealthcareProfessionalRoleCode().getOriginalText());
                                			dispensationAuth.getDispensationAuthorizer().setHealthcareProfessionalRoleCode(lkfCv);
                                			
                                			final OrgUnitType lkfOrg = new OrgUnitType();
                                			lkfOrg.setOrgUnitAddress(lkfPro.getHealthcareProfessionalOrgUnit().getOrgUnitAddress());
                                			lkfOrg.setOrgUnitEmail(lkfPro.getHealthcareProfessionalOrgUnit().getOrgUnitEmail());
                                			lkfOrg.setOrgUnitHSAId(lkfPro.getHealthcareProfessionalOrgUnit().getOrgUnitHSAId());
                                			lkfOrg.setOrgUnitLocation(lkfPro.getHealthcareProfessionalOrgUnit().getOrgUnitLocation());
                                			lkfOrg.setOrgUnitName(lkfPro.getHealthcareProfessionalOrgUnit().getOrgUnitName());
                                			lkfOrg.setOrgUnitTelecom(lkfPro.getHealthcareProfessionalOrgUnit().getOrgUnitTelecom());
                                			dispensationAuth.getDispensationAuthorizer().setHealthcareProfessionalOrgUnit(lkfOrg);
                							
                							//Map lkf body
                							for(ITEM lkfItem : lkfEntry.getItems()) {
                								if(lkfItem instanceof ELEMENT) {
                									final ELEMENT lkfElement = (ELEMENT) lkfItem;
                									if(lkfElement.getMeaning() != null && lkfElement.getMeaning().getCode() != null) {
                										switch (lkfElement.getMeaning().getCode()) {
                										case FORSKRIVNING_UTLAMMNIGS_INTERVAL:
                											if(lkfElement.getValue() != null && lkfElement.getValue() instanceof INT) {
                												final INT intValue = (INT) lkfElement.getValue();
                												final PQType pq = new PQType();
                												pq.setValue(intValue.getValue().doubleValue());
                												dispensationAuth.setMinimumDispensationInterval(pq);
                											} else if(lkfElement.getValue() != null && lkfElement.getValue() instanceof PQ) {
                												final PQ pqValue = (PQ) lkfElement.getValue();
                												final PQType pq = new PQType();
                												pq.setUnit(pqValue.getUnit());
                												pq.setValue(pqValue.getValue());
                												dispensationAuth.setMinimumDispensationInterval(pq);
                											}
                											break;
                										case FORSKRIVNING_TOTALMAGNG: 
                											if(lkfElement.getValue() instanceof PQ) {
                												final PQ amPq = (PQ) lkfElement.getValue();
                												dispensationAuth.setTotalAmount(amPq.getValue());
                											}
                											break;
                										case FORSKRIVNING_FORPACKNINGSENHET:
                											dispensationAuth.setPackageUnit(EHRUtil.getSTValue(lkfElement.getValue()));
                											break;
                										case FORSKRIVNING_DISTRIBUTIONSMETOD:
                											dispensationAuth.setDistributionMethod(EHRUtil.getSTValue(lkfElement.getValue()));
                											break;
                										case FORSKRIVNING_FORSKRIVNINGSTIDPUNKT:
                											if(dispensationAuth.getDispensationAuthorizer() == null) {
                												dispensationAuth.setDispensationAuthorizer(new HealthcareProfessionalType());
                											}
                											dispensationAuth.getDispensationAuthorizer().setAuthorTime(EHRUtil.getTSValue(lkfElement.getValue()));
                											break;
                										}
                									}
                								}
                							}
                						}
                					}
                				}
                			}
                		}
                		
                		
                		//Ordination
                		/** TODO: Mandatory fields but none existing in NPO
                		 * TypeOfPrescription
                		 * PrescriptionStatus
                		 */
                		String authorTime = null;
                		for(ITEM item : content.getItems()) {
                			if(item instanceof ELEMENT) {
                				final ELEMENT elm = (ELEMENT) item;
                				switch (EHRUtil.getCDCode(item.getMeaning())) {
                				case LAKEMEDELS_ORDINATION_NOT:
                					prescription.setPrescriptionNote(EHRUtil.getSTValue(elm.getValue()));
                					break;
                				case LAKEMEDELS_ORDINATION_UTVARDERINGSTIDPUNKT:
                					prescription.setEvaluationTime(EHRUtil.getTSValue(elm.getValue()));
                					break;
                				case LAKEMEDELS_ORDINATION_ANDAMAL:
                					prescription.setTreatmentPurpose(EHRUtil.getSTValue(elm.getValue()));
                					break;
                				case LAKEMEDELS_ORDINATION_ORDINATIONS_KEDJA:
                					if(elm.getValue() instanceof II) {
                						prescription.setPrescriptionChainId(EHRUtil.iiType((II) elm.getValue(), IIType.class));
                					}
                					break;
                				case LAKEMEDELS_ORDINATION_TIDPUNKT:
                					authorTime = EHRUtil.getTSValue(elm.getValue());
                					break;
                				}
                			} else if (item instanceof CLUSTER) {
                				//Map Dosering (lkm-dos) och Lakemedelsval (lkm-lva)
                				if(prescription.getDrug() == null) {
                					prescription.setDrug(new DrugChoiceType());
                				}
                				final CLUSTER cluster = (CLUSTER) item;
                				if(cluster.getMeaning() != null 
                						&& StringUtils.equals(cluster.getMeaning().getCode(), LAKEMEDELDOSERING)) {
                					//NPO Specc, En Lakemedelsordination innehaller en och endast en Dosering
                					final DosageType dosage = new DosageType();
                					prescription.getDrug().getDosage().add(dosage);
                					for(ITEM dosageItem : cluster.getParts()) {
                						if(dosageItem instanceof CLUSTER) {
                							final CLUSTER dosageStep = (CLUSTER) dosageItem;
                							for(ITEM dosageStepItem : dosageStep.getParts()) {
                								final ELEMENT dosageElm = (ELEMENT) dosageStepItem;
                								if(dosageElm.getMeaning() != null && dosageElm.getMeaning().getCode() != null) {
                									switch (dosageElm.getMeaning().getCode()) {
                									
                									case DOSERINGSSTEG_BEHANDLINGSTID: // lkm-dst-bet
                										if(dosageElm.getValue() != null) {
                											if(dosageElm.getValue() instanceof IVLTS) {
                                                                
                												final IVLTS dosageIvlts = (IVLTS) dosageElm.getValue();
                												PQIntervalType treatmentInterval = getTreatmentInterval(dosageIvlts);
                												if (treatmentInterval == null) {
                												    dosage.setLengthOfTreatment(null);
                												} else {
                                                                    if (dosage.getLengthOfTreatment() == null) {
                                                                        dosage.setLengthOfTreatment(new LengthOfTreatmentType());
                                                                    }
                    												dosage.getLengthOfTreatment().setTreatmentInterval(treatmentInterval);
                												}
                												
                                                                //Set prescriptionStartOfThreatment
                                                                if(dosageIvlts.getLow()  != null && StringUtils.isNotBlank(dosageIvlts.getLow().getValue())) {
                                                                    prescription.setStartOfTreatment(dosageIvlts.getLow().getValue());
                                                                } 
                                                                if(dosageIvlts.getHigh() != null && StringUtils.isNotBlank(dosageIvlts.getLow().getValue())) {
                                                                    prescription.setEndOfTreatment(dosageIvlts.getHigh().getValue());
                                                                }
                											} else {
                											    log.error("lkm-dst-bet: expecting IVL_TS, received " + dosageElm.getValue().getClass().getName());
                											}
                										}
                										break;
                										
                									case DOSERINGSSTEG_MAXTID:
                										if(dosageElm.getValue() != null && dosageElm.getValue() instanceof BL) {
                											boolean m = ((BL) dosageElm.getValue()).isValue();
                											if(dosage.getLengthOfTreatment() == null) {
                												dosage.setLengthOfTreatment(new LengthOfTreatmentType());
                											}
                											dosage.getLengthOfTreatment().setIsMaximumTreatmentTime(m);
                										}
                										break;
                									case DOSERINGSSTEG_DOSERINGSANVISNING:
                										dosage.setDosageInstruction(EHRUtil.getSTValue(dosageElm.getValue()));
                										break;
                									case DOSERINGSSTEG_DOSERINGSENHET:
                										dosage.setUnitDose(new CVType());
                										dosage.getUnitDose().setOriginalText(EHRUtil.getSTValue(dosageElm.getValue()));
                										break;
                									case DOSERINGSSTEG_KORTNOTATION:
                										dosage.setShortNotation(EHRUtil.getSTValue(dosageElm.getValue()));
                										break;
                									}
                								}
                							}
                						}
                					}	
                				} else if(cluster.getMeaning() != null 
                						&& StringUtils.equals(LAKEMDELSVAL, cluster.getMeaning().getCode())) {
                					for(ITEM clusterItem : cluster.getParts()) {
                						if(clusterItem instanceof ELEMENT) {
                							final ELEMENT clusterElm = (ELEMENT) clusterItem;
                							if(clusterElm.getMeaning() != null && clusterElm.getMeaning().getCode() != null) {
                								switch (clusterElm.getMeaning().getCode()) {
                								case LAKEMDELSVAL_KOMMENTAR:
                									prescription.getDrug().setComment(EHRUtil.getSTValue(clusterElm.getValue()));
                									break;
                								case EXTEMPORERINGSBEREDNING:
                									final UnstructuredDrugInformationType ext = new UnstructuredDrugInformationType();
                									ext.setUnstructuredInformation(EHRUtil.getSTValue(clusterElm.getValue()));
                									prescription.getDrug().setUnstructuredDrugInformation(ext);
                									break;
                								}
                							}
                						} else if (clusterItem instanceof CLUSTER) {
                							final CLUSTER innerCluster = (CLUSTER) clusterItem;
                							//Lakemedelsvara (lkm-lkm-lva)	
                							if(innerCluster.getMeaning() != null 
                									&& StringUtils.equals(LAKEMEDELSVARA, innerCluster.getMeaning().getCode())) {
                								final DrugArticleType drugArticle = new DrugArticleType();
                								for(ITEM innerClusterItem : innerCluster.getParts()) {
                									if(innerClusterItem instanceof ELEMENT) {
                										final ELEMENT lakemedelsElm = (ELEMENT) innerClusterItem;
                										if(lakemedelsElm.getMeaning() != null && lakemedelsElm.getMeaning().getCode() != null) {
                											switch(lakemedelsElm.getMeaning().getCode()) {
                												case LAKEMEDELSVARA_NPL_PACKID:
                												if(lakemedelsElm.getValue() != null) {
                													final II drugCvII = (II) lakemedelsElm.getValue();
                													final CVType drugCv = new CVType();
                													drugCv.setOriginalText(drugCvII.getExtension());
                													drugArticle.setNplPackId(drugCv);
                												}
                												break;
                											}
                										}
                										//Lakemdelsprodukt
                									} else if (innerClusterItem instanceof CLUSTER) {
                										final CLUSTER prodCluster = (CLUSTER) innerClusterItem;
                										if(prodCluster.getMeaning() != null 
                												&& StringUtils.equals(prodCluster.getMeaning().getCode(), LAKEMEDELSPRODUKT)) {
                											prescription.getDrug().setDrug(new DrugType());
                											for(ITEM prodItem : prodCluster.getParts()) {
                												if(prodItem instanceof ELEMENT) {
                													final ELEMENT prodElm = (ELEMENT) prodItem;
                													if(prodElm.getMeaning() != null && prodElm.getMeaning().getCode() != null) {
                														switch (prodElm.getMeaning().getCode()) {
                															case LAKEMEDELSPRODUKT_NPLID:
                															if(prodElm.getValue() instanceof II) {
                																final CVType drugProdCV = new CVType();
                																final II drugProdII = (II) prodElm.getValue();
                																drugProdCV.setOriginalText(drugProdII.getExtension());
                																prescription.getDrug().getDrug().setNplId(drugProdCV);
                															}
                															break;
                															case LAKEMEDELSPRODUKT_ATC:
                															if(prodElm.getValue() instanceof CD) {
                																final CVType drugAtcCv = new CVType();
                																final CD drugAtcCd = (CD) prodElm.getValue();
                																drugAtcCv.setCode(drugAtcCd.getCode());
                																drugAtcCv.setCodeSystem(drugAtcCd.getCodeSystem());
                																drugAtcCv.setDisplayName(EHRUtil.getSTValue(drugAtcCd.getDisplayName()));
                																prescription.getDrug().getDrug().setAtcCode(drugAtcCv);
                															}
                															break;
                															case LAKEMEDELSPRODUKT_BEREDNINGSFORM:
                																prescription.getDrug().getDrug().setPharmaceuticalForm(EHRUtil.getSTValue(prodElm.getValue()));
                															break;
                															case LAKEMEDELSPRODUKT_PRODUKT_STYRKA:
                																if(prodElm.getValue() instanceof PQ) {
                																	final PQ styrka = (PQ) prodElm.getValue();
                																	prescription.getDrug().getDrug().setStrength(styrka.getValue());
                																	prescription.getDrug().getDrug().setStrengthUnit(styrka.getUnit());
                																}
                															break;
                															case LAKEMEDELSPRODUKT_PRODUKT_STYRKA_ENHET:
                																prescription.getDrug().getDrug().setStrengthUnit(EHRUtil.getSTValue(prodElm.getValue()));
                															break;
                														}
                													}
                												}
                											}
                										} else if (prodCluster.getMeaning() != null && StringUtils.equals(prodCluster.getMeaning().getCode(), UTBYTESGRUPP)) {
                											prescription.getDrug().setGenerics(new GenericsType());
                											for(ITEM utbItem : prodCluster.getParts()) {
                												if(utbItem instanceof ELEMENT) {
                													final ELEMENT utbElm = (ELEMENT) utbItem;
                													if(utbElm.getMeaning() != null && utbElm.getMeaning().getCode() != null) {
                														switch(utbElm.getMeaning().getCode()) {
                														case UTBYTESGRUPP_STYRKEGRUPPNAMN:
                															prescription.getDrug().getGenerics().setStrength(new PQType());
                															prescription.getDrug().getGenerics().getStrength().setUnit(EHRUtil.getSTValue(utbElm.getValue()));
                															break;
                														case UTBYTESGRUPP_LAKMEDELSFORMNAMN:
                															prescription.getDrug().getGenerics().setForm(EHRUtil.getSTValue(utbElm.getValue()));
                															break;
                														case UTBYTESGRUPP_SUBSTANSGRUPPNAMN:
                															prescription.getDrug().getGenerics().setSubstance(EHRUtil.getSTValue(utbElm.getValue()));
                															break;
                														}
                													}
                												}
                											}
                										}
                									}
                								}
                								prescription.getDrug().setDrugArticle(drugArticle);
                							}
                						}
                					}
                				}
                			}
                		}
                		
                		
                		prescription.setPrescriber(prescriber);
                		//Set lkm-ord-tid
                		prescription.getPrescriber().setAuthorTime(authorTime);
                		
                		//Map Utv
                		if(utvProfessional != null) {
                			prescription.setEvaluator(new HealthcareProfessionalType());
                			prescription.getEvaluator().setAuthorTime(prescription.getEvaluationTime());
                			prescription.getEvaluator().setHealthcareProfessionalHSAId(utvProfessional.getHealthcareProfessionalHSAId());
                			prescription.getEvaluator().setHealthcareProfessionalName(utvProfessional.getHealthcareProfessionalName());
                			
                			final CVType cv = new CVType();
                			cv.setCode(utvProfessional.getHealthcareProfessionalRoleCode().getCode());
                			cv.setCodeSystem(utvProfessional.getHealthcareProfessionalRoleCode().getCodeSystem());
                			cv.setCodeSystemName(utvProfessional.getHealthcareProfessionalRoleCode().getCodeSystemName());
                			cv.setCodeSystemVersion(utvProfessional.getHealthcareProfessionalRoleCode().getCodeSystemVersion());
                			cv.setDisplayName(utvProfessional.getHealthcareProfessionalRoleCode().getDisplayName());
                			cv.setOriginalText(utvProfessional.getHealthcareProfessionalRoleCode().getOriginalText());
                			prescription.getEvaluator().setHealthcareProfessionalRoleCode(cv);
                			
                			final OrgUnitType org = new OrgUnitType();
                			org.setOrgUnitAddress(utvProfessional.getHealthcareProfessionalOrgUnit().getOrgUnitAddress());
                			org.setOrgUnitEmail(utvProfessional.getHealthcareProfessionalOrgUnit().getOrgUnitEmail());
                			org.setOrgUnitHSAId(utvProfessional.getHealthcareProfessionalOrgUnit().getOrgUnitHSAId());
                			org.setOrgUnitLocation(utvProfessional.getHealthcareProfessionalOrgUnit().getOrgUnitLocation());
                			org.setOrgUnitName(utvProfessional.getHealthcareProfessionalOrgUnit().getOrgUnitName());
                			org.setOrgUnitTelecom(utvProfessional.getHealthcareProfessionalOrgUnit().getOrgUnitTelecom());
                			prescription.getEvaluator().setHealthcareProfessionalOrgUnit(org);
                			
                		}
                		
                		body.setMedicationPrescription(prescription);
                	}
                	
                }
                
                // "Endast aktuella lakemedel ska levereras, dvs lakemedel man vet ar utsatta levereras ej".
                body.getMedicationPrescription().setPrescriptionStatus(PrescriptionStatusEnum.ACTIVE);
                
                // "Alla ordinationer som tillhandahalls i NPO1 ar att betrakta som "insattningar"
                // JIRA: SERVICE-353
                body.getMedicationPrescription().setTypeOfPrescription(TypeOfPrescriptionEnum.I);
                
                applyAdapterSpecificRules(body.getMedicationPrescription().getDrug());
                
                record.setMedicationMedicalRecordBody(body);
                record.setMedicationMedicalRecordHeader(patientSummaryHeader);
                responseType.getMedicationMedicalRecord().add(record);
        	}
        	
        }
    	
        return responseType;
    }
    

    // see SERVICE-334
    protected PQIntervalType getTreatmentInterval(IVLTS dosageIvlts) {
        
        PQIntervalType treatmentInterval = null;
        
        // if width is supplied, use it
        if (dosageIvlts.getWidth() != null) {
            QTY width = dosageIvlts.getWidth();
            if (width instanceof PQTIME) {
                PQTIME pqtimeWidth = (PQTIME)width;
                Double widthValue = pqtimeWidth.getValue();
                if (widthValue != null) {
                    if (widthValue >= 0) {
                        if (StringUtils.isNotBlank(pqtimeWidth.getUnit())) {
                            treatmentInterval = new PQIntervalType();
                            treatmentInterval.setLow(widthValue);
                            treatmentInterval.setHigh(widthValue);
                            treatmentInterval.setUnit(pqtimeWidth.getUnit());
                        } else {
                            log.error("lkm-dst-bet value/width/unit missing");
                        }
                    } else {
                        log.error("lkm-dst-bet value/width/value is negative " + widthValue);
                    }
                } else {
                    log.error("lkm-dst-bet value/width/value null");
                }
            } else {
                log.error("lkm-dst-bet value/width - expecting PQTIME, received " + width.getClass().getName());
            }
        } else if (   dosageIvlts.getLow() != null 
                   && dosageIvlts.getHigh() != null 
                   && StringUtils.isNotBlank(dosageIvlts.getLow().getValue())
                   && StringUtils.isNotBlank(dosageIvlts.getHigh().getValue())) {

            // there is no width, but there are low and high
            
            String lowString  = dosageIvlts.getLow().getValue();
            String highString = dosageIvlts.getHigh().getValue();
            try {
                Date lowDate = dateformatTS.parse(lowString);
                try {
                    Date highDate = dateformatTS.parse(highString);
                    if (!lowDate.after(highDate)) {
                        // let joda time do the hard work
                        int days = Days.daysBetween(new DateTime(lowDate), new DateTime(highDate)).getDays();
                        treatmentInterval = new PQIntervalType();
                        treatmentInterval.setLow(new Double(days));
                        treatmentInterval.setHigh(treatmentInterval.getLow());
                        treatmentInterval.setUnit("d");
                    } else {
                        log.error("lkm-dst-bet low (" + lowString + ") is after high (" + highString + ")");
                    }
                } catch (ParseException p) {
                    log.error("lkm-dst-bet value/high invalid timestamp:" + lowString);
                }
            } catch (ParseException p) {
                log.error("lkm-dst-bet value/low invalid timestamp:" + lowString);
            }
        } else {
            // the message does not follow the contract
            // this is the case for data in qa
            log.error("lkm-dst-bet width is null and both low and high are not present - low:" + 
                       (dosageIvlts.getLow()  == null ? "null" : dosageIvlts.getLow().getValue()) + 
                       ", high:" +        
                       (dosageIvlts.getHigh() == null ? "null" : dosageIvlts.getHigh().getValue()));
        }
        return treatmentInterval;
    }


    protected void sortCompositions(final List<COMPOSITION> comps, final Map<String, COMPOSITION> lko, final Map<String, COMPOSITION> lkf) {
    	for(COMPOSITION c : comps) {
    		if(c.getMeaning() != null && c.getMeaning().getCode() != null 
    				&& c.getRcId() != null && c.getRcId().getExtension() != null) {
    			switch(c.getMeaning().getCode()) {
    				case INFORMATIONSMANGD_LAKEMEDEL_FORSKRIVNING:
    					lkf.put(c.getRcId().getExtension(), c);
    					break;
    				case INFORMATIONSMANGD_LAKEMEDEL_ORDINATION:
    					lko.put(c.getRcId().getExtension(), c);
    					break;
    			}
    		}
    	}
    }
    
    /**
     * According to TKB only on of drugArticle, drug, merchandise, generics, unstructuredDrugInformation is allowed
     * 13606 contains more data.
     * @param drug
     */
    protected void applyAdapterSpecificRules(final DrugChoiceType drug) {
    	if(drug.getDrugArticle() != null) {
    		drug.setDrug(null);
    		drug.setMerchandise(null);
    		drug.setGenerics(null);
    		drug.setUnstructuredDrugInformation(null);
    	} else if(drug.getDrug() != null) {
    		drug.setMerchandise(null);
    		drug.setGenerics(null);
    		drug.setUnstructuredDrugInformation(null);
    	} else if(drug.getMerchandise() != null) {
    		drug.setGenerics(null);
    		drug.setUnstructuredDrugInformation(null);
    	} else if(drug.getGenerics() != null) {
    		drug.setUnstructuredDrugInformation(null);
    	}
    }
}

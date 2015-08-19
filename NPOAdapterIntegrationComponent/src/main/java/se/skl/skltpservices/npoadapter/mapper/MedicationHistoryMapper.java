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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.activityprescription.actoutcome._2.CVType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DispensationAuthorizationType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DosageType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugChoiceType;
import riv.clinicalprocess.activityprescription.actoutcome._2.DrugType;
import riv.clinicalprocess.activityprescription.actoutcome._2.HealthcareProfessionalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.IIType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordBodyType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationPrescriptionType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PQIntervalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PatientSummaryHeaderType;
import riv.clinicalprocess.activityprescription.actoutcome._2.ResultType;
import riv.clinicalprocess.activityprescription.actoutcome._2.SetDosageType;
import riv.clinicalprocess.activityprescription.actoutcome._2.SingleDoseType;
import riv.clinicalprocess.activityprescription.actoutcome._2.UnstructuredDosageInformationType;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.PrescriptionStatusEnum;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.TypeOfPrescriptionEnum;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryResponseType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.GetMedicationHistoryType;
import riv.clinicalprocess.activityprescription.actoutcome.getmedicationhistoryresponder._2.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.ANY;
import se.rivta.en13606.ehrextract.v11.BL;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.INT;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.IVLTS;
import se.rivta.en13606.ehrextract.v11.PQ;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TS;
import se.skl.skltpservices.npoadapter.mapper.error.Ehr13606AdapterError;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;


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
    
    static {
        MEANING_LKM_ORD.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_LKM_ORD.setCode(INFO_LKM_ORD);
    }

    private static final JaxbUtil jaxb 
      = new JaxbUtil(GetMedicationHistoryType.class, GetMedicationHistoryResponseType.class);
    private static final ObjectFactory objectFactory = new ObjectFactory();

    
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
        GetMedicationHistoryResponseType responseType = new GetMedicationHistoryResponseType();
        if (!ehrExtractList.isEmpty()) {
            List<String> careUnitHsaIds = EHRUtil.retrieveCareUnitHsaIdsInvocationProperties(message, log);
            final EHREXTRACT ehrExtract = ehrExtractList.get(0);
            for (int i = 0; i < ehrExtract.getAllCompositions().size(); i++) {
                if (EHRUtil.retain(ehrExtract.getAllCompositions().get(i), careUnitHsaIds, log)) {
                    final MedicationMedicalRecordType medicationMedicalRecordType = new MedicationMedicalRecordType();
                    medicationMedicalRecordType.setMedicationMedicalRecordHeader(mapHeader(ehrExtract, i));
                    medicationMedicalRecordType.setMedicationMedicalRecordBody(mapBody(ehrExtract, i));
                    responseType.getMedicationMedicalRecord().add(medicationMedicalRecordType);
                }
            }
        }
        return responseType;
    }

    /**
     * Maps contact header information.
     *
     * @param ehrExtract the extract.
     * @param compositionIndex the actual composition in the list.
     * @return the target header information.
     */
    private PatientSummaryHeaderType mapHeader(final EHREXTRACT ehrExtract, final int compositionIndex) {
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);
        if (composition.getComposer() == null) {
            // Note that lkf has no composer - does this mean lkf compositions should be ignored?
            log.warn("composition " + compositionIndex + " has a null composer");
        }
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        
        PatientSummaryHeaderType patient = (PatientSummaryHeaderType)EHRUtil.patientSummaryHeader(composition, sharedHeaderExtract, "not used", PatientSummaryHeaderType.class, true, false);
        if (StringUtils.isBlank(patient.getAccountableHealthcareProfessional().getAuthorTime())) {
            log.error("Unable to populate mandatory field authorTime"); // yyyyMMddHHmmss
            patient.getAccountableHealthcareProfessional().setAuthorTime(null);
        }
        if (StringUtils.isBlank(patient.getLegalAuthenticator().getSignatureTime())) {
            log.error("Unable to populate mandatory field signatureTime"); // yyyyMMddHHmmss
            patient.getLegalAuthenticator().setSignatureTime(null);
        }
        return patient;
    }
    
    
    /**
     * Create a MedicationMedicalRecord using the information
     * in the current ehr13606 composition.
     *
     * @param ehrExtract the extract containing the current composition
     * @param compositionIndex the actual composition in the list.
     * @return a new MedicationMedicalRecord
     */
    private MedicationMedicalRecordBodyType mapBody(final EHREXTRACT ehrExtract, final int compositionIndex) {
    
        final COMPOSITION composition = ehrExtract.getAllCompositions().get(compositionIndex);
        
        // parse this composition into values stored in a Map
        Map<String,String> ehr13606values = retrieveValues(composition, compositionIndex);
        
        // use the ehr values to build a medication medical history record body
        MedicationMedicalRecordBodyType bodyType = buildBody(ehr13606values);
        
        return bodyType;
    }


    /* 
    * Create a MedicationMedicalRecord for outgoing message.
    * Use values from ehr 13606 incoming message.
    * Populate values in outgoing message if there is data in the incoming message.
    * 
    * Attempt to avoid empty elements in the outgoing message
    * (this is why we check for data before creating outgoing objects).
    */
    private MedicationMedicalRecordBodyType buildBody (Map<String, String> ehr13606values) {
        
        MedicationPrescriptionType mpt = new MedicationPrescriptionType();
        
        mpt.setPrescriptionId(new IIType());
        mpt.getPrescriptionId().setRoot(INFO_LKM_ORD);
        //mpt.getPrescriptionId().setExtension("TODO");
        
        mpt.setTypeOfPrescription(TypeOfPrescriptionEnum.I);
        
        // Note : tjänstekontraktsbeskrivning
        // 'En aktiv ordination är den sista i sin ordinationskedja. Alla andra ordinationer i samma ordinationskedja är inaktiva.'
        // No indication that a 13606 ordination contain a chain of prescriptions
        
        mpt.setPrescriptionStatus(PrescriptionStatusEnum.ACTIVE); // mandatory
        
        if (ehr13606values.containsKey("lkm-dst-bet-low")) {
            mpt.setStartOfTreatment(ehr13606values.get("lkm-dst-bet-low"));
        } else if (ehr13606values.containsKey("lkm-for-tid")) {
            mpt.setStartOfTreatment(ehr13606values.get("lkm-for-tid"));
        } else {
            mpt.setStartOfTreatment(ehr13606values.get("lkm-ord-tid"));
        }
        
        mpt.setEndOfTreatment(ehr13606values.get("lkm-dst-bet-high"));
        
        mpt.setPrescriptionNote(ehr13606values.get("lkm-ord-not"));
        if (ehr13606values.containsKey("lkm-lva-kom")) {
            mpt.setPrescriptionNote(
              mpt.getPrescriptionNote() + (StringUtils.isNotBlank(mpt.getPrescriptionNote()) ? " " : "") +  ehr13606values.get("lkm-lva-kom")
            );
        }
        
        // mpt.getPrincipalPrescriptionReason().add(new PrescriptionReasonType());
        // mpt.getPrincipalPrescriptionReason().get(0).setReason(new CVType());
        
        mpt.setEvaluationTime  (ehr13606values.get("lkm-ord-utv"));
        mpt.setTreatmentPurpose(ehr13606values.get("lkm-ord-and"));
        
        
        // --- Drug
        
        mpt.setDrug(new DrugChoiceType());

        // --- Drug/Drug - läkemedelsprodukt
        
        mpt.getDrug().setDrug(new DrugType());
        
        mpt.getDrug().getDrug().setNplId(new CVType());
        mpt.getDrug().getDrug().getNplId().setCode(getNonBlank(ehr13606values,"lkm-lkm-lpr-npl"));
        mpt.getDrug().getDrug().getNplId().setCodeSystem("1.2.752.129.2.1.5.1");
        mpt.getDrug().getDrug().getNplId().setDisplayName(getNonBlank(null)); // Produktnamn. Handelsnamn i SIL. Text som anger namnet på den aktuella läkemedelsprodukten.
        
        mpt.getDrug().getDosage().add(new DosageType());
        mpt.getDrug().getDosage().get(0).setDosageInstruction(ehr13606values.get("lkm-dst-dan"));
        mpt.getDrug().getDosage().get(0).setShortNotation(ehr13606values.get("lkm-dst-kno"));
        
        
        if (ehr13606values.containsKey("lkm-dst-den")) {
            mpt.getDrug().getDosage().get(0).setUnitDose(new CVType());
            mpt.getDrug().getDosage().get(0).getUnitDose().setOriginalText(ehr13606values.get("lkm-dst-den"));
        }
        
        if (ehr13606values.containsKey("lkm-lva-ext") || ehr13606values.containsKey("lkm-dst-max") ) {
            mpt.getDrug().getDosage().get(0).setSetDosage(new SetDosageType());
            mpt.getDrug().getDosage().get(0).getSetDosage().setUnstructuredDosageInformation(new UnstructuredDosageInformationType());
            
            if (ehr13606values.containsKey("lkm-dst-ext")) {
                mpt.getDrug().getDosage().get(0).getSetDosage().getUnstructuredDosageInformation().setText(
                    "lkm-lva-ext- extemporeberedning beskriving:" + ehr13606values.get("lkm-lva-ext"));
            }
            if (ehr13606values.containsKey("lkm-dst-max")) {
                mpt.getDrug().getDosage().get(0).getSetDosage().getUnstructuredDosageInformation().setText(
                (StringUtils.isNotBlank(mpt.getDrug().getDosage().get(0).getSetDosage().getUnstructuredDosageInformation().getText()) ? " " : "") +
                "lkm-dst-max - maxtid " + ehr13606values.get("lkm-dst-max"));
            }
        }
        
        if (ehr13606values.containsKey("lkm-lkm-lva-prm")) {
            mpt.getDrug().getDosage().get(0).getSetDosage().setSingleDose(new SingleDoseType());
            mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().setDose(new PQIntervalType());
            //mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().getDose().setUnit("TODO dose unit");
            mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().getDose().setHigh(new Double(ehr13606values.get("lkm-lkm-lva-prm")));
            mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().getDose().setLow(new Double(ehr13606values.get("lkm-lkm-lva-prm")));
        }
        
        mpt.getDrug().getDrug().setAtcCode(new CVType());
        
        mpt.getDrug().getDrug().getAtcCode().setCode(ehr13606values.get("lkm-lkm-lpr-atc"));
        mpt.getDrug().getDrug().getAtcCode().setCodeSystem("1.2.752.129.2.2.3.1.1");
        mpt.getDrug().getDrug().getAtcCode().setOriginalText(ehr13606values.get("lkm-lva-typ"));
        mpt.getDrug().getDrug().getAtcCode().setDisplayName(getNonBlank(ehr13606values,"lkm-lkm-lva-pre","lkm-lva-typ","lkm-lkm-lpr-atc"));
        
        mpt.getDrug().getDrug().setRouteOfAdministration(new CVType());
        mpt.getDrug().getDrug().getRouteOfAdministration().setCode(ehr13606values.get("lkm-lkm-lpr-ber"));
        mpt.getDrug().getDrug().getRouteOfAdministration().setDisplayName(ehr13606values.get("lkm-lkm-lpr-ber"));
        
        
        // --- DispensationAuthorization
        
        if (ehr13606values.containsKey("lkm-for-fpe") ||
            ehr13606values.containsKey("lkm-for-tot") ||
            ehr13606values.containsKey("lkm-for-uiv") ||
            ehr13606values.containsKey("lkm-for-mpt") ||
            ehr13606values.containsKey("lkm-for-dbs")) {
            
            mpt.setDispensationAuthorization(new DispensationAuthorizationType());
            
            mpt.getDispensationAuthorization().setPackageUnit(ehr13606values.get("lkm-for-fpe"));
            if (ehr13606values.containsKey("lkm-for-tot")) {
                mpt.getDispensationAuthorization().setTotalAmount(new Double(ehr13606values.get("lkm-for-tot")));
            }
            
            mpt.getDispensationAuthorization().setDispensationAuthorizationId(new IIType());
            mpt.getDispensationAuthorization().getDispensationAuthorizationId().setRoot("1.2.752.129.2.1.2.1");
            
            mpt.getDispensationAuthorization().setDispensationAuthorizer(new HealthcareProfessionalType());
            mpt.getDispensationAuthorization().getDispensationAuthorizer().setAuthorTime(ehr13606values.get("lkm-ord-tid")); // Beslutstidpunkt/förskrivningsstidpunkt. Tidpunkt då beslut fattas om förskrivning.

            mpt.getDispensationAuthorization().setDispensationAuthorizerComment(
                    (ehr13606values.containsKey("lkm-for-uiv") ? "utlämningsinterval:" + ehr13606values.get("lkm-for-uiv") + " " : "") +
                    (ehr13606values.containsKey("lkm-for-mpt") ? "mängd per tillfälle:" + ehr13606values.get("lkm-for-mpt") + " " : "") +
                    (ehr13606values.containsKey("lkm-for-dbs") ? "distributionssätt:"  + ehr13606values.get("lkm-for-dbs") : ""));
            
            if (StringUtils.isNotBlank(mpt.getDispensationAuthorization().getDispensationAuthorizerComment())) {
                mpt.getDispensationAuthorization().setPrescriptionSignatura(mpt.getDispensationAuthorization().getDispensationAuthorizerComment());
            } else {
                mpt.getDispensationAuthorization().setPrescriptionSignatura("");
            }
            
        }
        
        // ---

        final MedicationMedicalRecordBodyType bodyType = new MedicationMedicalRecordBodyType();
        bodyType.setMedicationPrescription(mpt);
        return bodyType;
    }


    // Return first value from a list of keys. Default to NA.
    protected String getNonBlank(Map<String, String> ehr13606values, String... keys) {
        String result = null;
        if (ehr13606values != null && ehr13606values.size() > 0 && keys != null && keys.length > 0) {
            int i = 0;
            while (StringUtils.isBlank(result) && i < keys.length) {
                result = ehr13606values.get(keys[i]);
                i++;
            }
        }
        if (StringUtils.isBlank(result)) {
            result = "NA";
        }
        return result;
    }


    // Retrieve ehr values from message and store in a map
    private Map<String,String> retrieveValues(COMPOSITION composition, int compositionIndex) {
        
        Map<String,String> values = new LinkedHashMap<String,String>(); // Linked in order to preserve order of insertion
        for (final CONTENT content : composition.getContent()) {
            for (final ITEM item : ((ENTRY) content).getItems()) {
                retrieveItemValue(item, values);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("incoming composition : " + compositionIndex);
            Iterator<String> it = values.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                log.debug("|" + key + "|" + values.get(key) + "|");
            }
        }
        
        return values;
     }
    

    /*
     * Retrieve item values from incoming message and store 
     * as Strings in a Map.
     * Basing processing on Strings means converting numerics
     * and objects to String. This makes processing slightly less
     * efficient, but simplifies coding in parent methods.
     */
    private void retrieveItemValue(ITEM item, Map<String,String> values) {
        
        if (item.getMeaning() != null) {
            String code = item.getMeaning().getCode();
            if (StringUtils.isNotBlank(code)) {
                
                if (item instanceof ELEMENT) {
                    String text = "";    
                    ANY value = ((ELEMENT)item).getValue();
                    if (value != null) {
                               if (value instanceof ST) {
                            text = ((ST)value).getValue();
                        } else if (value instanceof PQ) {
                            text = ((PQ)value).getValue().toString();
                        } else if (value instanceof INT) {
                            text = ((INT)value).getValue().toString();
                        } else if (value instanceof TS) {
                            text = ((TS)value).getValue();
                        } else if (value instanceof BL) {
                            text = ((BL)value).isValue().toString();
                        } else if (value instanceof CD) {
                            text = ((CD)value).getCode();
                        } else if (value instanceof II) {
                            text = ((II)value).getRoot();
                        } else if (value instanceof IVLTS) {
                            // lkm-dst-bet is more complex
                            // split into two String values
                            // lkm-dst-bet-low, lkm-dst-bet-high
                            String low = ((IVLTS)value).getLow().getValue();
                            if (StringUtils.isNotBlank(low)) {
                                values.put(code + "-low", low);
                            }
                            if (((IVLTS)value).getHigh().getNullFlavor() == null ) {
                                String high = ((IVLTS)value).getHigh().getValue();
                                if (StringUtils.isNotBlank(high)) {
                                    values.put(code + "-high", high);
                                }
                            }
                        } else {
                            log.error("Code " + code + " has unknown value type " + value.getClass().getCanonicalName());
                        }
                               
                        if (StringUtils.isNotBlank(text)) {
                           values.put(code, text);
                        }
                    }
                } else if (item instanceof CLUSTER) {
                    CLUSTER cluster = (CLUSTER)item;
                    for (ITEM childItem : cluster.getParts()) {
                        retrieveItemValue(childItem, values);
                    }
                } else {
                    log.error("ITEM is neither an ELEMENT nor a CLUSTER:" + item.getMeaning().getCode());
                }
            }
        }
    }
}

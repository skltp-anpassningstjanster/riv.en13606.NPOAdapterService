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

import lombok.extern.slf4j.Slf4j;

import org.mule.api.MuleMessage;
import org.mule.util.StringUtils;
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
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordBodyType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationMedicalRecordType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MedicationPrescriptionType;
import riv.clinicalprocess.activityprescription.actoutcome._2.MerchandiseType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PQIntervalType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PQType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PatientSummaryHeaderType;
import riv.clinicalprocess.activityprescription.actoutcome._2.PrescriptionReasonType;
import riv.clinicalprocess.activityprescription.actoutcome._2.ResultType;
import riv.clinicalprocess.activityprescription.actoutcome._2.SetDosageType;
import riv.clinicalprocess.activityprescription.actoutcome._2.SingleDoseType;
import riv.clinicalprocess.activityprescription.actoutcome._2.UnstructuredDosageInformationType;
import riv.clinicalprocess.activityprescription.actoutcome.enums._2.ResultCodeEnum;
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
 * Maps from EHR_EXTRACT to RIV GetMedicationHistoryResponseType v2.0. 
 * <p>
 * Riv contract spec : 
 * http://rivta.se/downloads/ServiceContracts_clinicalpocess_activityprescription_actoutcome_2.0_RC1.zip
 *
 * @author Martin
 */
@Slf4j
public class MedicationHistoryMapper extends AbstractMapper implements Mapper {

    public static final CD MEANING_LKF = new CD();
    public static final CD MEANING_LKM = new CD();
    public static final CD MEANING_LKO = new CD();
    
    protected static final String TIME_ELEMENT = "lkm-ord-tid";
    
    static {
        MEANING_LKF.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_LKF.setCode(INFO_LKF);

        MEANING_LKM.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_LKM.setCode(INFO_LKM);

        MEANING_LKO.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_LKO.setCode(INFO_LKO);
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
            message.setPayload(riv13606REQUESTEHREXTRACTRequestType(EHRUtil.requestType(request, MEANING_LKM)));
            return message;
        } catch (Exception err) {
            throw new MapperException("Error when mapping request", err);
        }
    }

    
    @Override
    public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
    	try {
    		final RIV13606REQUESTEHREXTRACTResponseType ehrResponse 
    		   = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
    		
    		GetMedicationHistoryResponseType rivtaResponse = map(ehrResponse, message.getUniqueId());
    		
            message.setPayload(marshal(rivtaResponse));
            return message;
    	} catch (Exception err) {
    		throw new MapperException("Error when mapping response", err);
    	}
    }

    
    /**
     * Maps from EHR_EXTRACT (lko/lkf) to GetMedicationHistoryResponseType.
     *
     * @param ehrExtractList the EHR_EXTRACT XML Java bean.
     * @return GetMedicationHistoryResponseType response type
     */
    protected GetMedicationHistoryResponseType map(final RIV13606REQUESTEHREXTRACTResponseType ehrResponse, String uniqueId) {

        final List<EHREXTRACT> ehrExtractList = ehrResponse.getEhrExtract();
        log.debug("list of EHREXTRACT - " + ehrExtractList.size());
        GetMedicationHistoryResponseType responseType = mapEhrExtract(ehrExtractList);
        
        responseType.setResult(EHRUtil.resultType(uniqueId, ehrResponse.getResponseDetail(), ResultType.class));
        
        // TODO - investigate why this code is necessary - if EHRUtil.resultType cannot handle this message,
        // then maybe we shouldn't be trying to do any extra processing here.
        if (responseType.getResult() == null) {
            responseType.setResult(new ResultType());
        }
        if (responseType.getResult().getResultCode() == null) {
            responseType.getResult().setResultCode(ResultCodeEnum.OK); // TODO ok?
        }
        if (StringUtils.isEmpty(responseType.getResult().getLogId())) {
            responseType.getResult().setLogId("TODO log id");
        }
        return responseType;
    }
    
    
    protected GetMedicationHistoryResponseType mapEhrExtract(List<EHREXTRACT> ehrExtractList) {
        GetMedicationHistoryResponseType responseType = new GetMedicationHistoryResponseType();
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
            log.warn("composition " + compositionIndex + " has a null composer"); // TODO lkf has no composer
        }
        final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
        
        PatientSummaryHeaderType patient = (PatientSummaryHeaderType)EHRUtil.patientSummaryHeader(composition, sharedHeaderExtract, TIME_ELEMENT, PatientSummaryHeaderType.class);
        if (StringUtils.isBlank(patient.getAccountableHealthcareProfessional().getAuthorTime())) {
            patient.getAccountableHealthcareProfessional().setAuthorTime("TODO - author time");   
        }
        
        if (StringUtils.isBlank(patient.getLegalAuthenticator().getSignatureTime())) {
            patient.getLegalAuthenticator().setSignatureTime("TODO - signature time");
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
        return buildBody(ehr13606values);
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
        mpt.getPrescriptionId().setRoot(INFO_LKM);
        mpt.getPrescriptionId().setExtension("TODO");
        
        mpt.setTypeOfPrescription(TypeOfPrescriptionEnum.INSÄTTNING);
        
        mpt.setPrescriptionStatus(new CVType());
        mpt.getPrescriptionStatus().setCode("TODO");
        
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
            mpt.setPrescriptionNote(mpt.getPrescriptionNote() + 
                    (StringUtils.isNotBlank(mpt.getPrescriptionNote()) ? " " : "") +
                    ehr13606values.get("lkm-lva-kom"));
        }
        
        mpt.getPrincipalPrescriptionReason().add(new PrescriptionReasonType());
        mpt.getPrincipalPrescriptionReason().get(0).setReason(new CVType());
        mpt.getPrincipalPrescriptionReason().get(0).getReason().setCode("TODO");
        
        mpt.setEvaluationTime  (ehr13606values.get("lkm-ord-utv"));
        mpt.setTreatmentPurpose(ehr13606values.get("lkm-ord-and"));
        
        
        // --- Drug
        
        mpt.setDrug(new DrugChoiceType());

        mpt.getDrug().setDrug(new DrugType());
        
        mpt.getDrug().getDrug().setNplId(new CVType());
        
        mpt.getDrug().getDrug().getNplId().setCode("TODO");
        
        mpt.getDrug().getDosage().add(new DosageType());
        mpt.getDrug().getDosage().get(0).setDosageInstruction(ehr13606values.get("lkm-dst-dan"));
        mpt.getDrug().getDosage().get(0).setShortNotation(ehr13606values.get("lkm-dst-kno"));
        mpt.getDrug().getDosage().get(0).setUnitDose(new CVType());
        mpt.getDrug().getDosage().get(0).getUnitDose().setOriginalText(ehr13606values.get("lkm-dst-den"));
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
        if (StringUtils.isEmpty(mpt.getDrug().getDosage().get(0).getSetDosage().getUnstructuredDosageInformation().getText())) {
            mpt.getDrug().getDosage().get(0).getSetDosage().getUnstructuredDosageInformation().setText("TODO - unstructured dosage information text");
        }
        
        mpt.getDrug().getDosage().get(0).getSetDosage().setSingleDose(new SingleDoseType());
        mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().setDose(new PQIntervalType());
        mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().getDose().setUnit("TODO dose unit");
        if (ehr13606values.containsKey("lkm-lkm-lva-prm")) {
            mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().getDose().setHigh(new Double(ehr13606values.get("lkm-lkm-lva-prm")));
            mpt.getDrug().getDosage().get(0).getSetDosage().getSingleDose().getDose().setLow(new Double(ehr13606values.get("lkm-lkm-lva-prm")));
        }
        
        mpt.getDrug().getDrug().setAtcCode(new CVType());
        
        mpt.getDrug().getDrug().getAtcCode().setCode(ehr13606values.get("lkm-lkm-lpr-atc"));
        mpt.getDrug().getDrug().getAtcCode().setOriginalText(ehr13606values.get("lkm-lva-typ"));
        mpt.getDrug().getDrug().getAtcCode().setDisplayName(ehr13606values.get("lkm-lkm-lva-pre"));
        
        
        mpt.getDrug().getDrug().setRouteOfAdministration(new CVType());
        mpt.getDrug().getDrug().getRouteOfAdministration().setCode(ehr13606values.get("lkm-lkm-lpr-ber"));
        mpt.getDrug().getDrug().getRouteOfAdministration().setDisplayName(ehr13606values.get("lkm-lkm-lpr-ber"));
        
        mpt.getDrug().setDrugArticle(new DrugArticleType());
        mpt.getDrug().getDrugArticle().setNplPackId(new CVType());
        
        mpt.getDrug().getDrugArticle().getNplPackId().setCode(ehr13606values.get("lkm-lkm-lpr-npl"));
        

        // --- Generics

        if (ehr13606values.containsKey("lkm-lva-ubg-lfn") ||
            ehr13606values.containsKey("lkm-lva-ubg-sub") ||
            ehr13606values.containsKey("lkm-lva-ubg-sty")) {
            
            mpt.getDrug().setGenerics(new GenericsType());
            mpt.getDrug().getGenerics().setForm(ehr13606values.get("lkm-lva-ubg-lfn"));
            mpt.getDrug().getGenerics().setSubstance(ehr13606values.get("lkm-lva-ubg-sub"));
            
            mpt.getDrug().getGenerics().setStrength(new PQType());
            mpt.getDrug().getGenerics().getStrength().setUnit(ehr13606values.get("lkm-lva-ubg-sty"));
            mpt.getDrug().getGenerics().getStrength().setValue(0); // TODO - maybe parse lkm-lva-ubg-sty and retrieve a numeric value?
        }
        
        
        // --- Merchandise

        if (ehr13606values.containsKey("lkm-lkm-lva-fbe") ||
            ehr13606values.containsKey("lkm-lkm-lva-fst") ||
            ehr13606values.containsKey("lkm-lkm-lpr-spi") ||
            ehr13606values.containsKey("lkm-lkm-lva-vnr") ||
            ehr13606values.containsKey("lkm-lkm-lpr-prt") ||
            ehr13606values.containsKey("lkm-lkm-lpr-prn") ||
            ehr13606values.containsKey("lkm-lkm-lpr-pna") ||
            ehr13606values.containsKey("lkm-lkm-lva-fna") ||
            ehr13606values.containsKey("lkm-lkm-lva-prs")) {
                
            mpt.getDrug().setMerchandise(new MerchandiseType());
            
            mpt.getDrug().getMerchandise().setArticleNumber(new CVType());
            
            if (ehr13606values.containsKey("lkm-lkm-lva-fbe")) {
                mpt.getDrug().getMerchandise().getArticleNumber().setOriginalText(ehr13606values.get("lkm-lkm-lva-fbe") );
            }
            if (ehr13606values.containsKey("lkm-lkm-lva-fst")) {
                mpt.getDrug().getMerchandise().getArticleNumber().setOriginalText(mpt.getDrug().getMerchandise().getArticleNumber().getOriginalText() + " " + ehr13606values.get("lkm-lkm-lva-fst") );
            }
            
            mpt.getDrug().getMerchandise().getArticleNumber().setCode(
                     (ehr13606values.containsKey("lkm-lkm-lpr-spi") ? "lkm-lkm-lpr-spi:spec id:"          + ehr13606values.get("lkm-lkm-lpr-spi") + " " : "") + 
                     (ehr13606values.containsKey("lkm-lkm-lva-vnr") ? "lkm-lkm-lva-vnr:varunummer:"       + ehr13606values.get("lkm-lkm-lva-vnr") + " " : "") +
                     (ehr13606values.containsKey("lkm-lkm-lpr-prt") ? "lkm-lkm-lpr-prt:produkttype:"      + ehr13606values.get("lkm-lkm-lpr-prt") + " " : "") +
                     (ehr13606values.containsKey("lkm-lkm-lpr-prn") ? "lkm-lkm-lpr-prn:produktnamn:"      + ehr13606values.get("lkm-lkm-lpr-prn") + " " : "") + 
                     (ehr13606values.containsKey("lkm-lkm-lpr-pna") ? "lkm-lkm-lpr-pna:"                  + ehr13606values.get("lkm-lkm-lpr-pna") + " " : "") +
                     (ehr13606values.containsKey("lkm-lkm-lva-fna") ? "lkm-lkm-lva-fna:förpackningsnamn:" + ehr13606values.get("lkm-lkm-lva-fna") + " " : "") +
                     (ehr13606values.containsKey("lkm-lkm-lpr-prs") ? "lkm-lkm-lpr-prs:produktstyrka:"    + ehr13606values.get("lkm-lkm-lpr-prs") : "") );
        }
        
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
            mpt.getDispensationAuthorization().getDispensationAuthorizationId().setRoot("TODO authorization id");
            
            mpt.getDispensationAuthorization().setDispensationAuthorizer(new HealthcareProfessionalType());
            mpt.getDispensationAuthorization().getDispensationAuthorizer().setAuthorTime("TODO author time");
            mpt.getDispensationAuthorization().setPrescriptionSignatura("TODO signatura");
            
            mpt.getDispensationAuthorization().setDispensationAuthorizerComment(
                    (ehr13606values.containsKey("lkm-for-uiv") ? "lkm-for-uiv:utlämningsinterval:" + ehr13606values.get("lkm-for-uiv") + " " : "") +
                    (ehr13606values.containsKey("lkm-for-mpt") ? "lkm-for-mpt:mäng per tillfälle:" + ehr13606values.get("lkm-for-mpt") + " " : "") +
                    (ehr13606values.containsKey("lkm-for-dbs") ? "lkm-for-dbs:distributionssätt:"  + ehr13606values.get("lkm-for-dbs") : ""));
        }
        
        // ---

        final MedicationMedicalRecordBodyType bodyType = new MedicationMedicalRecordBodyType();
        bodyType.setMedicationPrescription(mpt);
        return bodyType;
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
                            // lkm-dst-bet-low, lkm-dst-bet-hiugh
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

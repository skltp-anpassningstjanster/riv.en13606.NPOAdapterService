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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import riv.clinicalprocess.healthcond.actoutcome._3.*;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.*;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.ObjectFactory;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.CLUSTER;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDENTITY;
import se.rivta.en13606.ehrextract.v11.IDENTIFIEDHEALTHCAREPROFESSIONAL;
import se.rivta.en13606.ehrextract.v11.II;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.LINK;
import se.rivta.en13606.ehrextract.v11.ORGANISATION;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTRequestType;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.error.NotImplementedException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;
import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps from EHR_EXTRACT (und-kkm-kli v1.1) to RIV GetLaboratoryOrderOutcomeResponseType v3.0. <p>
 *
 * Riv contract spec (TKB): "http://rivta.se/downloads/ServiceContracts_clinicalprocess_healthcond_actoutcome_3.0_RC1.zip"
 * 
 * @author torbjorncla
 *
 */
@Slf4j
public class LaboratoryOrderOutcomeMapper extends AbstractMapper implements Mapper {
	
	private static final JaxbUtil jaxb = new JaxbUtil(GetLaboratoryOrderOutcomeType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	private static final int MAX_ROWS = 500;
	
	public static final CD MEANING = new CD();
    static {
        MEANING.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING.setCode("und-kkm-kli");
    }

	@Override
	public MuleMessage mapRequest(final MuleMessage message) throws MapperException {
		try {
			final GetLaboratoryOrderOutcomeType req = unmarshal(payloadAsXMLStreamReader(message));
			message.setPayload(riv13606REQUESTEHREXTRACTRequestType(map13606Request(req)));
			return message;
		} catch (Exception err) {
			log.error("Error when transforming LaboratoryOrderOutcome request", err);
			throw new MapperException("Error when transforming LaboratoryOrderOutcome request");
		}
	}

	@Override
	public MuleMessage mapResponse(final MuleMessage message) throws MapperException {
		try {
			final RIV13606REQUESTEHREXTRACTResponseType ehrResponse = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			GetLaboratoryOrderOutcomeResponseType resp = mapResponseType(ehrResponse, message.getUniqueId());
			message.setPayload(marshal(resp));
			return message;
		} catch (Exception err) {
			log.error("Error when transforming LaboratoryOrderOutcome response", err);
			throw new MapperException("Error when transforming LaboratoryOrderOutcome response");
		}
	}
	
	protected GetLaboratoryOrderOutcomeResponseType mapResponseType(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, final String uniqueId) {
		final GetLaboratoryOrderOutcomeResponseType resp = new GetLaboratoryOrderOutcomeResponseType();
		resp.setResult(EHRUtil.resultType(uniqueId, ehrResp.getResponseDetail(), ResultType.class));
		if(ehrResp.getEhrExtract().isEmpty()) {
			return resp;
		}
		
		final EHREXTRACT ehrExtract = ehrResp.getEhrExtract().get(0);
		SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExtract);
		
		for(COMPOSITION comp : ehrExtract.getAllCompositions()) {
			if(comp.getMeaning() != null) {
				if(StringUtils.equals(comp.getMeaning().getCode(), "und")) {
					final COMPOSITION und = comp;
					final COMPOSITION vbe = EHRUtil.findCompositionByLink(ehrExtract.getAllCompositions(), EHRUtil.firstItem(und.getContent()).getLinks(), "vbe");
					final LaboratoryOrderOutcomeType type = new LaboratoryOrderOutcomeType();
					type.setLaboratoryOrderOutcomeHeader(EHRUtil.patientSummaryHeader(comp, sharedHeaderExtract, null, PatientSummaryHeaderType.class));
					type.setLaboratoryOrderOutcomeBody(mapBodyType(und, vbe, type.getLaboratoryOrderOutcomeHeader().getAccountableHealthcareProfessional()));
					resp.getLaboratoryOrderOutcome().add(type);
				}
			}
		}
		
		/**
		 * TODO: ResultType is mandatory in GetLaboratoryOrderOutcome.
		 */
		if(resp.getResult() == null) {
			final ResultType resultType = new ResultType();
			resultType.setResultCode(ResultCodeEnum.OK);
			resultType.setLogId(uniqueId);
			resp.setResult(resultType);
		}
		return resp;
	}
	
	
	/**
	 * TODO: Refactor, too complex.
	 * @param comp
	 * @return
	 */
	protected LaboratoryOrderOutcomeBodyType mapBodyType(final COMPOSITION und, final COMPOSITION vbe, final HealthcareProfessionalType healtcareProfessional) {
		final LaboratoryOrderOutcomeBodyType type = new LaboratoryOrderOutcomeBodyType();
		if(und.getCommittal() != null && und.getCommittal().getTimeCommitted() != null) {
			type.setRegistrationTime(und.getCommittal().getTimeCommitted().getValue());
		}
		for(CONTENT content : und.getContent()) {
			if(content instanceof ENTRY) {
				final ENTRY entry = (ENTRY) content;
				for(ITEM item : entry.getItems()) {
					if(item instanceof CLUSTER) {
						CLUSTER cluster = (CLUSTER) item;
						for(ITEM part : cluster.getParts()) {
							if(part instanceof ELEMENT) {
								final ELEMENT elm = (ELEMENT) part;
								if(part.getMeaning() != null) {
									switch(part.getMeaning().getCode()) {
										case "und-und-ure-typ":
											type.setResultType(EHRUtil.getElementTextValue(elm));
											break;
										case "und-kkm-ure-lab":
											type.setDiscipline(EHRUtil.getElementTextValue(elm));
											break;
										case "und-und-ure-utl":
											type.setResultReport(EHRUtil.getElementTextValue(elm));
											break;
										case "und-kkm-ure-kom":
											type.setResultComment(EHRUtil.getElementTextValue(elm));
											break;
										case "und-und-ure-stp":
											type.setAccountableHeathcareProfessional(mapAccountableHealthcareProfessional(EHRUtil.getElementTimeValue(elm), healtcareProfessional));
											break;
											
									}
								}
							}
							if(part instanceof CLUSTER) {
								final CLUSTER analys = (CLUSTER) part;
								if(analys.getMeaning() != null) {
									switch(analys.getMeaning().getCode()) {
									case "und-kkm-uat":
										type.getAnalysis().add(mapAnalysis(analys));
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		type.setOrder(mapOrder(vbe));
		return type;
	}
	
	/**
	 * @param healthcareProfessional
	 * @return
	 */
	protected HealthcareProfessionalType mapAccountableHealthcareProfessional(final String authorTime, final HealthcareProfessionalType healtcareProfessional) {
		final HealthcareProfessionalType type = new HealthcareProfessionalType();
		type.setAuthorTime(authorTime);
		type.setHealthcareProfessionalCareGiverHSAId(healtcareProfessional.getHealthcareProfessionalCareGiverHSAId());
		type.setHealthcareProfessionalCareUnitHSAId(healtcareProfessional.getHealthcareProfessionalCareUnitHSAId());
		type.setHealthcareProfessionalHSAId(healtcareProfessional.getHealthcareProfessionalHSAId());
		type.setHealthcareProfessionalName(healtcareProfessional.getHealthcareProfessionalName());
		type.setHealthcareProfessionalOrgUnit(healtcareProfessional.getHealthcareProfessionalOrgUnit());
		type.setHealthcareProfessionalRoleCode(healtcareProfessional.getHealthcareProfessionalRoleCode());
		return type;
	}

	protected OrderType mapOrder(final COMPOSITION vbe) {
		if(vbe == null) {
			return null;
		}
		final OrderType type = new OrderType();
		if(vbe.getRcId() != null) {
			type.setOrderId(vbe.getRcId().getExtension());
		}
		for(CONTENT content : vbe.getContent()) {
			if(content.getMeaning() != null && StringUtils.equalsIgnoreCase(content.getMeaning().getCode(), "vbe-vbe")) {
				if(content instanceof ENTRY) {
					final ENTRY vbeEntry = (ENTRY) content;
					for(ITEM vbeItem : vbeEntry.getItems()) {
						if(vbeItem.getMeaning() != null && StringUtils.equalsIgnoreCase(vbeItem.getMeaning().getCode(), "vbe-vbe-fst")) {
							if(vbeItem instanceof ELEMENT) {
								type.setOrderReason(EHRUtil.getElementTextValue((ELEMENT) vbeItem));
							}
						}
					}
				}
			}
		}
		return type;
	}
	
	/**
	 * according to spec und-kkm-uat-kod or und-kkm-uat-txt is sent.
	 * @param analys
	 * @return
	 */
	protected AnalysisType mapAnalysis(final CLUSTER analys) {
		final AnalysisType type = new AnalysisType();
		type.setAnalysisId(EHRUtil.iiType(analys.getRcId(), IIType.class));
		for(LINK link : analys.getLinks()) {
			if(!link.getTargetId().isEmpty()) {
				final RelationToAnalysisType rel = new RelationToAnalysisType();
				final II ii = link.getTargetId().get(0);
				rel.setAnalysisId(EHRUtil.iiType(ii, IIType.class));
				type.getRelationToAnalysis().add(rel);
			}
		}
		for(ITEM uatItem : analys.getParts()) {
			if(uatItem instanceof ELEMENT) {
				final ELEMENT uatElm = (ELEMENT) uatItem;
				if(uatElm.getMeaning() != null) {
					switch(uatElm.getMeaning().getCode()) {
					case "und-kkm-uat-kod":
						if(uatElm.getObsTime() != null) {
							type.setAnalysisTime(EHRUtil.datePeriod(uatElm.getObsTime(), TimePeriodType.class));
							if(uatElm.getValue() instanceof CD) {
								type.setAnalysisCode(EHRUtil.cvType((CD)uatElm.getValue(), CVType.class));
							}
						}
						break;
					case "und-kkm-uat-txt":
						if(uatElm.getObsTime() != null) {
							type.setAnalysisTime(EHRUtil.datePeriod(uatElm.getObsTime(), TimePeriodType.class));
							type.setAnalysisText(EHRUtil.getElementTextValue(uatElm));
						}
						break;
					case "und-kkm-uat-sta":
						type.setAnalysisStatus(EHRUtil.getElementTextValue(uatElm));
						break;
					case "und-kkm-uat-kom":
						type.setAnalysisComment(EHRUtil.getElementTextValue(uatElm));
						break;
					case "und-kkm-uat-mat":
						type.setSpecimen(EHRUtil.getElementTextValue(uatElm));
						break;
					case "und-kkm-uat-met":
						type.setMethod(EHRUtil.getElementTextValue(uatElm));
						break;
					}
				}
			}
			if(uatItem instanceof CLUSTER) {
				type.setAnalysisOutcome(mapAnalysisOutcome((CLUSTER) uatItem));
			}
		}
		return type;
	}
	
	protected AnalysisOutcomeType mapAnalysisOutcome(final CLUSTER utfCluster) {
		final AnalysisOutcomeType type = new AnalysisOutcomeType();
		if(utfCluster.getMeaning() != null && utfCluster.getMeaning().getCode() != null) {
			if(utfCluster.getMeaning().getCode().equalsIgnoreCase("und-kkm-utf")) {
				for(ITEM utfItem : utfCluster.getParts()) {
					if(utfItem instanceof ELEMENT) {
						final ELEMENT utfElm = (ELEMENT) utfItem;
						if(utfElm.getMeaning() != null) {
							switch(utfElm.getMeaning().getCode()) {
								case "und-kkm-utf-var":
									type.setOutcomeValue(EHRUtil.getElementTextValue(utfElm));
									if(utfElm.getObsTime() != null && utfElm.getObsTime().getLow() != null) {
										type.setObservationTime(utfElm.getObsTime().getLow().getValue());
									}
									break;
								case "und-kkm-utf-vae":
									type.setOutcomeUnit(EHRUtil.getElementTextValue(utfElm));
									break;
								case "und-kkm-utf-pat":
									final Boolean flag = EHRUtil.boolValue(utfElm);
									if(flag != null) {
										type.setPathologicalFlag(flag);
									}
									break;
								case "und-kkm-utf-bes":
									type.setOutcomeDescription(EHRUtil.getElementTextValue(utfElm));
									break;
								case "und-kkm-utf-ref":
									type.setReferenceInterval(EHRUtil.getElementTextValue(utfElm));
									break;
								case "und-kkm-utf-pop":
									type.setReferencePopulation(EHRUtil.getElementTextValue(utfElm));
									break;
							}
						}
					}
				}
			}
		}
		return type;
	}
	
	protected String marshal(final GetLaboratoryOrderOutcomeResponseType response) {
        final JAXBElement<GetLaboratoryOrderOutcomeResponseType> el = objFactory.createGetLaboratoryOrderOutcomeResponse(response);
        return jaxb.marshal(el);
    }
	
	protected GetLaboratoryOrderOutcomeType unmarshal(final XMLStreamReader reader) {
        try {
            return (GetLaboratoryOrderOutcomeType) jaxb.unmarshal(reader);
        } finally {
            close(reader);
        }
    }
	
	protected RIV13606REQUESTEHREXTRACTRequestType map13606Request(final GetLaboratoryOrderOutcomeType req) {
		final RIV13606REQUESTEHREXTRACTRequestType type = new RIV13606REQUESTEHREXTRACTRequestType();
		type.getMeanings().add(MEANING);
		type.setMaxRecords(EHRUtil.intType(MAX_ROWS));
		type.setSubjectOfCareId(EHRUtil.iiType(req.getPatientId()));
		type.setTimePeriod(EHRUtil.IVLTSType(req.getTimePeriod()));
		final List<String> ids = req.getCareUnitHSAId();
        if (ids.size() == 1) {
            type.getParameters().add(EHRUtil.createParameter("hsa_id", EHRUtil.firstItem(ids)));
        } else if (ids.size() > 1) {
            throw new IllegalArgumentException("Request includes several care units (HSAId search criteria), but only 1 is allowed by the source system: " + ids);
        }

        type.getParameters().add(EHRUtil.createParameter("version", "1.1"));
		return type;
	}

}

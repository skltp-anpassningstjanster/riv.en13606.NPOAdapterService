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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLStreamReader;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.mule.api.MuleMessage;
import org.soitoolkit.commons.mule.jaxb.JaxbUtil;

import riv.clinicalprocess.healthcond.actoutcome._3.CVType;
import riv.clinicalprocess.healthcond.actoutcome._3.ECGReferralType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImageRecordingType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImagingBodyType;
import riv.clinicalprocess.healthcond.actoutcome._3.ImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientDataType;
import riv.clinicalprocess.healthcond.actoutcome._3.PatientSummaryHeaderType;
import riv.clinicalprocess.healthcond.actoutcome._3.ResultType;
import riv.clinicalprocess.healthcond.actoutcome.enums._3.TypeOfResultCodeEnum;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeResponseType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeType;
import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.ObjectFactory;
import se.skl.skltpservices.npoadapter.mapper.error.MapperException;
import se.skl.skltpservices.npoadapter.mapper.util.EHRUtil;
import se.skl.skltpservices.npoadapter.mapper.util.SharedHeaderExtract;
import se.rivta.en13606.ehrextract.v11.*;

/**
 * Transformer for RIV-TA GetImagingOutcome -> EN13606 Informationsmangd UND-BDI
 * @author torbjorncla
 *
 */
@Slf4j
public class ImagingOutcomeMapper extends AbstractMapper implements Mapper {

	private static final JaxbUtil jaxb = new JaxbUtil(GetImagingOutcomeType.class);
	private static final ObjectFactory objFactory = new ObjectFactory();
	
	private static final CD MEANING_UND_BDI = new CD();
	static {
		MEANING_UND_BDI.setCodeSystem("1.2.752.129.2.2.2.1");
        MEANING_UND_BDI.setCode(INFO_UND_BDI);
	}
	
	private static final String VARDBEGARAN = "vbe";
	private static final String UNDERSOKNINGS_RESULTAT = "und";
	private static final String UND_BILD_DIAGNOSTIK = "und-bdi-ure";
	private static final String UND_RESULTAT = "und-und-ure";
	private static final String UND_SVARSTYP = "und-und-ure-typ";
	private static final String UND_SVARSTIDPUNKT = "und-und-ure-stp";
	private static final String UND_UTLATANDE = "und-und-ure-utl";
	private static final String UND_LABENHET = "und-bdi-ure-lab";
	private static final String UND_UTFORD_ATGARD = "und-und-uat";
	private static final String UND_ATGARDS_TEXT = "und-und-uat-txt";
	private static final String UND_ATGARDS_KOD = "und-und-uat-kod";
	
	
	@Override
	public MuleMessage mapRequest(MuleMessage message) throws MapperException {
		try {
			final GetImagingOutcomeType req = unmarshall(payloadAsXMLStreamReader(message));
			return message;
		}
		catch (Exception err) {
			throw new MapperException("Exception when mapping request", err);
		}
	}

	@Override
	public MuleMessage mapResponse(MuleMessage message) throws MapperException {
		try {
			final RIV13606REQUESTEHREXTRACTResponseType ehrResp = riv13606REQUESTEHREXTRACTResponseType(payloadAsXMLStreamReader(message));
			final GetImagingOutcomeResponseType resp = mapResponseType(ehrResp, message.getUniqueId());
			message.setPayload(marshal(resp));
			return message;
		}
		catch (Exception err) {
			throw new MapperException("Exception when mapping response", err);
		}
	}
	
	public GetImagingOutcomeResponseType mapResponseType(final RIV13606REQUESTEHREXTRACTResponseType ehrResp, final String uniqueId) {
		final GetImagingOutcomeResponseType resp = new GetImagingOutcomeResponseType();
		resp.setResult(EHRUtil.resultType(uniqueId, ehrResp.getResponseDetail(), ResultType.class));
		if(ehrResp.getEhrExtract().isEmpty()) {
			return resp;
		}
		final EHREXTRACT ehrExctract = ehrResp.getEhrExtract().get(0);
		final SharedHeaderExtract sharedHeaderExtract = extractInformation(ehrExctract);
		
		for(COMPOSITION comp : ehrExctract.getAllCompositions()) {
			if(StringUtils.equals(EHRUtil.getCDCode(comp.getMeaning()), UNDERSOKNINGS_RESULTAT)) {
				final COMPOSITION und = comp;
				final COMPOSITION vbe = EHRUtil.findCompositionByLink(ehrExctract.getAllCompositions(), EHRUtil.firstItem(und.getContent()).getLinks(), VARDBEGARAN);
				final ImagingOutcomeType type = new ImagingOutcomeType();
				type.setImagingOutcomeHeader(EHRUtil.patientSummaryHeader(comp, sharedHeaderExtract, UND_SVARSTIDPUNKT, PatientSummaryHeaderType.class));
				type.setImagingOutcomeBody(mapBody(und, vbe));
				resp.getImagingOutcome().add(type);
			}
		}
		
		return resp;
	}
	
	protected ImagingBodyType mapBody(final COMPOSITION und, final COMPOSITION vbe) {
		final ImagingBodyType body = new ImagingBodyType();
		final List<ITEM> items = getParts(und.getContent());
		if(items != null) {
			for(ITEM item : items) {
				if(item instanceof ELEMENT) {
					final ELEMENT elm = (ELEMENT) item;
					switch(EHRUtil.getCDCode(elm.getMeaning())) {
					case UND_SVARSTYP:
						body.setTypeOfResult(translate(EHRUtil.getElementTextValue(elm)));
						break;
					case UND_SVARSTIDPUNKT:
						body.setResultTime(EHRUtil.getElementTimeValue(elm));
						break;
					case UND_UTLATANDE:
						body.setResultReport(EHRUtil.getElementTextValue(elm));
						break;
					case UND_LABENHET:
						final ImageRecordingType image = new ImageRecordingType();
						image.setExaminationUnit(EHRUtil.getElementTextValue(elm));
						body.getImageRecording().add(image);
						break;
					}
				} else if(item instanceof CLUSTER) {
					if(!body.getImageRecording().isEmpty()) {
						final CLUSTER cluster = (CLUSTER) item;
						if(StringUtils.equals(EHRUtil.getCDCode(cluster.getMeaning()), UND_UTFORD_ATGARD)) {
							for(ITEM partItem : cluster.getParts()) {
								if(partItem instanceof ELEMENT) {
									final ELEMENT partElm = (ELEMENT) partItem;
									final String meaning = EHRUtil.getCDCode(partElm.getMeaning());
									final ImageRecordingType image = body.getImageRecording().get(0); //TODO: Is this correct is this labbunit value?
									switch(meaning) {
									case UND_ATGARDS_TEXT:
										break;
									case UND_ATGARDS_KOD:
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		return body;
	}
	
	protected TypeOfResultCodeEnum translate(final String svarstyp) {
		try {
			return TypeOfResultCodeEnum.valueOf(svarstyp);
		} catch (Exception err) {
			log.error(String.format("Could not map TypeOfResultCodeEnum of value: %s", svarstyp));
			return null;
		}
	}
	
	/**
	 * There could only be one list of parts in this Cluster
	 * @param content
	 * @return
	 */
	protected List<ITEM> getParts(List<CONTENT> content) {
		for(CONTENT c : content) {
			if(c instanceof ENTRY) {
				final ENTRY entry = (ENTRY) c;
				if(StringUtils.equals(EHRUtil.getCDCode(entry.getMeaning()), UND_BILD_DIAGNOSTIK)) {
					for(ITEM item : entry.getItems()) {
						if(item instanceof CLUSTER) {
							final CLUSTER cluster = (CLUSTER) item;
							if(StringUtils.equals(EHRUtil.getCDCode(cluster.getMeaning()), UND_RESULTAT)) {
								return cluster.getParts();
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	protected String marshal(final GetImagingOutcomeResponseType resp) {
		final JAXBElement<GetImagingOutcomeResponseType> el = objFactory.createGetImagingOutcomeResponse(resp);
		return jaxb.marshal(el);
	}
	
	protected GetImagingOutcomeType unmarshall(final XMLStreamReader reader) {
		try {
			return (GetImagingOutcomeType) jaxb.unmarshal(reader);
		} finally {
			close(reader);
		}
	}

}

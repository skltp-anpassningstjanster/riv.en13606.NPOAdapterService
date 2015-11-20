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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.api.MuleMessage;

import riv.clinicalprocess.healthcond.description._2.DiagnosisBodyType;
import riv.clinicalprocess.healthcond.description._2.DiagnosisType;
import riv.clinicalprocess.healthcond.description.enums._2.DiagnosisTypeEnum;
import riv.clinicalprocess.healthcond.description.getdiagnosisresponder._2.GetDiagnosisResponseType;
import se.rivta.en13606.ehrextract.v11.CD;
import se.rivta.en13606.ehrextract.v11.COMPOSITION;
import se.rivta.en13606.ehrextract.v11.CONTENT;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.ELEMENT;
import se.rivta.en13606.ehrextract.v11.ENTRY;
import se.rivta.en13606.ehrextract.v11.ITEM;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import se.rivta.en13606.ehrextract.v11.TS;
import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author torbjorncla
 */
public class DiagnosisMapperTest extends MapperTest {

    private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
    private static EHREXTRACT ehrExtract;
    private static DiagnosisMapper mapper;

    private final static CD cd = new CD();
    private final static ST st = new ST();

    private static final String TEST_DATA_1 = UUID.randomUUID().toString();
    private static final String TEST_DATA_2 = UUID.randomUUID().toString();

    @BeforeClass
    public static void init() throws JAXBException {
    	mapper = Mockito.spy(getDiagnosisMapper());
    	
        ehrExtract = Util.loadEhrTestData(Util.DIAGNOSIS_TEST_FILE);
        ehrResp.getEhrExtract().add(ehrExtract);

        cd.setCode(TEST_DATA_1);
        cd.setId(TEST_DATA_2);

        st.setValue(TEST_DATA_1);
    }
    
    private static DiagnosisMapper getDiagnosisMapper() {
    	DiagnosisMapper mapper = (DiagnosisMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_DIAGNOSIS_2);
    	return mapper;
    }    

    @Test
    public void testMapDiagnosisBodyType() throws Exception {
        boolean typeTouch = false;
        CD cd = null;

        for (COMPOSITION composition : ehrExtract.getAllCompositions()) {
            DiagnosisBodyType body = Mockito.spy(mapper.mapDiagnosisBodyType(composition));
            TS time = null;
            ST simpleText = null;
            for (CONTENT c : composition.getContent()) {
                ENTRY e = (ENTRY) c;
                for (ITEM i : e.getItems()) {
                    ELEMENT elm = (ELEMENT) i;
                    switch (i.getMeaning().getCode()) {
                    case DiagnosisMapper.CODE_ELEMENT:
                        cd = (CD) elm.getValue();
                        assertEquals(cd.getCode(), body.getDiagnosisCode().getCode());
                        assertEquals(cd.getCodeSystem(), body.getDiagnosisCode().getCodeSystem());
                        assertEquals(cd.getDisplayName().getValue(), body.getDiagnosisCode().getDisplayName());
                        break;
                    case DiagnosisMapper.TIME_ELEMENT:
                        time = (TS) elm.getValue();
                        assertTrue(body.getDiagnosisTime().startsWith(time.getValue()));
                        break;
                    case DiagnosisMapper.TYPE_ELEMENT:
                        simpleText = (ST) elm.getValue();
                        assertTrue(simpleText.getValue().equals(body.getTypeOfDiagnosis().value())
                               || (simpleText.getValue().equals("Kronisk diagnos") 
                                    && 
                                   body.isChronicDiagnosis() != null
                                    && 
                                   body.isChronicDiagnosis().booleanValue() 
                                    && 
                                   body.getTypeOfDiagnosis().equals(DiagnosisTypeEnum.BIDIAGNOS)));
                        typeTouch = true;
                        break;
                    }
                }
            }
            assertTrue(typeTouch);
        }
    }

    @Test
    public void mapResponse() {
        String responseXml = getRivtaXml(getDiagnosisMapper(), Util.DIAGNOSIS_TEST_FILE, true);

        assertTrue(responseXml.contains("GetDiagnosisResponse>"));
        assertTrue(responseXml.contains("typeOfDiagnosis>Bidiagnos"));
        assertTrue(responseXml.contains("chronicDiagnosis>true"));
        assertTrue(responseXml.contains("documentId>SE2321000164-1004Dia19381221704420090512083134940624000-1</"));
        assertTrue(responseXml.contains("healthcareProfessionalCareUnitHSAId>SE2321000164-12ab"));
        assertTrue(responseXml.contains("healthcareProfessionalCareGiverHSAId>SE2321000164-ab12"));
        assertTrue(responseXml.contains("relatedDiagnosis><"));
        assertTrue(responseXml.contains("documentId>SE123-relatedDiagnosis<"));
    }

    
    // TODO - activate for SERVICE-401
    @Ignore
    @Test
    public void testDiaDiaDbe() {
        RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
        ehrResp.getEhrExtract().add(Util.loadEhrTestData(Util.DIAGNOSISDIADIADBE));
        final MuleMessage mockMessage = mock(MuleMessage.class);
        when(mockMessage.getUniqueId()).thenReturn("1234");
        
        DiagnosisType diagnosisDiaDiaDbe = null;
        DiagnosisType diagnosisDiaDiaKod = null;
        GetDiagnosisResponseType resp = mapper.mapResponse(ehrResp, mockMessage);
        for(DiagnosisType rec : resp.getDiagnosis()) {
            if (rec.getDiagnosisHeader().getDocumentId().equals("SE1623210002198208149297ovdiag17/1")) {
                diagnosisDiaDiaDbe = rec;
            }
            if (rec.getDiagnosisHeader().getDocumentId().equals("SE1623210002198208149297ovdiag13/1")) {
                diagnosisDiaDiaKod = rec;
            }
        }
        if (diagnosisDiaDiaDbe == null || diagnosisDiaDiaKod == null) {
            fail("failed to load " + Util.DIAGNOSISDIADIADBE);
        }
        
        assertEquals("I25-P", diagnosisDiaDiaDbe.getDiagnosisBody().getDiagnosisCode().getCode());
        assertEquals("1.2.752.129.2.2.2.1", diagnosisDiaDiaDbe.getDiagnosisBody().getDiagnosisCode().getCodeSystem());
        assertEquals("Ischemisk hjärtsjukdom", diagnosisDiaDiaDbe.getDiagnosisBody().getDiagnosisCode().getDisplayName());
        
        assertEquals("E119", diagnosisDiaDiaKod.getDiagnosisBody().getDiagnosisCode().getCode());
        assertEquals("1.2.752.116.1.1.1.1.1", diagnosisDiaDiaKod.getDiagnosisBody().getDiagnosisCode().getCodeSystem());
        assertEquals("Diabetes mellitus, ej insulinberoende, utan komplikationer", diagnosisDiaDiaKod.getDiagnosisBody().getDiagnosisCode().getDisplayName());
    }
    
    
    // TODO - activate for SERVICE-401
    @Ignore
    @Test
    public void checkDiaDiaDbe() {
        String responseXml = getRivtaXml(mapper, Util.DIAGNOSISDIADIADBE, false);
        assertTrue(responseXml.contains("<ns2:diagnosisCode><ns2:code>I25-P</ns2:code><ns2:codeSystem>1.2.752.129.2.2.2.1</ns2:codeSystem><ns2:displayName>Ischemisk hjärtsjukdom</ns2:displayName></ns2:diagnosisCode>"));
        assertTrue(responseXml.contains("<ns2:diagnosisCode><ns2:code>E119</ns2:code><ns2:codeSystem>1.2.752.116.1.1.1.1.1</ns2:codeSystem><ns2:displayName>Diabetes mellitus, ej insulinberoende, utan komplikationer</ns2:displayName></ns2:diagnosisCode></ns2:diagnosisBody>"));
    }
    
}

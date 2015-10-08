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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.skl.skltpservices.npoadapter.test.Util;

/**
 * @author Martin Flower
 */
public class CareDocumentationMapperTest extends MapperTest {

    
    protected static final Logger log = LoggerFactory.getLogger(CareDocumentationMapper.class);
    
	private CareDocumentationMapper getCareDocumentationMapper() {
		CareDocumentationMapper mapper = (CareDocumentationMapper) AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_CAREDOCUMENTATION_2);
		return mapper;
	}
	
    @Test
    public void mapResponse() {

        String responseXml = getRivtaXml(getCareDocumentationMapper(), Util.CAREDOCUMENTATION_TEST_FILE, true);
        
        assertTrue (responseXml.contains("sourceSystemHSAid>SE2321000164-1006</"));
        assertTrue (responseXml.contains("<GetCareDocumentationResponse"));
        assertTrue (responseXml.contains("documentId>SE2321000164-1006Dok19381221704420090512082720692684000-1</"));
        assertTrue (responseXml.contains("Allmänmedicinska mottagningen vårdcentralen Forshaga"));
        assertTrue (responseXml.contains("<ns2:clinicalDocumentNoteTitle>Epikris</ns2:clinicalDocumentNoteTitle>"));
        assertFalse(responseXml.contains("This should not appear"));

        assertFalse(responseXml.contains("$$NL$$"));
        assertTrue(responseXml.contains("I110 Hypertensiv hjärtsjukdom med hjärtsvikt\r\n\r\nIntagningsorsak"));
    }
}

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

import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import riv.clinicalprocess.healthcond.actoutcome.getlaboratoryorderoutcomeresponder._3.GetLaboratoryOrderOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.test.Util;


public class LaboratoryOrderOutcomeMapperTest {
	
	private static final String TEST_UNIQUE_ID = UUID.randomUUID().toString();
	
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	private static EHREXTRACT ehrExctract;
	private static LaboratoryOrderOutcomeMapper mapper;

	@BeforeClass
	public static void init() {
		ehrExctract = Util.loadEhrTestData(Util.LAB_TEST_FILE);
		ehrResp.getEhrExtract().add(ehrExctract);
		mapper = Mockito.spy(new LaboratoryOrderOutcomeMapper());
	}
	
	private void print(GetLaboratoryOrderOutcomeResponseType resp) throws JAXBException {
		JAXBContext context =
		        JAXBContext.newInstance(GetLaboratoryOrderOutcomeResponseType.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(new JAXBElement<GetLaboratoryOrderOutcomeResponseType>(new QName("uri","local"), GetLaboratoryOrderOutcomeResponseType.class, resp), System.out);
	}

	@Test
	public void testMapResponseType() throws Exception {
		final GetLaboratoryOrderOutcomeResponseType type = mapper.mapResponseType(ehrResp, TEST_UNIQUE_ID);
	}
}

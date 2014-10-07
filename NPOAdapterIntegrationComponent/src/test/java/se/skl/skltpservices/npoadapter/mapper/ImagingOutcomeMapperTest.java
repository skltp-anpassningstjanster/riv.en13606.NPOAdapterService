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

import riv.clinicalprocess.healthcond.actoutcome.getimagingoutcomeresponder._1.GetImagingOutcomeResponseType;
import se.rivta.en13606.ehrextract.v11.EHREXTRACT;
import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.skl.skltpservices.npoadapter.test.Util;

public class ImagingOutcomeMapperTest {
	
	private static EHREXTRACT ehrExtract;
	private static ImagingOutcomeMapper mapper;
	private static final RIV13606REQUESTEHREXTRACTResponseType ehrResp = new RIV13606REQUESTEHREXTRACTResponseType();
	
	@BeforeClass
	public static void init() {
		ehrExtract = Util.loadEhrTestData(Util.IMAGE_TEST_FILE);
		mapper = new ImagingOutcomeMapper();
		ehrResp.getEhrExtract().add(ehrExtract);
	}
	
	
	@Test
	public void testMapResponseType() throws JAXBException {
		final GetImagingOutcomeResponseType resp = mapper.mapResponseType(ehrResp, UUID.randomUUID().toString());
	}
	
	private void print(GetImagingOutcomeResponseType resp) throws JAXBException {
		JAXBContext context =
		        JAXBContext.newInstance(GetImagingOutcomeResponseType.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(new JAXBElement<GetImagingOutcomeResponseType>(new QName("uri","local"), GetImagingOutcomeResponseType.class, resp), System.out);
	}
}

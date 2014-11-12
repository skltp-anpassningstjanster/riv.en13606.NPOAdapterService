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
package se.skl.skltpservices.npoadapter.test.stub;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skl.tp.vagvalsinfo.v2.HamtaAllaAnropsBehorigheterResponseType;
import skl.tp.vagvalsinfo.v2.HamtaAllaVirtualiseringarResponseType;
import skl.tp.vagvalsinfo.v2.SokVagvalsInfoInterface;
import skl.tp.vagvalsinfo.v2.VirtualiseringsInfoType;

/**
 * Stub service for retrieving all VirtualiseringsInfoType.
 * 
 * Only used by Integration tests. Not part of the distribution.
 */
@WebService(serviceName = "SokVagvalsServiceSoap11LitDocService",
        targetNamespace = "urn:skl:tp:vagvalsinfo:v2",
      endpointInterface = "skl.tp.vagvalsinfo.v2.SokVagvalsInfoInterface",
               portName = "SokVagvalsSoap11LitDocPort")
public class SokVagvalWS implements SokVagvalsInfoInterface {
    
    private static final Logger log = LoggerFactory.getLogger(SokVagvalWS.class);
    
    @Override
    @WebMethod
    @WebResult(name = "hamtaAllaVirtualiseringarResponse", targetNamespace = "urn:skl:tp:vagvalsinfo:v2", partName = "response")
    public HamtaAllaVirtualiseringarResponseType hamtaAllaVirtualiseringar(
                                                 @WebParam(partName = "parameters", name = "hamtaAllaVirtualiseringar", targetNamespace = "urn:skl:tp:vagvalsinfo:v2")
                                                 Object parameters) {
        
        // incoming parameter is ignored
        
        log.debug("SokVagvalsInfoInterface hamtaAllaVirtualiseringar");
        
        final HamtaAllaVirtualiseringarResponseType responseType = new HamtaAllaVirtualiseringarResponseType();
        VirtualiseringsInfoType infoType = new VirtualiseringsInfoType();
        infoType.setReceiverId("VS-1");
        infoType.setRivProfil("RIVEN13606");
        infoType.setTjansteKontrakt("urn:riv13606:v1.1:RIV13606REQUEST_EHR_EXTRACT");
        infoType.setVirtualiseringsInfoId("ID-1");
        infoType.setAdress("https://localhost:33002/npoadapter/ehrextract/stub");
        infoType.setFromTidpunkt(fromNow(-2));
        infoType.setTomTidpunkt(null);
        responseType.getVirtualiseringsInfo().add(infoType);

        infoType = new VirtualiseringsInfoType();
        infoType.setReceiverId("VS-2");
        infoType.setRivProfil("RIVTABP21");
        infoType.setTjansteKontrakt("urn:riv:ehr:patientsummary:GetEhrExtractResponder:1:GetEhrExtract:rivtabp21");
        infoType.setVirtualiseringsInfoId("ID-2");
        infoType.setAdress("http://localhost:33001/npoadapter/getehrextract/stub");
        infoType.setFromTidpunkt(fromNow(-2));
        infoType.setTomTidpunkt(null);
        responseType.getVirtualiseringsInfo().add(infoType);

        infoType = new VirtualiseringsInfoType();
        infoType.setReceiverId("VS-1");
        infoType.setRivProfil("RIVEN13606");
        infoType.setTjansteKontrakt("http://nationellpatientoversikt.se:SendStatus");
        infoType.setVirtualiseringsInfoId("ID-3");
        infoType.setAdress("https://localhost:33002/npoadapter/caresystem/stub");
        infoType.setFromTidpunkt(fromNow(-2));
        infoType.setTomTidpunkt(null);
        responseType.getVirtualiseringsInfo().add(infoType);

        infoType = new VirtualiseringsInfoType();
        infoType.setReceiverId("VS-2");
        infoType.setRivProfil("RIVEN13606");
        infoType.setTjansteKontrakt("http://nationellpatientoversikt.se:SendStatus");
        infoType.setVirtualiseringsInfoId("ID-987");
        infoType.setAdress("https://localhost:33002/npoadapter/caresystem/stub");
        infoType.setFromTidpunkt(fromNow(-2));
        infoType.setTomTidpunkt(null);
        responseType.getVirtualiseringsInfo().add(infoType);
        
        return responseType;
    }

    @Override
    /**
     * Throws IllegalArgumentException. Not implemented.
     */
    public HamtaAllaAnropsBehorigheterResponseType hamtaAllaAnropsBehorigheter(Object parameters) {
        throw new IllegalArgumentException("Method is not implemented (not valid in this context)!");
    }

    //
    protected XMLGregorianCalendar fromNow(int days) {
    	try {
        final GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        final XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        return date;
    	} catch (Exception err) {
    		return null;
    	}
    }
}

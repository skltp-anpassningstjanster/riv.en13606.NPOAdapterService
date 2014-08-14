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

import lombok.SneakyThrows;
import skl.tp.vagvalsinfo.v2.*;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Test stub service    .
 */
@WebService(serviceName = "SokVagvalsServiceSoap11LitDocService",
        targetNamespace = "urn:skl:tp:vagvalsinfo:v2",
        endpointInterface = "skl.tp.vagvalsinfo.v2.SokVagvalsInfoInterface",
                portName = "SokVagvalsSoap11LitDocPort")
public class SokVagvalWS implements SokVagvalsInfoInterface {
    @Override
    public HamtaAllaVirtualiseringarResponseType hamtaAllaVirtualiseringar(@WebParam(partName = "parameters", name = "hamtaAllaVirtualiseringar", targetNamespace = "urn:skl:tp:vagvalsinfo:v2") Object parameters) {
        final HamtaAllaVirtualiseringarResponseType responseType = new HamtaAllaVirtualiseringarResponseType();
        final VirtualiseringsInfoType infoType = new VirtualiseringsInfoType();
        infoType.setReceiverId("SE123456-00");
        infoType.setRivProfil("RIVEN13606");
        infoType.setTjansteKontrakt("urn:riv:ehr:patientsummary");
        infoType.setVirtualiseringsInfoId("ID");
        infoType.setAdress("http://localhost:11000/npoadapter/ehrextract/stub");
        infoType.setFromTidpunkt(fromNow(-2));
        infoType.setTomTidpunkt(null);
        responseType.getVirtualiseringsInfo().add(infoType);
        return responseType;
    }

    @Override
    public HamtaAllaAnropsBehorigheterResponseType hamtaAllaAnropsBehorigheter(@WebParam(partName = "parameters", name = "hamtaAllaAnropsBehorigheter", targetNamespace = "urn:skl:tp:vagvalsinfo:v2") Object parameters) {
        throw new IllegalArgumentException("Method is not implemented (not valid in this context)!");
    }

    //
    @SneakyThrows
    protected XMLGregorianCalendar fromNow(int days) {
        final GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        final XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        return date;
    }
}

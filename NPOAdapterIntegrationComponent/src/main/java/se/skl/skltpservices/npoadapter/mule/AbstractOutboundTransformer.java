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
package se.skl.skltpservices.npoadapter.mule;

import lombok.extern.slf4j.Slf4j;
import org.mule.api.MuleMessage;
import org.mule.transformer.AbstractMessageTransformer;
import se.skl.skltpservices.npoadapter.mapper.AbstractMapper;
import se.skl.skltpservices.npoadapter.mapper.Mapper;

import javax.xml.namespace.QName;

/**
 * Abstracts outbound transformers. <p/>
 *
 * Provides a method to lookup a valid {@link se.skl.skltpservices.npoadapter.mapper.Mapper} implementation.
 *
 * @author Peter
 */
@Slf4j
public abstract class AbstractOutboundTransformer extends AbstractMessageTransformer {

    static final String CXF_OPERATION = "cxf_operation";

    /**
     * Returns the actual mapper implementation if any can be found. <p/>
     *
     * The CXF message invocation property cxf_operation (QName) is expected, and the local part is used
     * as key to locate the actual mapper implementation.
     *
     * @see javax.xml.namespace.QName#getLocalPart()
     *
     * @param message the mule message.
     * @return the mapper.
     * @throws java.lang.IllegalStateException when no mapper can be found.
     */
    protected Mapper getMapper(final MuleMessage message) {
        log.debug("Retrieve the actual (SOAP) operation through the CXF message invocation property: " + CXF_OPERATION);
        final QName targetNS = message.getInvocationProperty(CXF_OPERATION);
        if (targetNS == null) {
            throw new IllegalStateException("NPOAdapater: Unable to determine operation because of a missing CXF invocation property in message: " + CXF_OPERATION);
        }
        final String sourceNS = message.getInvocationProperty(PreProcessor.ROUTE_SERVICE_SOAP_ACTION);
        return AbstractMapper.getInstance(sourceNS, targetNS.getNamespaceURI());
    }
}

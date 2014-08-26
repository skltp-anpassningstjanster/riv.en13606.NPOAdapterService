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
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import riv.itintegration.engagementindex._1.ResultCodeEnum;
import riv.itintegration.engagementindex.updateresponder._1.UpdateResponseType;

/**
 * Created by Peter on 2014-08-25.
 */
@Slf4j
public class CheckResponseTransformer extends AbstractMessageTransformer {


    @Override
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
        final Object payload = message.getPayload();

        log.info("================ response payload is: " + payload);
        if (payload instanceof UpdateResponseType) {
            check(((UpdateResponseType) payload).getResultCode() == ResultCodeEnum.OK);
        } else if (payload instanceof Boolean) {
            check((Boolean) payload);
        }

        return message;
    }

    //
    private void check(boolean t) {
        if (!t) {
            throw new IllegalArgumentException("Response error");
        }
    }
}

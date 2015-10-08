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

import org.junit.Test;
import org.mockito.Mockito;

import se.rivta.en13606.ehrextract.v11.RIV13606REQUESTEHREXTRACTResponseType;
import se.rivta.en13606.ehrextract.v11.ST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Peter on 2014-08-01.
 */
public class AbstractMapperTest {

    @Test
    public void testLookupMapper() {
        final Mapper mapper = AbstractMapper.getInstance(AbstractMapper.NS_EN_EXTRACT, AbstractMapper.NS_CARECONTACTS_2);
        assertTrue(mapper instanceof CareContactsMapper);
    }


    @Test(expected = IllegalStateException.class)
    public void testInvalidURNWhenLookupMapper() {
        AbstractMapper.getInstance("no-ns", "no-ns");
    }

    public void testWithNull() {
        try {
            AbstractMapper.getInstance(null, null);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException ie) {
        }
    }


    @Test
    public void continuation() {
        AbstractMapper am = Mockito.mock(AbstractMapper.class, Mockito.CALLS_REAL_METHODS);
        
        RIV13606REQUESTEHREXTRACTResponseType response13606 = new RIV13606REQUESTEHREXTRACTResponseType();
        response13606.setContinuationToken(new ST());
        response13606.getContinuationToken().setValue("something");
        assertTrue(am.continuation(response13606));
    }
    
    @Test
    public void noContinuation() {
        AbstractMapper am = Mockito.mock(AbstractMapper.class, Mockito.CALLS_REAL_METHODS);
        
        RIV13606REQUESTEHREXTRACTResponseType response13606 = new RIV13606REQUESTEHREXTRACTResponseType();
        response13606.setContinuationToken(new ST());
        response13606.getContinuationToken().setValue("");
        assertFalse(am.continuation(response13606));
    }
    
}

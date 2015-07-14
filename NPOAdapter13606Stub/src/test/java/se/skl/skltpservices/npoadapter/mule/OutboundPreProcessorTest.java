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

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.junit4.FunctionalTestCase;


public class OutboundPreProcessorTest {
	
	private static OutboundPreProcessor pre;
	private static MuleEvent event;
	private static MuleMessage message;
	
	@BeforeClass
	public static void init() {
		pre = new OutboundPreProcessor();
		message = new DefaultMuleMessage(null, Mockito.mock(MuleContext.class));
		
		event = Mockito.mock(MuleEvent.class);
		Mockito.when(event.getMessage()).thenReturn(message);
	}

	@Test
	public void testProcess() throws Exception {
		//MuleEvent e = pre.process(event);
	}


}

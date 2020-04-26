/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

package sharpen.ui.tests;

import sharpen.core.*;
import org.junit.Test;

public class EventConversionTestCase extends AbstractConversionTestCase {
	@Test
	public void testEventMapping() throws Throwable {
		
		final Configuration config = getConfiguration();
		config.mapEventAdd("events.Event4.addListener");
		config.mapEvent("events.EventRegistry.foo", "events.FooEventArgs");
		runBatchConverterTestCase(config,
				new TestCaseResource("events/EventMapping"),
				new TestCaseResource("events/EventMappingLib") {
					@Override
					public boolean isSupportingLibrary() {
						return true;
					}
				});
	}

	@Test
	public void testInterfaceWithEvents() throws Throwable {
		runResourceTestCase("events/EventInterface");
	}
	
	@Test
	public void testClassImplementingInterfaceWithEvents() throws Throwable {
		runResourceTestCase("events/EventInClassInterface");
	}
	
	@Test
	public void testEventConsumers() throws Throwable {
		runResourceTestCase("events/EventConsumer");
	}
	
	@Override
	protected Configuration getConfiguration() {
		return newPascalCaseIdentifiersConfiguration();
	}	
}

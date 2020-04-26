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

/* Copyright (C) 2004 - 2006 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;

import sharpen.core.*;
import org.junit.Test; 

public class RemoveTestCase extends AbstractConversionTestCase {
	
	@Test
	public void testRemoveClass() throws Throwable {
		runResourceTestCase("remove/RemoveClass");
	}

	@Test
	public void testRemoveAllClassesInArray() throws Throwable {
		runResourceTestCase("remove/RemoveAllClassesInArray");
	}

	@Test
	public void testRemoveMethod() throws Throwable {
		runResourceTestCase("remove/RemoveMethod");
	}
	
	@Test
	public void testRemoveInExpression() throws Throwable {
		runResourceTestCase("remove/RemoveMethodInExpression");
	}
	
	@Test
	public void testRemoveFirst() throws Throwable {
		runResourceTestCase("remove/RemoveFirst");
	}
	
	@Test
	public void testRemoveFirstInConstructor() throws Throwable {
		runResourceTestCase("remove/RemoveFirstInConstructor");
	}
	
	@Test
	public void testRemoveMethodByConfig() throws Throwable {
		final Configuration config = getConfiguration();
		config.removeMethod("remove.Foo.baz");
		runResourceTestCase(config, "remove/RemoveMethodByConfig");
	}
	
	@Override
	protected Configuration getConfiguration() {
	    final Configuration configuration = super.getConfiguration();
	    configuration.enableNativeTypeSystem();
		return configuration;
	}


}

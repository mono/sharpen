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

import java.io.IOException;

import sharpen.core.*;

import org.junit.Test;


public class GenericsTestCase extends AbstractConversionTestCase {
	
	@Test
	public void testGenericRuntimeTypeParameters() throws Throwable {
		Configuration config = newPascalCasePlusConfiguration();
		config.enableNativeTypeSystem();
		runResourceTestCase(config, "generics/GenericRuntimeTypeParameters");
	}
	
	@Test
	public void testCollectionMappings() throws Throwable {
		runResourceTestCase("CollectionMappings");
	}

	@Test
	public void testGenericMethodImpl() throws Throwable {
		runResourceTestCase("GenericMethodImpl");
	}
	
	@Test
	public void testWildcardTypes() throws Throwable {
		runResourceTestCase("WildcardTypes");
	}

	@Test
	public void testSimpleInterface() throws Throwable {
		runResourceTestCase("GenericInterface");
	}
	
	@Test
	public void testSimpleClass() throws Throwable {
		runResourceTestCase("GenericClass");
	}
	
	@Test
	public void testGenericImplements() throws Throwable {
		runResourceTestCase("GenericImplements");
	}
	
	@Test
	public void testGenericExtends() throws Throwable {
		runResourceTestCase("GenericExtends");
	}
	
	@Test
	public void testGenericReturnTypes() throws Throwable {
		runResourceTestCase("GenericReturnTypes");
	}
	
	@Test
	public void testGenericMethodParameters() throws Throwable {
		runResourceTestCase("GenericMethodParameters");
	}
	
	public void testGenericObjectConstruction() throws Throwable {
		runResourceTestCase("GenericObjectConstruction");
	}
	
	@Test
	public void testGenericMethods() throws Throwable {
		runResourceTestCase("GenericMethods");
	}
	
	@Override
	protected void runResourceTestCase(String resourceName) throws IOException {
		super.runResourceTestCase("generics/" + resourceName);
	}
}

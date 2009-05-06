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

import org.junit.*;

import sharpen.core.*;

public class PropertyConversionTestCase extends AbstractConversionTestCase {
	
	@Ignore
	public void testMappedProperties() throws Throwable {
		runResourceTestCase("properties/MappedProperties");
	}
	
	public void testGetterSetterWithDifferentNames() throws Throwable {
		runResourceTestCase("properties/GetterSetterWithDifferentNames");
	}
	
	public void testStaticGetter() throws Throwable {
		runResourceTestCase("properties/StaticGetter");
	}

	public void testSimpleGetter() throws Throwable {
		runResourceTestCase("properties/SimpleGetter");
	}
	
	public void testGetterSetterProperties() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/GetterSetterProperties");		
	}
	
	public void testSimpleSetter() throws Throwable {
		runResourceTestCase("properties/SimpleSetter");
	}
	
	public void testInterfaceGetter() throws Throwable {
		runResourceTestCase("properties/InterfaceGetter");
	}
	
	public void testTestIndexerGeneric() throws Throwable {
		runResourceTestCase("properties/TestIndexerGeneric");
	}
	
	public void testIndexer() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/TestIndexer",
				"properties/TestIndexerClient");		
	}
	
	public void testPropertyInterfaceAndClassInDifferentCompilationUnits() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/InterfaceGetterImpl");
	}
	
	public void testAbstractGetter() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/BaseInterfaceGetter",
				"properties/BaseInterfaceGetterImpl");
	}
	
	public void testOverrideGetter() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/InterfaceGetterImpl",
				"properties/OverrideGetter",
				"properties/OverrideGetterConsumer");
	}
	
	public void testNonStaticNestedUsingSuperProperty() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/InterfaceGetterImpl",
				"properties/NonStaticNestedUsingSuperProperty");
	}
	
	@Override
	protected Configuration getConfiguration() {
		return newPascalCaseIdentifiersConfiguration();
	}	
}

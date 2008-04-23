/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;


public class GenericsTestCase extends AbstractConverterTestCase {
	
	public void testCollectionMappings() throws Throwable {
		runResourceTestCase("generics/CollectionMappings");
	}
	
	public void testWildcardTypes() throws Throwable {
		runResourceTestCase("generics/WildcardTypes");
	}

	public void testSimpleInterface() throws Throwable {
		runResourceTestCase("generics/GenericInterface");
	}
	
	public void testSimpleClass() throws Throwable {
		runResourceTestCase("generics/GenericClass");
	}
	
	public void testGenericImplements() throws Throwable {
		runResourceTestCase("generics/GenericImplements");
	}
	
	public void testGenericExtends() throws Throwable {
		runResourceTestCase("generics/GenericExtends");
	}
	
	public void testGenericReturnTypes() throws Throwable {
		runResourceTestCase("generics/GenericReturnTypes");
	}
	
	public void testGenericMethodParameters() throws Throwable {
		runResourceTestCase("generics/GenericMethodParameters");
	}
	
	public void testGenericObjectConstruction() throws Throwable {
		runResourceTestCase("generics/GenericObjectConstruction");
	}
	
	public void testGenericMethods() throws Throwable {
		runResourceTestCase("generics/GenericMethods");
	}
}

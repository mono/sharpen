/* Copyright (C) 2010 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;

public class ArraysTestCase extends AbstractConversionTestCase {
	public void testArrayAccess() throws Throwable {
		runResourceTestCase("Arrays1");
	}
	
	public void testArrayCreation() throws Throwable {
		runResourceTestCase("Arrays2");
	}
	
	public void testArrayInitializer() throws Throwable {
		runResourceTestCase("Arrays3");
	}
	
	public void testNestedArrayInitializer() throws Throwable {
		runResourceTestCase("Arrays4");
	}
	
	public void testUntypedArrayInitializer() throws Throwable {
		runResourceTestCase("Arrays5");
	}
}

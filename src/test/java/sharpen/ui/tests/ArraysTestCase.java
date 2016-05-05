/* Copyright (C) 2010 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;
import org.junit.Test;

public class ArraysTestCase extends AbstractConversionTestCase {
	@Test
	public void testArrayAccess() throws Throwable {
		runResourceTestCase("Arrays1");
	}
	@Test
	public void testArrayCreation() throws Throwable {
		runResourceTestCase("Arrays2");
	}
	@Test
	public void testArrayInitializer() throws Throwable {
		runResourceTestCase("Arrays3");
	}
	@Test
	public void testNestedArrayInitializer() throws Throwable {
		runResourceTestCase("Arrays4");
	}
	@Test
	public void testUntypedArrayInitializer() throws Throwable {
		runResourceTestCase("Arrays5");
	}
}

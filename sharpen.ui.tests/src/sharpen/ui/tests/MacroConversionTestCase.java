/* Copyright (C) 2009  Versant Inc.  http://www.db4o.com */

package sharpen.ui.tests;
import org.junit.Test;

public class MacroConversionTestCase extends AbstractConversionTestCase {
	@Test
	public void testMethodMacro() throws Throwable {
		runResourceTestCase("macro/MethodMacro");
	}
	@Test
	public void testTypeMacro() throws Throwable {
		runResourceTestCase("macro/TypeMacro");
	}

}

/* Copyright (C) 2009  Versant Inc.  http://www.db4o.com */

package sharpen.ui.tests;

public class MacroConversionTestCase extends AbstractConversionTestCase {
	
	public void testMethodMacro() throws Throwable {
		runResourceTestCase("macro/MethodMacro");
	}
	
	public void testTypeMacro() throws Throwable {
		runResourceTestCase("macro/TypeMacro");
	}

}

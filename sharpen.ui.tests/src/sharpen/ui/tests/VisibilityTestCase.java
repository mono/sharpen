/* Copyright (C) 2010 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;

public class VisibilityTestCase extends AbstractConversionTestCase {
	
	public void testInternal() throws Throwable {
		runResourceTestCase("visibility/Internal");
	}
	
	public void testSharpenPublic() throws Throwable {
		runResourceTestCase("visibility/Public");
	}
	
	public void testSharpenPrivate() throws Throwable {
		runResourceTestCase("visibility/Private");
	}

}

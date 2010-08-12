/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com */

package sharpen.ui.tests;

public class ImportsTestCase extends AbstractConversionTestCase {
	
	public void testStaticImports() throws Throwable {
		runBatchConverterTestCase(getConfiguration(), "imports/StaticImports", "imports/StaticallyImported");
	}


}

/* Copyright (C) 2010 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;
import org.junit.Test;

public class VisibilityTestCase extends AbstractConversionTestCase {
	
	@Test
	public void testInternal() throws Throwable {
		runResourceTestCase("visibility/Internal");
	}
	
	@Test
	public void testSharpenPublic() throws Throwable {
		runResourceTestCase("visibility/Public");
	}
	@Test
	public void testSharpenPrivate() throws Throwable {
		runResourceTestCase("visibility/Private");
	}

}

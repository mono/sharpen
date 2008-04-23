/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

import junit.framework.*;


public class StringAssert {

	public static void assertEqualLines(String expected, String actual) {
		Assert.assertEquals(normalizeWhiteSpace(expected), normalizeWhiteSpace(actual));
	}
	
	public static String normalizeWhiteSpace(String expected) {
		return expected.trim().replaceAll("\r\n", "\n");
	}

}

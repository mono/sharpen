/* Copyright (C) 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

import java.io.*;

public class ProblemsOutputTestCase extends AbstractConverterTestCase {
	
	public void testProblemsGoToStderr() throws Throwable {		
		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		
		final PrintStream saved = System.err;
		try {
			System.setErr(new PrintStream(stderr));
			
			final String resourcePath = "/TestProject/src/problems/Spam.java";			
			try {
				runResourceTestCase("problems/Spam");
			} catch (RuntimeException x) {
				assertTrue(x.getMessage().contains(resourcePath));
			}			
			assertEquals(
					resourcePath + "(4): Eggs cannot be resolved to a type",
					stderr.toString().trim());
		} finally {
			System.setErr(saved);
		}
	}
}

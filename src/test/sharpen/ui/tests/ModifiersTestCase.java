/* Copyright (C) 2010 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;

import sharpen.core.*;
import org.junit.Test;

public class ModifiersTestCase extends AbstractConversionTestCase {

	@Test
	public void testVirtualMethod() throws Throwable {
		runResourceTestCase(pathFor("VirtualMethod1"));
	}

	@Test
	public void testOverrideAbstract() throws Throwable {
		runResourceTestCase(pathFor("Override4"));
	}	

	@Test
	public void testSimpleOverride() throws Throwable {
		runResourceTestCase(pathFor("Override1"));
	}
	
	@Test
	public void testDeepOverride() throws Throwable {
		runResourceTestCase(pathFor("Override2"));
	}
	
	@Test
	public void testOverrideMethodDefinedInInterface() throws Throwable {
		runResourceTestCase(pathFor("Override3"));
	}
	
	@Test
	public void testNew() throws Throwable {
		runResourceTestCase(pathFor("New"));
	}
	
	
	private String pathFor(String resource) {
		return "modifiers/" + resource;
	}
	
	@Override
	protected Configuration configuration() {
		Configuration config = super.configuration();
		return config;
	}
}

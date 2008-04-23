/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

import sharpen.core.*;

public class RemoveTestCase extends AbstractConverterTestCase {

	public void testRemoveMethod() throws Throwable {
		runResourceTestCase("remove/RemoveMethod");
	}
	
	public void testRemoveInExpression() throws Throwable {
		runResourceTestCase("remove/RemoveMethodInExpression");
	}
	
	public void testRemoveMethodByConfig() throws Throwable {
		final Configuration config = getConfiguration();
		config.removeMethod("remove.Foo.baz");
		runResourceTestCase(config, "remove/RemoveMethodByConfig");
	}


}

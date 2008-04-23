/* Copyright (C) 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

import sharpen.core.*;

/**
 * @exclude
 */
public class NamespaceMappingTestCase extends AbstractConverterTestCase {
	
	public void testPascalCaseNamespaces() throws Throwable {
		runBatchConverterTestCase(
				newPascalCaseConfiguration(),
				"namespaceMapping/foo/bar/Baz",
				"namespaceMapping/foo/bar/Gazonk");
	}
	
	public void testKeywordsInNamespace() throws Throwable {
		runResourceTestCase("namespaceMapping/out/event/Foo");
	}
	
	private Configuration newPascalCaseConfiguration() {
		final Configuration configuration = super.getConfiguration();
		configuration.setNamingStrategy(PascalCaseNamingStrategy.DEFAULT);
		return configuration;
	}

}

/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

import sharpen.core.*;


public class JavadocTestCase extends AbstractConverterTestCase {
	
	public void testJavadoc() throws Throwable {
		runResourceTestCase("javadoc/Javadoc1");
	}
	
	public void testLinkWithLabel() throws Throwable {
		runResourceTestCase("javadoc/LinkWithLabel");
	}
	
	public void testXmlDocumentOverlay() throws Throwable {
		String resourceName = "javadoc/XmlDocOverlay";
		runResourceTestCase(newDocumentationOverlayConfiguration(resourceName + ".xml"), resourceName);
	}
	
	private Configuration newDocumentationOverlayConfiguration(String resourceName) {
		Configuration configuration = getConfiguration();
		configuration.setDocumentationOverlay(new XmlDocumentationOverlay(ResourceUtility.getResourceUri(resourceName)));
		return configuration;
	}

	@Override
	protected Configuration getConfiguration() {
		final Configuration configuration = super.getConfiguration();
		configuration.enableNativeInterfaces();
		return configuration;
	}

}

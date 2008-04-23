/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

public class IgnoreAnnotationsTestCase extends AbstractConverterTestCase {
	
	public void testIgnoreImplements() throws Throwable {
		runResourceTestCase("ignore/IgnoreImplements");
	}

	public void testIgnoreExtendsOverride() throws Throwable {
		runResourceTestCase("ignore/IgnoreExtendsOverride");
	}
	
	public void testIgnoreExtends() throws Throwable {
		runResourceTestCase("ignore/IgnoreExtends");
	}
	
	public void testIgnore() throws Throwable {
		final String converted = sharpenResource(getConfiguration(), new TestCaseResource("ignore/Ignore"));
		StringAssert.assertEqualLines("namespace ignore\n{\n}", converted);
	}
	
	public void testIgnoreMethod() throws Throwable {
		runResourceTestCase("ignore/IgnoreMethod");
	}
	
	public void testIgnoreField() throws Throwable {
		runResourceTestCase("ignore/IgnoreField");
	}
}

package sharpen.ui.tests;

import sharpen.core.Configuration;

public class NativeTypeSystemTestCase extends AbstractConverterTestCase {
	public void testClassToType() throws Throwable {
		runResourceTestCase("nativeTypeSystem/ClassToType");
	}
	
	public void testStringMethods() throws Throwable {
		runResourceTestCase("nativeTypeSystem/StringMethods1");
	}
	
	public void testReflection() throws Throwable {
		runResourceTestCase(nativePascalCase(), "nativeTypeSystem/Reflection");
	}
	
	private Configuration nativePascalCase() {
		return enableNativeTypeSystem(newPascalCasePlusConfiguration());
	}

	@Override
	protected Configuration getConfiguration() {
		return enableNativeTypeSystem(super.getConfiguration());
	}

	private Configuration enableNativeTypeSystem(Configuration c) {
		c.enableNativeTypeSystem();
		return c;
	}
}

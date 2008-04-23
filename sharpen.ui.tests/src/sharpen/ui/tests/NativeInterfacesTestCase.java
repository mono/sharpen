package sharpen.ui.tests;

import sharpen.core.Configuration;

public class NativeInterfacesTestCase extends AbstractConverterTestCase {

	public void testNativeInterfaces() throws Throwable {
		runResourceTestCase(newNativeInterfacesConfiguration(), "interfaces/Foo", "interfaces/IFoo");
	}
	
	public void testNativeInterfacesOtherCompilationUnit() throws Throwable {
		runBatchConverterTestCase(
				newNativeInterfacesConfiguration(),
				"interfaces/FooImpl",
				"interfaces/BaseFoo");
	}
	
	public void testMappedNativeInterfacesOtherCompilationUnit() throws Throwable {
		Configuration configuration = newNativeInterfacesConfiguration();
		configuration.mapNamespace("interfaces", "What.Ever");
		runBatchConverterTestCase(
				configuration,
				"interfaces/MappedFooImpl",
				"interfaces/MappedBaseFoo");
	}	
	
	public Configuration newNativeInterfacesConfiguration() {
		Configuration configuration = newPascalCasePlusConfiguration();
		configuration.enableNativeInterfaces();
		configuration.enableNativeTypeSystem();
		return configuration;
	}
}

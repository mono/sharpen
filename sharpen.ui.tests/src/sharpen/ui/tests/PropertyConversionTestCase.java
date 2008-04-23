package sharpen.ui.tests;

import sharpen.core.*;

public class PropertyConversionTestCase extends AbstractConverterTestCase {
	
	public void testStaticGetter() throws Throwable {
		runResourceTestCase("properties/StaticGetter");
	}

	public void testSimpleGetter() throws Throwable {
		runResourceTestCase("properties/SimpleGetter");
	}
	
	public void testSimpleSetter() throws Throwable {
		runResourceTestCase("properties/SimpleSetter");
	}
	
	public void testInterfaceGetter() throws Throwable {
		runResourceTestCase("properties/InterfaceGetter");
	}
	
	public void testTestIndexerGeneric() throws Throwable {
		runResourceTestCase("properties/TestIndexerGeneric");
	}
	
	public void testIndexer() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/TestIndexer",
				"properties/TestIndexerClient");		
	}
	
	public void testPropertyInterfaceAndClassInDifferentCompilationUnits() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/InterfaceGetterImpl");
	}
	
	public void testAbstractGetter() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/BaseInterfaceGetter",
				"properties/BaseInterfaceGetterImpl");
	}
	
	public void testOverrideGetter() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/InterfaceGetterImpl",
				"properties/OverrideGetter",
				"properties/OverrideGetterConsumer");
	}
	
	public void testNonStaticNestedUsingSuperProperty() throws Throwable {
		runBatchConverterTestCase(
				getConfiguration(),
				"properties/InterfaceGetter",
				"properties/InterfaceGetterImpl",
				"properties/NonStaticNestedUsingSuperProperty");
	}
	
	@Override
	protected Configuration getConfiguration() {
		return newPascalCaseIdentifiersConfiguration();
	}	
}

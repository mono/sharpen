package sharpen.ui.tests;

import sharpen.core.Configuration;

public class OrganizeUsingsTestCase extends AbstractConverterTestCase {
	
	public void testGenerics() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/Generics");
	}
	
	public void testSimpleUsing() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/SimpleTest");
	}

	public void testFullyQualifiedName() throws Throwable {
		Configuration conf = newOrganizeUsingsConfiguration();
		conf.addFullyQualifiedTypeName("Test");
		runResourceTestCase(conf, "usings/FullyQualifiedType");
	}

	public void testNestedType() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/deep/tree/NestedType");
	}

	public void testNamespaceConflict() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/NamespaceConflict");
	}
	
	public void testMethodNameConflict() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/MethodNameConflict");
	}	
	
	public Configuration newOrganizeUsingsConfiguration() {
		Configuration configuration = newPascalCasePlusConfiguration();
		configuration.enableNativeTypeSystem();
		configuration.enableOrganizeUsings();
		return configuration;
	}
}

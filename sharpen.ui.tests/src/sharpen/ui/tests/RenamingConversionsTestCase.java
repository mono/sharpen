package sharpen.ui.tests;

import sharpen.core.Configuration;

public class RenamingConversionsTestCase extends AbstractConverterTestCase {

	public void testRenamingMethods() throws Throwable {
		runResourceTestCase("renaming/SimpleMethodRenaming");
	}
	
	public void testAllUppercase() throws Throwable {
		runResourceTestCase(newPascalCasePlusConfiguration(), "renaming/AllUpperCaseIdentifiers");
	}
	
	public void testRenamingTypes() throws Throwable {
		// TODO: explicitly configuring the renaming should be unnecessary
		// just like it is with method or interface names
		final Configuration configuration = getConfiguration();
		configuration.mapType("renaming.TypeRenaming", "renaming.Renamed");
		runResourceTestCase(configuration, "renaming/TypeRenaming", "renaming/Renamed");
	}
}

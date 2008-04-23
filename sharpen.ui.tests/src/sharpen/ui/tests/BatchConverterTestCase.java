package sharpen.ui.tests;

import java.io.IOException;

import sharpen.core.BatchConverter;
import sharpen.core.resources.SimpleProject;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;

public class BatchConverterTestCase extends AbstractConverterTestCase {

	public void testSingleClassEmptyPackage() throws Throwable {
		runBatchConverterTestCase("EmptyClass");
	}
	
	public void testMultipleClassesEmptyPackage() throws Throwable {
		runBatchConverterTestCase("EmptyClass", "AnotherEmptyClass");
	}
	
	public void testKeywordNamespaces() throws Throwable {
		runBatchConverterTestCase("namespaceMapping/out/event/Foo");
	}
	
	public void testEventInterfaceAndClassInDifferentCompilationUnits() throws Throwable, IOException, Throwable {
		runBatchConverterPascalCaseTestCase("events/EventInterface", "events/EventInterfaceImpl");
	}
	
	private void runBatchConverterPascalCaseTestCase(String... resourceNames) throws CoreException, IOException, Throwable {
		runBatchConverterTestCase(newPascalCaseIdentifiersConfiguration(), resourceNames);
	}
	
	private void runBatchConverterTestCase(String... resourceNames) throws CoreException, IOException, Throwable {
		runBatchConverterTestCase(getConfiguration(), resourceNames);
	}
	
	public void testSingleClassSimplePackageAndTargetFolder() throws Throwable {

		runResourceTestCaseWithTargetFolder("mp/Albatross");
	}
	
	public void testSingleClassNestedPackageAndTargetFolder() throws Throwable {
		
		runResourceTestCaseWithTargetFolder("mp/nested/Parrot");
		
	}

	private void runResourceTestCaseWithTargetFolder(String path)
			throws Throwable {

		TestCaseResource resource = new TestCaseResource(path);
		ICompilationUnit cu = createCompilationUnit(resource);

		SimpleProject targetProject = new SimpleProject("TargetProject");
		IFolder targetFolder = targetProject.createFolder("src");

		try {

			BatchConverter converter = new BatchConverter(getConfiguration());
			converter.setSource(new ICompilationUnit[] {cu});
			converter.setTargetFolder(targetFolder);
			converter.run();

			assertFile(resource, targetFolder.getFile(path + ".cs"));

		} finally {
			targetProject.dispose();
		}

	}

}
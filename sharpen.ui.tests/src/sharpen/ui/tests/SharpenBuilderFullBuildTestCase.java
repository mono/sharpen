package sharpen.ui.tests;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import sharpen.builder.*;
import sharpen.core.framework.resources.WorkspaceUtilities;

public class SharpenBuilderFullBuildTestCase extends AbstractConversionTestCase {
	@Override
	protected void tearDown() throws Exception {
		delete(getConvertedProject());
		super.tearDown();
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_project.addNature(SharpenNature.NATURE_ID);
		WorkspaceUtilities.setAutoBuilding(false);
	}
	
	public void testMultipleSourceFolders() throws Throwable {
		final TestCaseResource resource1 = new TestCaseResource("builder/EmptyClass");
		final TestCaseResource resource2 = new TestCaseResource("builder/EmptyInterface");
		final ICompilationUnit cu1 = createCompilationUnit(addSourceFolder("src1"), resource1);
		final ICompilationUnit cu2 = createCompilationUnit(addSourceFolder("src2"), resource2);
		
		_project.buildProject(null);
		
		assertConvertedFile(resource1, cu1);		
		assertConvertedFile(resource2, cu2);	
	}

	private void assertConvertedFile(final TestCaseResource resource,
			final ICompilationUnit cu) throws IOException, CoreException {
		resource.assertFile(getConvertedProject().getFile(cu.getResource().getParent().getProjectRelativePath() + "/" + resource.targetSimpleName() + ".cs"));
	}

	private IPackageFragmentRoot addSourceFolder(String path) throws CoreException {
		return _project.addSourceFolder(path);
	}

}

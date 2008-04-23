package sharpen.ui.tests;

import java.io.*;

import sharpen.core.*;
import sharpen.core.resources.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import sharpen.builder.*;

public class SharpenBuilderTestCase extends AbstractConverterTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_project.addNature(SharpenNature.NATURE_ID);
	}
	
	public void testConvertsNewFiles() throws Throwable {
		TestCaseResource resource = addResourceAndWaitForBuild("EmptyClass");
		
		IProject convertedProject = getConvertedProject();
		try {
			assertConvertedFile(resource, convertedProject);
		} finally {
			delete(convertedProject);
		}
	}

	private IFile getConvertedFile(IProject convertedProject,
			TestCaseResource resource) {
		return convertedProject.getFolder(SharpenConstants.DEFAULT_TARGET_FOLDER).getFile(resource.getSimpleName() + ".cs");
	}

	private void delete(IProject convertedProject) throws CoreException {
		convertedProject.delete(true, true, null);
	}

	private IProject getConvertedProject() {
		return getProject(_project.getName() + SharpenConstants.SHARPENED_PROJECT_SUFFIX);
	}
	
	public void testConvertsUpdatedFiles() throws Throwable {
		final TestCaseResource resource = addResourceAndWaitForBuild("EmptyClass");
		IProject convertedProject = getConvertedProject();
		try {
			delete(getConvertedFile(convertedProject, resource));
			touchFile("src/EmptyClass.java");
			waitForBuild();
			assertConvertedFile(resource, convertedProject);
		} finally {
			delete(convertedProject);
		}
	}

	private void delete(IFile file) throws CoreException {
		file.delete(true, null);
	}

	private void assertConvertedFile(final TestCaseResource resource,
			IProject convertedProject) throws Throwable {
		assertFile(resource, getConvertedFile(convertedProject, resource));
	}

	private void touchFile(String path) throws CoreException, IOException {
		IFile file = _project.getFile(path);
		file.setContents(encode(decode(file) + "\n"), true, false, null);
	}
	
	private InputStream encode(String string) throws CoreException {
		return WorkspaceUtilities.encode(string, "utf-8");
	}

	private String decode(IFile file) throws CoreException, IOException {
		BufferedReader reader = new BufferedReader(WorkspaceUtilities.decode(file));
		StringWriter writer = new StringWriter();
		String line = null;
		while (null != (line = reader.readLine())) {
			writer.write(line);
		}
		return writer.toString();
	}

	private IProject getProject(String name) {
		return WorkspaceUtilities.getProject(name);
	}
	
	private TestCaseResource addResourceAndWaitForBuild(String resourceName) throws CoreException,
			IOException {
		TestCaseResource resource = new TestCaseResource(resourceName);
		createCompilationUnit(resource);
		waitForBuild();
		return resource;
	}
	
	public void testTargetFolderConfiguration() throws Throwable {
		
		SimpleProject targetProject = new SimpleProject("TargetProject");
		try {
			IFolder targetFolder = targetProject.createFolder("target");
		
			ISharpenProject sharpen = SharpenProject.create(_project.getProject());
			sharpen.setTargetFolder(targetFolder);
			
			TestCaseResource resource = addResourceAndWaitForBuild("EmptyClass");
			assertFile(resource, targetFolder.getFile("EmptyClass.cs"));
		} finally {
			targetProject.dispose();
		}
	}
	
	private void waitForBuild() {
		_project.joinBuild();
	}
}

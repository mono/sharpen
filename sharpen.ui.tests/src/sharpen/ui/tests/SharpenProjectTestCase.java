package sharpen.ui.tests;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import sharpen.builder.*;

public class SharpenProjectTestCase extends AbstractConverterTestCase {
	
	public void testCreateReturnsNullForInvalidNature() throws CoreException {
		
		assertNull(SharpenProject.create(_project.getProject()));
	}
	
	public void testProjectWithNatureIsAdaptable() throws CoreException {
		assertNull(getAdapter(ISharpenProject.class));
		
		addSharpenNature();
		
		assertNotNull(getAdapter(ISharpenProject.class));
	}

	private Object getAdapter(Class<?> klass) {
		return _project.getProject().getAdapter(klass);
	}
	
	public void testDefaultConfiguration() throws CoreException {
		addSharpenNature();
		
		IProject srcProject = _project.getProject();
		ISharpenProject sharpen = SharpenProject.create(srcProject);
		assertNotNull(sharpen);
		
		// default configuration points to a non existent folder
		// which will be automatically created by the builder
		// the first time is needed
		IFolder targetFolder = sharpen.getTargetFolder();
		assertNotNull(targetFolder);
		
		assertTrue(!targetFolder.exists());
		assertTrue(!targetFolder.getProject().exists());
		
		IPath targetPath = srcProject.getParent().getFullPath().append(srcProject.getName() + ".net/src");
		assertPath(targetPath, targetFolder);
	}

	private void addSharpenNature() throws CoreException {
		_project.addNature(SharpenNature.NATURE_ID);
	}
	
	private void assertPath(IPath targetPath, IFolder targetFolder) {
		assertEquals(targetPath.toPortableString(), targetFolder.getFullPath().toPortableString());
	}

}

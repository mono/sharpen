package sharpen.core.resources;


import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

public class SimpleProject {

	protected final IProject _project;

	public SimpleProject(String name) throws CoreException {
		this(name, null);
	}
	
	public SimpleProject(String name, IProgressMonitor monitor) throws CoreException {
		this(WorkspaceUtilities.getProject(name), monitor);
	}
	
	public SimpleProject(IProject project, IProgressMonitor monitor) throws CoreException {
		_project = project;
		WorkspaceUtilities.initializeProject(_project, monitor);
	}

	public String getName() {
		return _project.getName();
	}

	public IFolder createFolder(String name) throws CoreException {
		return createFolder(name, null);
	}
	
	public IFolder createFolder(String name, IProgressMonitor monitor) throws CoreException {
		IFolder folder = _project.getFolder(name);
		folder.create(false, true, monitor);
		return folder;
	}

	public IProject getProject() {
		return _project;
	}
	
	public void dispose() throws CoreException {
		_project.delete(true, true, null);
	}
	
	public void addReferencedProject(IProject reference, IProgressMonitor monitor) throws CoreException {
		WorkspaceUtilities.addProjectReference(_project, reference, monitor);
	}

	public IFile getFile(String path) {
		return _project.getFile(path);
	}
}
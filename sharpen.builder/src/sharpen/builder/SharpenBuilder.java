package sharpen.builder;

import java.io.IOException;
import java.util.*;

import sharpen.core.*;
import sharpen.core.resources.WorkspaceUtilities;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

public class SharpenBuilder extends IncrementalProjectBuilder {

	class ChangedCompilationUnitCollector implements IResourceDeltaVisitor {
		
		private final ArrayList<ICompilationUnit> _changes = new ArrayList<ICompilationUnit>();
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				changed(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				changed(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
		
		public List<ICompilationUnit> changes() {
			return _changes;
		}

		private void changed(IResource resource) {
			if (isSourceFile(resource)) {
				_changes.add((ICompilationUnit) JavaCore.create(resource));
			}
		}

		private boolean isSourceFile(IResource resource) {
			if (resource.getType() != IResource.FILE) {
				return false;
			}
			return resource.getFileExtension().equals("java");
		}
	}
	
	public static final String BUILDER_ID = Activator.PLUGIN_ID + ".sharpenBuilder";

	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		try {
			if (kind == FULL_BUILD) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					incrementalBuild(delta, monitor);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException, IOException {
		sharpen(JavaModelUtility.collectCompilationUnits(getJavaProject()), monitor);
	}
	
	private IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException, IOException {
		sharpen(changedCompilationUnits(delta), monitor);
	}

	private void sharpen(List<ICompilationUnit> compilationUnits,
			IProgressMonitor monitor) throws CoreException, IOException {
		BatchConverter converter = new BatchConverter();
		converter.setTargetFolder(getTargetFolder(monitor));
		converter.setSource(compilationUnits);
		converter.setProgressMonitor(monitor);
		converter.run();
	}

	private IFolder getTargetFolder(IProgressMonitor monitor) throws CoreException {
		IFolder targetFolder = getConfiguration(monitor).getTargetFolder();
		WorkspaceUtilities.initializeTree(targetFolder, monitor);
		return targetFolder;
	}

	private ISharpenProject getConfiguration(IProgressMonitor monitor) throws CoreException {
		return SharpenProject.create(getProject(), monitor);
	}

	private List<ICompilationUnit> changedCompilationUnits(IResourceDelta delta)
			throws CoreException {
		ChangedCompilationUnitCollector collector = new ChangedCompilationUnitCollector();
		delta.accept(collector);
		return collector.changes();
	}
}

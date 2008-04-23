package sharpen.core;

import java.util.*;

import sharpen.core.resources.SimpleProject;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

public class JavaModelUtility {

	public static void collectCompilationUnits(List<ICompilationUnit> result, IPackageFragmentRoot root) throws JavaModelException {
		IJavaElement[] elements = root.getChildren();
		for (int j = 0; j < elements.length; ++j) {
			IPackageFragment p = (IPackageFragment)elements[j];
			result.addAll(Arrays.asList(p.getCompilationUnits()));
		}
	}
	
	public static List<ICompilationUnit> collectCompilationUnits(IJavaProject project) throws JavaModelException {
		
		List<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
		
		IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
		for (int i = 0; i < roots.length; ++i) {
			IPackageFragmentRoot root = roots[i];
			if (IPackageFragmentRoot.K_SOURCE == root.getKind()) {
				collectCompilationUnits(result, root);
			}
		}
		
		return result;
	}
	
	public static IFolder getTargetProjectAndFolder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
		SimpleProject targetProject = new SimpleProject(getTargetProjectName(project), monitor);
		return targetProject.createFolder(SharpenConstants.DEFAULT_TARGET_FOLDER, monitor);
	}
	
	public static String getTargetProjectName(IJavaProject project) {
		return project.getElementName() + SharpenConstants.SHARPENED_PROJECT_SUFFIX;
	}

	public static List<ICompilationUnit> collectCompilationUnits(IPackageFragmentRoot root) throws JavaModelException {
		List<ICompilationUnit> result = new ArrayList<ICompilationUnit>();
		collectCompilationUnits(result, root);
		return result;
	}

	public static void deleteTargetProject(IJavaProject javaProject) throws CoreException {
		IProject target = workspaceRoot().getProject(getTargetProjectName(javaProject));
		if (target.exists()) {
			target.close(null);
			target.delete(true, true, null);
		}
	}

	public static IWorkspaceRoot workspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}

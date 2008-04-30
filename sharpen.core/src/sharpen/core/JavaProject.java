/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

package sharpen.core;

import java.util.*;

import sharpen.core.resources.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.JavaRuntime;

public class JavaProject extends SimpleProject {
	
	private IJavaProject _javaProject;

	private final List<IPackageFragmentRoot> _sourceFolders = new ArrayList<IPackageFragmentRoot>();

	/**
	 * @throws CoreException
	 */
	public JavaProject() throws CoreException {
		this("TestProject");
	}
	
	public JavaProject(String projectName) throws CoreException {
		super(projectName);

		_javaProject = JavaCore.create(_project);
		setJavaNature();
		initializeClassPath();
		createOutputFolder(getBinFolder());
		addSystemLibraries();
	}

	private void initializeClassPath() throws JavaModelException {
		_javaProject.setRawClasspath(new IClasspathEntry[0], null);
	}
	
	public void addSourceFolder(String path) throws CoreException {
		_sourceFolders.add(createSourceFolder(path));
	}

	/**
	 * @throws CoreException
	 */
	public void buildProject() throws CoreException {
		_project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}

	/**
	 *  
	 */
	public void joinBuild() {
		try {
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
					null);
		} catch (InterruptedException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * @return Returns the javaProject.
	 */
	public IJavaProject getJavaProject() {
		return _javaProject;
	}
	
	public void addClasspathEntry(String absolutePath) throws JavaModelException {
		addClasspathEntry(new Path(absolutePath));
	}

	private void addClasspathEntry(IPath absolutePath) throws JavaModelException {
		IClasspathEntry newLibraryEntry = JavaCore.newLibraryEntry(absolutePath, null,
				null);
		addClasspathEntry(newLibraryEntry);
	}

	private void addClasspathEntry(IClasspathEntry newLibraryEntry) throws JavaModelException {
		IClasspathEntry[] oldEntries = _javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		
		newEntries[oldEntries.length] = newLibraryEntry;
		_javaProject.setRawClasspath(newEntries, null);
	}

	/**
	 * @param name
	 * @return @throws
	 *         CoreException
	 */
	public IPackageFragment createPackage(String name) throws CoreException {
		return getMainSourceFolder().createPackageFragment(name, false, null);
	}

	/**
	 * @throws CoreException
	 */
	public IPackageFragmentRoot getMainSourceFolder() throws CoreException {
		if (_sourceFolders.size() == 0) {
			_sourceFolders.add(createDefaultSourceFolder());
		}
		return _sourceFolders.get(0);
	}

	public ICompilationUnit createCompilationUnit(String packageName,
			String cuName, String source) throws CoreException {
		IPackageFragment packageFragment = getMainSourceFolder()
				.getPackageFragment(packageName);
		if (!packageFragment.exists()) {
			packageFragment = getMainSourceFolder().createPackageFragment(
					packageName, false, null);
		}
		return createCompilationUnit(packageFragment, cuName, source);
	}

	/**
	 * @param packageFragment
	 * @param cuName
	 * @param source
	 * @return @throws
	 *         JavaModelException
	 */
	public ICompilationUnit createCompilationUnit(
			IPackageFragment packageFragment, String cuName, String source)
			throws CoreException {

		return packageFragment.createCompilationUnit(
				cuName, source, false, null);
	}

	/**
	 * @return @throws
	 *         CoreException
	 */
	private IFolder getBinFolder() throws CoreException {
		return safeGetFolder("bin");
	}

	private IFolder safeGetFolder(final String folderName) throws CoreException {
		IFolder folder = _project.getFolder(folderName);
		return folder.exists()
			? folder
			: createFolder(folderName);
	}

	/**
	 * @throws CoreException
	 */
	private void setJavaNature() throws CoreException {
		addNature(JavaCore.NATURE_ID);
	}

	public void addNature(String natureId) throws CoreException {
		IProject project = _project;
		WorkspaceUtilities.addProjectNature(project, natureId);
	}

	/**
	 * @param binFolder
	 * @throws JavaModelException
	 */
	private void createOutputFolder(IFolder binFolder)
			throws JavaModelException {
		IPath outputLocation = binFolder.getFullPath();
		_javaProject.setOutputLocation(outputLocation, null);
	}

	/**
	 * @return @throws
	 *         CoreException
	 */
	private IPackageFragmentRoot createDefaultSourceFolder() throws CoreException {
		return createSourceFolder("src");
	}

	private IPackageFragmentRoot createSourceFolder(final String path) throws CoreException, JavaModelException {
		IFolder folder = safeGetFolder(path);
		IPackageFragmentRoot root = _javaProject.getPackageFragmentRoot(folder);
		IClasspathEntry newSourceEntry = JavaCore.newSourceEntry(root.getPath(),
				new IPath[] {});
		addClasspathEntry(newSourceEntry);
		return root;
	}

	/**
	 * @throws JavaModelException
	 */
	private void addSystemLibraries() throws JavaModelException {
		addClasspathEntry(JavaRuntime
						.getDefaultJREContainerEntry());
	}

	public List<ICompilationUnit> getAllCompilationUnits() throws CoreException {
		return JavaModelUtility.collectCompilationUnits(getJavaProject());
	}
}
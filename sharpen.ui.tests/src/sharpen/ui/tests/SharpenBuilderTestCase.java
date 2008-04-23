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

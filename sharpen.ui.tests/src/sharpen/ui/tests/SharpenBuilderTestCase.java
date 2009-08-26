/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import sharpen.builder.*;
import sharpen.core.*;
import sharpen.core.framework.resources.*;

public class SharpenBuilderTestCase extends AbstractConversionTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_project.addNature(SharpenNature.NATURE_ID);
	}
	
//	@Override
//	protected void tearDown() throws Exception {
//		waitForBuild(); // don't try to delete resources if the workspace is still building
//	    super.tearDown();
//	}
	
	public void testTargetFolderConfiguration() throws Throwable {
		
		SimpleProject targetProject = new SimpleProject("TargetProject");
		try {
			ISharpenProject sharpen = SharpenProject.create(_project.getProject());
			sharpen.setTargetProject(targetProject.getProject());
			
			TestCaseResource resource = addResourceAndWaitForBuild("EmptyClass");
			assertFile(resource, targetProject.getFile("src/EmptyClass.cs"));
		} finally {
			targetProject.dispose();
		}
	}	
	
	public void testConvertsNewFiles() throws Throwable {
		TestCaseResource resource1 = new TestCaseResource("builder/EmptyInterface");
		TestCaseResource resource2 = new TestCaseResource("builder/EmptyClass");
		createCompilationUnit(resource1);
		createCompilationUnit(resource2);
		waitForBuild();
		IProject convertedProject = getConvertedProject();
		try {
			assertConvertedFile(resource1, convertedProject);
			assertConvertedFile(resource2, convertedProject);
		} finally {
			delete(convertedProject);
		}
	}

	private IFile getConvertedFile(IProject convertedProject,
			TestCaseResource resource) {
		return convertedProject.getFolder(SharpenConstants.DEFAULT_TARGET_FOLDER).getFile(resource.packageName().replace('.', '/') + '/' + resource.targetSimpleName() + ".cs");
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

	private void waitForBuild() {
		_project.joinAutoBuild();
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

	private TestCaseResource addResourceAndWaitForBuild(String resourceName) throws CoreException,
			IOException {
		TestCaseResource resource = new TestCaseResource(resourceName);
		createCompilationUnit(resource);
		waitForBuild();
		return resource;
	}
	
}

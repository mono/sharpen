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

import junit.framework.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

import sharpen.core.*;
import sharpen.core.framework.resources.SimpleProject;
import sharpen.core.framework.resources.WorkspaceUtilities;

public abstract class AbstractConversionTestCase extends TestCase {

	protected JavaProject _project;

	protected void setUp() throws Exception {
		Sharpen.getDefault().configuration(configuration());
		_project = new JavaProject();		
	}

	protected void tearDown() throws Exception {
		_project.dispose();
	}
	
	protected Configuration configuration() {
		return ConfigurationFactory.defaultConfiguration();
	}

	/**
	 * @param resource
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */
	protected ICompilationUnit createCompilationUnit(TestCaseResource resource) throws CoreException, IOException {
		return _project.createCompilationUnit(resource.packageName(), resource.javaFileName(), resource.actualStringContents());
	}
	
	protected ICompilationUnit createCompilationUnit(IPackageFragmentRoot srcFolder, TestCaseResource resource) throws CoreException, IOException {
		return _project.createCompilationUnit(srcFolder, resource.packageName(), resource.javaFileName(), resource.actualStringContents());
	}
	
	protected void runResourceTestCase(String resourceName) throws Throwable {		
		runResourceTestCase(getConfiguration(), resourceName);
	}

	protected void runResourceTestCase(final Configuration configuration, String resourceName) throws CoreException, IOException {
		runResourceTestCase(configuration, resourceName, resourceName);
	}
	
	protected void runResourceTestCase(final Configuration configuration, String originalResourceName, String expectedResourceName) throws CoreException, IOException {
		TestCaseResource resource = new TestCaseResource(originalResourceName, expectedResourceName);		
		resource.assertExpectedContent(sharpenResource(configuration, resource));
	}

	protected String sharpenResource(final Configuration configuration,
			TestCaseResource resource) throws CoreException, IOException {
		ICompilationUnit cu = createCompilationUnit(resource);
	
		StandaloneConverter converter = new StandaloneConverter(configuration);
		converter.setSource(cu);
		converter.setTargetWriter(new StringWriter());
		converter.run();
		
		return converter.getTargetWriter().toString();
	}
	
	protected Configuration getConfiguration() {
		return newConfiguration();
	}

	private Configuration newConfiguration() {
		return ConfigurationFactory.defaultConfiguration();
	}

	protected Configuration newPascalCaseIdentifiersConfiguration() {
		Configuration configuration = newConfiguration();
		configuration.setNamingStrategy(new PascalCaseIdentifiersNamingStrategy());
		return configuration;
	}
	
	protected Configuration newPascalCasePlusConfiguration() {
		Configuration configuration = newConfiguration();
		configuration.setNamingStrategy(new PascalCaseNamingStrategy());
		return configuration;
	}

	protected void runBatchConverterTestCase(Configuration configuration, String... resourceNames) throws CoreException, IOException, Throwable {
		runBatchConverterTestCase(configuration, toTestCaseResources(resourceNames));
	}

	private TestCaseResource[] toTestCaseResources(String... resourceNames) {
		final TestCaseResource[] resources = new TestCaseResource[resourceNames.length];
		for (int i=0; i<resourceNames.length; ++i) {
			resources[i] = new TestCaseResource(resourceNames[i]);
		}
		return resources;
	}

	protected void runBatchConverterTestCase(Configuration configuration,
			TestCaseResource... resources) throws CoreException,
			IOException, Throwable {
		final SimpleProject targetProject = new SimpleProject("converted");
		try {
			runBatchConverterTestCaseWithTargetProject(targetProject, configuration, resources);
		} finally {
			targetProject.dispose();
		}
	}

	private void runBatchConverterTestCaseWithTargetProject(final SimpleProject targetProject,
            Configuration configuration, TestCaseResource... resources) throws CoreException, IOException, Throwable {
	    final ICompilationUnit[] units = createCompilationUnits(resources); 
		
		final SharpenConversionBatch converter = new SharpenConversionBatch(configuration);
		converter.setSource(units);
		converter.setTargetProject(targetProject.getProject());
		converter.run();
	
		for (int i=0; i<resources.length; ++i) { 
			final TestCaseResource resource = resources[i];
			if (resource.isSupportingLibrary()) {
				continue;
			}
			checkConversionResult(configuration, targetProject.getFolder("src"), units[i], resource);
		}
    }

	private ICompilationUnit[] createCompilationUnits(
			TestCaseResource... resources) throws CoreException, IOException {
		final ICompilationUnit[] units = new ICompilationUnit[resources.length];
		for (int i=0; i<resources.length; ++i) {		
			units[i] = createCompilationUnit(resources[i]);
		}
		return units;
	}

	/**
	 * @param configuration 
	 * @param targetFolder 
	 * @param cu
	 * @param resource
	 * @throws JavaModelException
	 * @throws IOException
	 * @throws Throwable
	 */
	private void checkConversionResult(Configuration configuration, IFolder targetFolder, ICompilationUnit cu, TestCaseResource resource) throws Throwable {
		String path = pathFromNamespace(configuration.mappedNamespace(getNamespace(cu)));
		IFile file = targetFolder.getFile(path + "/" + resource.targetSimpleName() + ".cs");
		assertFile(resource, file);
	}

	private String pathFromNamespace(String s) {
		return SharpenConversionBatch.cleanupNamespace(s).replace('.', '/');
	}

	private String getNamespace(ICompilationUnit cu) throws CoreException {
		IPackageDeclaration[] packages = cu.getPackageDeclarations();
		return packages.length > 0 ? packages[0].getElementName() : "";
	}

	protected void assertFile(TestCaseResource expectedResource, IFile actualFile) throws Throwable {
		expectedResource.assertFile(actualFile);
	}

	protected IProject getConvertedProject() {
		return getProject(_project.getName() + SharpenConstants.SHARPENED_PROJECT_SUFFIX);
	}

	IProject getProject(String name) {
		return WorkspaceUtilities.getProject(name);
	}

	protected void delete(IProject convertedProject) throws CoreException {
		convertedProject.delete(true, true, null);
	}
}

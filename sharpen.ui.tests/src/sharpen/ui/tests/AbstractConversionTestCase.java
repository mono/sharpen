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

import junit.framework.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

import sharpen.core.*;

public abstract class AbstractConversionTestCase extends TestCase {

	protected JavaProject _project;

	protected void setUp() throws Exception {
		_project = new JavaProject();
	}

	protected void tearDown() throws Exception {
		_project.dispose();
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
		return new Configuration("Sharpen.Runtime");
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
		TestCaseResource[] resources = new TestCaseResource[resourceNames.length];
		ICompilationUnit[] units = new ICompilationUnit[resourceNames.length];
		for (int i=0; i<resourceNames.length; ++i) {
			resources[i] = new TestCaseResource(resourceNames[i]);
			units[i] = createCompilationUnit(resources[i]);
		} 
		
		IFolder targetFolder = _project.createFolder("converted");
	
		SharpenConversionBatch converter = new SharpenConversionBatch(configuration);
		converter.setSource(units);
		converter.setTargetFolder(targetFolder);
		converter.run();
	
		for (int i=0; i<resourceNames.length; ++i) { 
			checkConversionResult(configuration, targetFolder, units[i], resources[i]);
		}
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
		IFile file = targetFolder.getFile(path + "/" + resource.getSimpleName() + ".cs");
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
}

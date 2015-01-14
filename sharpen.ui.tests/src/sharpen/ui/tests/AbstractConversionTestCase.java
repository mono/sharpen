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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Before;

import sharpen.core.*;

public abstract class AbstractConversionTestCase  {

	protected JavaProjectCmd _project;
	private String projectName="DPrj";
	//To Run from MAVEN
	protected String projecttempLocation = System.getProperty("user.dir") + "/sharpen.core/target/testcases";
	//To Run From Eclipse GUI
	//protected String projecttempLocation = System.getProperty("user.dir") + "/sharpen.ui.tests/testcases";
	@Before
	public void setUp() throws Exception {
		Sharpen.getDefault().configuration(configuration());
		_project = new JavaProjectCmd();		
	}
	@After
	public  void tearDown() throws Exception {
		_project.deleteProject();
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
	protected String createCompilationUnit(TestCaseResource resource) throws IOException {
		
		String sourcePackage= projecttempLocation +"/temp/" + projectName + "/src/" + resource.packageName().replace(".", "/");
		sourcePackage =sourcePackage.replace("\\", "/");
		File sourcePackagePath = new File(sourcePackage);
		if (!sourcePackagePath.exists())
		{
			sourcePackagePath.mkdirs();
		}
		return _project.createCompilationUnit(sourcePackage, resource.javaFileName(), resource.actualStringContents());
	}
	
	protected String createCompilationUnit(TestCaseResource resource, String targetProject) throws IOException  {
		
		String sourcePackage= projecttempLocation +"/temp/" + targetProject + "/src/" + resource.packageName().replace(".", "/");
		sourcePackage =sourcePackage.replace("\\", "/");
		File sourcePackagePath = new File(sourcePackage);
		if (!sourcePackagePath.exists())
		{
			sourcePackagePath.mkdirs();
		}
		return _project.createCompilationUnit(sourcePackage, resource.javaFileName(), resource.actualStringContents());
	}
	
	
	protected void runResourceTestCase(String resourceName) throws IOException, CoreException {
		runResourceTestCase(getConfiguration(), resourceName);
	}
	
	protected void runResourceTestCaseCMD(String resourceName) throws IOException {		
		runResourceTestCaseCMD(resourceName,resourceName);
	}

	protected void runResourceTestCase(final Configuration configuration, String resourceName) throws IOException, CoreException {
		runResourceTestCase(configuration, resourceName, resourceName);
	}
	
	protected void runResourceTestCase(final Configuration configuration, String originalResourceName, String expectedResourceName) throws IOException, CoreException {
		TestCaseResource resource = new TestCaseResource(originalResourceName, expectedResourceName);		
		resource.assertExpectedContent(sharpenResource(configuration, resource));
	}
	
	protected void runResourceTestCaseCMD(String originalResourceName, String expectedResourceName) throws IOException  {
		TestCaseResource resource = new TestCaseResource(originalResourceName, expectedResourceName);		
		resource.assertExpectedContent(sharpenResourceCMD(resource));
	}

	protected String sharpenResource(final Configuration configuration,
			TestCaseResource resource) throws IOException, CoreException {

			String cu = createCompilationUnit(resource);
			File cufile = new File(cu);


			String sourceFilePath =projecttempLocation +"/temp/" +projectName + "/src";
			String targetProject = projecttempLocation +"/temp/" +projectName + "/" +getConvertedProject();
			configuration.setSharpenNamespace("nonamespace");

			final SharpenConversionBatch converter = new SharpenConversionBatch(configuration);
			converter.setsourceFiles(new String[] { cu });
			converter.setsourcePathEntries(sourceFilePath);
			converter.setTargetProject(targetProject);
			converter.setclassPathEntries(_project.getclassPath());
			converter.run();

			String targetDir = resource.getTargetDir();
			if(targetDir.isEmpty()){
				targetDir ="src";
			}

			Path filePath = Paths.get(projecttempLocation, "temp", projectName, getConvertedProject(), targetDir, resource.targetSimpleName()+ ".cs");

			//	to reproduce old behaviour we will return empty string if file not exists
			if(!Files.exists(filePath)){
				return "";
			}

			 byte[] encoded = Files.readAllBytes(filePath);
			 return new String(encoded);
	}
	
	protected String sharpenResourceCMD(TestCaseResource resource)  {
		
		String result ="Success";
				
		try {
			String cu = createCompilationUnit(resource);			
			File cufile = new File(cu);
			
			result = result + cufile;
		
			String sourceFilePath =projecttempLocation +"/temp/" +projectName + "/src";
			

			SharpenApplication AppCmd = new SharpenApplication();
			
			String[] args = new String[3];
			
			args[0] =sourceFilePath;
			args[1] = "-sharpenNamespace";
			args[2] = "nonamespace";
			
			AppCmd.start(args);
			
			
			String packageName = resource.packageName();
			if(resource.packageName().isEmpty()){
				packageName ="src";
			}
			
			result= projecttempLocation +"/temp/" +
										projectName + "/" + 
			                            getConvertedProject() + "/" +
			                            packageName.replace(".", "/") + "/" +
					                    cufile.getName().substring(0,cufile.getName().lastIndexOf("."))
			                            + ".cs";
			
			 byte[] encoded = Files.readAllBytes(Paths.get(result));
			 return new String(encoded);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = result + e.toString();
		}
		return result;
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

	protected void runBatchConverterTestCase(Configuration configuration, String... resourceNames) throws IOException, Throwable {
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
			TestCaseResource... resources) throws 
			IOException, Throwable {
		
		try {
			runBatchConverterTestCaseWithTargetProject(configuration, resources);
		} finally {
			tearDown();
		}
	}

	private void runBatchConverterTestCaseWithTargetProject(
            Configuration configuration, TestCaseResource... resources) throws IOException, Throwable {
		String projectName="MultipleSource";
		String[] units;
		if(resources.length> 0){
			units = createCompilationUnits(projectName,resources); 
		}
		else {
			projectName = resources[0].targetSimpleName();
			units = createCompilationUnits(resources);
		}
			
		final String targetProject  = Paths.get(projecttempLocation, "temp", projectName,projectName + ".net").toString();
	    
	    configuration.setSharpenNamespace("nonamespace");
		final SharpenConversionBatch converter = new SharpenConversionBatch(configuration);
		converter.setsourceFiles(units);
		converter.setTargetProject(targetProject);
		converter.run();
	
		for (int i=0; i<resources.length; ++i) { 
			final TestCaseResource resource = resources[i];
			if (resource.isSupportingLibrary()) {
				continue;
			}
			checkConversionResult(configuration, targetProject, resource);
		}
    }

	private String[] createCompilationUnits(
			TestCaseResource... resources) throws IOException  {
		final String[] units = new String[resources.length];
		for (int i=0; i<resources.length; ++i) {
			units[i] = createCompilationUnit(resources[i]);
		}
		return units;
	}
	
	private String[] createCompilationUnits(String projectName,
			TestCaseResource... resources) throws IOException {
		final String[] units = new String[resources.length];
		for (int i=0; i<resources.length; ++i) {
			if(resources.length >1)
				units[i] = createCompilationUnit(resources[i],projectName);
			else
				units[i] = createCompilationUnit(resources[i],projectName);
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
	private void checkConversionResult(Configuration configuration, String targetFolder, TestCaseResource resource) throws Throwable {

		String targetDir = resource.getTargetDir();
		if(targetDir.isEmpty()){
			targetDir ="src";
		}

		String file = Paths.get(targetFolder, targetDir, resource.targetSimpleName()+ ".cs").toString();
		
		assertFile(resource, file);
	}

	protected void assertFile(TestCaseResource expectedResource, String actualFile) throws IOException {
		expectedResource.assertFile(actualFile);
	}

	protected String getConvertedProject() {
		return _project.getProjectName() + SharpenConstants.SHARPENED_PROJECT_SUFFIX;
	}

	
}

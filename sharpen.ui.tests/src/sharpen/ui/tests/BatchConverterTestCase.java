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

import java.io.IOException;

import sharpen.core.SharpenConversionBatch;

import org.junit.Test;


public class BatchConverterTestCase extends AbstractConversionTestCase {

	@Test
	public void testSingleClassEmptyPackage() throws Throwable {
		runBatchConverterTestCase("EmptyClass");
	}
	
  @Test
	public void testMultipleClassesEmptyPackage() throws Throwable {
		runBatchConverterTestCase("EmptyClass", "AnotherEmptyClass");
	}
	
	@Test
	public void testKeywordNamespaces() throws Throwable {
		runBatchConverterTestCase("namespaceMapping/out/event/Foo");
	}
	
	
	@Test
	public void testEventInterfaceAndClassInDifferentCompilationUnits() throws Throwable, IOException, Throwable {
		runBatchConverterPascalCaseTestCase("events/EventInterface", "events/EventInterfaceImpl");
	}
	
	

	private void runBatchConverterPascalCaseTestCase(String... resourceNames) throws IOException, Throwable {
		runBatchConverterTestCase(newPascalCaseIdentifiersConfiguration(), resourceNames);
	}
	
	private void runBatchConverterTestCase(String... resourceNames) throws  IOException, Throwable {
		runBatchConverterTestCase(getConfiguration(), resourceNames);
	}
	
	
	@Test
	public void testSingleClassSimplePackageAndTargetFolder() throws Throwable {

		runResourceTestCaseWithTargetProject("mp/Albatross");
	}
	
	@Test
	public void testSingleClassNestedPackageAndTargetFolder() throws Throwable {
		
		runResourceTestCaseWithTargetProject("mp/nested/Parrot");
		
	}

	private void runResourceTestCaseWithTargetProject(String path)
			throws Throwable {

		TestCaseResource resource = new TestCaseResource(path);
		String cu = createCompilationUnit(resource,"TargetProject");
		
		String targetProject= projecttempLocation +"/temp/" +
				"TargetProject/TargetProject.net"; 

		String targetFolder =  projecttempLocation +"/temp/" +
				"TargetProject/src";

		try {
			SharpenConversionBatch converter = new SharpenConversionBatch(getConfiguration());
			converter.setsourceFiles(new String[] {cu});
			converter.setTargetProject(targetProject);
			converter.setsourcePathEntries(targetFolder);
			converter.getConfiguration().setSharpenNamespace("nonamespace");
			converter.run();

			assertFile(resource, targetProject + "/"+ path + ".cs");

		} finally {
			tearDown();
		}

	}

}
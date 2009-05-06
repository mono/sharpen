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

import sharpen.core.*;
import sharpen.core.io.IO;
import junit.framework.TestCase;

public class SharpenCommandLineTestCase extends TestCase {
	
	public void testDefaults() {
		SharpenCommandLine cmdLine = parse("core/src");		
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
		assertEquals(0, cmdLine.classpath.size());
		assertEquals(0, cmdLine.namespaceMappings.size());
		assertSame(NamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals(false, cmdLine.nativeTypeSystem);
		assertEquals("Sharpen.Runtime", cmdLine.runtimeTypeName);
		assertNull(cmdLine.headerFile);
		assertEquals(0, cmdLine.eventMappings.size());
		assertEquals(0, cmdLine.eventAddMappings.size());
	}
	
	public void testEventMappings() {
		SharpenCommandLine cmdLine = parse("-eventAddMapping", "foo.bar", "-eventMapping", "foo", "bar", "core/src");
		assertEquals("core", cmdLine.project);
		assertEquals(1, cmdLine.eventMappings.size());
		assertEquals(1, cmdLine.eventAddMappings.size());
	}
	
	public void testNativeInterfaces() {
		SharpenCommandLine cmdLine = parse("-nativeInterfaces", "core/src");
		assertTrue(cmdLine.nativeInterfaces);
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
	}

	public void testManageUsings() {
		SharpenCommandLine cmdLine = parse("-organizeUsings", "core/src");
		assertTrue(cmdLine.organizeUsings);
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));		
	}
	
	public void testNameConflicts() {
		SharpenCommandLine cmdLine = parse("-fullyQualify", "File", "core/src");
		assertTrue(cmdLine.fullyQualifiedTypes.contains("File"));
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
	}
	
	public void testPascalCase() {
		SharpenCommandLine cmdLine = parse("-pascalCase", "core/src");
		assertSame(PascalCaseIdentifiersNamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
	}
	
	public void testClasspath() {
		SharpenCommandLine cmdLine = parse("foo/bar", "-cp", "../foo.jar");
		assertEquals(NamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals("foo", cmdLine.project);
		assertEquals("bar", cmdLine.sourceFolders.get(0));
		assertEquals(1, cmdLine.classpath.size());
		assertEquals("../foo.jar", cmdLine.classpath.get(0));
	}
	
	public void testSourceFolders() {
		SharpenCommandLine cmdLine = parse("foo", "-srcFolder", "bar", "-srcFolder", "baz");
		assertEquals(NamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals("foo", cmdLine.project);
		assertEquals(0, cmdLine.classpath.size());
		assertEquals(2, cmdLine.sourceFolders.size());
		assertEquals("bar", cmdLine.sourceFolders.get(0));
		assertEquals("baz", cmdLine.sourceFolders.get(1));
	}
	
	public void testNativeTypeSystem() {
		SharpenCommandLine cmdLine = parse("foo", "-nativeTypeSystem");
		assertEquals("foo", cmdLine.project);
		assertEquals(true, cmdLine.nativeTypeSystem);
	}
	
	public void testNamespaceMappings() {
		SharpenCommandLine cmdLine = parse("foo", "-namespaceMapping", "^from", "to", "-namespaceMapping", "anotherFrom", "anotherTo");
		assertEquals("foo", cmdLine.project);
		assertEquals(2, cmdLine.namespaceMappings.size());
		assertEquals(new Configuration.NameMapping("^from", "to"), cmdLine.namespaceMappings.get(0));
		assertEquals(new Configuration.NameMapping("anotherFrom", "anotherTo"), cmdLine.namespaceMappings.get(1));
	}
	
	public void testMethodMappings() {
		SharpenCommandLine cmdLine = parse("foo", "-methodMapping", "Foo.bar", "Foo.baz");
		assertEquals("foo", cmdLine.project);
		assertEquals(1, cmdLine.memberMappings.size());
		assertEquals(new Configuration.MemberMapping("Foo.baz", sharpen.core.MemberKind.Method), cmdLine.memberMappings.get("Foo.bar"));
	}
	
	public void testResponseFile() throws Exception {
		String fname = createTempFileFromResource("resources/options");
		SharpenCommandLine cmdLine = parse("foo", "@" + fname);
		assertEquals("foo", cmdLine.project);
		assertEquals(1, cmdLine.memberMappings.size());
		assertEquals(new Configuration.MemberMapping("Foo.bar", sharpen.core.MemberKind.Method), cmdLine.memberMappings.get("Foo.foo"));
		assertEquals(1, cmdLine.namespaceMappings.size());
		assertEquals(new Configuration.NameMapping("spam", "eggs"), cmdLine.namespaceMappings.get(0));
	}
	
	public void testPascalCasePlus() throws Exception {
		final SharpenCommandLine cmdLine = parse("foo", "-pascalCase+");
		assertEquals("foo", cmdLine.project);
		assertSame(PascalCaseNamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
	}
	
	public void testRuntimeTypeName() throws Exception {
		final SharpenCommandLine cmdLine = parse("foo", "-runtimeTypeName", "Foo.Bar");
		assertEquals("Foo.Bar", cmdLine.runtimeTypeName);
	}
	
	public void testHeader() throws Exception {
		final SharpenCommandLine cmdLine = parse("foo", "-header", "header.txt");
		assertEquals("header.txt", cmdLine.headerFile);
	}
	
	public void testXmlDoc() throws Exception {
		final SharpenCommandLine cmdLine = parse("foo", "-xmldoc", "foo.xml");
		assertEquals("foo.xml", cmdLine.xmldoc);
	}
	
	public void testConditionalCompilation() {
		final SharpenCommandLine cmdLine = parse("fooSourceFolder", "-conditionalCompilation", "package.name", "IAMRICH");
		assertEquals("IAMRICH", cmdLine.conditionalCompilation.get("package.name"));
	}
	
	private String createTempFileFromResource(String resourceName) throws Exception {
		File temp = java.io.File.createTempFile("sharpen", null);
		IO.writeFile(temp, ResourceUtility.getStringContents(resourceName));
		return temp.getAbsolutePath();
	}

	private SharpenCommandLine parse(String ...args) {
		SharpenCommandLine cmdLine = SharpenCommandLine.parse(args);
		assertNotNull(cmdLine);
		return cmdLine;
	}
}

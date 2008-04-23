package sharpen.ui.tests;

import java.io.*;

import sharpen.core.*;
import sharpen.core.io.IO;
import junit.framework.TestCase;

public class JavaToCSharpCommandLineTestCase extends TestCase {
	
	public void testDefaults() {
		JavaToCSharpCommandLine cmdLine = parse("core/src");		
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
		assertEquals(0, cmdLine.classpath.size());
		assertEquals(0, cmdLine.namespaceMappings.size());
		assertSame(NamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals(false, cmdLine.nativeTypeSystem);
		assertEquals("Sharpen.Runtime", cmdLine.runtimeTypeName);
		assertNull(cmdLine.headerFile);
	}
	
	public void testNativeInterfaces() {
		JavaToCSharpCommandLine cmdLine = parse("-nativeInterfaces", "core/src");
		assertTrue(cmdLine.nativeInterfaces);
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
	}

	public void testManageUsings() {
		JavaToCSharpCommandLine cmdLine = parse("-organizeUsings", "core/src");
		assertTrue(cmdLine.organizeUsings);
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));		
	}
	
	public void testNameConflicts() {
		JavaToCSharpCommandLine cmdLine = parse("-fullyQualify", "File", "core/src");
		assertTrue(cmdLine.fullyQualifiedTypes.contains("File"));
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
	}
	
	public void testPascalCase() {
		JavaToCSharpCommandLine cmdLine = parse("-pascalCase", "core/src");
		assertSame(PascalCaseIdentifiersNamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals("core", cmdLine.project);
		assertEquals("src", cmdLine.sourceFolders.get(0));
	}
	
	public void testClasspath() {
		JavaToCSharpCommandLine cmdLine = parse("foo/bar", "-cp", "../foo.jar");
		assertEquals(NamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals("foo", cmdLine.project);
		assertEquals("bar", cmdLine.sourceFolders.get(0));
		assertEquals(1, cmdLine.classpath.size());
		assertEquals("../foo.jar", cmdLine.classpath.get(0));
	}
	
	public void testSourceFolders() {
		JavaToCSharpCommandLine cmdLine = parse("foo", "-srcfolder", "bar", "-srcfolder", "baz");
		assertEquals(NamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
		assertEquals("foo", cmdLine.project);
		assertEquals(0, cmdLine.classpath.size());
		assertEquals(2, cmdLine.sourceFolders.size());
		assertEquals("bar", cmdLine.sourceFolders.get(0));
		assertEquals("baz", cmdLine.sourceFolders.get(1));
	}
	
	public void testNativeTypeSystem() {
		JavaToCSharpCommandLine cmdLine = parse("foo", "-nativeTypeSystem");
		assertEquals("foo", cmdLine.project);
		assertEquals(true, cmdLine.nativeTypeSystem);
	}
	
	public void testNamespaceMappings() {
		JavaToCSharpCommandLine cmdLine = parse("foo", "-namespaceMapping", "^from", "to", "-namespaceMapping", "anotherFrom", "anotherTo");
		assertEquals("foo", cmdLine.project);
		assertEquals(2, cmdLine.namespaceMappings.size());
		assertEquals(new Configuration.NameMapping("^from", "to"), cmdLine.namespaceMappings.get(0));
		assertEquals(new Configuration.NameMapping("anotherFrom", "anotherTo"), cmdLine.namespaceMappings.get(1));
	}
	
	public void testMethodMappings() {
		JavaToCSharpCommandLine cmdLine = parse("foo", "-methodMapping", "Foo.bar", "Foo.baz");
		assertEquals("foo", cmdLine.project);
		assertEquals(1, cmdLine.memberMappings.size());
		assertEquals(new Configuration.MemberMapping("Foo.baz", sharpen.core.MemberKind.Method), cmdLine.memberMappings.get("Foo.bar"));
	}
	
	public void testResponseFile() throws Exception {
		String fname = createTempFileFromResource("resources/options");
		JavaToCSharpCommandLine cmdLine = parse("foo", "@" + fname);
		assertEquals("foo", cmdLine.project);
		assertEquals(1, cmdLine.memberMappings.size());
		assertEquals(new Configuration.MemberMapping("Foo.bar", sharpen.core.MemberKind.Method), cmdLine.memberMappings.get("Foo.foo"));
		assertEquals(1, cmdLine.namespaceMappings.size());
		assertEquals(new Configuration.NameMapping("spam", "eggs"), cmdLine.namespaceMappings.get(0));
	}
	
	public void testPascalCasePlus() throws Exception {
		final JavaToCSharpCommandLine cmdLine = parse("foo", "-pascalCase+");
		assertEquals("foo", cmdLine.project);
		assertSame(PascalCaseNamingStrategy.DEFAULT, cmdLine.pascalCase.getNamingStrategy());
	}
	
	public void testRuntimeTypeName() throws Exception {
		final JavaToCSharpCommandLine cmdLine = parse("foo", "-runtimeTypeName", "Foo.Bar");
		assertEquals("Foo.Bar", cmdLine.runtimeTypeName);
	}
	
	public void testHeader() throws Exception {
		final JavaToCSharpCommandLine cmdLine = parse("foo", "-header", "header.txt");
		assertEquals("header.txt", cmdLine.headerFile);
	}
	
	public void testXmlDoc() throws Exception {
		final JavaToCSharpCommandLine cmdLine = parse("foo", "-xmldoc", "foo.xml");
		assertEquals("foo.xml", cmdLine.xmldoc);
	}
	
	private String createTempFileFromResource(String resourceName) throws Exception {
		File temp = java.io.File.createTempFile("sharpen", null);
		IO.writeFile(temp, ResourceUtility.getStringContents(resourceName));
		return temp.getAbsolutePath();
	}

	private JavaToCSharpCommandLine parse(String ...args) {
		JavaToCSharpCommandLine cmdLine = JavaToCSharpCommandLine.parse(args);
		assertNotNull(cmdLine);
		return cmdLine;
	}
}

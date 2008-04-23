package sharpen.ui.tests;

import sharpen.core.Bindings;
import sharpen.core.JavaProject;
import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class BindingTestCase extends TestCase {
	
	JavaProject _project;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_project = new JavaProject();
	}
	
	@Override
	protected void tearDown() throws Exception {
		_project.dispose();
		super.tearDown();
	}

	public void testSimpleMethod() throws Exception {
		String source = "public class Bar {\n" +
							"public void foo() {}" + 
						"}";
		CompilationUnit ast = createAST(source);
        TypeDeclaration bar = getType(ast, "Bar");
        MethodDeclaration foo = bar.getMethods()[0];
        assertNull(findMethodDefinition(foo));
	}

	private TypeDeclaration getType(CompilationUnit ast, String typeName) {		
		for (Object item : ast.types()) {
			TypeDeclaration type = ((TypeDeclaration)item);
			if (type.getName().toString().equals(typeName)) {
				return type;
			}
		}
		return null;
	}
	
	public void testMethodInInterface() throws Exception {
		String source = "interface Foo { void bar(); }" +
			"class Bar implements Foo { public void bar() {} }";
		CompilationUnit ast = createAST(source);
		MethodDeclaration fooBar = getType(ast, "Foo").getMethods()[0];
		MethodDeclaration barBar = getType(ast, "Bar").getMethods()[0];
		assertMethodDefinition(fooBar, barBar);
	}

	private void assertMethodDefinition(MethodDeclaration expected, MethodDeclaration subject) {
		assertSame(expected.resolveBinding(), findMethodDefinition(subject));
	}
	
	private Object findMethodDefinition(MethodDeclaration subject) {
		return Bindings.findMethodDefininition(subject.resolveBinding(), null);
	}

	public void testMethodInExternalInterface() throws Exception {
		String jar = JarUtilities.createJar(BindingTestCaseSubject.class, BindingTestCaseSubject.Foo.class, BindingTestCaseSubject.Baz.class);
		_project.addClasspathEntry(jar);
		
		String source = "class Gazonk {" +
			"public void bar(sharpen.ui.tests.BindingTestCaseSubject.Baz b) { b.bar(); }" +
			"}";
		CompilationUnit ast = createAST(source);
		MethodDeclaration method = getType(ast, "Gazonk").getMethods()[0];
		MethodInvocation invocation = getFirstMethodInvocation(method);
		IMethodBinding definition = Bindings.findMethodDefininition(invocation.resolveMethodBinding(), null);
		assertEquals("sharpen.ui.tests.BindingTestCaseSubject.Foo.bar", Bindings.qualifiedName(definition));
	}

	private MethodInvocation getFirstMethodInvocation(MethodDeclaration method) {
		ExpressionStatement stmt = (ExpressionStatement) method.getBody().statements().get(0);
		MethodInvocation invocation = (MethodInvocation) stmt.getExpression();
		return invocation;
	}

	public void testMethodInInterfaceWithIntermediateClass() throws Exception {
		String source = "interface Foo { void bar(); }" +
			"class Intermediate implements Foo { public void bar() {} }" +
			"class Bar extends Intermediate { public void bar() {} }";
		CompilationUnit ast = createAST(source);
		MethodDeclaration fooBar = getType(ast, "Foo").getMethods()[0];
		
		MethodDeclaration barBar = getType(ast, "Bar").getMethods()[0];
		assertMethodDefinition(fooBar, barBar);
	}

	private CompilationUnit createAST(String source) throws CoreException {
		ICompilationUnit cu = _project.createCompilationUnit("", "Bar.java", source);
		CompilationUnit ast = createAST(cu);
		assertEquals(toString(ast.getMessages()), 0, ast.getMessages().length);
		return ast;
	}

	private String toString(Message[] messages) {
		if (messages.length == 0) return "";
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < messages.length; i++) {			
			builder.append(messages[i].getMessage());
			builder.append("\n");
		}
		return builder.toString();
	}

	private CompilationUnit createAST(ICompilationUnit cu) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		
		parser.setSource(cu);
		parser.setResolveBindings(true);
        
        CompilationUnit ast = (CompilationUnit) parser.createAST(null);
		return ast;
	}
}

/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

public class InnerClassesTestCase extends AbstractConverterTestCase {
	
	public void testNestedThis() throws Throwable {
		runResourceTestCase("NestedThis");
	}
	
	public void testNestedClass() throws Throwable {
		runResourceTestCase("NestedClass1");
	}
	
	public void testNestedClassWithConstructor() throws Throwable {
		runResourceTestCase("NestedClass2");
	}
	
	public void testNestedStaticClass() throws Throwable {
		runResourceTestCase("NestedStaticClass1");
	}
	
	public void testAnonymousWithFinalField() throws Throwable {
		runResourceTestCase("AnonymousWithFinalField");
	}
	
	public void testAnonymousThis() throws Throwable {
		runResourceTestCase("AnonymousInnerClass12");
	}
	
	public void testAnonymousTypeWithCtorArguments() throws Throwable {
		runResourceTestCase("AnonymousInnerClass13");
	}
	
	public void testAnonymousInnerAsStaticFieldInitializer() throws Throwable {
		runResourceTestCase("AnonymousInnerClass11");
	}
	
	public void testAnonymousInnerWithBaseClass() throws Throwable {
		runResourceTestCase("AnonymousInnerClass10");
	}
	
	public void testNestedAnonymousClassesWithLocals() throws Throwable {
		runResourceTestCase("AnonymousInnerClass9");
	}
	
	public void testAnonymousInnerClassAccessingInheritedMethod() throws Throwable {
		runResourceTestCase("AnonymousInnerClass8");
	}
	
	public void testAnonymousInnerClassWithParameters() throws Throwable {
		runResourceTestCase("AnonymousInnerClass7");
	}
	
	public void testAnonymousInnerClassInStaticMethod() throws Throwable {
		runResourceTestCase("AnonymousInnerClass6");
	}
	
	public void testAnonymousInnerCapturingLocals() throws Throwable {
		runResourceTestCase("AnonymousInnerClass5");
	}
	
	public void testAnonymousInnerClassMethodWithLocal() throws Throwable {
		runResourceTestCase("AnonymousInnerClass4");
	}
	
	public void testNestedAnonymousInnerClassesAccessingFields() throws Throwable {
		runResourceTestCase("AnonymousInnerClass3");
	}
	
	public void testNestedAnonymousInnerClasses() throws Throwable {
		runResourceTestCase("AnonymousInnerClass2");
	}
	
	public void testAnonymousInnerClassImplementingInterface() throws Throwable {
		runResourceTestCase("AnonymousInnerClass1");
	}
	
	@Override
	protected void runResourceTestCase(String resourceName) throws Throwable {
		super.runResourceTestCase("innerclasses/" + resourceName);
	}

}

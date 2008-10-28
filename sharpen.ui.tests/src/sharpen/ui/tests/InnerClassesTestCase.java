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

/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

public class InnerClassesTestCase extends AbstractConversionTestCase {
	
	public void testInnerClassInFieldInitializer() throws Throwable {
		runResourceTestCase("InnerClassInFieldInitializer");
	}
	
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

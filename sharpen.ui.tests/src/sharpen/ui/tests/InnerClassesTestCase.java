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

/* Copyright (C) 2004 - 2006 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public class InnerClassesTestCase extends AbstractConversionTestCase {
	
	@Test
	public void testInnerClassInFieldInitializer() throws Throwable {
		runResourceTestCase("InnerClassInFieldInitializer");
	}
	
	@Test
	public void testNestedThis() throws Throwable {
		runResourceTestCase("NestedThis");
	}
	
	@Test
	public void testNestedClass() throws Throwable {
		runResourceTestCase("NestedClass1");
	}
	
	@Test
	public void testNestedClassWithConstructor() throws Throwable {
		runResourceTestCase("NestedClass2");
	}
	
	@Test
	public void testNestedStaticClass() throws Throwable {
		runResourceTestCase("NestedStaticClass1");
	}
	
	@Test
	public void testAnonymousWithFinalField() throws Throwable {
		runResourceTestCase("AnonymousWithFinalField");
	}
	
	// SHA-75
	@Test
	public void _testGenericAnonymousWithPrivateMethod() throws Throwable {
		runResourceTestCase("GenericAnonymousWithAdditionalMethods");
	}
	
	@Test
	public void testAnonymousThis() throws Throwable {
		runResourceTestCase("AnonymousInnerClass12");
	}
	@Test
	public void testAnonymousTypeWithCtorArguments() throws Throwable {
		runResourceTestCase("AnonymousInnerClass13");
	}
	
	@Test
	public void testAnonymousInnerAsStaticFieldInitializer() throws Throwable {
		runResourceTestCase("AnonymousInnerClass11");
	}
	
	@Test
	public void testAnonymousInnerWithBaseClass() throws Throwable {
		runResourceTestCase("AnonymousInnerClass10");
	}
	
	@Test
	public void testNestedAnonymousClassesWithLocals() throws Throwable {
		runResourceTestCase("AnonymousInnerClass9");
	}
	
	@Test
	public void testAnonymousInnerClassAccessingInheritedMethod() throws Throwable {
		runResourceTestCase("AnonymousInnerClass8");
	}
	
	@Test
	public void testAnonymousInnerClassWithParameters() throws Throwable {
		runResourceTestCase("AnonymousInnerClass7");
	}
	
	@Test
	public void testAnonymousInnerClassInStaticMethod() throws Throwable {
		runResourceTestCase("AnonymousInnerClass6");
	}
	
	@Test
	public void testAnonymousInnerCapturingLocals() throws Throwable {
		runResourceTestCase("AnonymousInnerClass5");
	}
	
	@Test
	public void testAnonymousInnerClassMethodWithLocal() throws Throwable {
		runResourceTestCase("AnonymousInnerClass4");
	}
	
	@Test
	public void testNestedAnonymousInnerClassesAccessingFields() throws Throwable {
		runResourceTestCase("AnonymousInnerClass3");
	}
	
	@Test
	public void testNestedAnonymousInnerClasses() throws Throwable {
		runResourceTestCase("AnonymousInnerClass2");
	}
	
	@Test
	public void testAnonymousInnerClassImplementingInterface() throws Throwable {
		runResourceTestCase("AnonymousInnerClass1");
	}
	
	@Override
	protected void runResourceTestCase(String resourceName) throws IOException, CoreException {
		super.runResourceTestCase("innerclasses/" + resourceName);
	}

}

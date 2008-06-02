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

import sharpen.core.Configuration;

public class OrganizeUsingsTestCase extends AbstractConversionTestCase {
	
	public void testGenerics() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/Generics");
	}
	
	public void testSimpleUsing() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/SimpleTest");
	}

	public void testFullyQualifiedName() throws Throwable {
		Configuration conf = newOrganizeUsingsConfiguration();
		conf.addFullyQualifiedTypeName("Test");
		runResourceTestCase(conf, "usings/FullyQualifiedType");
	}

	public void testNestedStaticType() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/deep/tree/InnerStaticClass");
	}
	
	public void testNestedType() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/deep/tree/NestedType");
	}

	public void testNamespaceConflict() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/NamespaceConflict");
	}
	
	public void testMethodNameConflict() throws Throwable {
		runResourceTestCase(newOrganizeUsingsConfiguration(), "usings/MethodNameConflict");
	}	
	
	public Configuration newOrganizeUsingsConfiguration() {
		Configuration configuration = newPascalCasePlusConfiguration();
		configuration.enableNativeTypeSystem();
		configuration.enableOrganizeUsings();
		return configuration;
	}
}

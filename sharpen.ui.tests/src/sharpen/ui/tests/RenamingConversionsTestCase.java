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

public class RenamingConversionsTestCase extends AbstractConversionTestCase {

	public void testRenamingMethods() throws Throwable {
		runResourceTestCase("renaming/SimpleMethodRenaming");
	}
	
	public void testAllUppercase() throws Throwable {
		runResourceTestCase(newPascalCasePlusConfiguration(), "renaming/AllUpperCaseIdentifiers");
	}
	
	public void testRenamingTypes() throws Throwable {
		// TODO: explicitly configuring the renaming should be unnecessary
		// just like it is with method or interface names
		final Configuration configuration = getConfiguration();
		configuration.mapType("renaming.TypeRenaming", "renaming.Renamed");
		runResourceTestCase(configuration, "renaming/TypeRenaming", "renaming/Renamed");
	}
}

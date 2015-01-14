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

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public class CollectionConversionTestCase extends AbstractConversionTestCase {
	
	@Test
	public void testComparatorSort() throws Throwable {
		runResourceTestCase(newPascalCasePlusConfiguration(), resourcePath("ComparatorSort"));
	}
	
	@Test
	public void testSet() throws Throwable {
		runResourceTestCase("Set");
	}
	
	@Test
	public void testMap1() throws Throwable {
		runResourceTestCase("Map1");
	}
	
	@Test
	public void testList1() throws Throwable {
		runResourceTestCase("List1");
	}
	
	@Test
	public void testEntrySet() throws Throwable {
		runResourceTestCase("EntrySet1");
	}
	
	@Test
	public void testIterator() throws Throwable {
		runResourceTestCase("Iterator1");
	}
	
	@Test
	public void testVector() throws Throwable {
		runResourceTestCase("Vector1");
	}
	
	@Test
	public void testHashtable() throws Throwable {
		runResourceTestCase("Hashtable1");
	}
	
	@Test
	public void testCollectionToArray() throws Throwable {
		runResourceTestCase("CollectionToArray");
	}
	
	@Override
	protected void runResourceTestCase(String resourceName) throws IOException, CoreException {
		super.runResourceTestCase(resourcePath(resourceName));
	}

	private String resourcePath(String resourceName) {
		return "collections/" + resourceName;
	}

}

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

import static org.junit.Assert.*;
import sharpen.core.*;

import org.junit.*;

public class ConfigurationTestCase {
	private Configuration _configuration;
	
  @Before
	public void setUp() {
		_configuration = ConfigurationFactory.defaultConfiguration();
	}
	
	@Test
	public void testIgnoredAnnotationsByDefault() {
		
		assertTrue(_configuration.isIgnoredAnnotation("java.lang.Override"));
	}
	
	@Test
	public void testDefaultMappings() {
		assertMappedTypeName("javanese.Foo", "javanese.Foo");
	}

	@Test
	public void testMapNamespace() {		
		_configuration.mapNamespace("^foo\\.bar", "System.IO");		
		assertMappedTypeName("foo.bar.Writer", "System.IO.Writer");
		
		assertEquals("System.IO", _configuration.mappedNamespace("foo.bar"));
	}
	
	@Test
	public void testPascalCaseNamespaces() {
		_configuration.setNamingStrategy(PascalCaseNamingStrategy.DEFAULT);
		
		assertMappedTypeName("foo.bar.Writer", "Foo.Bar.Writer");
	}

	private void assertMappedTypeName(String typeName, String expected) {
		assertEquals(expected, mappedTypeName(typeName));
	}

	private String mappedTypeName(String typeName) {
		return _configuration.mappedTypeName(typeName, typeName);
	}
}

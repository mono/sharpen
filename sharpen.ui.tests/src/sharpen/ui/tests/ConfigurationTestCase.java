package sharpen.ui.tests;

import sharpen.core.*;
import junit.framework.TestCase;

public class ConfigurationTestCase extends TestCase {
	private Configuration _configuration;
	
	public void setUp() {
		_configuration = new Configuration("Sharpen.Runtime");
	}
	
	public void testDefaultMappings() {
		assertMappedTypeName("javanese.Foo", "javanese.Foo");
	}

	public void testMapNamespace() {		
		_configuration.mapNamespace("^foo\\.bar", "System.IO");		
		assertMappedTypeName("foo.bar.Writer", "System.IO.Writer");
		
		assertEquals("System.IO", _configuration.mappedNamespace("foo.bar"));
	}
	
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

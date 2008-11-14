package sharpen.ui.tests;

import sharpen.core.*;

public class MappingsTestCase extends AbstractConversionTestCase {
	
	public void testRemovedConstructor() throws Throwable {
		runResourceTestCase("mappings/RemovedConstructor");
	}
	
	@Override
	protected Configuration getConfiguration() {
	    final Configuration config = super.getConfiguration();
	    config.mapMethod("mappings.Foo.Foo", "");
		return config;
	}

}

package sharpen.ui.tests;

import sharpen.core.*;

public class AnnotationsTestCase extends AbstractConversionTestCase {

	public void testSimpleAnnotation() throws Throwable {
		runResourceTestCase("SimpleAnnotation");
	}
	
	public void testCompilerAnnotations() throws Throwable {
		runResourceTestCase("CompilerAnnotations");
	}
	
	@Override
	protected void runResourceTestCase(String resourceName) throws Throwable {
		super.runResourceTestCase("annotations/" + resourceName);
	}
	
	@Override
	protected Configuration getConfiguration() {
	    final Configuration config = super.getConfiguration();
	    config.enableNativeInterfaces();
		return config;
	}
}

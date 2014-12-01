package sharpen.ui.tests;

import java.io.IOException;

import org.junit.Test;
import sharpen.core.*;


public class AnnotationsTestCase extends AbstractConversionTestCase {

	@Test
	public void testSimpleAnnotation() throws IOException  {
		runResourceTestCase("SimpleAnnotation");
	}
	
	@Test
	public void testCompilerAnnotations() throws IOException  {
		runResourceTestCase("CompilerAnnotations");
	}
	
	@Override
	protected void runResourceTestCase(String resourceName) throws IOException  {
		super.runResourceTestCase("annotations/" + resourceName);
	}
	
	@Override
	protected Configuration getConfiguration() {
	    final Configuration config = super.getConfiguration();
	    config.enableNativeInterfaces();
		return config;
	}
}

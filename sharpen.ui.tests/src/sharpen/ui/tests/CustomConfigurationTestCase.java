/* Copyright (C) 2010 Versant Inc. http://www.db4o.com */

package sharpen.ui.tests;

import junit.framework.*;
import sharpen.core.*;

public class CustomConfigurationTestCase extends AbstractConversionTestCase {
	
	@Override
	protected Configuration configuration() {
		return new CustomConfiguration(ConfigurationFactory.DEFAULT_RUNTIME_TYPE_NAME);
	}
	
	private static class CustomConfiguration extends Configuration {

		public CustomConfiguration(String runtimeTypeName) {
			super(runtimeTypeName);
		}

		@Override
		public boolean isIgnoredExceptionType(String exceptionType) {
			return false;
		}
		
		@Override
		public boolean mapByteToSbyte() {
			return false;
		}
	}
	
	public void test() {
		Assert.assertSame(CustomConfiguration.class, Sharpen.getDefault().configuration().getClass());
	}
}

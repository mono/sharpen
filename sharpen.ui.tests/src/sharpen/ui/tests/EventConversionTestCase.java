package sharpen.ui.tests;

import sharpen.core.*;

public class EventConversionTestCase extends AbstractConverterTestCase {

	public void testInterfaceWithEvents() throws Throwable {
		runResourceTestCase("events/EventInterface");
	}
	
	public void testClassImplementingInterfaceWithEvents() throws Throwable {
		runResourceTestCase("events/EventInClassInterface");
	}
	
	public void testEventConsumers() throws Throwable {
		runResourceTestCase("events/EventConsumer");
	}
	
	@Override
	protected Configuration getConfiguration() {
		return newPascalCaseIdentifiersConfiguration();
	}	
}

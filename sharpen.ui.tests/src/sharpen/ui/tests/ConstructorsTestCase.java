package sharpen.ui.tests;

public class ConstructorsTestCase extends AbstractConversionTestCase {
	
	public void testConstructorDeclaration() throws Throwable {
		runResourceTestCase("Constructors1");
	}
	
	public void testConstructorInvocationFromConstructor() throws Throwable {
		runResourceTestCase("Constructors2");
	}
	
	public void testInitializers() throws Throwable {
		runResourceTestCase("Initializers");
	}
}

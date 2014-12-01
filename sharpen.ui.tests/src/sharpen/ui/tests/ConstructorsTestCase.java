package sharpen.ui.tests;
import org.junit.Test;

public class ConstructorsTestCase extends AbstractConversionTestCase {
	@Test
	public void testConstructorDeclaration() throws Throwable {
		runResourceTestCase("Constructors1");
	}
	
	@Test
	public void testConstructorInvocationFromConstructor() throws Throwable {
		runResourceTestCase("Constructors2");
	}
	
	@Test
	public void testInitializers() throws Throwable {
		runResourceTestCase("Initializers");
	}
}

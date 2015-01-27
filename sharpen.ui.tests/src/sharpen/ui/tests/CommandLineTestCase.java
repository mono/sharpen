package sharpen.ui.tests;

import java.io.IOException;

import org.junit.Test;

public class CommandLineTestCase extends AbstractConversionTestCase {

	@Test
	public void testSimpleAnnotation() throws IOException  {
		runResourceTestCaseCMD("annotations/"+ "SimpleAnnotation");
	}
	
	
	@Test
	public void testArrayAccess() throws Throwable {
		runResourceTestCaseCMD("Arrays1");
	}
	@Test
	public void testArrayCreation() throws Throwable {
		runResourceTestCaseCMD("Arrays2");
	}
	@Test
	public void testArrayInitializer() throws Throwable {
		runResourceTestCaseCMD("Arrays3");
	}
	@Test
	public void testNestedArrayInitializer() throws Throwable {
		runResourceTestCaseCMD("Arrays4");
	}
	@Test
	public void testUntypedArrayInitializer() throws Throwable {
		runResourceTestCaseCMD("Arrays5");
	}

	@Test
	public void testChar() throws Throwable {
		runResourceTestCaseCMD("autocasting/Char");
	}
	
	@Test
	public void testHex() throws Throwable {
		runResourceTestCaseCMD("autocasting/Hex");
	}
	
	@Test
	public void testConstructorDeclaration() throws Throwable {
		runResourceTestCaseCMD("Constructors1");
	}
		
	@Test
	public void testConstructorInvocationFromConstructor() throws Throwable {
		runResourceTestCaseCMD("Constructors2");
	}
		
	@Test
	public void testInitializers() throws Throwable {
		runResourceTestCaseCMD("Initializers");
	}
	
	@Test
	public void testCollectionMappings() throws Throwable {
		runResourceTestCaseCMD("generics/" + "CollectionMappings");
	}

	@Test
	public void testGenericMethodImpl() throws Throwable {
		runResourceTestCaseCMD("generics/" + "GenericMethodImpl");
	}
	
	@Test
	public void testWildcardTypes() throws Throwable {
		runResourceTestCaseCMD("generics/" +"WildcardTypes");
	}

	@Test
	public void testSimpleInterface() throws Throwable {
		runResourceTestCaseCMD("generics/" + "GenericInterface");
	}		
}

package sharpen.ui.tests;

public class CollectionConversionTestCase extends AbstractConverterTestCase {
	
	public void testMap1() throws Throwable {
		runResourceTestCase("collections/Map1");
	}
	
	public void testList1() throws Throwable {
		runResourceTestCase("collections/List1");
	}
	
	public void testEntrySet() throws Throwable {
		runResourceTestCase("collections/EntrySet1");
	}
	
	public void testIterator() throws Throwable {
		runResourceTestCase("collections/Iterator1");
	}
	
	public void testVector() throws Throwable {
		runResourceTestCase("collections/Vector1");
	}
	
	public void testHashtable() throws Throwable {
		runResourceTestCase("collections/Hashtable1");
	}

}

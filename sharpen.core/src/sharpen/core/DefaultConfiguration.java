/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */
package sharpen.core;

public class DefaultConfiguration extends Configuration {

	DefaultConfiguration(String runtimeTypeName) {
		super(runtimeTypeName);
		
		setUpPrimitiveMappings();
		setUpAnnotationMappings();
	
		mapType("java.lang.System", runtimeTypeName);
		mapType("java.lang.Math", "System.Math");
		mapMethod("java.lang.System.exit", "System.Environment.Exit");
		
		setUpIoMappings();
	
		setUpExceptionMappings();
	    
	    setUpCollectionMappings();
	    
	    mapType("java.lang.Cloneable", "System.ICloneable");
	    
	    mapType("java.util.Date", "System.DateTime");
	
	    mapMethod("java.lang.Object.toString", "ToString");
	    mapMethod("java.lang.Object.hashCode", "GetHashCode");
	    mapMethod("java.lang.Object.equals", "Equals");
	    
	    mapMethod("java.lang.Float.isNaN", "float.IsNaN");
	    mapMethod("java.lang.Double.isNaN", "double.IsNaN");
	    
	    setUpStringMappings();
	
	    mapMethod("java.lang.Throwable.printStackTrace", runtimeMethod("printStackTrace"));
	    
	    mapMethod("java.lang.System.arraycopy", "System.Array.Copy");
	    mapMethod("java.lang.Object.wait", runtimeMethod("wait"));
	    mapMethod("java.lang.Object.notify", runtimeMethod("notify"));
	    mapMethod("java.lang.Object.notifyAll", runtimeMethod("notifyAll"));
	    mapMethod("java.lang.Object.getClass", runtimeMethod("getClassForObject"));		
	
	    mapMethod("length", "Length");	// see qualifiedName(IVariableBinding)
		setUpPrimitiveWrappers();		
	}
	
	private void setUpPrimitiveMappings() {
		mapType("boolean", "bool");
		mapPrimitive("void");
		mapPrimitive("char");
		mapPrimitive("byte");		
		mapPrimitive("short");
		mapPrimitive("int");
		mapPrimitive("long");
		mapPrimitive("float");
		mapPrimitive("double");
		mapType("ubyte", "byte");
		
		mapType("java.lang.Object", "object");
		mapType("java.lang.String", "string");
		mapType("java.lang.Character", "char");
		mapType("java.lang.Byte", "byte");
		mapType("java.lang.Boolean", "bool");
		mapType("java.lang.Short", "short");
		mapType("java.lang.Integer", "int");
		mapType("java.lang.Long", "long");
		mapType("java.lang.Float", "float");
		mapType("java.lang.Double", "double");
	}
	
	private void setUpCollectionMappings() {
		// collection framework
	    mapType("java.util.Collection", "System.Collections.ICollection");
	    mapType("java.util.Collection<>", "System.Collections.Generic.ICollection");
	    mapType("java.util.Set<>", "System.Collections.Generic.ICollection");
	    if (mapIteratorToEnumerator()) {
	    	mapType("java.util.Iterator", "System.Collections.IEnumerator");
	    	mapType("java.util.Iterator<>", "System.Collections.Generic.IEnumerator");
	    	mapType("java.lang.Iterable", "System.Collections.IEnumerable");
	    	mapType("java.lang.Iterable<>", "System.Collections.Generic.IEnumerable");
	    }
	    mapType("java.util.Map", "System.Collections.IDictionary");
	    mapType("java.util.Map<,>", "System.Collections.Generic.IDictionary");
	    mapType("java.util.Map.Entry", "System.Collections.DictionaryEntry");
	    mapType("java.util.Map.Entry<,>", "System.Collections.Generic.KeyValuePair");
	    mapType("java.util.HashMap", "System.Collections.Hashtable");
	    mapType("java.util.HashMap<,>", "System.Collections.Generic.Dictionary");
	    mapType("java.util.TreeMap", "System.Collections.SortedList");
	    mapType("java.util.TreeMap<,>", "System.Collections.Generic.SortedDictionary");	    
	    mapType("java.util.SortedMap<,>", "System.Collections.Generic.SortedDictionary");	    
	    mapType("java.util.List", "System.Collections.IList");
	    mapType("java.util.List<>", "System.Collections.Generic.IList");
	    mapType("java.util.ArrayList", "System.Collections.ArrayList");
	    mapType("java.util.ArrayList<>", "System.Collections.Generic.List");
	    mapType("java.util.LinkedList", "System.Collections.ArrayList");
	    mapType("java.util.LinkedList<>", "System.Collections.Generic.LinkedList");
	    mapType("java.util.Stack", "System.Collections.Stack");	    
	    
	    mapProperty("java.util.LinkedList<>.getFirst", "First");
	    
	    mapType("java.util.Comparator", "System.Collections.IComparer");
	    mapMethod("java.util.Collections.sort", "Sort");
	    
	    mapProperty("java.util.Collection.size", "Count");
	    mapProperty("java.util.Map.size", "Count");
	    
	    mapProperty("java.util.List.size", "Count");
	    mapIndexer("java.util.List.get");
	    mapMethod("java.util.Collection.addAll", collectionRuntimeMethod("AddAll"));
	    mapMethod("java.util.Collection.toArray", collectionRuntimeMethod("ToArray"));
	    
	    if (mapIteratorToEnumerator()) {
	    	mapMethod("java.lang.Iterable.iterator", "GetEnumerator");
	    	mapMethod("java.util.Collection.iterator", "GetEnumerator");
	    	mapMethod("java.util.List.iterator", "GetEnumerator");
	    	mapMethod("java.util.Set.iterator", "GetEnumerator");
	    	mapMethod("java.util.Iterator.hasNext", "MoveNext");
	    	mapProperty("java.util.Iterator.next", "Current");
	    }
	    mapMethod("java.util.Map.remove", collectionRuntimeMethod("Remove"));
	    mapProperty("java.util.Map.Entry.getKey", "Key");
	    mapProperty("java.util.Map.Entry.getValue", "Value");
	    mapProperty("java.util.Map.values", "Values");
	    mapProperty("java.util.Map.keySet", "Keys");
	    mapIndexer("java.util.Dictionary.get");
	    
	    //  jdk 1.0 collection framework
	    mapType("java.util.Vector", "System.Collections.ArrayList");
	    mapType("java.util.Enumeration", "System.Collections.IEnumerator");
	    mapProperty("java.util.Vector.size", "Count");
	    // converter thinks size belong to AbstractCollection on jdk 6
	    mapProperty("java.util.AbstractCollection.size", "Count");
	    mapMethod("java.util.Vector.addElement", "Add");
	    mapIndexer("java.util.Vector.elementAt");
	    mapMethod("java.util.Vector.elements", "GetEnumerator");
	    mapMethod("java.util.Vector.copyInto", "CopyTo");
	    mapMethod("java.util.Vector.removeAllElements", "Clear");
	    mapMethod("java.util.Enumeration.hasMoreElements", "MoveNext");
	    mapProperty("java.util.Enumeration.nextElement", "Current");
	    
	    mapType("java.util.Hashtable", "System.Collections.Hashtable");
	    mapProperty("java.util.Dictionary.keys", "Keys");
	    mapProperty("java.util.Dictionary.size", "Count");
	    
		// JUnit
		mapNamespace("junit.framework", "NUnit.Framework");
		mapMethod("junit.framework.Assert.assertEquals", "NUnit.Framework.Assert.AreEqual");
		mapMethod("junit.framework.Assert.assertTrue", "NUnit.Framework.Assert.IsTrue");
		mapMethod("junit.framework.Assert.assertFalse", "NUnit.Framework.Assert.IsFalse");
		mapMethod("junit.framework.Assert.assertNotNull", "NUnit.Framework.Assert.IsNotNull");
		mapMethod("junit.framework.Assert.assertNull", "NUnit.Framework.Assert.IsNull");
		mapMethod("junit.framework.Assert.assertSame", "NUnit.Framework.Assert.AreSame");
		mapMethod("junit.framework.Assert.assertNotSame", "NUnit.Framework.Assert.AreNotSame");
	    
		// JUnit 4
		mapNamespace("org.junit", "NUnit.Framework");
		mapMethod("org.junit.Assert.assertEquals", "NUnit.Framework.Assert.AreEqual");
		mapMethod("org.junit.Assert.assertTrue", "NUnit.Framework.Assert.IsTrue");
		mapMethod("org.junit.Assert.assertFalse", "NUnit.Framework.Assert.IsFalse");
		mapMethod("org.junit.Assert.assertNotNull", "NUnit.Framework.Assert.IsNotNull");
		mapMethod("org.junit.Assert.assertNull", "NUnit.Framework.Assert.IsNull");
		mapMethod("org.junit.Assert.assertSame", "NUnit.Framework.Assert.AreSame");
		mapMethod("org.junit.Assert.assertNotSame", "NUnit.Framework.Assert.AreNotSame");
		mapMethod("org.junit.Assert.fail", "NUnit.Framework.Assert.Fail");
		mapType("org.junit.Assert", "NUnit.Framework.Assert");
		mapType("org.junit.Before", "NUnit.Framework.SetUp");
		mapType("org.junit.After", "NUnit.Framework.TearDown");
	}
	
	private void setUpExceptionMappings() {
		mapType("java.lang.Throwable", "System.Exception");
		mapProperty("java.lang.Throwable.getMessage", "Message");
		mapProperty("java.lang.Throwable.getCause", "InnerException");
		mapType("java.lang.Error", "System.Exception");
		mapType("java.lang.OutOfMemoryError", "System.OutOfMemoryException");
		mapType("java.lang.Exception", "System.Exception");
		mapType("java.lang.RuntimeException", "System.Exception");
		mapType("java.lang.ClassCastException", "System.InvalidCastException");
		mapType("java.lang.NullPointerException", "System.ArgumentNullException");
		mapType("java.lang.IllegalArgumentException", "System.ArgumentException");
		mapType("java.lang.IllegalStateException", "System.InvalidOperationException");
		mapType("java.lang.InterruptedException", "System.Exception");
	    mapType("java.lang.IndexOutOfBoundsException", "System.IndexOutOfRangeException");
	    mapType("java.lang.UnsupportedOperationException", "System.NotSupportedException");
	    mapType("java.lang.ArrayIndexOutOfBoundsException", "System.IndexOutOfRangeException");
	    mapType("java.lang.NoSuchMethodError", "System.MissingMethodException");
	    mapType("java.io.IOException", "System.IO.IOException");
	    mapType("java.net.SocketException", "System.Net.Sockets.SocketException");
	    mapType("java.lang.SecurityException", "System.Security.SecurityException");
	}
	
	private void setUpPrimitiveWrappers() {
	    mapField("java.lang.Short.MAX_VALUE", "short.MaxValue");
		mapField("java.lang.Short.MIN_VALUE", "short.MinValue");
		mapField("java.lang.Integer.MAX_VALUE", "int.MaxValue");
		mapField("java.lang.Integer.MIN_VALUE", "int.MinValue");
		mapField("java.lang.Long.MAX_VALUE", "long.MaxValue");
		mapField("java.lang.Long.MIN_VALUE", "long.MinValue");
		mapField("java.lang.Float.MAX_VALUE", "float.MaxValue");
		mapField("java.lang.Float.MIN_VALUE", "float.MinValue");
		mapField("java.lang.Float.POSITIVE_INFINITY", "float.PositiveInfinity");
		mapField("java.lang.Float.NEGATIVE_INFINITY", "float.NegativeInfinity");
		mapField("java.lang.Double.MAX_VALUE", "double.MaxValue");
		mapField("java.lang.Double.MIN_VALUE", "double.MinValue");
		mapField("java.lang.Double.NEGATIVE_INFINITY", "double.NegativeInfinity");
		mapField("java.lang.Double.POSITIVE_INFINITY", "double.PositiveInfinity");
		mapField("java.lang.Boolean.TRUE", "true");
		mapField("java.lang.Boolean.FALSE", "false");
		mapField("java.lang.Byte.MAX_VALUE", "byte.MaxValue");
		mapField("java.lang.Byte.MIN_VALUE", "byte.MinValue");
		mapField("java.lang.Character.MAX_VALUE", "char.MaxValue");
		mapField("java.lang.Character.MIN_VALUE", "char.MinValue");
		mapMethod("java.lang.Character.isWhitespace", "char.IsWhiteSpace");
		
		mapWrapperConstructor("java.lang.Boolean.Boolean", "System.Convert.ToBoolean", "boolean");
		mapWrapperConstructor("java.lang.Byte.Byte", "System.Convert.ToByte", "byte");
		mapWrapperConstructor("java.lang.Character.Character", "System.Convert.ToChar", "char");
		mapWrapperConstructor("java.lang.Short.Short", "System.Convert.ToInt16", "short");
		mapWrapperConstructor("java.lang.Integer.Integer", "System.Convert.ToInt32", "int");
		mapWrapperConstructor("java.lang.Long.Long", "System.Convert.ToInt64", "long");
		mapWrapperConstructor("java.lang.Float.Float", "System.Convert.ToSingle", "float");
		mapWrapperConstructor("java.lang.Double.Double", "System.Convert.ToDouble", "double");
		
		mapMethod("java.lang.Long.toString", "System.Convert.ToString");
		mapMethod("java.lang.Long.parseLong", "long.Parse");
		mapMethod("java.lang.Integer.valueOf", "int.Parse");
		mapMethod("java.lang.Integer.parseInt", "System.Convert.ToInt32");
		mapMethod("java.lang.Number.shortValue", "");
		mapMethod("java.lang.Number.intValue", "");
		mapMethod("java.lang.Number.longValue", "");
		mapMethod("java.lang.Number.byteValue", "");
		mapMethod("java.lang.Number.floatValue", "");
		mapMethod("java.lang.Number.doubleValue", "");
		mapMethod("java.lang.Character.charValue", "");
		mapMethod("java.lang.Boolean.booleanValue", "");
		mapMethod("java.lang.Float.floatToIntBits", runtimeMethod("floatToIntBits"));
		mapMethod("java.lang.Float.intBitsToFloat", runtimeMethod("intBitsToFloat"));
    }	
	
	public boolean isIgnoredExceptionType(String exceptionType) {
		return exceptionType.equals("java.lang.CloneNotSupportedException");
	}	

	@Override
	public boolean mapByteToSbyte() {
		return false;
	}
}


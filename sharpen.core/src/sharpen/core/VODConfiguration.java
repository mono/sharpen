package sharpen.core;

public class VODConfiguration extends Configuration {

	VODConfiguration(String runtimeTypeName) {
	    super(runtimeTypeName);

	    setUpPrimitiveMappings();
	    setUpAnnotationMappings();

	    mapType("java.lang.System", runtimeTypeName);
	    mapMethod("java.lang.Math.abs", "System.Math.Abs");
	    mapMethod("java.lang.Math.min", "System.Math.Min");
	    mapMethod("java.lang.Math.max", "System.Math.Max");
	    mapMethod("java.lang.System.exit", "System.Environment.Exit");

	    mapType("java.lang.Cast", "Sharpen.Cast");

	    setUpIoMappings();
	    mapMethod("System.IO.TextWriter.flush", "Flush");
	    mapMethod("System.IO.TextWriter.print", "Write");
	    mapMethod("System.IO.TextWriter.println", "WriteLine");

	    setUpExceptionMappings();

	    //setUpCollectionMappings();

	    //mapType("java.lang.Cloneable", "System.ICloneable");

	    mapType("java.lang.Comparable", "System.IComparable");
	    mapMethod("java.lang.Comparable.compareTo", "CompareTo");

	    //mapType("java.util.Date", "System.DateTime");

	    mapMethod("java.lang.Object.toString", "ToString");
	    mapMethod("java.lang.Object.hashCode", "GetHashCode");
	    mapMethod("java.lang.Object.equals", "Equals");
	    
	    mapMethod("java.lang.Float.isNaN", "float.IsNaN");
	    mapMethod("java.lang.Double.isNaN", "double.IsNaN");
	    
	    setUpStringMappings();

	    mapMethod("java.lang.Throwable.printStackTrace", runtimeMethod("printStackTrace"));
	    //mapMethod("System.DateTime.TryParse", runtimeMethod("DateTimeTryParse"));
	    //mapMethod("System.Int64.TryParse", runtimeMethod("Int64TryParse"));

	    mapMethod("java.lang.System.arraycopy", "System.Array.Copy");
	    //mapMethod("java.lang.Object.clone", "MemberwiseClone");
	    mapMethod("java.lang.Object.wait", runtimeMethod("wait"));
	    mapMethod("java.lang.Object.notify", runtimeMethod("notify"));
	    mapMethod("java.lang.Object.notifyAll", runtimeMethod("notifyAll"));
	    mapMethod("java.lang.Object.getClass", runtimeMethod("getClassForObject"));		
	
	    mapMethod("length", "Length");	// see qualifiedName(IVariableBinding)
		//setUpPrimitiveWrappers();
	    
	    //does not work that easily
	    //mapMethod("java.lang.Cast.ToUByte", "((byte)");		//"byte" in C# is ubyte in J#
	    //mapMethod("java.lang.Cast.FromUByte", "((sbyte)");		//"sbyte" in C# is byte in J#
	    
	    mapType("java.util.Map.Entry", "java.util.MapEntry");
	    
	    //VersantSocketChannel is just a placeholder
	    mapType("Versant.Persistence.RT.VersantSocketChannel", "java.nio.channels.SocketChannel");

	    //mapMethod("??? Array.clone", runtimeMethod("Clone"));
	}
		
	private void setUpPrimitiveMappings() {
		mapType("boolean", "bool");
		mapPrimitive("void");
		mapPrimitive("char");
		mapType("byte", "sbyte");
		mapType("java.lang.ubyte", "byte");		//artificially introduced type
		mapPrimitive("short");
		mapPrimitive("int");
		mapPrimitive("long");
		mapPrimitive("float");
		mapPrimitive("double");
		
		mapType("java.lang.Object", "object");
		mapType("java.lang.String", "string");
		/*mapType("java.lang.Character", "char");
		mapType("java.lang.Byte", "byte");
		mapType("java.lang.Boolean", "bool");
		mapType("java.lang.Short", "short");
		mapType("java.lang.Integer", "int");
		mapType("java.lang.Long", "long");
		mapType("java.lang.Float", "float");
		mapType("java.lang.Double", "double");*/
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
		//mapType("java.lang.InterruptedException", "System.Exception");
		mapType("java.lang.IndexOutOfBoundsException", "System.IndexOutOfRangeException");
	    mapType("java.lang.UnsupportedOperationException", "System.NotSupportedException");
	    mapType("java.lang.ArrayIndexOutOfBoundsException", "System.IndexOutOfRangeException");
	    mapType("java.lang.NoSuchMethodError", "System.MissingMethodException");
	    mapType("java.io.IOException", "System.IO.IOException");
	    mapType("java.net.SocketException", "System.Net.Sockets.SocketException");
	    mapType("java.lang.SecurityException", "System.Security.SecurityException");
	        
	    //in .NET: ArgumentNullException, FormatException, or OverflowException: common base class is System.SystemException
	    mapType("java.lang.NumberFormatException", "System.SystemException");
	    mapType("java.lang.NoSuchFieldException", "System.MissingFieldException");
	    mapType("java.rmi.NoSuchObjectException", "System.InvalidOperationException");
	    mapType("java.util.NoSuchElementException", "System.InvalidOperationException");
	    mapType("java.lang.NoSuchMethodException", "System.MissingMethodException");
	    mapType("java.lang.reflect.InvocationTargetException", "System.Reflection.TargetInvocationException");
	    mapType("java.lang.IllegalAccessException", "System.MethodAccessException");
	    mapType("java.io.FileNotFoundException", "System.IO.FileNotFoundException");
	    mapType("java.io.StreamCorruptedException", "System.IO.IOException");
	    mapType("java.io.UTFDataFormatException", "System.IO.IOException");    	    
	    mapType("java.io.InvalidObjectException", "System.Runtime.Serialization.InvalidDataContractException");
	    
	    mapType("java.util.ConcurrentModificationException", "System.InvalidOperationException");
	    
	    //I've tried interfaces and abstract classes with System.Activator.CreateInstance,
	    //and both attempts yield 'MissingMethodException' and not 'MemberAccessException'
	    mapType("java.lang.InstantiationException", "System.MissingMethodException");
	    
	}	
	
	public boolean isIgnoredExceptionType(String exceptionType) {
		return exceptionType.equals("java.lang.CloneNotSupportedException") || 
			   exceptionType.equals("java.lang.InterruptedException");
	}
	
	public boolean mapProtectedToProtectedInternal() {
		return true;
	}

	@Override
	public boolean mapByteToSbyte() {
		return true;
	}
}
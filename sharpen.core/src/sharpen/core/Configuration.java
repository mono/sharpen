/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

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

import java.util.*;

public class Configuration {
	
	public static final String DEFAULT_RUNTIME_TYPE_NAME = "Sharpen.Runtime";

	public static class MemberMapping {
		public String name;
		public MemberKind kind;
		
		public MemberMapping(String name, MemberKind kind) {
			this.name = name;
			this.kind = kind;
		}
		
		@Override
		public String toString() {
			return "MemberMapping(" + name + ", " + kind + ")";
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MemberMapping)) return false;
			MemberMapping other = (MemberMapping)obj;
			return name.equals(other.name) && kind.equals(other.kind);
		} 
	}
	
	public static class NameMapping {
		public String from;
		public String to;
		
		public NameMapping(String from, String to) {
			this.from = from;
			this.to = to;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof NameMapping)) return false;
			NameMapping other = (NameMapping)obj;
			return from.equals(other.from) && to.equals(other.to);
		}
	}
	
	private static final WarningHandler NULL_WARNING_HANDLER = new WarningHandler();	
	
	private Map<String, String> _typeMappings = new HashMap<String, String>();
	
	private Map<String, MemberMapping> _memberMappings = new HashMap<String, MemberMapping>();
	
	private Map<String, String> _systemConvertWellKnownTypes = new HashMap<String, String>();
	
	private List<NameMapping> _namespaceMappings = new ArrayList<NameMapping>();
	
	private WarningHandler _warningHandler = Configuration.NULL_WARNING_HANDLER;
	
	private NamingStrategy _namingStrategy = NamingStrategy.DEFAULT;

	private boolean _nativeTypeSystem = false;
	
	private boolean _ignoreErrors = false;

	private final String _runtimeTypeName;

	private boolean _nativeInterfaces;
	
	private boolean _organizeUsings;
	
	private List<String> _fullyQualifiedTypes = new ArrayList<String>();
	
	private boolean _createProblemMarkers = false;

	private String _header = "";
	
	private DocumentationOverlay _docOverlay = NullDocumentationOverlay.DEFAULT;

	private final List<String> _removedMethods = new ArrayList<String>();

	private final Set<String> _mappedEventAdds = new HashSet<String>();

	private final Map<String, String> _mappedEvents = new HashMap<String, String>();
	
	/**
	 * Maps package names to expressions used in conditional compilation. 
	 * Sub-packages will be considered to match also. 
	 */
	private Map<String, String> _conditionalCompilations = new HashMap<String, String>();

	public Configuration() {
		this(DEFAULT_RUNTIME_TYPE_NAME);
	}
	
	public Configuration(String runtimeTypeName) {
		
		_runtimeTypeName = runtimeTypeName;
		
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
	    mapMethod("java.lang.Object.clone", "MemberwiseClone");
	    mapMethod("java.lang.Object.wait", runtimeMethod("wait"));
	    mapMethod("java.lang.Object.notify", runtimeMethod("notify"));
	    mapMethod("java.lang.Object.notifyAll", runtimeMethod("notifyAll"));
	    mapMethod("java.lang.Object.getClass", runtimeMethod("getClassForObject"));		
	
	    mapMethod("length", "Length");	// see qualifiedName(IVariableBinding)
		setUpPrimitiveWrappers();
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
		mapMethod("java.lang.Integer.parseInt", "int.Parse");
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

	private void setUpAnnotationMappings() {
		mapType("java.lang.Deprecated", "System.Obsolete");
    }

	private void setUpStringMappings() {
		mapType("java.lang.StringBuffer", "System.Text.StringBuilder");
	    mapProperty("java.lang.StringBuffer.length", "Length");
	    
	    mapMethod("java.lang.String.intern", "string.Intern");
	    mapMethod("java.lang.String.substring", "Substring");
	    mapMethod("java.lang.String.indexOf", "IndexOf");
	    mapMethod("java.lang.String.lastIndexOf", "LastIndexOf");
	    mapMethod("java.lang.String.trim", "Trim");
	    mapMethod("java.lang.String.toUpperCase", "ToUpper");
	    mapMethod("java.lang.String.toLowerCase", "ToLower");
	    mapMethod("java.lang.String.startsWith", "StartsWith");
	    mapMethod("java.lang.String.endsWith", "EndsWith");
		mapMethod("java.lang.String.substring", runtimeMethod("substring"));
	    mapIndexer("java.lang.String.charAt");
	    mapIndexer("java.lang.CharSequence.charAt");
	    mapMethod("java.lang.String.getChars", runtimeMethod("getCharsForString"));
	    mapMethod("java.lang.String.getBytes", runtimeMethod("getBytesForString"));
	    mapMethod("java.lang.String.equalsIgnoreCase", runtimeMethod("equalsIgnoreCase"));
	    mapMethod("java.lang.String.valueOf", runtimeMethod("getStringValueOf"));
	    mapMethod("java.lang.String.String(byte[],int,int)", runtimeMethod("getStringForBytes"));
	    mapProperty("java.lang.String.length", "Length");
	    mapProperty("java.lang.CharSequence.length", "Length");
	}

	private void setUpIoMappings() {
		mapProperty("java.lang.System.out", "System.Console.Out");
		mapProperty("java.lang.System.err", "System.Console.Error");
		mapType("java.io.PrintStream", "System.IO.TextWriter");
		mapType("java.io.Writer", "System.IO.TextWriter");
		mapType("java.io.StringWriter", "System.IO.StringWriter");
		mapMethod("java.io.PrintStream.print", "Write");
		mapMethod("java.io.PrintStream.println", "WriteLine");
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

	private void setUpCollectionMappings() {
		// collection framework
	    mapType("java.util.Collection", "System.Collections.ICollection");
	    mapType("java.util.Collection<>", "System.Collections.Generic.ICollection");
	    mapType("java.util.Set<>", "System.Collections.Generic.ICollection");
	    mapType("java.util.Iterator", "System.Collections.IEnumerator");
	    mapType("java.util.Iterator<>", "System.Collections.Collection.IEnumerator");
	    mapType("java.lang.Iterable", "System.Collections.IEnumerable");
	    mapType("java.lang.Iterable<>", "System.Collections.Generic.IEnumerable");
	    mapType("java.util.Map", "System.Collections.IDictionary");
	    mapType("java.util.Map<,>", "System.Collections.Generic.IDictionary");
	    mapType("java.util.Map.Entry", "System.Collections.DictionaryEntry");
	    mapType("java.util.Map.Entry<,>", "System.Collections.Generic.KeyValuePair");
	    mapType("java.util.HashMap", "System.Collections.Hashtable");
	    mapType("java.util.HashMap<,>", "System.Collections.Generic.Dictionary");
	    mapType("java.util.TreeMap", "System.Collections.SortedList");
	    mapType("java.util.TreeMap<,>", "System.Collections.Generic.SortedList");	    
	    mapType("java.util.List", "System.Collections.IList");
	    mapType("java.util.List<>", "System.Collections.Generic.IList");
	    mapType("java.util.ArrayList", "System.Collections.ArrayList");
	    mapType("java.util.ArrayList<>", "System.Collections.Generic.List");
	    mapType("java.util.LinkedList", "System.Collections.ArrayList");
	    mapType("java.util.LinkedList<>", "System.Collections.Generic.LinkedList");
	    mapType("java.util.Stack", "System.Collections.Stack");	    
	    
	    mapProperty("java.util.Collection.size", "Count");
	    mapProperty("java.util.Map.size", "Count");
	    
	    mapProperty("java.util.List.size", "Count");
	    mapIndexer("java.util.List.get");
	    mapMethod("java.util.Collection.addAll", collectionRuntimeMethod("AddAll"));
	    mapMethod("java.util.Collection.toArray", collectionRuntimeMethod("ToArray"));
	    
	    mapMethod("java.lang.Iterable.iterator", "GetEnumerator");
	    mapMethod("java.util.Collection.iterator", "GetEnumerator");
	    mapMethod("java.util.List.iterator", "GetEnumerator");
	    mapMethod("java.util.Set.iterator", "GetEnumerator");
	    mapMethod("java.util.Iterator.hasNext", "MoveNext");
	    mapProperty("java.util.Iterator.next", "Current");
	    mapIndexer("java.util.Map.put");
	    mapMethod("java.util.Map.remove", collectionRuntimeMethod("Remove"));
	    mapMethod("java.util.Map.entrySet", "");
	    mapProperty("java.util.Map.Entry.getKey", "Key");
	    mapProperty("java.util.Map.Entry.getValue", "Value");
	    mapMethod("java.util.Map.containsKey", "Contains");
	    mapProperty("java.util.Map.values", "Values");
	    mapProperty("java.util.Map.keySet", "Keys");
	    mapIndexer("java.util.Map.get");
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
	    mapMethod("java.util.Enumeration.hasMoreElements", "MoveNext");
	    mapProperty("java.util.Enumeration.nextElement", "Current");
	    
	    mapType("java.util.Hashtable", "System.Collections.Hashtable");
	    mapIndexer("java.util.Dictionary.put");
	    mapProperty("java.util.Dictionary.size", "Count");
	}

	private String collectionRuntimeMethod(String methodName) {
		return collectionRuntimeType() + "." + methodName;
    }

	private String collectionRuntimeType() {
		return runtimeTypeNamespace() + ".Collections";
    }

	private String runtimeTypeNamespace() {
		return _runtimeTypeName.substring(0, _runtimeTypeName.lastIndexOf('.'));
    }

	private String runtimeMethod(String methodName) {
		return _runtimeTypeName + "." + methodName;
	}
	
	private void mapPrimitive(String typeName) {
		mapType(typeName, typeName);
	}

	public String getRuntimeTypeName() {
		return _runtimeTypeName;
	}
	
	public void setDocumentationOverlay(DocumentationOverlay docOverlay) {
		if (null == docOverlay) throw new IllegalArgumentException("docOverlay");
		_docOverlay = docOverlay;
	}
	
	public DocumentationOverlay documentationOverlay() {
		return _docOverlay;
	}
	
	public void enableNativeTypeSystem() {
		_nativeTypeSystem = true;
		
		mapType("java.lang.ClassNotFoundException", "System.TypeLoadException");
		mapType("java.lang.reflect.InvocationTargetException", "System.Reflection.TargetInvocationException");
		mapProperty("java.lang.reflect.InvocationTargetException.getTargetException", "InnerException");
		mapType("java.lang.IllegalAccessException", "System.MemberAccessException");
		
//		mapType("java.lang.reflect.Array", "System.Array");
		mapMethod("java.lang.reflect.Array.getLength", runtimeMethod("GetArrayLength"));
		mapMethod("java.lang.reflect.Array.get", runtimeMethod("GetArrayValue"));
		mapMethod("java.lang.reflect.Array.set", runtimeMethod("SetArrayValue"));
		
		mapMethod("java.lang.Object.getClass", "GetType");
		mapType("java.lang.Class", "System.Type");
		mapType("java.lang.Class<>", "System.Type");
		mapJavaLangClassProperty("getName", "FullName");
		mapJavaLangClassProperty("getSuperclass", "BaseType");
		mapJavaLangClassProperty("isArray", "IsArray");
		mapJavaLangClassProperty("isPrimitive", "IsPrimitive");
		mapJavaLangClassProperty("isInterface", "IsInterface");
		mapJavaLangClassMethod("isInstance", "IsInstanceOfType");
		mapJavaLangClassMethod("newInstance", "System.Activator.CreateInstance");
		mapJavaLangClassMethod("forName", runtimeMethod("GetType"));
		mapJavaLangClassMethod("getComponentType", "GetElementType");
		mapJavaLangClassMethod("getDeclaredField", runtimeMethod("GetDeclaredField"));		
		mapJavaLangClassMethod("getDeclaredFields", runtimeMethod("GetDeclaredFields"));
		mapJavaLangClassMethod("getDeclaredMethod", runtimeMethod("GetDeclaredMethod"));
		mapJavaLangClassMethod("getDeclaredMethods", runtimeMethod("GetDeclaredMethods"));
		
		mapProperty("java.lang.reflect.Member.getName", "Name");
		mapProperty("java.lang.reflect.Member.getDeclaringClass", "DeclaringType");
		
		mapType("java.lang.reflect.Field", "System.Reflection.FieldInfo");
		mapProperty("java.lang.reflect.Field.getName", "Name");
		mapMethod("java.lang.reflect.Field.get", "GetValue");
		mapMethod("java.lang.reflect.Field.set", "SetValue");
		
		mapType("java.lang.reflect.Method", "System.Reflection.MethodInfo");
		mapProperty("java.lang.reflect.Method.getName", "Name");
		mapProperty("java.lang.reflect.Method.getReturnType", "ReturnType");
		mapMethod("java.lang.reflect.Method.getParameterTypes", runtimeMethod("GetParameterTypes"));
		removeMethod("java.lang.reflect.AccessibleObject.setAccessible");
		mapType("java.lang.reflect.Constructor", "System.Reflection.ConstructorInfo");
		
		mapMethod("java.lang.String.valueOf", "ToString");
	}

	private void mapJavaLangClassProperty(String methodName, String propertyName) {
		mapProperty("java.lang.Class." + methodName, propertyName);
	}
	
	private void mapJavaLangClassMethod(String methodName, String newMethodName) {
		mapMethod("java.lang.Class." + methodName, newMethodName);
	}
	
	public boolean nativeTypeSystem() {
		return _nativeTypeSystem;
	}
	
	public void setWarningHandler(WarningHandler warningHandler) {
		_warningHandler = warningHandler;
	}
	
	public WarningHandler getWarningHandler() {
		return _warningHandler;
	}
	
	public void setNamingStrategy(NamingStrategy namingStrategy) {
		_namingStrategy = namingStrategy;
	}
	
	public NamingStrategy getNamingStrategy() {
		return _namingStrategy;
	}
	
	public void mapNamespace(String fromRegex, String to) {
		_namespaceMappings.add(new NameMapping(fromRegex, to));
	}
	
	public void mapNamespaces(List<NameMapping> namespaceMappings) {
		_namespaceMappings.addAll(namespaceMappings);
	}
	
	public void mapMembers(Map<String, MemberMapping> memberMappings) {
		_memberMappings.putAll(memberMappings);
	}
	
	public String mappedNamespace(String namespace) {
		String mapped = applyNamespaceMappings(namespace + ".");
		return _namingStrategy.namespace(mapped.substring(0, mapped.length()-1));
	}
	
	public String mappedTypeName(String typeName, String defaultValue) {
		String mappedName = _typeMappings.get(typeName);
		return (null != mappedName)
			? mappedName
			: mappedNamespace(defaultValue);
	}

	private String applyNamespaceMappings(String typeName) {
		for (NameMapping mapping : _namespaceMappings) {
			typeName = typeName.replaceFirst(mapping.from + "\\.", mapping.to + ".");
		}
		return typeName;
	}
	
	public MemberMapping mappedMember(String qualifiedName) {
		return _memberMappings.get(qualifiedName);
	}
	
	public String getConvertRelatedWellKnownTypeName(String mappedConstructor) {
		return _systemConvertWellKnownTypes.get(mappedConstructor);
	}

	public void mapType(String from, String to) {
		_typeMappings.put(from, to);
	}
	
	public boolean typeHasMapping(String type) {
		return _typeMappings.containsKey(type);
	}

	public void mapField(String fromQualifiedName, String to) {
		mapMember(fromQualifiedName, new MemberMapping(to, MemberKind.Field));
	}

	public void mapMethod(String fromQualifiedName, String to) {
		mapMember(fromQualifiedName, new MemberMapping(to, MemberKind.Method));
	}
	
	public void mapIndexer(String fromQualifiedName) {
		mapMember(fromQualifiedName, new MemberMapping(null, MemberKind.Indexer));
	}

	public void mapProperty(String fromQualifiedName, String to) {
		mapMember(fromQualifiedName, new MemberMapping(to, MemberKind.Property));
	}
	
	public void setIgnoreErrors(boolean value) {
		_ignoreErrors = value;
	}

	public boolean getIgnoreErrors() {
		return _ignoreErrors;
	}

	void mapMember(String fromQualifiedName, MemberMapping mapping) {
		_memberMappings.put(fromQualifiedName, mapping);
	}

	private void mapWrapperConstructor(String from, String to, String wellKnownTypeName) {
		mapMethod(from, to);
		_systemConvertWellKnownTypes.put(to, wellKnownTypeName);
	}
	
	public boolean nativeInterfaces() {
		return _nativeInterfaces;
	}

	public void enableNativeInterfaces() {
		_nativeInterfaces = true;
	}
	
	public void enableOrganizeUsings() {
		_organizeUsings = true;
	}
	
	public boolean organizeUsings() {
		return _organizeUsings;
	}
	
	public void addFullyQualifiedTypeName(String name) {
		_fullyQualifiedTypes.add(name);
	}
	
	public boolean shouldFullyQualifyTypeName(String name) {
		return _fullyQualifiedTypes.contains(name);
	}

	public void setCreateProblemMarkers(boolean value) {
		_createProblemMarkers = value;
	}

	public boolean createProblemMarkers() {
		return _createProblemMarkers;
	}

	public void setHeader(String header) {
		if (null == header) throw new IllegalArgumentException("header");
		_header = header;
	}
	
	public String header() {
		return _header;
	}

	public void removeMethod(String fullyQualifiedName) {
		_removedMethods.add(fullyQualifiedName);
	}

	public boolean isRemoved(String qualifiedName) {
		return _removedMethods.contains(qualifiedName);
	}

	public void mapEventAdd(String qualifiedMethodName) {
		_mappedEventAdds.add(qualifiedMethodName);
	}
	
	public boolean isMappedEventAdd(String qualifiedMethodName) {
		return _mappedEventAdds.contains(qualifiedMethodName);
	}

	public void mapEvent(String qualifiedMethodName, String eventArgsTypeName) {
		mapProperty(qualifiedMethodName, unqualify(qualifiedMethodName));
		_mappedEvents.put(qualifiedMethodName, eventArgsTypeName);
	}
	
	public String mappedEvent(String qualifiedMethodName) {
		return _mappedEvents.get(qualifiedMethodName);
	}

	private String unqualify(String qualifiedMethodName) {
		final int lastDot = qualifiedMethodName.lastIndexOf('.');
		return lastDot == -1
			? qualifiedMethodName
			: qualifiedMethodName.substring(lastDot+1);
	}

	public void mapEventAdds(Iterable<String> eventAddMappings) {
		for (String m : eventAddMappings) {
			mapEventAdd(m);
		}
	}

	public void mapEvents(Iterable<NameMapping> eventMappings) {
		for (NameMapping m : eventMappings) {
			mapEvent(m.from, m.to);
		}
	}

	public boolean isIgnoredAnnotation(String typeName) {
	    return typeName.equals("java.lang.Override");
    }

	public void conditionalCompilation(Map<String, String> conditionalCompilation) {
		_conditionalCompilations = conditionalCompilation;
	}
	
	public String conditionalCompilationExpressionFor(String packageName) {
		for(String current : _conditionalCompilations.keySet()) {
			if (isSubPackage(current, packageName)) {
				return _conditionalCompilations.get(current);	
			}
		}
		
		return null;
	}

	private boolean isSubPackage(String parentPackage, String packageName) {
		return packageName.startsWith(parentPackage);
	}

	public String toInterfaceName(String name) {
		if (!nativeInterfaces()) {
			return name;
		}
		return "I" + name;
	}
}

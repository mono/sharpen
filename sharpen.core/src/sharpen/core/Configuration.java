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

public abstract class Configuration {

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

	private String _indentString;

	private int _maxColumns = 80;

	private boolean _nativeTypeSystem = false;
	
	private boolean _ignoreErrors = false;

	private final String _runtimeTypeName;

	private boolean _nativeInterfaces;
	
	private boolean _separateInterfaceConstants;

	private boolean _organizeUsings;
	
	private boolean _paramCountFileNames;

	private boolean _junitConvert;
	
	private String _sharpenNamespace = "Sharpen";

	private List<String> _fullyQualifiedTypes = new ArrayList<String>();
	
	private String _header = "";
	
	private DocumentationOverlay _docOverlay = NullDocumentationOverlay.DEFAULT;

	private final List<String> _removedMethods = new ArrayList<String>();

	private final Set<String> _mappedEventAdds = new HashSet<String>();

	private final Map<String, String> _mappedEvents = new HashMap<String, String>();
	
	private List<String> _partialTypes = new ArrayList<String>();
	
	/**
	 * Maps package names to expressions used in conditional compilation. 
	 * Sub-packages will be considered to match also. 
	 */
	private Map<String, String> _conditionalCompilations = new HashMap<String, String>();


	public Configuration(String runtimeTypeName) {
		
		_runtimeTypeName = runtimeTypeName;
	}

	protected void setUpAnnotationMappings() {
		mapType("java.lang.Deprecated", "System.Obsolete");
    }

	protected void setUpStringMappings() {		
		mapType("java.lang.StringBuffer", "System.Text.StringBuilder");
	    mapProperty("java.lang.StringBuffer.length", "Length");
	    mapMethod("java.lang.StringBuffer.append", "Append");	    
	    mapMethod("java.lang.StringBuffer.insert", "Insert");
	    //"append" is also defined in an interface, and that must be mapped as well
	    //(so that it works with all JREs):
	    mapMethod("java.lang.Appendable.append", "Append");	
	    mapMethod("java.lang.AbstractStringBuilder.append", "Append");  
	    mapMethod("java.lang.AbstractStringBuilder.insert", "Insert");
	    
	    mapMethod("java.lang.StringBuffer.deleteCharAt", runtimeMethod("deleteCharAt"));
	    mapMethod("java.lang.AbstractStringBuilder.deleteCharAt", runtimeMethod("deleteCharAt"));
	    mapMethod("java.lang.StringBuffer.setCharAt", runtimeMethod("setCharAt"));
	    mapMethod("java.lang.AbstractStringBuilder.setCharAt", runtimeMethod("setCharAt"));
	    
	    mapProperty("java.lang.StringBuffer.setLength", "Length");
	    mapProperty("java.lang.AbstractStringBuilder.setLength", "Length");
	    
	    mapMethod("java.lang.String.intern", "string.Intern");
	    mapMethod("java.lang.String.indexOf", "IndexOf");
	    mapMethod("java.lang.String.lastIndexOf", "LastIndexOf");
	    mapMethod("java.lang.String.trim", "Trim");
	    mapMethod("java.lang.String.toUpperCase", "ToUpper");
	    mapMethod("java.lang.String.toLowerCase", "ToLower");
	    mapMethod("java.lang.String.compareTo", "CompareTo");
	    mapMethod("java.lang.Comparable.compareTo(java.lang.String)", "string.CompareOrdinal");
	    mapMethod("java.lang.String.toCharArray", "ToCharArray");
	    mapMethod("java.lang.String.replace", "Replace");
	    mapMethod("java.lang.String.startsWith", "StartsWith");
	    mapMethod("java.lang.String.endsWith", "EndsWith");
		mapMethod("java.lang.String.substring", runtimeMethod("substring"));
	    mapIndexer("java.lang.String.charAt");
	    mapIndexer("java.lang.CharSequence.charAt");
	    mapMethod("java.lang.String.getChars", runtimeMethod("getCharsForString"));
	    mapMethod("java.lang.String.getBytes", runtimeMethod("getBytesForString"));
	    mapMethod("java.lang.String.equalsIgnoreCase", runtimeMethod("equalsIgnoreCase"));
	    mapMethod("java.lang.String.valueOf", runtimeMethod("getStringValueOf"));
	    mapMethod("java.lang.String.String(byte[])", runtimeMethod("getStringForBytes"));
	    mapMethod("java.lang.String.String(byte[],int,int)", runtimeMethod("getStringForBytes"));
	    mapMethod("java.lang.String.String(byte[],int,int,java.lang.String)", runtimeMethod("getStringForBytes"));
	    mapMethod("java.lang.String.String(byte[],java.lang.String)", runtimeMethod("getStringForBytes"));
	    mapProperty("java.lang.String.length", "Length");
	    mapProperty("java.lang.CharSequence.length", "Length");
	}

	protected void setUpIoMappings() {
		mapProperty("java.lang.System.out", "System.Console.Out");
		mapProperty("java.lang.System.err", "System.Console.Error");
		
		mapType("java.io.PrintStream", "System.IO.TextWriter");

		mapType("java.io.Writer", "System.IO.TextWriter");
		mapMethod("java.io.Writer.flush", "Flush");
		mapType("java.io.StringWriter", "System.IO.StringWriter");

		mapMethod("java.io.PrintStream.print", "Write");
		mapMethod("java.io.PrintStream.println", "WriteLine");			
	}

	protected String collectionRuntimeMethod(String methodName) {
		return collectionRuntimeType() + "." + methodName;
    }

	private String collectionRuntimeType() {
		return runtimeTypeNamespace() + ".Collections";
    }

	private String runtimeTypeNamespace() {
		return _runtimeTypeName.substring(0, _runtimeTypeName.lastIndexOf('.'));
    }

	protected String runtimeMethod(String methodName) {
		return _runtimeTypeName + "." + methodName;
	}
	
	protected void mapPrimitive(String typeName) {
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
	
	public String getIndentString() {
		return _indentString;
	}

	public void setIndentString(String indentString) {
		this._indentString = indentString;
	}

	public int getMaxColumns() {
		return _maxColumns;
	}

	public void setMaxColumns(int maxColumns) {
		this._maxColumns = maxColumns;
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
		mapMethod("java.lang.reflect.Array.newInstance", "System.Array.CreateInstance");
		
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
		mapJavaLangClassMethod("getField", "GetField");
		mapJavaLangClassMethod("getFields", "GetFields");
		mapJavaLangClassMethod("getDeclaredField", runtimeMethod("GetDeclaredField"));		
		mapJavaLangClassMethod("getDeclaredFields", runtimeMethod("GetDeclaredFields"));
		mapJavaLangClassMethod("getDeclaredMethod", runtimeMethod("GetDeclaredMethod"));
		mapJavaLangClassMethod("getDeclaredMethods", runtimeMethod("GetDeclaredMethods"));
		mapJavaLangClassMethod("isAssignableFrom", "IsAssignableFrom");
		
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

	protected void mapWrapperConstructor(String from, String to, String wellKnownTypeName) {
		mapMethod(from, to);
		_systemConvertWellKnownTypes.put(to, wellKnownTypeName);
	}
	
	public boolean nativeInterfaces() {
		return _nativeInterfaces;
	}

	public void enableNativeInterfaces() {
		_nativeInterfaces = true;
	}
	
	public boolean separateInterfaceConstants() {
		return _separateInterfaceConstants;
	}

	public void enableSeparateInterfaceConstants() {
		_separateInterfaceConstants = true;
	}

	public void enableOrganizeUsings() {
		_organizeUsings = true;
	}
	
	public boolean organizeUsings() {
		return _organizeUsings;
	}

	public void enableParamCountFileNames() {
		_paramCountFileNames = true;
	}

	public boolean paramCountFileNames() {
		return _paramCountFileNames;
	}
	
	public void enableJUnitConversion () {
		_junitConvert = true;
	}
	
	public boolean junitConversion () {
		return _junitConvert;
	}

	public void setSharpenNamespace(String sharpenNamespace) {
		if (null == sharpenNamespace) throw new IllegalArgumentException("sharpenNamespace");
		_sharpenNamespace = sharpenNamespace;
	}

	public String sharpenNamespace() {
		return _sharpenNamespace;
	}
	
	public void addFullyQualifiedTypeName(String name) {
		_fullyQualifiedTypes.add(name);
	}
	
	public void addPartialType (String name) {
		_partialTypes.add (name);
	}
	
	public boolean shouldFullyQualifyTypeName(String name) {
		//if a type is configured to be fully qualified,
		//then also nested types of it need to be fully qualified
		for( String s : _fullyQualifiedTypes ) {
			if( name.equals(s) || name.startsWith(s + ".") ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean shouldMakePartial(String name) {
		//if a type is configured to be fully qualified,
		//then also nested types of it need to be fully qualified
		for( String s : _partialTypes) {
			if( name.equals(s) || name.startsWith(s + ".") ) {
				return true;
			}
		}
		return false;
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
	
	public abstract boolean isIgnoredExceptionType(String exceptionType);
	
	public boolean mapProtectedToProtectedInternal() {
		return true;
	}

	public boolean mapIteratorToEnumerator() {
		return true;
	}

	public abstract boolean mapByteToSbyte();

}

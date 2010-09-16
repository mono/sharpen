/* Copyright (C) 2009  Versant Inc.   http://www.db4o.com */
package sharpen.core.internal;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

import sharpen.core.*;
import sharpen.core.Configuration.*;
import sharpen.core.csharp.ast.*;
import sharpen.core.framework.*;
import static sharpen.core.framework.Environments.*;

public class MappingsImpl implements Mappings {

	private final List<String> _namespaces = new ArrayList<String>();
	
	private final Configuration _configuration = my(Configuration.class);
	private final CSCompilationUnit _compilationUnit = my(CSCompilationUnit.class);
	private final NameScope _nameScope = my(NameScope.class);
	private final Annotations _annotations = my(Annotations.class);
	private final Bindings _bindings = my(Bindings.class);
	private final PreserveFullyQualifiedNamesState _preserveFQNState = my(PreserveFullyQualifiedNamesState.class);
	
	public String mappedFieldName(IVariableBinding binding) {
		if (!binding.isField())
			return null;
		
		Configuration.MemberMapping mapping = _configuration.mappedMember(BindingUtils.qualifiedName(binding));
		return mapping != null 
				? mapping.name 
				: null;
	}

	public String mappedMethodName(IMethodBinding binding) {
		Configuration.MemberMapping mapping = effectiveMappingFor(binding);
		return computeMethodName(binding, mapping);
	}

	public String mappedTypeName(ITypeBinding type) {
		if (type.isArray() || type.isWildcardType()) {
			return type.getQualifiedName();
		}
		
		if (!hasMapping(type)) {
			String annotatedRenaming = annotatedRenaming(type);
			if (annotatedRenaming != null) {
				return registerMappedType(type, fullyQualifyIfNeeded(annotatedRenaming, type));
			}
		}
		
		String mappedTypeName = mappedTypeName(typeMappingKey(type), qualifiedName(type));
		if (shouldPrefixInterface(type)) {
			return registerMappedType(type, mappedInterfaceName(mappedTypeName));
		}
		
		return registerMappedType(type, mappedTypeName);
	}

	private String fullyQualifyIfNeeded(String typeName, ITypeBinding type) {
		if (isFullyQualified(typeName)) {
			return typeName;
		}
		
		final String originalNamespace = namespace(qualifiedName(type));
		final String mappedNamespace = _configuration.mappedNamespace(originalNamespace);
		if (originalNamespace.equals(mappedNamespace)) {
			return typeName;
		}
		
		return mappedNamespace + "." + typeName;
	}
	
	private String namespace(final String typeName) {
		return substringBeforeLast(typeName, '.');
	}

	private String substringBeforeLast(String s, char marker) {
		return s.substring(0, s.lastIndexOf(marker));
	}

	private boolean isFullyQualified(String typeName) {
		return typeName.contains(".");
	}

	private String annotatedRenaming(ITypeBinding type) {
		if (type.isTypeVariable()) return null;
		
		final AbstractTypeDeclaration typeDeclaration = findDeclaringNode(type);
		return (typeDeclaration != null && isAnnotatedWith(typeDeclaration, SharpenAnnotations.SHARPEN_RENAME)) 
				? annotatedRenaming(typeDeclaration)
				: null;
	}
	
	private boolean shouldPrefixInterface(ITypeBinding type) {
		return _configuration.nativeInterfaces()
			&& type.isInterface()
			&& !type.isAnnotation()
			&& !hasMapping(type);
	}	
	
	private String registerMappedType(ITypeBinding type, String fullName) {
		if (_preserveFQNState.value())
			return fullName;
		
		if (!_configuration.organizeUsings())
			return fullName;

		int pos = fullName.lastIndexOf(".");
		if (pos == -1)
			return fullName;

		if (!hasMapping(type)) {
			pos = nameSpaceLength(type, fullName, pos);
		}

		String namespace = fullName.substring(0, pos);
		registerNamespace(namespace);
		String name = fullName.substring(pos + 1);

		if (keepFullyQualified(name))
			return fullName;

		_compilationUnit.addUsing(new CSUsing(namespace));
		return name;
	}

	private int nameSpaceLength(ITypeBinding type, String fullName, int pos) {
		while (type.isNested()) {
			pos = fullName.lastIndexOf(".", pos - 1);
			type = type.getDeclaringClass();
		}
		return pos;
	}

	private boolean keepFullyQualified(String name) {
		return _configuration.shouldFullyQualifyTypeName(name) 
				|| _namespaces.contains(name)
		        || _nameScope.contains(name);
	}

	private void registerNamespace(String namespace) {
		if (_namespaces.contains(namespace))
			return;
		int pos = namespace.lastIndexOf(".");
		if (pos == -1) {
			_namespaces.add(namespace);
			return;
		}

		_namespaces.add(namespace.substring(pos + 1));
		registerNamespace(namespace.substring(0, pos));
	}

	private boolean hasMapping(ITypeBinding type) {
		return _configuration.typeHasMapping(typeMappingKey(type));
	}
	
	private String typeMappingKey(ITypeBinding type) {
		final ITypeBinding[] typeArguments = type.getTypeArguments();
		if (typeArguments.length > 0) {
			return qualifiedName(type) + "<" + repeat(',', typeArguments.length - 1) + ">";
		}
		return qualifiedName(type);
	}	
	
	private String mappedInterfaceName(String name) {
		int pos = name.lastIndexOf('.');
		return name.substring(0, pos) + "." + interfaceName(name.substring(pos + 1));
	}

	private String interfaceName(String name) {
		return _configuration.toInterfaceName(name);
	}

	public Configuration.MemberMapping effectiveMappingFor(final IMethodBinding binding) {
		final MemberMapping mapping = configuredMappingFor(binding);
		if (null != mapping)
			return mapping;
	
		boolean declaringClassIgnoresExtends = isDeclaringClassIgnoringExtends(binding);
		
		//TODO: Always check method in current class first (irrespective to sharpen.ignore.implements/extends)?
		//TODO: Check ignore implements also 
		
		MethodDeclaration method = (MethodDeclaration) (declaringClassIgnoresExtends  
										? findDeclaringNode(binding) 
										: findDeclaringNode(originalMethodBinding(binding))) ;
		if (method == null)
			return null;
		
		if (isAnnotatedWith(method, SharpenAnnotations.SHARPEN_INDEXER))
			return new MemberMapping(null, MemberKind.Indexer);
		
		if (isAnnotatedWith(method, SharpenAnnotations.SHARPEN_EVENT))
			return new MemberMapping(binding.getName(), MemberKind.Property);
		
		if (isAnnotatedWith(method, SharpenAnnotations.SHARPEN_PROPERTY))
			return new MemberMapping(annotatedPropertyName(method), MemberKind.Property);
		
		if (isAnnotatedWith(method, SharpenAnnotations.SHARPEN_RENAME))
			return new MemberMapping(annotatedRenaming(method), MemberKind.Method);
		
		//TODO: Check originalMethodBinding if declaringClassIgnoresExtends == true
		//      and we reach this point?
		return null;
	}

	private boolean isDeclaringClassIgnoringExtends(final IMethodBinding binding) {
		ITypeBinding declaringClassBinding = binding.getDeclaringClass();
		if (declaringClassBinding.isAnonymous()) return false;
		
		TypeDeclaration declaringClass = findDeclaringNode(declaringClassBinding);
		return declaringClass == null 
					? false 
					: isAnnotatedWith(declaringClass, SharpenAnnotations.SHARPEN_IGNORE_EXTENDS);
	}
	
	private String annotatedRenaming(BodyDeclaration method) {
		return _annotations.annotatedRenaming(method);
	}

	private String annotatedPropertyName(MethodDeclaration node) {
		return _annotations.annotatedPropertyName(node);
	}
	
	private <T extends ASTNode> T findDeclaringNode(IBinding binding) {
		return (T) _bindings.findDeclaringNode(binding);		
	}	
	
	private boolean isAnnotatedWith(final BodyDeclaration node, final String annotation) {
		return JavadocUtility.containsJavadoc(node, annotation);
	}

	private Configuration.MemberMapping configuredMappingFor(final IMethodBinding binding) {
		final IMethodBinding actual = originalMethodBinding(binding);
		final MemberMapping mapping = _configuration.mappedMember(BindingUtils.qualifiedSignature(actual));
		if (null != mapping)
			return mapping;
		
		return _configuration.mappedMember(qualifiedName(actual));
	}
	
	private String qualifiedName(IMethodBinding actual) {
		return BindingUtils.qualifiedName(actual);
	}
	
	private String qualifiedName(ITypeBinding type) {
		return BindingUtils.qualifiedName(type);
	}
	
	private String computeMethodName(IMethodBinding binding, Configuration.MemberMapping mapping) {
		if (isStaticVoidMain(binding))
			return "Main";
		String name = isNameMapping(mapping)
			? mapping.name
			: binding.getName();
		return methodName(name);
	}
	
	private boolean isStaticVoidMain(IMethodBinding binding) {
		return isStatic(binding) && "main".equals(binding.getName());
	}
	
	private boolean isStatic(IMethodBinding binding) {
		return Modifier.isStatic(binding.getModifiers());
	}
	
	private boolean isNameMapping(Configuration.MemberMapping mapping) {
	    return null != mapping && null != mapping.name;
    }
	
	private String methodName(String name) {
		return _configuration.getNamingStrategy().methodName(name);
	}
	
	private IMethodBinding originalMethodBinding(IMethodBinding binding) {
		return _bindings.originalBindingFor(binding);
	}
	
	private String repeat(char c, int count) {
		StringBuilder builder = new StringBuilder(count);
		for (int i = 0; i < count; ++i) {
			builder.append(c);
		}
		return builder.toString();
	}
	
	private String mappedTypeName(String typeName, String defaultValue) {
		return _configuration.mappedTypeName(typeName, defaultValue);
	}	
}

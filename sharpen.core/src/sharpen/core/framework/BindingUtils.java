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

/**
 * Portions of this file were taken from Eclipse:
 * /org.eclipse.jdt.ui/core extension/org/eclipse/jdt/internal/corext/dom/Bindings.java
 * and are subject to the CPL.
 * 
 * Original copyright notice on file follows:
 * /*******************************************************************************
 *  * Copyright (c) 2000, orporation and others.
 *  * All rights reserved. This program and the accompanying materials 
 *  * are made available under the terms of the Common Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/cpl-v10.html
 *  * 
 *  * Contributors:
 *  *    IBM Corporation - initial API and implementation
 *  *    Dmitry Stalnov (dstalnov@fusionone.com) - contributed fix for
 *  *       bug "inline method - doesn't handle implicit cast" (see
 *  *       https://bugs.eclipse.org/bugs/show_bug.cgi?id=24941).
 *  *******************************************************************************
 */

package sharpen.core.framework;

import org.eclipse.jdt.core.dom.*;

public class BindingUtils {	
	
	/**
	 * Finds the method in the given <code>type</code> that is overrideen by the specified <code>method<code> . Returns <code>null</code> if no such method exits.
	 * @param type The type to search the method in
	 * @param method The specified method that would override the result
	 * @return the method binding representing the method oevrriding the specified <code>method<code>
	 */
	public static IMethodBinding findOverriddenMethodInType(ITypeBinding type, IMethodBinding method) {
		if (type.getName().equals("Object") && method.getName().equals("clone"))
			return null;
		for (Object o : type.getDeclaredMethods()) {
			IMethodBinding existing = (IMethodBinding)o;
			
			if (existing.isSubsignature(method) 
				|| method.isSubsignature(existing)) {
				return existing;
			}
		}
		return null;
	}
	
	public static IMethodBinding findOverriddenMethodInTypeOrSuperclasses(ITypeBinding type, IMethodBinding method) {
		IMethodBinding found = findOverriddenMethodInType(type, method);
		if (null != found) {
			return found;
		}
		ITypeBinding superClass = type.getSuperclass();
		if (null != superClass) {
			return findOverriddenMethodInTypeOrSuperclasses(superClass, method);
		}
		return null;
	}


	/**
	 * Finds a method in the hierarchy of <code>type</code> that is overridden by </code>binding</code>.
	 * Returns <code>null</code> if no such method exists. First the super class is examined and than the implemented interfaces.
	 * @param type The type to search the method in
	 * @param binding The method that overrides
	 * @return the method binding overridden the method
	 */
	public static IMethodBinding findOverriddenMethodInHierarchy(ITypeBinding type, IMethodBinding binding) {
		return findOverriddenMethodInHierarchy(type, binding, true);
	}
	
	public static IMethodBinding findOverriddenMethodInHierarchy(ITypeBinding type, IMethodBinding binding, boolean considerInterfaces) {
		final ITypeBinding superClass= type.getSuperclass();
		if (superClass != null) {
			final IMethodBinding superClassMethod= findOverriddenMethodInHierarchy(superClass, binding);
			if (superClassMethod != null) return superClassMethod;			
		}
		final IMethodBinding method = findOverriddenMethodInType(type, binding);
		if (method != null) return method;
		
		if (considerInterfaces) {
			final ITypeBinding[] interfaces= type.getInterfaces();
			for (int i= 0; i < interfaces.length; i++) {
				final IMethodBinding interfaceMethod= findOverriddenMethodInHierarchy(interfaces[i], binding);
				if (interfaceMethod != null) return interfaceMethod;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Finds the method that is defines the given method. The returned method might not be visible.
	 * @param method The method to find
	 * @param typeResolver TODO
	 * @return the method binding representing the method
	 */
	public static IMethodBinding findMethodDefininition(IMethodBinding method, AST typeResolver) {
		if (null == method) {
			return null;
		}
		
		IMethodBinding definition = null;
		ITypeBinding type= method.getDeclaringClass();
		ITypeBinding[] interfaces= type.getInterfaces();
		for (int i= 0; i < interfaces.length; i++) {
			IMethodBinding res= findOverriddenMethodInHierarchy(interfaces[i], method);
			if (res != null) {
				definition = res; // methods from interfaces are always public and therefore visible
				break;
			}
		}
		
		if (type.getSuperclass() != null) {
			IMethodBinding res= findOverriddenMethodInHierarchy(type.getSuperclass(), method);
			if (res != null && !Modifier.isPrivate(res.getModifiers())) {
				definition = res;
			}
		} else if (type.isInterface() && null != typeResolver) {
			IMethodBinding res = findOverriddenMethodInHierarchy(typeResolver.resolveWellKnownType("java.lang.Object"), method);
			if (res != null) {
				definition = res;
			}
		}
		
		IMethodBinding def = findMethodDefininition(definition, typeResolver);
		if (def != null) {
			return def;
		}
		
		return definition;
	}

	public static boolean isVisibleInHierarchy(IMethodBinding member, IPackageBinding pack) {
		int otherflags= member.getModifiers();
		ITypeBinding declaringType= member.getDeclaringClass();
		if (Modifier.isPublic(otherflags) || Modifier.isProtected(otherflags) || (declaringType != null && declaringType.isInterface())) {
			return true;
		} else if (Modifier.isPrivate(otherflags)) {
			return false;
		}		
		return pack == declaringType.getPackage();
	}
	
	public static String qualifiedName(IMethodBinding binding) {
		return qualifiedName(binding.getDeclaringClass()) + "."
				+ binding.getName();
	}

	public static String qualifiedSignature(IMethodBinding binding) {
		StringBuffer buf = new StringBuffer();
		buf.append(qualifiedName(binding.getDeclaringClass())).append(".").append(binding.getName()).append("(");
		ITypeBinding[] parameterTypes = binding.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			if (i != 0) buf.append(",");
			buf.append(qualifiedName(parameterTypes[i]));
		}
		buf.append(")");
		return buf.toString();
	}

	public static String typeMappingKey(final ITypeBinding type) {
		ITypeBinding[] typeArguments = type.getTypeArguments();
		if (typeArguments.length == 0)
			typeArguments = type.getTypeParameters();
		if (typeArguments.length > 0) {
			return qualifiedName(type) + "<" + repeat(',', typeArguments.length - 1) + ">";
		}
		return qualifiedName(type);
	}	
	
	private static String repeat(char c, int count) {
		StringBuilder builder = new StringBuilder(count);
		for (int i = 0; i < count; ++i) {
			builder.append(c);
		}
		return builder.toString();
	}
	
	public static String qualifiedName(final ITypeBinding declaringClass) {
		String qn = declaringClass.getTypeDeclaration().getQualifiedName();
		if (qn.length() > 0)
			return qn;
		else
			return declaringClass.getQualifiedName();
	}

	public static String qualifiedName(IVariableBinding binding) {
		ITypeBinding declaringClass = binding.getDeclaringClass();
	
		if (null == declaringClass) {
			return binding.getName();
		}
		return qualifiedName(declaringClass) + "." + binding.getName();
	}

	public static boolean isStatic(IMethodBinding binding) {
		return Modifier.isStatic(binding.getModifiers());
	}
	
	public static boolean isStatic(IVariableBinding binding) {
		return Modifier.isStatic(binding.getModifiers());
	}
	
	public static boolean isStatic(MethodInvocation invocation) {
		return isStatic(invocation.resolveMethodBinding());
	}

}

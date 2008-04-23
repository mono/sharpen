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

package sharpen.core;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

public class Bindings {	
		
	/**
	 * Finds the method specified by <code>methodName<code> and </code>parameters</code> in
	 * the given <code>type</code>. Returns <code>null</code> if no such method exits.
	 * @param type The type to search the method in
	 * @param methodName The name of the method to find
	 * @param parameters The parameter types of the method to find. If <code>null</code> is passed, only the name is matched and parameters are ignored.
	 * @return the method binding representing the method
	 */
	public static IMethodBinding findMethodInType(ITypeBinding type, String methodName, ITypeBinding[] parameters) {
		if (type.isPrimitive())
			return null;
		IMethodBinding[] methods= type.getDeclaredMethods();
		for (int i= 0; i < methods.length; i++) {
			if (parameters == null) {
				if (methodName.equals(methods[i].getName()))
					return methods[i];
			} else {
				if (isEqualMethod(methods[i], methodName, parameters))
					return methods[i];
			}
		}
		return null;
	}
	
	/**
	 * Finds the method in the given <code>type</code> that is overrideen by the specified <code>method<code> . Returns <code>null</code> if no such method exits.
	 * @param type The type to search the method in
	 * @param method The specified method that would override the result
	 * @return the method binding representing the method oevrriding the specified <code>method<code>
	 */
	public static IMethodBinding findOverriddenMethodInType(ITypeBinding type, IMethodBinding method) {
		return findMethodInType(type, method.getName(), method.getParameterTypes());
	}
	
	public static IMethodBinding findOverriddenMethodInTypeOrSuperclasses(ITypeBinding type, IMethodBinding method) {
		IMethodBinding found = findOverriddenMethodInType(type, method);
		if (null == found) {
			ITypeBinding superClass = type.getSuperclass();
			if (null != superClass) {
				found = findOverriddenMethodInTypeOrSuperclasses(superClass, method);
			}
		}
		return found;
	}


	/**
	 * Finds a method in the hierarchy of <code>type</code> that is overridden by </code>binding</code>.
	 * Returns <code>null</code> if no such method exists. First the super class is examined and than the implemented interfaces.
	 * @param type The type to search the method in
	 * @param binding The method that overrrides
	 * @return the method binding overridden the method
	 */
	public static IMethodBinding findOverriddenMethodInHierarchy(ITypeBinding type, IMethodBinding binding) {
		return findOverriddenMethodInHierarchy(type, binding, true);
	}
	
	public static IMethodBinding findOverriddenMethodInHierarchy(ITypeBinding type, IMethodBinding binding, boolean considerInterfaces) {
		IMethodBinding method = null;
		ITypeBinding superClass= type.getSuperclass();
		if (superClass != null) {
			method= findOverriddenMethodInHierarchy(superClass, binding);
			if (method != null)
				return method;			
		}
		method = findOverriddenMethodInType(type, binding);
		if (method != null) {			
			return method;
		}
		if (considerInterfaces) {
			ITypeBinding[] interfaces= type.getInterfaces();
			for (int i= 0; i < interfaces.length; i++) {
				method= findOverriddenMethodInHierarchy(interfaces[i], binding);
				if (method != null)
					return method;
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
	public static IMethodBinding findMethodDefininition(IMethodBinding method, WellKnownTypeResolver typeResolver) {
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
	
	public static boolean isEqualMethod(IMethodBinding method, String methodName, ITypeBinding[] parameters) {
		if (!method.getName().equals(methodName))
			return false;
			
		ITypeBinding[] methodParameters= method.getParameterTypes();
		if (methodParameters.length != parameters.length)
			return false;
		for (int i= 0; i < parameters.length; i++) {
			if (parameters[i].getErasure() != methodParameters[i].getErasure())
				return false;
		}
		return true;
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

	public static String qualifiedName(final ITypeBinding declaringClass) {
		return declaringClass.getTypeDeclaration().getQualifiedName();
	}
}

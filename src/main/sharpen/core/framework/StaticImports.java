/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com */

package sharpen.core.framework;

import static sharpen.core.framework.BindingUtils.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

public class StaticImports {
	
	public static boolean isStaticMethodImport(ImportDeclaration imp, IMethodBinding method) {
		final IBinding binding = imp.resolveBinding();
		switch (binding.getKind()) {
		case IBinding.TYPE:
			return imp.isOnDemand() && method.getDeclaringClass() == binding;
		case IBinding.METHOD:
			IMethodBinding bound = (IMethodBinding) binding; 
			return bound.getDeclaringClass() == method.getDeclaringClass() && binding.getName().equals(method.getMethodDeclaration().getName());
		}
		return false;
	}
	
	public static boolean isStaticFieldImport(ImportDeclaration imp, IVariableBinding field) {
		final IBinding binding = imp.resolveBinding();
		if (binding.getKind() == IBinding.VARIABLE)
			return binding == field;

		return false;
	}
	
	public static boolean isStaticImport(IMethodBinding method, List imports) {
		if (!isStatic(method))
			return false;
		for (Object imp : imports)
			if (isStaticMethodImport((ImportDeclaration) imp, method))
				return true;
		
		return false;
	}
	
	public static boolean isStaticImport(IVariableBinding field, List imports) {
		if (!isStatic(field))
			return false;
		for (Object imp : imports)
			if (isStaticFieldImport((ImportDeclaration) imp, field))
				return true;
		
		return false;
	}
	
	public static IMethodBinding staticImportMethodBinding(SimpleName node, List imports) {
		if (node.getLocationInParent() != MethodInvocation.NAME_PROPERTY)
			return null;
		
		final MethodInvocation invocation = parentMethodInvocation(node);
		if (invocation.getExpression() != null)
			return null;
		
		final IMethodBinding method = invocation.resolveMethodBinding();
		
		if (!isStaticImport(method, imports))
			return null;
		
		return method;
	}
	
	private static MethodInvocation parentMethodInvocation(SimpleName node) {
		return ((MethodInvocation)node.getParent());
	}

}

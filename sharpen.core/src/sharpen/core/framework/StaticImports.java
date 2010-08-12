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
			return binding == method.getMethodDeclaration();
		}
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

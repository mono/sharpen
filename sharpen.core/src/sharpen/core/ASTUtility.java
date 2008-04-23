/* Copyright (C) 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.*;

/**
 * @exclude
 */
public class ASTUtility {
	
	public static String sourceInformation(CompilationUnit ast, ASTNode node) {
		return compilationUnitPath(ast) + ":" + lineNumber(ast, node);
	}

	public static String compilationUnitPath(CompilationUnit ast) {
		IJavaElement element = ast.getJavaElement();
		if (null == element) return "<unknown>";
		return element.getResource().getFullPath().toPortableString();
	}

	@SuppressWarnings("deprecation")
	public static int lineNumber(CompilationUnit ast, ASTNode node) {
		return ast.lineNumber(node.getStartPosition());
	}


}

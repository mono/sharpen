package sharpen.core;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

public class DefaultASTResolver implements ASTResolver {
	
	List<CompilationUnitPair> _pairs;
	
	public DefaultASTResolver(List<CompilationUnitPair> pairs) {
		_pairs = pairs;
	}

	public ASTNode findDeclaringNode(IBinding binding) {
		for (CompilationUnitPair pair : _pairs) {
			ASTNode node = pair.ast.findDeclaringNode(binding);
			if (null != node) return node;
		}
		
		return null;
	}
}

package sharpen.core;

import org.eclipse.jdt.core.dom.*;

public interface ASTResolver {
	ASTNode findDeclaringNode(IBinding binding);
}

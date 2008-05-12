package sharpen.core.csharp.ast;

import java.util.*;

public interface CSTypeArgumentProvider {
	
	List<CSTypeReferenceExpression> typeArguments();
	
	void addTypeArgument(CSTypeReferenceExpression typeArgument);

}

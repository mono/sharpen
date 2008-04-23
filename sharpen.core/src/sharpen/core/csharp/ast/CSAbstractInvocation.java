package sharpen.core.csharp.ast;

import java.util.*;

public abstract class CSAbstractInvocation extends CSExpression {
	
	protected List<CSExpression> _arguments = new ArrayList<CSExpression>();
	
	public CSAbstractInvocation() {
	}

	public CSAbstractInvocation(CSExpression[] args) {
		for (CSExpression arg : args) {
			addArgument(arg);
		}
	}
	
	public void addArgument(CSExpression argument) {
		_arguments.add(argument);
	}
	
	public List<CSExpression> arguments() {
		return Collections.unmodifiableList(_arguments);
	}

}
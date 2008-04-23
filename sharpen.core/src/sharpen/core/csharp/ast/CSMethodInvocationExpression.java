package sharpen.core.csharp.ast;


public class CSMethodInvocationExpression extends CSAbstractInvocation {
	
	private CSExpression _expression;
	
	public CSMethodInvocationExpression(CSExpression expression, CSExpression ...args) {
		super(args);
		_expression = expression;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSNode expression() {
		return _expression;
	}

}

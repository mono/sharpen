package sharpen.core.csharp.ast;

public class CSExpressionStatement extends CSStatement {
	
	private CSExpression _expression;

	public CSExpressionStatement(int startPosition, CSExpression expression) {
		super(startPosition); 
		assert null != expression;
		_expression = expression;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSNode expression() {
		return _expression;
	}

}

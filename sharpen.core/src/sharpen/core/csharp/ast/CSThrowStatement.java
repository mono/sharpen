package sharpen.core.csharp.ast;

public class CSThrowStatement extends CSStatement {
	
	private CSExpression _expression;

	public CSThrowStatement(int startPosition, CSExpression expression) {
		super(startPosition);
		_expression = expression;
	}
	
	public CSExpression expression() {
		return _expression;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

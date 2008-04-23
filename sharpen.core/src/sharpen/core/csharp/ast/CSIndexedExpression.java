package sharpen.core.csharp.ast;

public class CSIndexedExpression extends CSExpression {

	private CSExpression _expression;
	private CSExpression _index;

	public CSIndexedExpression(CSExpression expression, CSExpression index) {
		_expression = expression;
		_index = index;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSExpression expression() {
		return _expression;
	}
	
	public CSExpression index() {
		return _index;
	}
}

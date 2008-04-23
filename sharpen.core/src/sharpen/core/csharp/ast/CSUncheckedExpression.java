package sharpen.core.csharp.ast;

public class CSUncheckedExpression extends CSExpression {

	private CSExpression _expression;

	public CSUncheckedExpression(CSExpression expression) {
		_expression = expression;
	}
	
	public CSExpression expression() {
		return _expression;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

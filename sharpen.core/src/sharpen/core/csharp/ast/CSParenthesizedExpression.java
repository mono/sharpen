package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSParenthesizedExpression extends CSExpression {
	
	private CSExpression _expression;

	public CSParenthesizedExpression(CSExpression expression) {
		_expression = expression;
	}
	
	public CSExpression expression() {
		return _expression;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

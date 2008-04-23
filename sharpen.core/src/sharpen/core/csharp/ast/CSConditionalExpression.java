package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSConditionalExpression extends CSExpression {

	private CSExpression _expression;
	private CSExpression _trueExpression;
	private CSExpression _falseExpression;

	public CSConditionalExpression(CSExpression expression, CSExpression trueExpression, CSExpression falseExpression) {
		_expression = expression;
		_trueExpression = trueExpression;
		_falseExpression = falseExpression;
	}
	
	public CSExpression expression() {
		return _expression;
	}
	
	public CSExpression trueExpression() {
		return _trueExpression;
	}
	
	public CSExpression falseExpression() {
		return _falseExpression;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

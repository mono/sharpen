package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public abstract class CSUnaryExpression extends CSExpression {

	protected String _operator;
	protected CSExpression _operand;
	
	public CSUnaryExpression(String operator, CSExpression operand) {
		_operator = operator;
		_operand = operand;
	}
	
	public String operator() {
		return _operator;
	}
	
	public CSExpression operand() {
		return _operand;
	}

}

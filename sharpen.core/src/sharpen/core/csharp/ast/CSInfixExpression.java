package sharpen.core.csharp.ast;

public class CSInfixExpression extends CSExpression {

	private String _operator;
	private CSExpression _lhs;
	private CSExpression _rhs;

	public CSInfixExpression(String operator, CSExpression lhs, CSExpression rhs) {
		_operator = operator;
		_lhs = lhs;
		_rhs = rhs;
	}
	
	public String operator() {
		return _operator;
	}
	
	public CSExpression lhs() {
		return _lhs;
	}
	
	public CSExpression rhs() {
		return _rhs;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

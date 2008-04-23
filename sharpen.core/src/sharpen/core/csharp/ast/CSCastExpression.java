package sharpen.core.csharp.ast;

public class CSCastExpression extends CSExpression {

	private CSTypeReferenceExpression _type;
	private CSExpression _expression;

	public CSCastExpression(CSTypeReferenceExpression type, CSExpression expression) {
		if (null == type) {
			throw new IllegalArgumentException("type");
		}
		if (null == expression) {
			throw new IllegalArgumentException("expression");
		}
		_type = type;
		_expression = expression;
	}
	
	public CSTypeReferenceExpression type() {
		return _type;
	}
	
	public CSExpression expression() {
		return _expression;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

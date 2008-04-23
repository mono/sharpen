package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSMemberReferenceExpression extends CSReferenceExpression {
	
	private CSExpression _expression;

	public CSMemberReferenceExpression(CSExpression expression, String name) {
		super(name);
		_expression = expression;
	}
	
	public CSExpression expression() {
		return _expression;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSPrefixExpression extends CSUnaryExpression {

	public CSPrefixExpression(String operator, CSExpression operand) {
		super(operator, operand);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

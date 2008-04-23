package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSPostfixExpression extends CSUnaryExpression {

	public CSPostfixExpression(String operator, CSExpression operand) {
		super(operator, operand);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSNullLiteralExpression extends CSExpression {

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSBaseExpression extends CSExpression {

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

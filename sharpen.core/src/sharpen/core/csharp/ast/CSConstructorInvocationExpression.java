package sharpen.core.csharp.ast;

public class CSConstructorInvocationExpression extends CSMethodInvocationExpression {

	public CSConstructorInvocationExpression(CSExpression expression) {
		super(expression);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

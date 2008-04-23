package sharpen.core.csharp.ast;

public class CSWhileStatement extends CSBlockStatement {

	public CSWhileStatement(int startPosition, CSExpression expression) {
		super(startPosition, expression);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

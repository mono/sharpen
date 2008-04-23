package sharpen.core.csharp.ast;

public class CSDoStatement extends CSWhileStatement {
	public CSDoStatement(int startPosition, CSExpression expression) {
		super(startPosition, expression);
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

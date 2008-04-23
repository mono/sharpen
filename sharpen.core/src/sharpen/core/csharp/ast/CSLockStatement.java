package sharpen.core.csharp.ast;

public class CSLockStatement extends CSBlockStatement {

	public CSLockStatement(int startPosition, CSExpression expression) {
		super(startPosition, expression);
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

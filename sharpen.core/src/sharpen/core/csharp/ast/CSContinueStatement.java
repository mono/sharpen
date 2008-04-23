package sharpen.core.csharp.ast;

public class CSContinueStatement extends CSStatement {

	public CSContinueStatement(int startPosition) {
		super(startPosition);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

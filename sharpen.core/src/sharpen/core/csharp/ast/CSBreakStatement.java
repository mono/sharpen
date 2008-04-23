package sharpen.core.csharp.ast;

public class CSBreakStatement extends CSStatement {

	public CSBreakStatement(int startPosition) {
		super(startPosition);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

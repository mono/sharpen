package sharpen.core.csharp.ast;

public class CSUsingStatement extends CSExpressionStatement {
	private CSBlock _body = new CSBlock();

	public CSUsingStatement(int startPosition, CSExpression expression) {
		super(startPosition, expression);
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public CSBlock body() {
		return _body ;
	}
}

package sharpen.core.csharp.ast;

public class CSIfStatement extends CSStatement {

	private CSExpression _expression;
	
	private CSBlock _trueBlock = new CSBlock();
	
	private CSBlock _falseBlock = new CSBlock();

	public CSIfStatement(int startPosition, CSExpression expression) {
		super(startPosition);
		_expression = expression;
	}
	
	public CSExpression expression() {
		return _expression;
	}
	
	public CSBlock trueBlock() {
		return _trueBlock;
	}
	
	public CSBlock falseBlock() {
		return _falseBlock;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

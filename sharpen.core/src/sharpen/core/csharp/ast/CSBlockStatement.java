package sharpen.core.csharp.ast;

public abstract class CSBlockStatement extends CSStatement {

	protected CSExpression _expression;
	private CSBlock _body = new CSBlock();

	protected CSBlockStatement(int startPosition, CSExpression expression) {
		super(startPosition);
		_expression = expression;
	}

	public CSExpression expression() {
		return _expression;
	}

	public CSBlock body() {
		return _body;
	}

}

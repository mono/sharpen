package sharpen.core.csharp.ast;

public class CSCatchClause extends CSNode {

	private CSVariableDeclaration _exception;
	private CSBlock _body = new CSBlock();
	
	public CSCatchClause() {
	}

	public CSCatchClause(CSVariableDeclaration exception) {
		_exception = exception;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSBlock body() {
		return _body ;
	}

	public CSVariableDeclaration exception() {
		return _exception;
	}

	public void anonymous(boolean an) {
		if (an && null != _exception) {
			_exception.name(null);
		}
	}
}

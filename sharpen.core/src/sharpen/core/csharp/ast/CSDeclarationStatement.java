package sharpen.core.csharp.ast;

public class CSDeclarationStatement extends CSStatement {

	private CSVariableDeclaration _declaration;

	public CSDeclarationStatement(int startPosition, CSVariableDeclaration declaration) {
		super(startPosition);
		_declaration = declaration;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public CSVariableDeclaration declaration() {
		return _declaration;
	}
}

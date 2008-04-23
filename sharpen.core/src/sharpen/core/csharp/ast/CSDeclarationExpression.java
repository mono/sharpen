package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSDeclarationExpression extends CSExpression {

	private CSVariableDeclaration _declaration;

	public CSDeclarationExpression(CSVariableDeclaration declaration) {
		_declaration = declaration;
	}
	
	public CSVariableDeclaration declaration() {
		return _declaration;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

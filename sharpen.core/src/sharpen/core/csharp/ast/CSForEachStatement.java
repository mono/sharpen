/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;

public class CSForEachStatement extends CSBlockStatement {

	private CSVariableDeclaration _variable;

	public CSForEachStatement(int startPosition, CSExpression expression) {
		super(startPosition, expression);
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void variable(CSVariableDeclaration variable) {
		_variable = variable;
	}

	public CSVariableDeclaration variable() {
		return _variable;
	}

}

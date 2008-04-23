package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSBlock extends CSNode {
	
	private List<CSStatement> _statements = new ArrayList<CSStatement>();
	
	public boolean isEmpty() {
		return _statements.isEmpty();
	}
	
	public void addStatement(CSStatement statement) {
		_statements.add(statement);
	}
	
	public void addStatement(CSExpression expression) {
		_statements.add(newStatement(expression));
	}

	private CSExpressionStatement newStatement(CSExpression expression) {
		return new CSExpressionStatement(CSExpressionStatement.UNKNOWN_START_POSITION, expression);
	}
	
	public void addStatement(int index, CSExpression expression) {
		_statements.add(index, newStatement(expression));
	}

	
	public List<CSStatement> statements() {
		return Collections.unmodifiableList(_statements);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

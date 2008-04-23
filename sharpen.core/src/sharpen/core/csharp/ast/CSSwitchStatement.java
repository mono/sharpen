package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.List;

public class CSSwitchStatement extends CSStatement {

	private CSExpression _expression;
	private List<CSCaseClause> _caseClauses = new ArrayList<CSCaseClause>();

	public CSSwitchStatement(int startPosition, CSExpression expression) {
		super(startPosition);
		_expression = expression;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void addCase(CSCaseClause caseClause) {
		if (null == caseClause) {
			throw new IllegalArgumentException("caseClause");
		}
		_caseClauses.add(caseClause);
	}
	
	public List<CSCaseClause> caseClauses() {
		return java.util.Collections.unmodifiableList(_caseClauses );
	}

	public CSExpression expression() {
		return _expression;
	}

}

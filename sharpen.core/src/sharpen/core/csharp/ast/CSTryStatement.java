package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSTryStatement extends CSStatement {
	
	public CSTryStatement(int startPosition) {
		super(startPosition);
	}

	private CSBlock _body = new CSBlock();
	
	private List<CSCatchClause> _catchClauses = new ArrayList<CSCatchClause>();

	private CSBlock _finallyBlock;
	
	public CSBlock body() {
		return _body ;
	}
	
	public void addCatchClause(CSCatchClause clause) {
		_catchClauses.add(clause);
	}
	
	public List<CSCatchClause> catchClauses() {
		return Collections.unmodifiableList(_catchClauses);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void finallyBlock(CSBlock finallyBlock) {
		_finallyBlock = finallyBlock;
	}
	
	public CSBlock finallyBlock() {
		return _finallyBlock;
	}
}

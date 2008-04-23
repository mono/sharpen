package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rodrigob
 *
 */
public class CSCaseClause extends CSNode {

	private boolean _isDefault;
	private CSBlock _body = new CSBlock();
	private List<CSExpression> _expressions = new ArrayList<CSExpression>();

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSBlock body() {
		return _body;
	}
	
	public void isDefault(boolean isDefault) {
		_isDefault = isDefault;
	}
	
	public boolean isDefault() {
		return _isDefault;
	}

	public void addExpression(CSExpression expression) {
		if (null == expression) {
			throw new IllegalArgumentException("expression");
		}
		_expressions.add(expression);
	}
	
	public List<CSExpression> expressions() {
		return Collections.unmodifiableList(_expressions);
	}

}

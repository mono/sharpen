package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author rodrigob
 *
 */
public class CSArrayInitializerExpression extends CSExpression {

	private List<CSExpression> _expressions = new ArrayList<CSExpression>();

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void addExpression(CSExpression expression) {
		_expressions.add(expression);
	}
	
	public List<CSExpression> expressions() {
		return Collections.unmodifiableList(_expressions);
	}

}

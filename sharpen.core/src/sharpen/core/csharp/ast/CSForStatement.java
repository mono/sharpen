package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSForStatement extends CSBlockStatement {	
	
	private List<CSExpression> _updaters = new ArrayList<CSExpression>();
	private List<CSExpression> _initializers = new ArrayList<CSExpression>();

	public CSForStatement(int startPosition, CSExpression expression) {
		super(startPosition, expression);
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void addInitializer(CSExpression initializer) {
		_initializers.add(initializer);
	}
	
	public List<CSExpression> initializers() {
		return Collections.unmodifiableList(_initializers);
	}

	public void addUpdater(CSExpression expression) {
		_updaters.add(expression);
	}
	
	public List<CSExpression> updaters() {
		return Collections.unmodifiableList(_updaters);
	}
}

package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSConstructor extends CSMethodBase {
	
	private CSConstructorInvocationExpression _chainedConstructorInvocation;
	
	private CSConstructorModifier _modifier;

	public CSConstructor() {
		this(CSConstructorModifier.None);
	}
	
	public CSConstructor(CSConstructorModifier modifier) {
		super("ctor");
		_modifier = modifier;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void chainedConstructorInvocation(CSConstructorInvocationExpression cie) {
		_chainedConstructorInvocation = cie;
	}

	public CSConstructorInvocationExpression chainedConstructorInvocation() {
		return _chainedConstructorInvocation;
	}
	
	public CSConstructorModifier modifier() {
		return _modifier;
	}
	
	public boolean isStatic() {
		return _modifier == CSConstructorModifier.Static;
	}	
}

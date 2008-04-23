package sharpen.core.csharp.ast;

public abstract class CSMetaMember extends CSTypedMember {

	private CSMethodModifier _modifier = CSMethodModifier.None;

	public CSMetaMember(String name, CSTypeReferenceExpression type) {
		super(name, type);
	}
	
	public boolean isAbstract() {
		return _modifier == CSMethodModifier.Abstract
			|| _modifier == CSMethodModifier.AbstractOverride;
	}
	
	public void modifier(CSMethodModifier modifier) {
		_modifier = modifier;
	}

	public CSMethodModifier modifier() {
		return _modifier;
	}

}
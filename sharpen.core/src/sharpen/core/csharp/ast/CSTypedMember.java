package sharpen.core.csharp.ast;


public abstract class CSTypedMember extends CSMember {
	
	private CSTypeReferenceExpression _type;
	
	protected CSTypedMember(String name, CSTypeReferenceExpression type) {
		super(name);
		_type = type;
	}
	
	public CSTypeReferenceExpression type() {
		return _type;
	}
	
	public void type(CSTypeReferenceExpression type) {
		_type = type;
	}
}

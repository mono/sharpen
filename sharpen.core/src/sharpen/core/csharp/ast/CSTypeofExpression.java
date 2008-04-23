package sharpen.core.csharp.ast;

public class CSTypeofExpression extends CSExpression {

	private CSTypeReferenceExpression _type;

	public CSTypeofExpression(CSTypeReferenceExpression typeName) {
		_type = typeName;
	}
	
	public CSTypeReferenceExpression type() {
		return _type;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

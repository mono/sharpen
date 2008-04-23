package sharpen.core.csharp.ast;

public class CSVariableDeclaration extends CSNode {
	
	private String _name;
	private CSTypeReferenceExpression _type;
	private CSExpression _initializer;
	
	public CSVariableDeclaration(String name, CSTypeReferenceExpression type) {
		this(name, type, null);
	}

	public CSVariableDeclaration(String name, CSTypeReferenceExpression type, CSExpression initializer) {
		_name = name;
		_type = type;
		_initializer = initializer;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSTypeReferenceExpression type() {
		return _type;
	}
	
	public void type(CSTypeReferenceExpression type) {
		_type = type;
	}
	
	public String name() {
		return _name;
	}
	
	public void initializer(CSExpression initializer) {
		_initializer = initializer;
	}
	
	public CSExpression initializer() {
		return _initializer;
	}

	public void name(String name) {
		_name = name;
	}
}

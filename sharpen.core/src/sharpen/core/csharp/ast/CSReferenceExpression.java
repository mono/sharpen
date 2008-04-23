package sharpen.core.csharp.ast;

public class CSReferenceExpression extends CSExpression {
	
	private String _name;

	public CSReferenceExpression(String name) {
		_name = name;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public String name() {
		return _name;
	}

}

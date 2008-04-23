package sharpen.core.csharp.ast;

public class CSUsing extends CSNode {
	
	private String _namespace;
	
	public CSUsing(String namespace) {
		_namespace = namespace;
	}
	
	public String namespace() {
		return _namespace;
	}

	public void namespace(String namespace) {
		_namespace = namespace;
	}
	
	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

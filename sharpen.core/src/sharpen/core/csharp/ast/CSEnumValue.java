package sharpen.core.csharp.ast;

public class CSEnumValue extends CSNode {

	private final String _name;

	public CSEnumValue(String name) {
		_name = name;
	}
	
	public String name() {
		return _name;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

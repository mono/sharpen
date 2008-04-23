package sharpen.core.csharp.ast;

public class CSAttribute extends CSAbstractInvocation {

	private String _name;

	public CSAttribute(String attributeName) {
		_name = attributeName;
	}
	
	public String name() {
		return _name;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

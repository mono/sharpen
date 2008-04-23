package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSDocAttributeNode extends CSDocNode {
	
	private String _name;
	private String _value;

	public CSDocAttributeNode(String name, String value) {
		_name = name;
		_value = value;
	}
	
	public String name() {
		return _name;
	}
	
	public String value() {
		return _value;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

package sharpen.core.csharp.ast;

public class CSDocTextNode extends CSDocNode {
	
	private String _text;

	public CSDocTextNode(String text) {
		if (null == text) {
			throw new IllegalArgumentException("text");
		}
		_text = text;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public String text() {
		return _text;
	}

}

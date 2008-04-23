package sharpen.core.csharp.ast;


public abstract class CSType extends CSMember {
	
	private int _sourceLength;

	CSType(String name) {
		super(name);
	}
	
	public void sourceLength(int value) {
		_sourceLength = value;
	}
	
	public int sourceLength() {
		return _sourceLength;
	}
}

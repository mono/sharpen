package sharpen.core.csharp.ast;

public abstract class CSNode {
	
	public static final int UNKNOWN_START_POSITION = Integer.MIN_VALUE;

	private int _startPosition;
	
	public CSNode() {
		this(UNKNOWN_START_POSITION);
	}
	
	public CSNode(int startPosition) {
		_startPosition = startPosition;
	}
	
	public int startPosition() {
		return _startPosition;
	}
	
	public void startPosition(int value) {
		_startPosition = value;
	}

	public abstract void accept(CSVisitor visitor);	
}

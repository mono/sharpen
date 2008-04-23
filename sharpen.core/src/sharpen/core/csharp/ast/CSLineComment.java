/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;


public class CSLineComment extends CSNode {

	private final int _startPosition;
	private final String _text;

	public CSLineComment(int startPosition, String text) {
		_startPosition = startPosition;
		_text = text;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public int startPosition() {
		return _startPosition;
	}

	public String text() {
		return _text;
	}

}

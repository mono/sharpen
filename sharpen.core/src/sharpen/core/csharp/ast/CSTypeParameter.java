/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;


public class CSTypeParameter extends CSNode {

	private final String _name;

	public CSTypeParameter(String name) {
		_name = name;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public String name() {
		return _name;
	}

}

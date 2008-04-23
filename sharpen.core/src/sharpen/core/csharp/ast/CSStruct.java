/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;


public class CSStruct extends CSTypeDeclaration {

	public CSStruct(String name) {
		super(name);
	}
	
	@Override
	public boolean isSealed() {
		return true;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

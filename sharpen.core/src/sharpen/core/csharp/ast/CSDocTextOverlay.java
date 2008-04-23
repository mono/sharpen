/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;


public class CSDocTextOverlay extends CSDocTextNode {

	public CSDocTextOverlay(String text) {
		super(text);
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;

public class CSArrayTypeReference extends CSTypeReferenceExpression {

	private final CSTypeReferenceExpression _elementType;
	private final int _dimensions;

	public CSArrayTypeReference(CSTypeReferenceExpression elementType, int dimensions) {
		_elementType = elementType;
		_dimensions = dimensions;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSTypeReferenceExpression elementType() {
		return _elementType;
	}
	
	public int dimensions() {
		return _dimensions;
	}

}

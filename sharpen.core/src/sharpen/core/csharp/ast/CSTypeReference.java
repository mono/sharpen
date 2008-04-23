/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core.csharp.ast;

import java.util.*;

public class CSTypeReference extends CSTypeReferenceExpression {

	private final String _typeName;
	private List<CSTypeReferenceExpression> _arguments;

	public CSTypeReference(String typeName) {
		_typeName = typeName;
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public String typeName() {
		return _typeName;
	}
	
	@Override
	public String toString() {
		return _typeName;
	}
	
	@SuppressWarnings("unchecked")
	public List<CSTypeReferenceExpression> typeArguments() {
		if (null == _arguments) {
			return Collections.EMPTY_LIST;
		}
		return Collections.unmodifiableList(_arguments);
	}

	public void addTypeArgument(CSTypeReferenceExpression typeArgument) {
		if (null == _arguments) {
			_arguments = new ArrayList<CSTypeReferenceExpression>();
		}
		_arguments.add(typeArgument);
	}

}

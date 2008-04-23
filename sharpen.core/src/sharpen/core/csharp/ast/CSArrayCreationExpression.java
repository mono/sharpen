package sharpen.core.csharp.ast;

public class CSArrayCreationExpression extends CSExpression {

	private CSTypeReferenceExpression _elementType;
	private CSArrayInitializerExpression _initializer;
	private CSExpression _length;

	public CSArrayCreationExpression(CSTypeReferenceExpression elementType) {
		_elementType = elementType;
	}
	
	public CSArrayCreationExpression(CSTypeReferenceExpression elementType, CSExpression length) {
		this(elementType);
		_length = length;
	}

	public CSTypeReferenceExpression elementType() {
		return _elementType;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void initializer(CSArrayInitializerExpression initializer) {
		_initializer = initializer;
	}
	
	public CSArrayInitializerExpression initializer() {
		return _initializer;
	}

	public void length(CSExpression expression) {
		_length = expression;
	}

	public CSExpression length() {
		return _length;
	}

}

package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSCharLiteralExpression extends CSExpression {
	
	private String _escapedValue;

	public CSCharLiteralExpression(String escapedValue) {
		_escapedValue = escapedValue;
	}
	
	public String escapedValue() {
		return _escapedValue;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

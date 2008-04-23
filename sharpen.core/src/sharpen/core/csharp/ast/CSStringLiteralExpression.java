package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSStringLiteralExpression extends CSExpression {

	private String _escapedValue;

	public CSStringLiteralExpression(String escapedValue) {
		_escapedValue = escapedValue;
	}
	
	public String escapedValue() {
		return _escapedValue;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

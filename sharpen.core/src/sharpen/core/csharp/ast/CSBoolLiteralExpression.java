package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSBoolLiteralExpression extends CSExpression {

	private boolean _booleanValue;

	public CSBoolLiteralExpression(boolean booleanValue) {
		_booleanValue = booleanValue;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public boolean booleanValue() {
		return _booleanValue;
	}

}

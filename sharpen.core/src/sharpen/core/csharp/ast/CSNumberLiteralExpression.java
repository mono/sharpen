package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSNumberLiteralExpression extends CSExpression {
	
	private String _token;

	public CSNumberLiteralExpression(String token) {
		_token = token;
	}
	
	public String token() {
		return _token;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

package sharpen.core.csharp.ast;

/**
 * @author rodrigob
 *
 */
public class CSDestructor extends CSMethodBase {
	
	public CSDestructor() {
		super("");
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

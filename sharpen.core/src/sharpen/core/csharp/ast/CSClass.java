package sharpen.core.csharp.ast;


/**
 * @author rodrigob
 * 
 */
public class CSClass extends CSTypeDeclaration {

	private CSClassModifier _modifier = CSClassModifier.None;
	
	public CSClass(String name, CSClassModifier modifier) {
		super(name);
		_modifier = modifier;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void modifier(CSClassModifier modifier) {
		_modifier = modifier;
	}

	public CSClassModifier modifier() {
		return _modifier;
	}
	
	public boolean isSealed() {
		return CSClassModifier.Sealed == _modifier;
	}
}

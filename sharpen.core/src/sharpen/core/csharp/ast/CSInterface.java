package sharpen.core.csharp.ast;

public class CSInterface extends CSTypeDeclaration {
	public CSInterface(String name) {
		super(name);
	}
	
	public boolean isInterface() {
		return true;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
}

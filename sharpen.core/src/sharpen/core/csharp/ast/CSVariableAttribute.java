package sharpen.core.csharp.ast;

public class CSVariableAttribute extends CSAttribute {

	public CSVariableAttribute(String attributeName) {
		super(attributeName);
	}

    @Override
    public boolean isParameter() {
        return true;
    }
}

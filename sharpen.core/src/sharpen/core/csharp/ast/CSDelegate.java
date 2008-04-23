package sharpen.core.csharp.ast;

import java.util.*;

public class CSDelegate extends CSType {
	
	private List<CSVariableDeclaration> _parameters = new ArrayList<CSVariableDeclaration>();

	public CSDelegate(String name) {
		super(name);
	}
	
	public void addParameter(String name, CSTypeReference type) {
		_parameters.add(new CSVariableDeclaration(name, type));
	}

	public List<CSVariableDeclaration> parameters() {
		return Collections.unmodifiableList(_parameters);
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

}

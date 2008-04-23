package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CSMethodBase extends CSMember implements CSParameterized {
	
	private List<CSVariableDeclaration> _parameters = new ArrayList<CSVariableDeclaration>();
	
	private CSBlock _body = new CSBlock();

	public CSMethodBase(String name) {
		super(name);
	}

	public void addParameter(CSVariableDeclaration parameter) {
		_parameters.add(parameter);
	}
	
	public void addParameter(int index, CSVariableDeclaration parameter) {
		_parameters.add(index, parameter);
	}
	
	public void addParameter(String name, CSTypeReferenceExpression type) {
		addParameter(new CSVariableDeclaration(name, type));
	}

	public void removeParameter(int index) {
		_parameters.remove(index);
	}

	public List<CSVariableDeclaration> parameters() {
		return Collections.unmodifiableList(_parameters);
	}
	
	public CSBlock body() {
		return _body;
	}

	public String signature() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(name());
		buffer.append("(");
		
		boolean first = true;
		for (CSVariableDeclaration p : _parameters) {
			if (!first) buffer.append(", ");
			buffer.append(p.type());
			first = false;
		}
		buffer.append(")");
		return buffer.toString();
	}
}

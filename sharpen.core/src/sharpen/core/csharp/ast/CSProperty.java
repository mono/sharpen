package sharpen.core.csharp.ast;

import java.util.*;

public class CSProperty extends CSMetaMember implements CSParameterized {

	public static final String INDEXER = "this";
	
	private List<CSVariableDeclaration> _parameters;
	
	private CSBlock _getter;
	
	private CSBlock _setter;

	public CSProperty(String name, CSTypeReferenceExpression type) {
		super(name, type);
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public void addParameter(CSVariableDeclaration parameter) {
		if (null == _parameters) {
			_parameters = new ArrayList<CSVariableDeclaration>();
		}
		_parameters.add(parameter);
	}
	
	public List<CSVariableDeclaration> parameters() {
		if (null == _parameters) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(_parameters);
	}

	public void getter(CSBlock getter) {
		_getter = getter;
	}
	
	public CSBlock getter() {
		return _getter;
	}
	
	public void setter(CSBlock block) {
		_setter = block;
	}
	
	public CSBlock setter() {
		return _setter;
	}

	public boolean isIndexer() {
		return INDEXER.equals(name());
	}
}

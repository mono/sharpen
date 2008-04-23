package sharpen.core.csharp.ast;

import java.util.*;

public class CSMethod extends CSMethodBase implements CSTypeParameterProvider {
	
	private CSMethodModifier _modifier = CSMethodModifier.None;
	
	private CSTypeReferenceExpression _returnType;
	
	private List<CSTypeParameter> _typeParameters = new ArrayList<CSTypeParameter>();
	
	public CSMethod(String name) {
		super(name);
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public void modifier(CSMethodModifier modifier) {
		_modifier = modifier;
	}	

	public CSMethodModifier modifier() {
		return _modifier;
	}

	public void returnType(CSTypeReferenceExpression returnType) {
		_returnType = returnType;
	}
	
	public CSTypeReferenceExpression returnType() {
		return _returnType;
	}

	public boolean isAbstract() {
		return CSMethodModifier.Abstract == _modifier
			|| CSMethodModifier.AbstractOverride == _modifier;
	}
	
	public void addTypeParameter(CSTypeParameter typeParameter) {
		_typeParameters.add(typeParameter);
	}
	
	public List<CSTypeParameter> typeParameters() {
		return Collections.unmodifiableList(_typeParameters);
	}

}

package sharpen.core.csharp.ast;

import java.util.*;

public abstract class CSTypeDeclaration extends CSType implements CSTypeParameterProvider {
	
	protected List<CSMember> _members = new ArrayList<CSMember>();
	
	protected List<CSTypeReferenceExpression> _baseTypes = new ArrayList<CSTypeReferenceExpression>();
	
	protected List<CSTypeParameter> _typeParameters = new ArrayList<CSTypeParameter>();

	private boolean _partial;

	CSTypeDeclaration(String name) {
		super(name);
	}
	
	public boolean isSealed() {
		return false;
	}
	
	public boolean isInterface() {
		return false;
	}

	public void addMember(CSMember member) {
		_members.add(member);
	}

	public List<CSMember> members() {
		return Collections.unmodifiableList(_members);
	}
	
	public List<CSConstructor> constructors() {
		ArrayList<CSConstructor> ctors = new ArrayList<CSConstructor>();
		for (CSMember member : _members) {
			if (member instanceof CSConstructor) {
				ctors.add((CSConstructor) member);
			}
		}
		return Collections.unmodifiableList(ctors);
	}

	public void addBaseType(CSTypeReferenceExpression typeRef) {
		_baseTypes.add(typeRef);
	}
	
	public void clearBaseTypes() {
		_baseTypes.clear();
	}
	
	public List<CSTypeReferenceExpression> baseTypes() {
		return Collections.unmodifiableList(_baseTypes);
	}

	public void addTypeParameter(CSTypeParameter typeParameter) {
		_typeParameters.add(typeParameter);
	}
	
	public List<CSTypeParameter> typeParameters() {
		return Collections.unmodifiableList(_typeParameters);
	}

	public void partial(boolean partial) {
		_partial = partial;
	}
	
	public boolean partial() {
		return _partial;
	}

	public CSMember getMember(String name) {
		for (CSMember member : _members) {
			if (member.name().equals(name)) {
				return member;
			}
		}
		return null;
	}
}

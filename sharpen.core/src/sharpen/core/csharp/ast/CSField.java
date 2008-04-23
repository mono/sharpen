package sharpen.core.csharp.ast;

import java.util.*;

public class CSField extends CSTypedMember {

	private CSExpression _initializer;
	private Set<CSFieldModifier> _modifiers = new LinkedHashSet<CSFieldModifier>();	
	
	public CSField(String name, CSTypeReferenceExpression type, CSVisibility visibility) {
		this(name, type, visibility, null);
	}

	public CSField(String name, CSTypeReferenceExpression type, CSVisibility visibility, CSExpression initializer) {
		super(name, type);
		_visibility = visibility;
		_initializer = initializer;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}
	
	public CSExpression initializer() {
		return _initializer;
	}
	
	public void initializer(CSExpression initializer) {
		_initializer = initializer;
	}

	public void addModifier(CSFieldModifier modifier) {
		_modifiers.add(modifier);
	}
	
	public void removeModifier(CSFieldModifier modifier) {
		_modifiers.remove(modifier);
	}
	
	public Set<CSFieldModifier> modifiers() {
		return Collections.unmodifiableSet(_modifiers);
	}		
}

package sharpen.core.csharp.ast;

import java.util.*;

public class CSEnum extends CSType {
	
	private final ArrayList<CSEnumValue> _values = new ArrayList<CSEnumValue>();

	public CSEnum(String name) {
		super(name);
	}
	
	public void addValue(String name) {
		_values.add(new CSEnumValue(name));
	}

	@Override
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public List<CSEnumValue> values() {
		return Collections.unmodifiableList(_values);
	}

}

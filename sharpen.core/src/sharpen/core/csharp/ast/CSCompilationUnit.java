package sharpen.core.csharp.ast;

import java.util.*;

public class CSCompilationUnit extends CSNode {


	private String _elementName;
	private boolean _ignore;
	private String _namespace;

	private final List<CSUsing> _usings = new ArrayList<CSUsing>();
	private final List<CSType> _types = new ArrayList<CSType>();
	private final List<CSLineComment> _comments = new ArrayList<CSLineComment>();

	public void namespace(String value) {
		_namespace = value;
	}
	
	public String namespace() {
		return _namespace;
	}
	
	public void addUsing(CSUsing using) {
		if (namespaceAlreadyUsed(using.namespace()))
			return;

		_usings.add(using);
	}
	
	private boolean namespaceAlreadyUsed(String namespace) {
		for (CSUsing us : _usings)
			if (us.namespace().equals(namespace))
				return true;

		return false;
	}

	public void addType(CSType type) {
		_types.add(type);
	}
	
	public Iterable<CSUsing> usings() {
		return Collections.unmodifiableList(_usings);
	}
	
	public Iterable<CSType> types() {
		return Collections.unmodifiableList(_types);
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void insertTypeBefore(CSType type, CSType anchor) {
		_types.add(_types.indexOf(anchor), type);
	}

	public boolean ignore() {
		return _ignore;
	}

	public void ignore(boolean value) {
		_ignore = value;
	}
	
	public String elementName() {
		return _elementName;
	}

	public void elementName(String elementName) {
		_elementName = elementName;		
	}

	public void addComment(CSLineComment lineComment) {
		_comments.add(lineComment);
	}

	public List<CSLineComment> comments() {
		return Collections.unmodifiableList(_comments);
	}
}

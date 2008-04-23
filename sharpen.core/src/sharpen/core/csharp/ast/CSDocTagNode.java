package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSDocTagNode extends CSDocNode {
	
	private List<CSDocNode> _fragments;
	private List<CSDocAttributeNode> _attributes;
	private String _tagName;

	public CSDocTagNode(String tagName) {
		_tagName = tagName;
	}

	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public void addFragment(CSDocNode node) {
		if (null == _fragments) {
			 _fragments = new ArrayList<CSDocNode>();
		}
		_fragments.add(node);
	}

	public List<CSDocNode> fragments() {
		return safeList(_fragments);
	}

	public void addAttribute(String name, String value) {
		if (null == _attributes) {
			_attributes = new ArrayList<CSDocAttributeNode>();
		}
		_attributes.add(new CSDocAttributeNode(name, value));
	}
	
	public List<CSDocAttributeNode> attributes() {
		return safeList(_attributes);
	}
	
	private <T extends CSDocNode> List<T> safeList(List<T> list) {
		if (null == list) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(list);
	}

	public String tagName() {
		return _tagName;
	}

	public void addTextFragment(String text) {
		addFragment(new CSDocTextNode(text));
	}

	public String getAttribute(String attributeName) {		 
		for (CSDocAttributeNode attribute : attributes()) {
			if (attribute.name().equals(attributeName)) {
				return attribute.value();
			}
		}
		return null;
	}
}

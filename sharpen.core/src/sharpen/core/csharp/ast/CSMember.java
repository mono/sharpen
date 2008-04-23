/*
 * Created on Jan 21, 2005
 *
 */
package sharpen.core.csharp.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CSMember extends CSNode {

	protected String _name;
	
	protected CSVisibility _visibility = CSVisibility.Internal;
	
	protected List<CSDocNode> _docs;
	
	private List<CSAttribute> _attributes = new ArrayList<CSAttribute>();
	
	private List<String> _enclosingIfDefs = new ArrayList<String>();
	
	protected CSMember(String name) {
		_name = name;
	}

	public String name() {
		return _name;
	}

	public void visibility(CSVisibility visibility) {
		_visibility = visibility;
	}

	public CSVisibility visibility() {
		return _visibility;
	}
	
	public void addEnclosingIfDef(String expression) {
		_enclosingIfDefs.add(expression);
	}
	
	public List<String> enclosingIfDefs() {
		return Collections.unmodifiableList(_enclosingIfDefs);
	}

	public void addDoc(CSDocNode node) {
		if (null == _docs) {
			_docs = new ArrayList<CSDocNode>();
		}
		_docs.add(node);
		
	}

	public List<CSDocNode> docs() {
		if (null == _docs) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(_docs);
	}
	
	public void addAttribute(CSAttribute attribute) {
		_attributes.add(attribute);
	}
	
	public List<CSAttribute> attributes() {
		return Collections.unmodifiableList(_attributes);
	}
	
	public String signature() {
		return _name;
	}

}

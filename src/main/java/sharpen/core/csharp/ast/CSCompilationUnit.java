/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

package sharpen.core.csharp.ast;

import java.util.*;

public class CSCompilationUnit extends CSNode {


	private String _elementName;
	private boolean _ignore;
	private String _namespace;

	private final List<CSUsing> _usings = new ArrayList<CSUsing>();
	private final List<CSType> _types = new ArrayList<CSType>();
	private final List<CSComment> _comments = new ArrayList<CSComment>();
	private int _packagePosition;

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

		//	do not add using with same name as current namespace
		if(!isEmpty(_namespace) && !isEmpty(namespace) && namespace.equals(_namespace)){
			return true;
		}

		for (CSUsing us : _usings)
			if (us.namespace().equals(namespace))
				return true;

		return false;
	}

	private boolean isEmpty(String namespace) {
		return namespace == null || namespace.length() == 0;
	}

	public void addType(CSType type) {
		_types.add(type);
	}
	
	public Collection<CSUsing> usings() {
		return Collections.unmodifiableList(_usings);
	}
	
	public Collection<CSType> types() {
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

	public void addComment(CSComment lineComment) {
		_comments.add(lineComment);
	}

	public List<CSComment> comments() {
		return Collections.unmodifiableList(_comments);
	}

	public int getPackagePosition() {
		return _packagePosition; 
	}
	
	public void setPackagePosition(int startPosition) {
		_packagePosition = startPosition; 
	}
}

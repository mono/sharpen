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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CSVariableDeclaration extends CSNode implements CSAttributesContainer {
	
	private String _name;
	private CSTypeReferenceExpression _type;
	private CSExpression _initializer;
    private List<CSAttribute> _attributes = new ArrayList<CSAttribute>();
    private boolean _isVarArgs;

    public CSVariableDeclaration(String name, CSTypeReferenceExpression type) {
		this(name, type, null);
	}

	public CSVariableDeclaration(String name, CSTypeReferenceExpression type, CSExpression initializer) {
		_name = name;
		_type = type;
		_initializer = initializer;
	}
	
	public void accept(CSVisitor visitor) {
		visitor.visit(this);
	}

	public CSTypeReferenceExpression type() {
		return _type;
	}
	
	public void type(CSTypeReferenceExpression type) {
		_type = type;
	}
	
	public String name() {
		return _name;
	}
	
	public void initializer(CSExpression initializer) {
		_initializer = initializer;
	}
	
	public CSExpression initializer() {
		return _initializer;
	}

	public void name(String name) {
		_name = name;
	}

    @Override
    public void addAttribute(CSAttribute attribute) {
        _attributes.add(new CSVariableAttribute(attribute.name()));
    }

    @Override
    public boolean removeAttribute(String name) {
        for (CSAttribute at : _attributes) {
            if (at.name().equals(name)) {
                _attributes.remove(at);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<CSAttribute> attributes() {
        return Collections.unmodifiableList(_attributes);
    }

    public void isVarArgs(boolean isVarArgs) {
        _isVarArgs = isVarArgs;
    }

    public boolean isVarArgs() {
        return _isVarArgs;
    }
}

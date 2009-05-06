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

/* Copyright (C) 2006 Versant Inc. http://www.db4o.com */

package sharpen.core;

import java.util.List;

import sharpen.core.csharp.ast.*;

import org.eclipse.jdt.core.dom.*;

/**
 * @exclude
 */
public class NonStaticNestedClassBuilder extends AbstractNestedClassBuilder {

	private TypeDeclaration _nestedType;
	private CSTypeDeclaration _convertedType;
	private CSField _enclosingField;

	public NonStaticNestedClassBuilder(CSharpBuilder other, TypeDeclaration nestedType) {
		super(other);
		_nestedType = nestedType;
		_convertedType = processTypeDeclaration(_nestedType);
		_enclosingField = createEnclosingField();
		patchConstructors();		
		_convertedType.addMember(_enclosingField);
	}

	private void patchConstructors() {
		final List<CSConstructor> ctors = _convertedType.constructors();
		if (ctors.isEmpty()) {
			introduceConstructor();
		} else {
			for (CSConstructor ctor : ctors) {
				patchConstructor(ctor);
			}
		}
	}

	private void patchConstructor(CSConstructor ctor) {
		ctor.addParameter(0, new CSVariableDeclaration(_enclosingField.name(), _enclosingField.type()));
		ctor.body().addStatement(0, createFieldAssignment(_enclosingField.name(), _enclosingField.name()));
	}

	private void introduceConstructor() {
		final CSConstructor ctor = new CSConstructor();
		ctor.visibility(CSVisibility.Internal);
		patchConstructor(ctor);
		_convertedType.addMember(ctor);
	}

	@Override
	protected ITypeBinding nestedTypeBinding() {
		return _nestedType.resolveBinding();
	}

}

/* Copyright (C) 2006 db4objects Inc. http://www.db4o.com */

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

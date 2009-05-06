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

import sharpen.core.csharp.ast.*;

import org.eclipse.jdt.core.dom.*;

/**
 * @exclude
 */
public abstract class AbstractNestedClassBuilder extends CSharpBuilder {

	private boolean _usesEnclosingMember;

	public AbstractNestedClassBuilder(CSharpBuilder other) {
		super(other);
	}

	protected abstract ITypeBinding nestedTypeBinding();
	
	@Override
	protected CSExpression mapMethodTargetExpression(MethodInvocation node) {
		if (null == node.getExpression()) {
			return createEnclosingTargetReferences(node.getName());
		}
		return super.mapMethodTargetExpression(node);
	}

	public boolean visit(ThisExpression node) {
		if (null == node.getQualifier()) return super.visit(node);
		pushExpression(createEnclosingThisReference(node.getQualifier().resolveTypeBinding(), true));
		return false;
	}

	public boolean visit(SimpleName name) {
		if (isInstanceFieldReference(name)) {
			pushExpression(createEnclosingReferences(name));
			return false;
		}
		return super.visit(name);
	}

	private boolean isInstanceFieldReference(SimpleName name) {
		IBinding binding = name.resolveBinding();
		if (IBinding.VARIABLE != binding.getKind()) return false;
		return ((IVariableBinding)binding).isField();
	}

	private CSExpression createEnclosingReferences(SimpleName name) {
		CSExpression target = createEnclosingTargetReferences(name);
		return new CSMemberReferenceExpression(target, mappedName(name));
	}

	private CSExpression createEnclosingTargetReferences(SimpleName name) {
		ITypeBinding enclosingClassBinding = getDeclaringClass(name);
		
		CSExpression target = isStaticMember(name)
			? createTypeReference(enclosingClassBinding)
			: createEnclosingThisReference(enclosingClassBinding);
		return target;
	}

	private CSExpression createEnclosingThisReference(ITypeBinding enclosingClassBinding) {
		return createEnclosingThisReference(enclosingClassBinding, false);
	}

	private CSExpression createEnclosingThisReference(
			ITypeBinding enclosingClassBinding, boolean ignoreSuperclass) {
		CSExpression enclosing = new CSThisExpression();			
		ITypeBinding binding = nestedTypeBinding();
		ITypeBinding to = enclosingClassBinding;
		while (binding != to && (ignoreSuperclass || !isSuperclass(binding, to))) {
			requireEnclosingReference();
			enclosing = new CSMemberReferenceExpression(enclosing, "_enclosing");
			binding = binding.getDeclaringClass();
			if (null == binding) break;
		}
		return enclosing;
	}
	
	protected boolean isEnclosingReferenceRequired() {
		return _usesEnclosingMember;
	}
	
	protected void requireEnclosingReference() {
		_usesEnclosingMember = true;
	}

	private String mappedName(SimpleName name) {
		IBinding binding = name.resolveBinding();
		return binding instanceof IMethodBinding ? mappedMethodName((IMethodBinding) binding) : identifier(name);
	}

	private boolean isStaticMember(SimpleName name) {
		return Modifier.isStatic(name.resolveBinding().getModifiers());
	}

	private boolean isSuperclass(ITypeBinding type, ITypeBinding candidate) {
		ITypeBinding superClass = type.getSuperclass();
		while (null != superClass) {
			if (superClass == candidate) {
				return true;
			}
			superClass = superClass.getSuperclass();
		}
		return false;
	}

	private ITypeBinding getDeclaringClass(Name reference) {
		IBinding binding = reference.resolveBinding();
		switch (binding.getKind()) {
			case IBinding.METHOD: {
				return ((IMethodBinding)binding).getDeclaringClass(); 
			}
			case IBinding.VARIABLE: {
				IVariableBinding variable = (IVariableBinding)binding;
				return variable.getDeclaringClass();
			}
		}
		//throw new UnsupportedOperationException();
		return null;
	}

	protected CSField createEnclosingField() {
		return CSharpCode.newPrivateReadonlyField("_enclosing", enclosingTypeReference());
	}

	private CSTypeReference enclosingTypeReference() {
		return new CSTypeReference(_currentType.name());
	}

	protected CSInfixExpression createFieldAssignment(String fieldName, String rvalue) {
		return createFieldAssignment(fieldName, new CSReferenceExpression(rvalue));
	}

	protected CSInfixExpression createFieldAssignment(String fieldName,
			final CSExpression fieldValue) {
		CSExpression fieldReference = new CSMemberReferenceExpression(new CSThisExpression(), fieldName);
		return new CSInfixExpression("=", fieldReference, fieldValue);
	}

}
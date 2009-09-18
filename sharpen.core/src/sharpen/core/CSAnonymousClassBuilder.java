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

package sharpen.core;

import java.util.*;

import sharpen.core.csharp.ast.*;

import org.eclipse.jdt.core.dom.*;

public class CSAnonymousClassBuilder extends AbstractNestedClassBuilder {
	
	private AnonymousClassDeclaration _node;
	
	private CSClass _type;

	private CSConstructor _constructor;
	
	private Set<IVariableBinding> _capturedVariables = new LinkedHashSet<IVariableBinding>();

	private CSharpBuilder _parent;

	private Set<VariableDeclarationFragment> _fieldInitializers = new LinkedHashSet<VariableDeclarationFragment>();

	public CSAnonymousClassBuilder(CSharpBuilder builder, AnonymousClassDeclaration node) {
		super(builder);
		_parent = builder;
		_node = node;
		run();
	}
	
	public CSClass type() {
		return _type;
	}
	
	public Set<IVariableBinding> capturedVariables() {
		return _capturedVariables;
	}

	public CSExpression createConstructorInvocation() {
		CSConstructorInvocationExpression invocation = new CSConstructorInvocationExpression(new CSReferenceExpression(_type.name()));
		if (isEnclosingReferenceRequired()) {
			invocation.addArgument(new CSThisExpression());
		}
		addCapturedVariables(invocation);
		addBaseConstructorArguments(invocation);
		return invocation;
	}

	private void addCapturedVariables(CSConstructorInvocationExpression invocation) {
		for (IVariableBinding variable : _capturedVariables) {
			invocation.addArgument(new CSReferenceExpression(identifier(variable.getName())));
		}
	}

	private void addBaseConstructorArguments(CSConstructorInvocationExpression invocation) {
		List arguments = classInstanceCreationArguments();
		if (arguments.isEmpty()) {
			return;
		}
		
		final ITypeBinding[] ctorParameterTypes = classInstanceCreation().resolveConstructorBinding().getParameterTypes();
		
		_constructor.chainedConstructorInvocation(new CSConstructorInvocationExpression(new CSBaseExpression()));
		
		for (int i=0; i<ctorParameterTypes.length; ++i) {
			ITypeBinding parameterType = ctorParameterTypes[i];
			Expression argument = (Expression)arguments.get(i);	
			
			String parameterName = "baseArg" + (i + 1);
			_constructor.addParameter(parameterName, mappedTypeReference(parameterType));
			_constructor.chainedConstructorInvocation().addArgument(new CSReferenceExpression(parameterName));
			
			invocation.addArgument(_parent.mapExpression(argument));
		}
	}
	
	private List classInstanceCreationArguments() {
		return classInstanceCreation().arguments();
	}

	private ClassInstanceCreation classInstanceCreation() {
	    return ((ClassInstanceCreation)_node.getParent());
    }

	public void run() {
		captureExternalLocalVariables();
		setUpAnonymousType();
		setUpConstructor();
		processAnonymousBody();
		int capturedVariableCount = flushCapturedVariables();
		flushFieldInitializers();
		flushInstanceInitializers(_type, capturedVariableCount);
	}

	private void flushFieldInitializers() {
		
		for (VariableDeclarationFragment field : _fieldInitializers) {
			addToConstructor(createFieldAssignment(fieldName(field), mapExpression(field.getInitializer())));
		}
	}
	
	@Override
	protected CSExpression mapFieldInitializer(VariableDeclarationFragment fragment) {
		if (fragment.getInitializer() != null) {
			_fieldInitializers.add(fragment);
		}
		return null;
	}

	private void processAnonymousBody() {
		CSTypeDeclaration saved = _currentType;
		_currentType = _type;
		visit(_node.bodyDeclarations());
		_currentType = saved;
	}
	
	public boolean visit(AnonymousClassDeclaration node) {
		CSAnonymousClassBuilder builder = new CSAnonymousClassBuilder(this, node);
		if (builder.isEnclosingReferenceRequired()) {
			requireEnclosingReference();
		}
		captureNeededVariables(builder);
		pushExpression(builder.createConstructorInvocation());
		_currentType.addMember(builder.type());
		return false;
	}
	
	private void captureNeededVariables(CSAnonymousClassBuilder builder) {
		
		IMethodBinding currentMethod = currentMethodDeclarationBinding();
		for (IVariableBinding variable : builder.capturedVariables()) {
			IMethodBinding method = variable.getDeclaringMethod();
			if (method != currentMethod) {
				_capturedVariables.add(variable);
			}
		}
	}

	private IMethodBinding currentMethodDeclarationBinding() {
		return _currentBodyDeclaration instanceof MethodDeclaration
			? ((MethodDeclaration)_currentBodyDeclaration).resolveBinding()
			: null;
	}

	private void addFieldParameter(String name, CSTypeReferenceExpression type) {
		addFieldParameter(CSharpCode.newPrivateReadonlyField(name, type));
	}

	private void addFieldParameter(CSField field) {
		_type.addMember(field);
		
		String parameterName = field.name();
		_constructor.addParameter(parameterName, field.type());
		addToConstructor(createFieldAssignment(field.name(), parameterName));
	}

	private void addToConstructor(final CSExpression expression) {
		_constructor.body().addStatement(expression);
	}

	private String anonymousBaseTypeName() {
		return mappedTypeName(anonymousBaseType());
	}
	
	public ITypeBinding anonymousBaseType() {
		ITypeBinding binding = nestedTypeBinding();
		return binding.getInterfaces().length > 0
			? binding.getInterfaces()[0]
			: binding.getSuperclass();
	}

	@Override
	protected ITypeBinding nestedTypeBinding() {
		return _node.resolveBinding();
	}

	private String anonymousInnerClassName() {
		return "_" + simpleName(anonymousBaseTypeName()) + "_" + lineNumber(_node);
	}
	
	private String simpleName(String typeName) {
		final int index = typeName.lastIndexOf('.');
		if (index < 0) return typeName;
		return typeName.substring(index + 1);
	}

	private void setUpAnonymousType() {
		_type = classForAnonymousType();
	}

	private CSClass classForAnonymousType() {
		CSClass type = new CSClass(anonymousInnerClassName(), CSClassModifier.Sealed);
		type.visibility(CSVisibility.Private);
		type.addBaseType(new CSTypeReference(anonymousBaseTypeName()));
		return type;
	}
	
	private void setUpConstructor() {
		_constructor = new CSConstructor();
		_constructor.visibility(CSVisibility.Public);
		_type.addMember(_constructor);
	}
	
	private int flushCapturedVariables() {		
		int capturedVariableCount = 0;
		if (isEnclosingReferenceRequired()) {
			capturedVariableCount++;
			addFieldParameter(createEnclosingField());
		}
		
		for (IVariableBinding variable : _capturedVariables) {
			capturedVariableCount++;
			addFieldParameter(identifier(variable.getName()), mappedTypeReference(variable.getType()));
		}
		
		return capturedVariableCount;
	}
	
	private void captureExternalLocalVariables() {
		_node.accept(new ASTVisitor() {
			
			IMethodBinding _currentMethodBinding;
			
			public boolean visit(MethodDeclaration node) {
				IMethodBinding saved = _currentMethodBinding;
				_currentMethodBinding = node.resolveBinding();
				node.getBody().accept(this);
				_currentMethodBinding = saved;
				return false;
			}
			
			public boolean visit(AnonymousClassDeclaration node) {
				return node == _node;
			}
			
			public boolean visit(SimpleName node) {
				IBinding binding = node.resolveBinding();
				if (isExternalLocal(binding)) {
					_capturedVariables.add((IVariableBinding)binding);
				}
				return false;
			}
			
			boolean isExternalLocal(IBinding binding) {
				if (binding instanceof IVariableBinding) {
					IVariableBinding variable = (IVariableBinding)binding;
					if (!variable.isField()) {
						return variable.getDeclaringMethod() != _currentMethodBinding;
					}
				}
				return false;
			}
		});
	}

}

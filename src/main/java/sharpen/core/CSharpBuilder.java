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
import java.util.regex.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import sharpen.core.Configuration.*;
import sharpen.core.csharp.ast.*;
import sharpen.core.framework.*;
import static sharpen.core.framework.StaticImports.*;

import static sharpen.core.framework.Environments.*;

public class CSharpBuilder extends ASTVisitor {

	private static final String JAVA_LANG_VOID_TYPE = "java.lang.Void.TYPE";

	private static final String JAVA_LANG_BOOLEAN_TYPE = "java.lang.Boolean.TYPE";

	private static final String JAVA_LANG_CHARACTER_TYPE = "java.lang.Character.TYPE";

	private static final String JAVA_LANG_INTEGER_TYPE = "java.lang.Integer.TYPE";

	private static final String JAVA_LANG_LONG_TYPE = "java.lang.Long.TYPE";

	private static final String JAVA_LANG_BYTE_TYPE = "java.lang.Byte.TYPE";

	private static final String JAVA_LANG_SHORT_TYPE = "java.lang.Short.TYPE";

	private static final String JAVA_LANG_FLOAT_TYPE = "java.lang.Float.TYPE";

	private static final String JAVA_LANG_DOUBLE_TYPE = "java.lang.Double.TYPE";

	private static final CSTypeReference OBJECT_TYPE_REFERENCE = new CSTypeReference("object");

	private final CSCompilationUnit _compilationUnit;

	protected CSTypeDeclaration _currentType;

	protected CSTypeDeclaration _currentAuxillaryType;

	private String _content;

	private CSBlock _currentBlock;

	private CSExpression _currentExpression;

	protected CSMethodBase _currentMethod;

	protected BodyDeclaration _currentBodyDeclaration;
	
	private CSLabelStatement _currentContinueLabel;

	private static final Pattern SUMMARY_CLOSURE_PATTERN = Pattern.compile("\\.(\\s|$)");

	private static final Pattern HTML_ANCHOR_PATTERN = Pattern.compile("<([aA])\\s+.+>");

	protected CompilationUnit _ast;

	protected Configuration _configuration;

	private ASTResolver _resolver;

	private IVariableBinding _currentExceptionVariable;

	private final DynamicVariable<Boolean> _ignoreExtends = new DynamicVariable<Boolean>(Boolean.FALSE);

	private List<Initializer> _instanceInitializers = new ArrayList<Initializer>();
	
	private Stack<Set<String>> _blockVariables = new Stack<Set<String>> ();
	private Stack<Set<String>> _localBlockVariables = new Stack<Set<String>> ();
	private Stack<HashMap<String,String>> _renamedVariables = new Stack<HashMap<String,String>> ();
	
	private ITypeBinding _currentExpectedType;


	protected NamingStrategy namingStrategy() {
		return _configuration.getNamingStrategy();
	}

	protected WarningHandler warningHandler() {
		return _configuration.getWarningHandler();
	}

	public CSharpBuilder() {
		_configuration = my(Configuration.class);
		_ast = my(CompilationUnit.class);
		_resolver = my(ASTResolver.class);
		_compilationUnit = my(CSCompilationUnit.class);
		if(!_configuration.sharpenNamespace().equals("nonamespace")) {
			_compilationUnit.addUsing(new CSUsing (_configuration.sharpenNamespace()));
		}
	}

	protected CSharpBuilder(CSharpBuilder other) {
		_configuration = other._configuration;
		_ast = other._ast;
		_resolver = other._resolver;
		_compilationUnit = other._compilationUnit;
		
		_currentType = other._currentType;
		_currentBlock = other._currentBlock;
		_currentExpression = other._currentExpression;
		_currentMethod = other._currentMethod;
		_currentBodyDeclaration = other._currentBodyDeclaration;
	}

	public void setSourceCompilationUnit(CompilationUnit ast) {
		_ast = ast;
	}

	public void setSourceContent(String content) {
		_content = content;
	}

	public void run() {
		if (null == warningHandler() || null == _ast) {
			throw new IllegalStateException();
		}
		_ast.accept(this);
		visit(_ast.getCommentList());
	}

	@Override
	public boolean visit(LineComment node) {
		_compilationUnit.addComment(new CSLineComment(node.getStartPosition(), getText(node.getStartPosition(), node
		        .getLength())));
		return false;
	}
	
	@Override
	public boolean visit(BlockComment node) {
		_compilationUnit.addComment(new CSBlockComment(node.getStartPosition(), getText(node.getStartPosition(), node
		        .getLength())));
		return false;
	};

	private String getText(int startPosition, int length) {
		try {
			ICompilationUnit cu = (ICompilationUnit) _ast.getJavaElement();
			if(cu != null){
				IBuffer buffer = cu.getBuffer();
				if(buffer != null){
					return buffer.getText(startPosition, length);
				}
			}

			if(_content != null && !_content.isEmpty()){
				return _content.substring(startPosition, startPosition + length);
			}

			return ""; 
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
	}

	public CSCompilationUnit compilationUnit() {
		return _compilationUnit;
	}

	public boolean visit(ImportDeclaration node) {
		return false;
	}

	public boolean visit(final EnumDeclaration node) {
		if (processIgnoredType(node)) {
			return false;
		}

		if (processEnumType(node)) {
			return false;
		}

		try {
			my(NameScope.class).enterTypeDeclaration(node);
			_ignoreExtends.using(ignoreExtends(node), new Runnable() {
				public void run() {
					final ITypeBinding binding = node.resolveBinding();
					if (!binding.isNested()) {
						processTypeDeclaration(node);
						return;
					}

					if (isNonStaticNestedType(binding)) {
						processNonStaticNestedTypeDeclaration(node);
						return;
					}

					new CSharpBuilder(CSharpBuilder.this).processTypeDeclaration(node);
				}
			});
		} finally {
			my(NameScope.class).leaveTypeDeclaration(node);
		}

		return false;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		// TODO: SHA-51
		return false;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		// TODO: SHA-51
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		// TODO: SHA-51
		return false;
	}

	public boolean visit(final LabeledStatement node) {
		String identifier = node.getLabel().getIdentifier();
		_currentContinueLabel = new CSLabelStatement(continueLabel(identifier));
		try{
			node.getBody().accept(this);
		}finally{
			_currentContinueLabel = null;
		}
		addStatement(new CSLabelStatement(breakLabel(identifier)));
		return false;
	}

	private String breakLabel(String identifier) {
		return identifier + "_break";
	}

	private String continueLabel(String identifier) {
		return identifier + "_continue";
	}

	public boolean visit(SuperFieldAccess node) {
		String name = mappedFieldName(node);
		if (null == node.getQualifier()) {
			pushExpression(new CSMemberReferenceExpression(new CSBaseExpression(), name));
		} else {
			notImplemented(node);
		}
		return false;
	}

	public boolean visit(MemberRef node) {
		notImplemented(node);
		return false;
	}

	@Override
	public boolean visit(WildcardType node) {
		notImplemented(node);
		return false;
	}

	private void notImplemented(ASTNode node) {
		throw new IllegalArgumentException(sourceInformation(node) + ": " + node.toString());
	}

	public boolean visit(PackageDeclaration node) {
		_compilationUnit.setPackagePosition(node.getStartPosition());
		String namespace = node.getName().toString();
		_compilationUnit.namespace(mappedNamespace(namespace));
		
		processDisableTags(node, _compilationUnit);
		return false;
	}

	public boolean visit(AnonymousClassDeclaration node) {
		CSAnonymousClassBuilder builder = mapAnonymousClass(node);
		pushExpression(builder.createConstructorInvocation());
		return false;
	}

	private CSAnonymousClassBuilder mapAnonymousClass(AnonymousClassDeclaration node) {
		CSAnonymousClassBuilder builder = new CSAnonymousClassBuilder(this, node);
		_currentType.addMember(builder.type());
		return builder;
	}

	public boolean visit(final TypeDeclaration node) {

		if (processIgnoredType(node)) {
			return false;
		}

		if (processEnumType(node)) {
			return false;
		}
		
		try {
			my(NameScope.class).enterTypeDeclaration(node);
	
			_ignoreExtends.using(ignoreExtends(node), new Runnable() {
				public void run() {
	
					final ITypeBinding binding = node.resolveBinding();
					if (!binding.isNested()) {
						processTypeDeclaration(node);
						return;
					}
					
					if (isNonStaticNestedType(binding)) {
						processNonStaticNestedTypeDeclaration(node);
						return;
					}
					
					new CSharpBuilder(CSharpBuilder.this).processTypeDeclaration(node);
				}
			});
		} finally {
			my(NameScope.class).leaveTypeDeclaration(node);
		}

		return false;
	}

	private boolean processEnumType(TypeDeclaration node) {
		if (!isEnum(node)) {
			return false;
		}

		return buildEnumType(node);
	}

	private boolean processEnumType(EnumDeclaration node) {
		try {
			return buildEnumType(node);
		} catch (IllegalArgumentException ex) {
			if (isEnum(node)) {
				// the user explicitly used the @sharpen.enum annotation 
				throw ex;
			}

			return false;
		}
	}

	private boolean buildEnumType(final AbstractTypeDeclaration typeNode) {
		final CSEnum theEnum = new CSEnum(typeName(typeNode));
		typeNode.accept(new ASTVisitor() {
			public boolean visit(EnumConstantDeclaration node) {
				if (!(typeNode instanceof EnumDeclaration)) {
					unsupportedConstruct(node, "Only enum types can contain enum constants.");
					return false;
				}

				theEnum.addValue(identifier(node.getName()));
				return false;
			}

			public boolean visit(VariableDeclarationFragment node) {
				if (typeNode instanceof EnumDeclaration) {
					unsupportedConstruct(node, "Enum types cannot contain variable declarations.");
					return false;
				}

				theEnum.addValue(identifier(node.getName()));
				return false;
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor() && isPrivate(node) && node.parameters().isEmpty()) {
					return false;
				}
				unsupportedConstruct(node, "Enum can contain only fields and a private constructor.");
				return false;
			}
		});

		mapVisibility(typeNode, theEnum);
		mapJavadoc(typeNode, theEnum);
		addType(typeNode.resolveBinding(), theEnum);
		return true;
	}

	protected boolean isPrivate(MethodDeclaration node) {
		return Modifier.isPrivate(node.getModifiers());
	}

	private boolean isEnum(AbstractTypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_ENUM);
	}

	private boolean processIgnoredType(AbstractTypeDeclaration node) {
		if (!hasIgnoreOrRemoveAnnotation(node)) {
			return false;
		}
		if (isMainType(node)) {
			compilationUnit().ignore(true);
		}
		return true;
	}

	private boolean hasIgnoreOrRemoveAnnotation(AbstractTypeDeclaration node) {
	    return SharpenAnnotations.hasIgnoreAnnotation(node) || hasRemoveAnnotation(node);
    }

	private void processNonStaticNestedTypeDeclaration(AbstractTypeDeclaration node) {
		if (!(node instanceof TypeDeclaration)) {
			unsupportedConstruct(node, "Declarations of type " + node.getClass().getName() + " cannot be non-static nested classes.");
		}

		new NonStaticNestedClassBuilder(this, (TypeDeclaration)node);
	}

	protected CSTypeDeclaration processTypeDeclaration(AbstractTypeDeclaration node) {
		CSTypeDeclaration type = mapTypeDeclaration(node);
		CSTypeDeclaration auxillaryType = mapAuxillaryTypeDeclaration(node);
		
		processDisabledType(node, isMainType(node) ? _compilationUnit : type);
		
		if (_configuration.shouldMakePartial(node.getName().getFullyQualifiedName()))
			type.partial(true);

		ITypeBinding typeBinding = node.resolveBinding();
		addType(typeBinding, type);
		if (auxillaryType != null) {
			addType(typeBinding, auxillaryType);
		}

		mapSuperTypes(node, type);

		mapVisibility(node, type);
		adjustVisibility (typeBinding.getSuperclass(), type);
		if (auxillaryType != null) {
			mapVisibility(node, auxillaryType);
			adjustVisibility(typeBinding.getSuperclass(), auxillaryType);
		}

		mapDocumentation(node, type);
		processConversionJavadocTags(node, type);

		mapMembers(node, type, auxillaryType);
		autoImplementCloneable(node, type);
		
		moveInitializersDependingOnThisReferenceToConstructor(type);
		
		if (_configuration.junitConversion() && hasTests (type))
			type.addAttribute(new CSAttribute ("NUnit.Framework.TestFixture"));
	
		return type;
	}

	private void processDisabledType(AbstractTypeDeclaration node, CSNode type) {
		final String expression = _configuration.conditionalCompilationExpressionFor(packageNameFor(node));
		if (null != expression) {
			compilationUnit().addEnclosingIfDef(expression);
		}
		
		processDisableTags(node, type);
	}

	private String packageNameFor(AbstractTypeDeclaration node) {
		ITypeBinding type = node.resolveBinding();
		return type.getPackage().getName();
	}

	protected void flushInstanceInitializers(CSTypeDeclaration type, int startStatementIndex) {
		
		if (_instanceInitializers.isEmpty()) {
			return;
		}
		
		ensureConstructorsFor(type);
		
		int initializerIndex = startStatementIndex;
		for (Initializer node : _instanceInitializers) {
			final CSBlock body = mapInitializer(node);
			
			for (CSConstructor ctor : type.constructors()) {
				if (ctor.isStatic() || hasChainedThisInvocation(ctor)) {
					continue;
				}
				ctor.body().addStatement(initializerIndex, body);
			}
			
			++initializerIndex;
		}
		
		_instanceInitializers.clear();
    }

	private CSBlock mapInitializer(Initializer node) {
	    final CSConstructor template = new CSConstructor();
	    visitBodyDeclarationBlock(node, node.getBody(), template);
	    final CSBlock body = template.body();
	    return body;
    }

	private boolean hasChainedThisInvocation(CSConstructor ctor) {
		final CSConstructorInvocationExpression chained = ctor.chainedConstructorInvocation();
		return chained != null && chained.expression() instanceof CSThisExpression;
	}

	private void moveInitializersDependingOnThisReferenceToConstructor(CSTypeDeclaration type) {
		
		final HashSet<String> memberNames = memberNameSetFor(type);
		
		int index = 0;
		for (CSMember member : copy(type.members())) {
			if (!(member instanceof CSField))
				continue;
			
			final CSField field = (CSField)member;
			if (!isDependentOnThisOrMember(field, memberNames))
				continue;
			
			moveFieldInitializerToConstructors(field, type, index++);
        }
	}

	private HashSet<String> memberNameSetFor(CSTypeDeclaration type) {
	    final HashSet<String> members = new HashSet<String>();
		for (CSMember member : type.members()) {
			if (member instanceof CSType)
				continue;
			if (isStatic(member))
				continue;
			members.add(member.name());
		}
	    return members;
    }

	private boolean isStatic(CSMember member) {
		if (member instanceof CSField)
			return isStatic((CSField) member);
		if (member instanceof CSMethod)
			return isStatic((CSMethod) member);
		return false;
    }
	
	private boolean isStatic(CSMethod method) {
		return method.modifier() == CSMethodModifier.Static;
	}

	private boolean isStatic(CSField member) {
		final Set<CSFieldModifier> fieldModifiers = member.modifiers();
		return fieldModifiers.contains(CSFieldModifier.Static)
			|| fieldModifiers.contains(CSFieldModifier.Const);
    }

	private CSMember[] copy(final List<CSMember> list) {
	    return list.toArray(new CSMember[0]);
    }

	private boolean isDependentOnThisOrMember(CSField field, final Set<String> fields) {
		if (null == field.initializer())
			return false;
		
		final ByRef<Boolean> foundThisReference = new ByRef<Boolean>(false);
		field.initializer().accept(new CSExpressionVisitor() {
			@Override
			public void visit(CSThisExpression node) {
				foundThisReference.value = true;
			}
			
			@Override
			public void visit(CSReferenceExpression node) {
				if (fields.contains(node.name())) {
					foundThisReference.value = true;
				}
			}
		});
		return foundThisReference.value;
    }
	
	private void moveFieldInitializerToConstructors(CSField field, CSTypeDeclaration type, int index) {
		final CSExpression initializer = field.initializer();
		for (CSConstructor ctor : ensureConstructorsFor(type))
			ctor.body().addStatement(
					index,
					newAssignment(field, initializer));
		field.initializer(null);
    }

	private CSExpression newAssignment(CSField field, final CSExpression initializer) {
	    return CSharpCode.newAssignment(CSharpCode.newReference(field), initializer);
    }

	private Iterable<CSConstructor> ensureConstructorsFor(CSTypeDeclaration type) {
		final List<CSConstructor> ctors = type.constructors();
		if (!ctors.isEmpty())
			return ctors;
		
		return Collections.singletonList(addDefaultConstructor(type));
    }

	private CSConstructor addDefaultConstructor(CSTypeDeclaration type) {
		final CSConstructor ctor = CSharpCode.newPublicConstructor();
		type.addMember(ctor);
		return ctor;
    }

	private void autoImplementCloneable(AbstractTypeDeclaration node, CSTypeDeclaration type) {

		if (!implementsCloneable(type)) {
			return;
		}

		CSMethod clone = new CSMethod("System.ICloneable.Clone");
		clone.returnType(OBJECT_TYPE_REFERENCE);
		clone.body().addStatement(
		        new CSReturnStatement(-1,
		                new CSMethodInvocationExpression(new CSReferenceExpression("MemberwiseClone"))));

		type.addMember(clone);
	}

	private boolean implementsCloneable(CSTypeDeclaration node) {
		for (CSTypeReferenceExpression typeRef : node.baseTypes()) {
			if (typeRef.toString().equals("System.ICloneable")) {
				return true;
			}
		}
		return false;
	}

	private void mapSuperTypes(AbstractTypeDeclaration node, CSTypeDeclaration type) {
		if (!_ignoreExtends.value()) {
			mapSuperClass(node, type);
		}
		if (!ignoreImplements(node)) {
			mapSuperInterfaces(node, type);
		}
	}

	private boolean ignoreImplements(AbstractTypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_IGNORE_IMPLEMENTS);
	}

	private boolean ignoreExtends(AbstractTypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_IGNORE_EXTENDS);
	}

	private void processConversionJavadocTags(AbstractTypeDeclaration node, CSTypeDeclaration type) {
		processPartialTagElement(node, type);
	}

	private CSTypeDeclaration mapTypeDeclaration(AbstractTypeDeclaration node) {
		if (!(node instanceof TypeDeclaration) && !(node instanceof EnumDeclaration)) {
			unsupportedConstruct(node, "Cannot map type declaration for node.");
		}

		CSTypeDeclaration type = typeDeclarationFor(node);
		type.startPosition(node.getStartPosition());
		type.sourceLength(node.getLength());
		if (node instanceof TypeDeclaration) {
			mapTypeParameters(((TypeDeclaration)node).typeParameters(), type);
		}
		return checkForMainType(node, type);
	}

	private CSTypeDeclaration mapAuxillaryTypeDeclaration(AbstractTypeDeclaration node) {
		if (!(node instanceof TypeDeclaration) && !(node instanceof EnumDeclaration)) {
			unsupportedConstruct(node, "Cannot map auxillary type declaration for node.");
		}

		CSTypeDeclaration type = auxillaryTypeDeclarationFor(node);
		if (type == null) {
			return null;
		}

		type.startPosition(node.getStartPosition());
		type.sourceLength(node.getLength());
		return type;
	}

	private void mapTypeParameters(final List typeParameters, CSTypeParameterProvider type) {
		for (Object item : typeParameters) {
			type.addTypeParameter(mapTypeParameter((TypeParameter) item));
		}
	}

	private CSTypeParameter mapTypeParameter(TypeParameter item) {
		CSTypeParameter tp = new CSTypeParameter(identifier(item.getName()));
		ITypeBinding tb = item.resolveBinding ();
		if (tb != null) {
			ITypeBinding superc = mapTypeParameterExtendedType (tb);
			if (superc != null)
				tp.superClass(mappedTypeReference(superc));
		}
		return tp;
	}

	private CSTypeDeclaration typeDeclarationFor(AbstractTypeDeclaration node) {
		return typeDeclarationFor(node, false);
	}

	private CSTypeDeclaration auxillaryTypeDeclarationFor(AbstractTypeDeclaration node) {
		return typeDeclarationFor(node, true);
	}

	private CSTypeDeclaration typeDeclarationFor(AbstractTypeDeclaration node, boolean auxillary) {
		final String typeName = typeName(node);
		if (node instanceof TypeDeclaration && ((TypeDeclaration)node).isInterface()) {
			boolean valid = isValidCSInterface(node.resolveBinding());
			if (valid && auxillary) {
				return null;
			} else if (valid || (!auxillary && _configuration.separateInterfaceConstants())) {
				boolean overriddenName = !typeName.equals(node.getName().toString());
				if (overriddenName) {
					return new CSInterface(typeName);
				} else {
					return new CSInterface(processInterfaceName(node));
				}
			} else if (!valid && auxillary) {
				return new CSClass(processInterfaceConstantsName(node), CSClassModifier.Static);
			} else {
				warningHandler().warning(node, "Java interface converted to C# class.");
				return new CSClass(typeName, CSClassModifier.Abstract);
			}
		}

		if (auxillary) {
			return null;
		}

		if (isStruct(node)) {
			return new CSStruct(typeName);
		}

		CSClassModifier modifier;
		if (node instanceof TypeDeclaration) {
			modifier = mapClassModifier(node.getModifiers());
		} else if (node instanceof EnumDeclaration) {
			modifier = CSClassModifier.Sealed;
		} else {
			unsupportedConstruct(node, "cannot determine modifiers for type node");
			throw new UnsupportedOperationException("shouldn't be reachable");
		}

		return new CSClass(typeName, modifier);
	}

	private String typeName(AbstractTypeDeclaration node) {
		String renamed = annotatedRenaming(node);
		if (renamed != null)
			return renamed;
		renamed = mappedTypeName(node.resolveBinding());
		if (renamed != null) {
			int i = renamed.lastIndexOf('.');
			if (i != -1)
				return renamed.substring(i + 1);
			else
				return renamed;
		}
		return node.getName().toString();
	}

	private boolean isStruct(AbstractTypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_STRUCT);
	}

	private CSTypeDeclaration checkForMainType(AbstractTypeDeclaration node, CSTypeDeclaration type) {
		if (isMainType(node)) {
			String name = type.name();
			if (_configuration.paramCountFileNames()) {
				if (type.typeParameters().size() > 0) {
					name += "`" + Integer.toString(type.typeParameters().size());
				}
			}

			setCompilationUnitElementName(name);
		}
		return type;
	}

	private void setCompilationUnitElementName(String name) {
		_compilationUnit.elementName(name + ".cs");
	}

	private String processInterfaceName(AbstractTypeDeclaration node) {

		String name = node.getName().getFullyQualifiedName();
		return interfaceName(name);
	}

	private String processInterfaceConstantsName(AbstractTypeDeclaration node) {
		String name = node.getName().getFullyQualifiedName();
		return name + "Constants";
	}

	private boolean isMainType(AbstractTypeDeclaration node) {
		return node.isPackageMemberTypeDeclaration() && Modifier.isPublic(node.getModifiers());
	}

	private void mapSuperClass(AbstractTypeDeclaration node, CSTypeDeclaration type) {
		if (handledExtends(node, type))
			return;

		if (!(node instanceof TypeDeclaration)) {
			return;
		}
		
		Type superclassType = ((TypeDeclaration)node).getSuperclassType();
		if (superclassType == null) {
			return;
		}
		
		final ITypeBinding superClassBinding = superclassType.resolveBinding();
		if (null == superClassBinding)
			unresolvedTypeBinding(superclassType);
		
		if (!isLegacyTestFixtureClass (superClassBinding))
			type.addBaseType(mappedTypeReference(superClassBinding));
		else {
			type.addAttribute(new CSAttribute ("NUnit.Framework.TestFixture"));
		}
	}
	
	private boolean isLegacyTestFixtureClass (ITypeBinding type)
	{
		return (_configuration.junitConversion() && type.getQualifiedName().equals("junit.framework.TestCase"));
	}
	
	private boolean isLegacyTestFixture (ITypeBinding type) {
		if (!_configuration.junitConversion())
			return false;
		if (isLegacyTestFixtureClass (type))
			return true;
		ITypeBinding base = type.getSuperclass();
		return (base != null) && isLegacyTestFixture (base);
	}
	
	private boolean hasTests (CSTypeDeclaration type) {
		for (CSMember m: type.members()) {
			if (m instanceof CSMethod) {
				CSMethod met = (CSMethod)m;
				for (CSAttribute at: met.attributes()) {
					if (at.name().equals("Test") || at.name().equals("NUnit.Framework.Test"))
						return true;
				}
			}
		}
		return false;
	}
	
	private boolean handledExtends(AbstractTypeDeclaration node, CSTypeDeclaration type) {
		if (!(node instanceof TypeDeclaration) && !(node instanceof EnumDeclaration)) {
			unsupportedConstruct(node, "Cannot handle extends for type node.");
		}

		final TagElement replaceExtendsTag = javadocTagFor(node, SharpenAnnotations.SHARPEN_EXTENDS);
		if (null == replaceExtendsTag)
			return false;
	
		final String baseType = JavadocUtility.singleTextFragmentFrom(replaceExtendsTag);
		type.addBaseType(new CSTypeReference(baseType));		
		return true;
	}

	private void mapSuperInterfaces(AbstractTypeDeclaration node, CSTypeDeclaration type) {
		List superInterfaceTypes;
		if (node instanceof TypeDeclaration) {
			superInterfaceTypes = ((TypeDeclaration)node).superInterfaceTypes();
		} else if (node instanceof EnumDeclaration) {
			superInterfaceTypes = ((EnumDeclaration)node).superInterfaceTypes();
		} else {
			unsupportedConstruct(node, "cannot map super interfaces for type node");
			throw new UnsupportedOperationException("shouldn't be reachable");
		}

		final ITypeBinding serializable = resolveWellKnownType("java.io.Serializable");
		for (Object itf : superInterfaceTypes) {
			Type iface = (Type) itf;
			if (iface.resolveBinding() == serializable) {
				continue;
			}
			type.addBaseType(mappedTypeReference(iface));
		}

		if (!type.isInterface() && node.resolveBinding().isSubTypeCompatible(serializable)) {
			type.addAttribute(new CSAttribute("System.Serializable"));
		}
	}

	private ITypeBinding resolveWellKnownType(String typeName) {
		return _ast.getAST().resolveWellKnownType(typeName);
	}

	private void mapMembers(AbstractTypeDeclaration node, CSTypeDeclaration type, CSTypeDeclaration auxillaryType) {
		if (!(node instanceof TypeDeclaration) && !(node instanceof EnumDeclaration)) {
			unsupportedConstruct(node, "Cannot map members for node.");
		}

		CSTypeDeclaration saved = _currentType;
		CSTypeDeclaration savedAuxillary = _currentAuxillaryType;
		_currentType = type;
		_currentAuxillaryType = auxillaryType;
		try {
			if (node instanceof EnumDeclaration) {
				visit(((EnumDeclaration)node).enumConstants());
			}
			visit(node.bodyDeclarations());
			createInheritedAbstractMemberStubs(node);
			flushInstanceInitializers(type, 0);
		} finally {
			_currentType = saved;
			_currentAuxillaryType = savedAuxillary;
		}
	}

	private void mapVisibility(BodyDeclaration node, CSMember member) {
		member.visibility(mapVisibility(node));
	}

	protected boolean isNonStaticNestedType(ITypeBinding binding) {
		if (binding.isInterface())
			return false;
		if (!binding.isNested())
			return false;
		return !isStatic(binding);
	}

	private boolean isStatic(ITypeBinding binding) {
	    return Modifier.isStatic(binding.getModifiers());
    }

	private void addType(ITypeBinding binding, CSType type) {
		if (null != _currentType && !isExtractedNestedType(binding)) {
			_currentType.addMember(type);
		} else {
			_compilationUnit.addType(type);
		}
	}

	private void mapDocumentation(final BodyDeclaration bodyDecl, final CSMember member) {
		my(PreserveFullyQualifiedNamesState.class).using(true, new Runnable() { public void run() {			
			if (processDocumentationOverlay(member)) {
				return;
			}
	
			mapJavadoc(bodyDecl, member);
			mapDeclaredExceptions(bodyDecl, member);
			
		}});
	}

	private void mapDeclaredExceptions(BodyDeclaration bodyDecl, CSMember member) {
		if (!(bodyDecl instanceof MethodDeclaration))
			return;

		MethodDeclaration method = (MethodDeclaration) bodyDecl;
		mapThrownExceptions(method.thrownExceptions(), member);
	}

	private void mapThrownExceptions(List thrownExceptions, CSMember member) {
		for (Object exception : thrownExceptions) {
			mapThrownException((Name) exception, member);
		}
	}

	private void mapThrownException(Name exception, CSMember member) {
		final String typeName = mappedTypeName(exception.resolveTypeBinding());
		if (containsExceptionTagWithCRef(member, typeName))
			return;

		member.addDoc(newTagWithCRef("exception", typeName));
	}

	private boolean containsExceptionTagWithCRef(CSMember member, String cref) {
		for (CSDocNode node : member.docs()) {
			if (!(node instanceof CSDocTagNode))
				continue;

			if (cref.equals(((CSDocTagNode) node).getAttribute("cref"))) {
				return true;
			}
		}
		return false;
	}

	private void mapJavadoc(final BodyDeclaration bodyDecl, final CSMember member) {
		final Javadoc javadoc = bodyDecl.getJavadoc();
		if (null == javadoc) {
			return;
		}
		
		mapJavadocTags(javadoc, member);
	}

	private boolean processDocumentationOverlay(CSMember node) {
		if (node instanceof CSTypeDeclaration) {
			return processTypeDocumentationOverlay((CSTypeDeclaration) node);
		}
		return processMemberDocumentationOverlay((CSMember) node);
	}

	private boolean processMemberDocumentationOverlay(CSMember node) {
		String overlay = documentationOverlay().forMember(currentTypeQName(), node.signature());
		return processDocumentationOverlay(node, overlay);
	}

	private String currentTypeQName() {
		return qualifiedName(_currentType);
	}

	private boolean processTypeDocumentationOverlay(CSTypeDeclaration node) {
		String overlay = documentationOverlay().forType(qualifiedName(node));
		return processDocumentationOverlay(node, overlay);
	}

	private boolean processDocumentationOverlay(CSMember node, String overlay) {
		if (null == overlay) {
			return false;
		}
		node.addDoc(new CSDocTextOverlay(overlay.trim()));
		return true;
	}

	private DocumentationOverlay documentationOverlay() {
		return _configuration.documentationOverlay();
	}

	private String qualifiedName(CSTypeDeclaration node) {
		if (currentNamespace() == null) {
			return node.name();
		}
		return currentNamespace() + "." + node.name();
	}

	private String currentNamespace() {
		return _compilationUnit.namespace();
	}

	private void mapJavadocTags(final Javadoc javadoc, final CSMember member) {
		for (Object tag : javadoc.tags()) {
			try {
				TagElement element = (TagElement) tag;
				String tagName = element.getTagName();
				if (null == tagName) {
					mapJavadocSummary(member, element);
				} else {
					processTagElement(member, element);
				}
			} catch (Exception x) {
				warning((ASTNode) tag, x.getMessage());
				x.printStackTrace();
			}
		}
	}

	private void processTagElement(final CSMember member, TagElement element) {
		if (processSemanticallySignificantTagElement(member, element)) {
			return;
		}
		if (!isConversionTag(element.getTagName())) {
			member.addDoc(mapTagElement(member, element));
		}
		else if (isAttributeAnnotation(element)){
			processAttribute(member, element);
		}
		else if (isNewAnnotation(element)){
			member.setNewModifier(true);
		}
	}

	private boolean isAttributeAnnotation(TagElement element) {
		return element.getTagName().equals(SharpenAnnotations.SHARPEN_ATTRIBUTE);
	}

	private boolean isNewAnnotation(TagElement element) {
		return element.getTagName().equals(SharpenAnnotations.SHARPEN_NEW);
	}
	
	private void processAttribute(CSMember member, TagElement element) {
		String attrType = mappedTypeName(JavadocUtility.singleTextFragmentFrom(element));
		CSAttribute attribute = new CSAttribute(attrType);
		member.addAttribute(attribute);
	}

	private boolean processSemanticallySignificantTagElement(CSMember member, TagElement element) {
		if (element.getTagName().equals("@deprecated")) {
			member.removeAttribute("System.Obsolete");
			member.removeAttribute("System.ObsoleteAttribute");
			member.removeAttribute("Obsolete");
			member.removeAttribute("ObsoleteAttribute");
			member.addAttribute(obsoleteAttributeFromDeprecatedTagElement(element));
			return true;
		}
		return false;
	}

	private CSAttribute obsoleteAttributeFromDeprecatedTagElement(TagElement element) {

		CSAttribute attribute = new CSAttribute(mappedTypeName("System.ObsoleteAttribute"));
		if (element.fragments().isEmpty()) {
			return attribute;
		}
		attribute.addArgument(new CSStringLiteralExpression(toLiteralStringForm(getWholeText(element))));
		return attribute;
	}

	private String getWholeText(TagElement element) {
		StringBuilder builder = new StringBuilder();
		
		for (ASTNode fragment : (List<ASTNode>) element.fragments()) {
			if (fragment instanceof TextElement) {
				TextElement textElement = (TextElement) fragment;
				String text = textElement.getText();
				appendWithSpaceIfRequired(builder, text);
			} else if (fragment instanceof TagElement) {
				builder.append(getWholeText((TagElement) fragment));
			} else if (fragment instanceof MethodRef) {
				builder.append(mapCRefTarget(fragment));
			} else if (fragment instanceof MemberRef) {
				builder.append(mapCRefTarget(fragment));
			} else if (fragment instanceof Name) {
				builder.append(mapCRefTarget(fragment));
			} else {
				break;
			}
		}
		return builder.toString().trim();
	}

	private void appendWithSpaceIfRequired(StringBuilder builder, String text) {
		if (builder.length() > 0 && builder.charAt(builder.length()-1) != ' ' && text.startsWith(" ") == false) {
			builder.append(" ");
		}
		builder.append(text);
	}

	private String toLiteralStringForm(String s) {
		// TODO: deal with escaping sequences here
		return "@\"" + s.replace("\"", "\"\"") + "\"";
	}

	private boolean isConversionTag(String tagName) {
		return tagName.startsWith("@sharpen.");
	}

	private void processPartialTagElement(AbstractTypeDeclaration node, CSTypeDeclaration member) {
		TagElement element = javadocTagFor(node, SharpenAnnotations.SHARPEN_PARTIAL);
		if (null == element)
			return;
		((CSTypeDeclaration) member).partial(true);
	}

	private TagElement javadocTagFor(PackageDeclaration node, final String withName) {
		return JavadocUtility.getJavadocTag(node, withName);
	}
	
	private TagElement javadocTagFor(BodyDeclaration node, final String withName) {
		return JavadocUtility.getJavadocTag(node, withName);
	}

	private void mapJavadocSummary(final CSMember member, TagElement element) {
		List<String> summary = getFirstSentence(element);
		if (null != summary) {
			CSDocTagNode summaryNode = new CSDocTagNode("summary");
			for (String fragment : summary) {
				summaryNode.addFragment(new CSDocTextNode(fragment));
			}
			member.addDoc(summaryNode);
			CSDocNode remarksNode = createTagNode(member, "remarks", element, false);
			if(!summaryEqualsRemarks(summary, remarksNode))
				member.addDoc(remarksNode);
		} else {
			member.addDoc(createTagNode(member, "summary", element, false));
		}
	}
	
	private boolean summaryEqualsRemarks(List<String> summaryList, CSDocNode remarksNode) {
		if(!(remarksNode instanceof CSDocTagNode)){
			return false;
		}
		
		String summary = "";
		
		for(String str : summaryList){
			summary += str.trim();
		}
		
		String remarksStr = "";
		
		CSDocTagNode remarks = (CSDocTagNode) remarksNode;
		for(CSDocNode node : remarks.fragments()){
			if(!(node instanceof CSDocTextNode)){
				return false;
			}
			
			CSDocTextNode remarksDoc = (CSDocTextNode)node;			
			remarksStr += remarksDoc.text().trim();
		}
		
		return summary.equalsIgnoreCase(remarksStr);
	}

	private List<String> getFirstSentence(TagElement element) {
		List<String> fragments = new LinkedList<String>();
		for (Object fragment : element.fragments()) {
			if (fragment instanceof TextElement) {
				TextElement textElement = (TextElement) fragment;
				String text = textElement.getText();
				int index = findSentenceClosure(text);
				if (index > -1) {
					fragments.add(text.substring(0, index + 1));
					return fragments;
				} else {
					fragments.add(text);
				}
			} else {
				break;
			}
		}
		return null;
	}

	private int findSentenceClosure(String text) {
		Matcher matcher = SUMMARY_CLOSURE_PATTERN.matcher(text);
		return matcher.find() ? matcher.start() : -1;
	}

	private CSDocNode mapTagElement(CSMember member, TagElement element) {
		String tagName = element.getTagName();
		if (TagElement.TAG_PARAM.equals(tagName)) {
			return mapTagParam(member, element);
		} else if (TagElement.TAG_RETURN.equals(tagName)) {
			return createTagNode(member, "returns", element, false);
		} else if (TagElement.TAG_LINK.equals(tagName)) {
			return mapTagLink(member, element);
		} else if (TagElement.TAG_THROWS.equals(tagName)) {
			return mapTagThrows(member, element);
		} else if (TagElement.TAG_SEE.equals(tagName)) {
			return mapTagWithCRef(member, "seealso", element);
		} else if (TagElement.TAG_CODE.equals(tagName)) {
			if (element.fragments().size() == 1) {
				if (element.fragments().get(0) instanceof TextElement) {
					return mapSingleTextElementCodeTagNode(member, element);
				}
			}

			return createTagNode(member, "c", element, true);
		}
		return createTagNode(member, tagName.substring(1), element, false);
	}

	// Used for emitting <see langword="true"/>, etc.
	// http://www.ewoodruff.us/xmlcommentsguide/html/983fed56-321c-4daf-af16-e3169b28ffcd.htm
	private static final Set<String> LANGUAGE_KEYWORDS = new HashSet<String>();
	static {
		LANGUAGE_KEYWORDS.add("true");
		LANGUAGE_KEYWORDS.add("false");
		LANGUAGE_KEYWORDS.add("null");
		LANGUAGE_KEYWORDS.add("static");
		LANGUAGE_KEYWORDS.add("abstract");
	}

	private CSDocNode mapSingleTextElementCodeTagNode(CSMember member, TagElement element) {
		TextElement fragment = (TextElement)element.fragments().get(0);
		String word = fragment.getText().trim();

		// {@code true} --> <see langword="true"/>
		if (LANGUAGE_KEYWORDS.contains(word)) {
			CSDocTagNode node = new CSDocTagNode("see");
			node.addAttribute("langword", word);
			return node;
		}

		// {@code foo} --> <paramref name="foo"/>
		if (member instanceof CSMethodBase) {
			CSMethodBase method = (CSMethodBase)member;
			boolean isParameterName = false;
			for (CSVariableDeclaration parameter : method.parameters()) {
				if (parameter.name().equals(word)) {
					isParameterName = true;
					break;
				}
			}

			if (isParameterName) {
				CSDocTagNode node = new CSDocTagNode("paramref");
				node.addAttribute("name", word);
				return node;
			}
		}

		return createTagNode(member, "c", element, true);
	}

	private CSDocNode mapTagThrows(CSMember member, TagElement element) {
		return mapTagWithCRef(member, "exception", element);
	}

	private CSDocNode mapTagLink(CSMember member, TagElement element) {
		return mapTagWithCRef(member, "see", element);
	}

	private CSDocNode mapTagWithCRef(CSMember member, String tagName, TagElement element) {
		final List fragments = element.fragments();
		if (fragments.isEmpty()) {
			return invalidTagWithCRef(member, element, tagName, element);
		}
		final ASTNode linkTarget = (ASTNode) fragments.get(0);
		String cref = mapCRefTarget(linkTarget);
		if (null == cref) {
			return invalidTagWithCRef(member, linkTarget, tagName, element);
		}
		CSDocTagNode node = newTagWithCRef(tagName, cref);
		if (fragments.size() > 1) {
			if (isLinkWithSimpleLabel(fragments, linkTarget)) {
				node.addTextFragment(unqualifiedName(cref));
			} else {
				collectFragments(member, node, fragments, 1);
			}
		}
		return node;
	}

	private ASTNode documentedNodeAttachedTo(TagElement element) {
		ASTNode attachedToNode = element;
		while (attachedToNode instanceof TagElement || attachedToNode instanceof Javadoc) {
			attachedToNode = attachedToNode.getParent();
		}
		return attachedToNode;
	}
	
	private CSDocNode invalidTagWithCRef(CSMember member, final ASTNode linkTarget, String tagName, TagElement element) {
		warning(linkTarget, "Tag '" + element.getTagName() + "' demands a valid cref target.");
		CSDocNode newTag = createTagNode(member, tagName, element, false);
		return newTag;
	}

	private CSDocTagNode newTagWithCRef(String tagName, String cref) {
		CSDocTagNode node = new CSDocTagNode(tagName);
		node.addAttribute("cref", cref);
		return node;
	}

	private boolean isLinkWithSimpleLabel(List<ASTNode> fragments, final ASTNode linkTarget) {
		if (fragments.size() != 2)
			return false;
		if (!JavadocUtility.isTextFragment(fragments, 1))
			return false;
		final String link = linkTarget.toString();
		final String label = JavadocUtility.textFragment(fragments, 1);
		return label.equals(link) || label.equals(unqualifiedName(link));
	}

	private String mapCRefTarget(final ASTNode crefTarget) {
		return new CRefBuilder(crefTarget).build();		
	}

	private CSDocNode mapTagParam(CSMember member, TagElement element) {
		
		List fragments = element.fragments();
		
		if (!(fragments.get(0) instanceof SimpleName))
			return new CSDocTagNode("?");
		SimpleName name = (SimpleName) fragments.get(0);
		if (null == name.resolveBinding()) {
			warning(name, "Parameter '" + name + "' not found.");
		}
		
		CSDocTagNode param = isPropertyNode(documentedNodeAttachedTo(element)) 
									? new CSDocTagNode("value") 
									: newCSDocTag(fixIdentifierNameFor(identifier(name), element));
		
		collectFragments(member, param, fragments, 1);
		return param;
	}

	private CSDocTagNode newCSDocTag(final String paramName) {
		CSDocTagNode param;
		param = new CSDocTagNode("param");
		param.addAttribute("name", paramName);
		return param;
	}

	private boolean isPropertyNode(ASTNode node) {
		if (node.getNodeType() != ASTNode.METHOD_DECLARATION) {
			return false;
		}
		
		return isProperty((MethodDeclaration) node);
	}

	private String fixIdentifierNameFor(String identifier, TagElement element) {
		return removeAtSign(identifier);
	}

	private String removeAtSign(String identifier) {
		return identifier.startsWith("@") 
						? identifier.substring(1) 
						: identifier;
	}

	private void collectFragments(CSMember member, CSDocTagNode node, List fragments, int index) {
		for (int i = index; i < fragments.size(); ++i) {
			node.addFragment(mapTagElementFragment(member, (ASTNode) fragments.get(i), false));
		}
	}

	private CSDocNode mapTextElement(TextElement element, boolean escapeText) {
		String text = element.getText();
		if (escapeText) {
			text = text.replace("<", "&lt;").replace(">", "&gt;");
		} else if (HTML_ANCHOR_PATTERN.matcher(text).find()) {
			warning(element, "Caution: HTML anchors can result in broken links. Consider using @link instead.");
		}
		return new CSDocTextNode(text);
	}

	private CSDocNode createTagNode(CSMember member, String tagName, TagElement element, boolean escapeText) {
		CSDocTagNode summary = new CSDocTagNode(tagName);
		for (Object f : element.fragments()) {
			summary.addFragment(mapTagElementFragment(member, (ASTNode) f, escapeText));
		}
		return summary;
	}

	private CSDocNode mapTagElementFragment(CSMember member, ASTNode node, boolean escapeText) {
		switch (node.getNodeType()) {
		case ASTNode.TAG_ELEMENT:
			return mapTagElement(member, (TagElement) node);
		case ASTNode.TEXT_ELEMENT:
			return mapTextElement((TextElement) node, escapeText);
		}
		warning(node, "Documentation node not supported: " + node.getClass() + ": " + node);
		return new CSDocTextNode(node.toString());
	}

	public boolean visit(FieldDeclaration node) {
		if (SharpenAnnotations.hasIgnoreAnnotation(node)) {
			return false;
		}

		ITypeBinding fieldType = node.getType().resolveBinding();
		CSTypeReferenceExpression typeName = mappedTypeReference(fieldType);
		CSVisibility visibility = mapVisibility(node);
		if (((VariableDeclarationFragment)node.fragments().get(0)).resolveBinding().getDeclaringClass().isInterface())
			visibility = CSVisibility.Public;

		for (Object item : node.fragments()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) item;
			ITypeBinding saved = pushExpectedType(fieldType);
			CSField field = mapFieldDeclarationFragment(node, fragment, typeName, visibility);

			popExpectedType(saved);
			adjustVisibility (fieldType, field);

			CSTypeDeclaration enclosing = _currentType;
			if (_currentType.isInterface() && _currentAuxillaryType != null) {
				enclosing = _currentAuxillaryType;
			}

			enclosing.addMember(field);
		}

		return false;
	}

	private CSField mapFieldDeclarationFragment(FieldDeclaration node, VariableDeclarationFragment fragment,
	        CSTypeReferenceExpression fieldType, CSVisibility fieldVisibility) {
		if (fragment.getExtraDimensions() > 0) {
			fieldType = new CSArrayTypeReference(fieldType, fragment.getExtraDimensions());
		}
		CSField field = new CSField(fieldName(fragment), fieldType, fieldVisibility, mapFieldInitializer(fragment));
		if (isConstField(node, fragment)) {
			field.addModifier(CSFieldModifier.Const);
		} else {
			processFieldModifiers(field, node.getModifiers());
		}
		mapAnnotations(node, field);
		mapDocumentation(node, field);
		return field;
	}

	private void mapAnnotations(BodyDeclaration node, CSMember member) {
		for (Object m : node.modifiers()) {
			if (!(m instanceof Annotation)) {
				continue;
			}
			if (isIgnoredAnnotation((Annotation)m)) {
				continue;
			}
			if (m instanceof MarkerAnnotation) {
				mapMarkerAnnotation((MarkerAnnotation)m, member);
			}
		}
	}

	private boolean isIgnoredAnnotation(Annotation m) {
		return _configuration.isIgnoredAnnotation(qualifiedName(m.resolveAnnotationBinding().getAnnotationType()));
    }

	private void mapMarkerAnnotation(MarkerAnnotation annotation, CSMember member) {
		final IAnnotationBinding binding = annotation.resolveAnnotationBinding();
		final CSAttribute attribute = new CSAttribute(mappedTypeName(binding.getAnnotationType()));
		member.addAttribute(attribute);
    }

	protected String fieldName(VariableDeclarationFragment fragment) {
		return identifier(fragment.getName());
	}

	protected String fieldName(EnumConstantDeclaration enumConstant) {
		return identifier(enumConstant.getName());
	}

	protected CSExpression mapFieldInitializer(VariableDeclarationFragment fragment) {
		return mapExpression(fragment.getInitializer());
	}

	private boolean isConstField(FieldDeclaration node, VariableDeclarationFragment fragment) {
		//
		if (fragment.resolveBinding().getDeclaringClass().isInterface())
			return true;
		return Modifier.isFinal(node.getModifiers()) && isSupportedConstantType(node.getType()) &&
			hasConstValue(fragment) && Modifier.isStatic(node.getModifiers());
	}

	private boolean isSupportedConstantType(Type type) {
		return type.isPrimitiveType()
			|| type.resolveBinding().getQualifiedName().equals(String.class.getCanonicalName());
	}

	private boolean hasConstValue(VariableDeclarationFragment fragment) {
		return null != fragment.resolveBinding().getConstantValue();
	}

	private void processFieldModifiers(CSField field, int modifiers) {
		if (Modifier.isStatic(modifiers)) {
			field.addModifier(CSFieldModifier.Static);
		}
		if (Modifier.isFinal(modifiers)) {
			field.addModifier(CSFieldModifier.Readonly);
		}
		if (Modifier.isTransient(modifiers)) {
			field.addAttribute(new CSAttribute(mappedTypeName("System.NonSerialized")));
		}
		if (Modifier.isVolatile(modifiers)) {
			field.addModifier(CSFieldModifier.Volatile);
		}

	}

	private boolean isDestructor(MethodDeclaration node) {
		return node.getName().toString().equals("finalize");
	}

	public boolean visit(Initializer node) {
		if (Modifier.isStatic(node.getModifiers())) {
			CSConstructor ctor = new CSConstructor(CSConstructorModifier.Static);
			_currentType.addMember(ctor);
			visitBodyDeclarationBlock(node, node.getBody(), ctor);
		} else {
			_instanceInitializers.add(node);
		}
		return false;
	}

	public boolean visit(MethodDeclaration node) {
		if (SharpenAnnotations.hasIgnoreAnnotation(node) || isRemoved(node)) {
			return false;
		}

		if (isEvent(node)) {
			processEventDeclaration(node);
			return false;
		}

		if (isMappedToProperty(node)) {
			processMappedPropertyDeclaration(node);
			return false;
		}
		
		if (isTaggedAsProperty(node)) {
			processPropertyDeclaration(node);
			return false;
		}

		if (isIndexer(node)) {
			processIndexerDeclaration(node);
			return false;
		}

		processMethodDeclaration(node);

		return false;
	}

	private void processIndexerDeclaration(MethodDeclaration node) {
		processPropertyDeclaration(node, CSProperty.INDEXER);
	}

	private boolean isIndexer(MethodDeclaration node) {
		return isTaggedDeclaration(node, SharpenAnnotations.SHARPEN_INDEXER);
	}

	private boolean isRemoved(MethodDeclaration node) {
		return hasRemoveAnnotation(node) || isRemoved(node.resolveBinding());
	}

	private boolean hasRemoveAnnotation(BodyDeclaration node) {
	    return containsJavadoc(node, SharpenAnnotations.SHARPEN_REMOVE);
    }

	private boolean isRemoved(final IMethodBinding binding) {
		return _configuration.isRemoved(qualifiedName(binding));
	}

	public static boolean containsJavadoc(BodyDeclaration node, final String tag) {
		return JavadocUtility.containsJavadoc(node, tag);
	}

	private void processPropertyDeclaration(MethodDeclaration node) {
		processPropertyDeclaration(node, propertyName(node));
	}
	
	private void processMappedPropertyDeclaration(MethodDeclaration node) {
		processPropertyDeclaration(node, mappedMethodName(node));
	}

	private void processPropertyDeclaration(MethodDeclaration node, final String name) {
		mapPropertyDeclaration(node, producePropertyFor(node, name));
	}

	private CSProperty producePropertyFor(MethodDeclaration node, final String name) {
	    CSProperty existingProperty = findProperty(node, name);
	    if (existingProperty != null) {
	    	return existingProperty;
	    }
		CSProperty property = newPropertyFor(node, name);
		_currentType.addMember(property);
	    return property;
    }

	private CSProperty findProperty(MethodDeclaration node, final String name) {
		CSMember existingProperty = _currentType.getMember(name);
		if (existingProperty != null) {
			if (! (existingProperty instanceof CSProperty)) {
				throw new IllegalArgumentException(sourceInformation(node) + ": Previously declared member redeclared as property.");
			}
		}
		return (CSProperty) existingProperty;
	}

	private CSProperty mapPropertyDeclaration(MethodDeclaration node, CSProperty property) {
		final CSBlock block = mapBody(node);
		
		if (isGetter(node)) {
			property.getter(block);
		} else {
			property.setter(block);
			mapImplicitSetterParameter(node, property);
		}
		mapMetaMemberAttributes(node, property);
		mapParameters(node, property);
		return property;
	}

	private void mapImplicitSetterParameter(MethodDeclaration node, CSProperty property) {
	    final String parameterName = parameter(node, 0).getName().toString();
	    if (parameterName.equals("value")) {
	    	return;
	    }
	    
    	property.setter().addStatement(0,
    			newVariableDeclarationExpression(parameterName, property.type(), new CSReferenceExpression("value")));
    }

	private CSDeclarationExpression newVariableDeclarationExpression(final String name,
            final CSTypeReferenceExpression type, final CSReferenceExpression initializer) {
	    return new CSDeclarationExpression(
	    	new CSVariableDeclaration(name, type, initializer));
    }

	private CSProperty newPropertyFor(MethodDeclaration node, final String propName) {
	    final CSTypeReferenceExpression propertyType = isGetter(node)
				? mappedReturnType(node)
		        : mappedTypeReference(lastParameter(node).getType());
		CSProperty p = new CSProperty(propName, propertyType);
	    return p;
    }

	private CSBlock mapBody(MethodDeclaration node) {
		final CSBlock block = new CSBlock();
		processBlock(node, node.getBody(), block);
		return block;
	}

	private boolean isGetter(MethodDeclaration node) {
		return !"void".equals(node.getReturnType2().toString());
	}

	private SingleVariableDeclaration lastParameter(MethodDeclaration node) {
		return parameter(node, node.parameters().size() - 1);
	}

	private String propertyName(MethodDeclaration node) {
		return my(Annotations.class).annotatedPropertyName(node);
	}
	
	private String propertyName(IMethodBinding binding) {
		return propertyName((MethodDeclaration)declaringNode(binding));
	}
	
	private boolean isProperty(BodyDeclaration node) {
		return isTaggedAsProperty(node)
			|| isMappedToProperty(node);
	}

	private boolean isTaggedAsProperty(BodyDeclaration node) {
	    return isTaggedDeclaration(node, SharpenAnnotations.SHARPEN_PROPERTY);
    }

	private boolean isTaggedDeclaration(BodyDeclaration node, final String tag) {
		return effectiveAnnotationFor(node, tag) != null;
	}

	private void processMethodDeclaration(MethodDeclaration node) {
		if (isDestructor(node)) {
			mapMethodParts(node, new CSDestructor());
			return;
		}

		if (node.isConstructor()) {
			mapMethodParts(node, new CSConstructor());
			return;
		}

		CSMethod method = new CSMethod(mappedMethodDeclarationName(node));
		method.returnType(mappedReturnType(node));
		method.modifier(mapMethodModifier(node));
		mapTypeParameters(node.typeParameters(), method);
		mapMethodParts(node, method);
		
		if (_configuration.junitConversion() && isLegacyTestFixture(node.resolveBinding().getDeclaringClass())) {
			if (method.name().startsWith("Test") && method.visibility() == CSVisibility.Public)
				method.addAttribute(new CSAttribute ("NUnit.Framework.Test"));
			if (isLegacyTestFixtureClass (node.resolveBinding().getDeclaringClass().getSuperclass())) {
				if (method.name().equals("SetUp")) {
					method.addAttribute(new CSAttribute ("NUnit.Framework.SetUp"));
					method.modifier (CSMethodModifier.Virtual);
					cleanBaseSetupCalls (method);
				}
				else if (method.name().equals("TearDown")) {
					method.addAttribute(new CSAttribute ("NUnit.Framework.TearDown"));
					method.modifier (CSMethodModifier.Virtual);
					cleanBaseSetupCalls (method);
				}
			}
		}
	}
	
	private void cleanBaseSetupCalls (CSMethod method) {
		ArrayList<CSStatement> toDelete = new ArrayList<CSStatement> ();
		for (CSStatement st: method.body().statements()) {
			if (st instanceof CSExpressionStatement) {
				CSExpressionStatement es = (CSExpressionStatement) st;
				if (es.expression() instanceof CSMethodInvocationExpression) {
					CSMethodInvocationExpression mie = (CSMethodInvocationExpression) es.expression();
					if (mie.expression() instanceof CSMemberReferenceExpression) {
						CSMemberReferenceExpression mr = (CSMemberReferenceExpression) mie.expression();
						if ((mr.expression() instanceof CSBaseExpression) && (mr.name().equals("SetUp") || mr.name().equals("TearDown")))
							toDelete.add(st);
					}
				}
			}
		}
		for (CSStatement st : toDelete)
			method.body().removeStatement (st);
	}

	private void mapMethodParts(MethodDeclaration node, CSMethodBase method) {

		_currentType.addMember(method);

		method.startPosition(node.getStartPosition());
		method.isVarArgs(node.isVarargs());
		mapParameters(node, method);
		mapAnnotations(node, method);
		mapDocumentation(node, method);
		visitBodyDeclarationBlock(node, node.getBody(), method);
		
		IMethodBinding overriden = getOverridedMethod(node);
		if (!node.isConstructor() && overriden != null) {
			CSVisibility vis = mapVisibility (overriden.getModifiers());
			if (vis == CSVisibility.ProtectedInternal && !overriden.getDeclaringClass().isFromSource())
				vis = CSVisibility.Protected;
			method.visibility(vis);
		}
		else if (node.resolveBinding().getDeclaringClass().isInterface())
			method.visibility(CSVisibility.Public);
		else
			mapVisibility(node, method);
	}
	
	private String mappedMethodDeclarationName(MethodDeclaration node) {
		final String mappedName = mappedMethodName(node);
		if (null == mappedName || 0 == mappedName.length()|| mappedName.contains(".")) {
			return methodName(node.getName().toString());
		}
		return mappedName;
	}

	private void mapParameters(MethodDeclaration node, CSParameterized method) {
		if (method instanceof CSMethod) {
			mapMethodParameters(node, (CSMethod) method);
			return;
		}
		for (Object p : node.parameters()) {
			mapParameter((SingleVariableDeclaration) p, method);
		}
	}

	private void mapParameter(SingleVariableDeclaration parameter, CSParameterized method) {
		if (method instanceof CSMethod) {
			IVariableBinding vb = parameter.resolveBinding();
			ITypeBinding[] ta = vb.getType().getTypeArguments();
			//	we need to check that generic class is not Class<?>
			if (ta.length > 0 && ta[0].getName().startsWith("?") && !isJavaLangClass(parameter.resolveBinding().getType())) {
				ITypeBinding extended = mapTypeParameterExtendedType (ta[0]);
				CSMethod met = (CSMethod)method;
				String genericArg = "_T" + met.typeParameters().size();
				CSTypeParameter tp = new CSTypeParameter (genericArg);
				if (extended != null)
					tp.superClass(mappedTypeReference(extended));
				met.addTypeParameter(tp);
				
				CSTypeReference tr = new CSTypeReference (mappedTypeName(vb.getType()));
				tr.addTypeArgument(new CSTypeReference (genericArg));
				method.addParameter(new CSVariableDeclaration (identifier (vb.getName()), tr));
				return;
			}
		}
		method.addParameter(createParameter(parameter));
	}
	
	ITypeBinding mapTypeParameterExtendedType (ITypeBinding tb) {
		ITypeBinding superc = tb.getSuperclass();
		if (superc != null && !superc.getQualifiedName().equals("java.lang.Object") && !superc.getQualifiedName().equals("java.lang.Enum<?>")) {
			return superc;
		}
		ITypeBinding[] ints = tb.getInterfaces();
		if (ints.length > 0)
			return ints[0];
		return null;
	}

	private void mapMethodParameters(MethodDeclaration node, CSMethod method) {
		for (Object o : node.parameters()) {
			SingleVariableDeclaration p = (SingleVariableDeclaration) o;
			ITypeBinding parameterType = p.getType().resolveBinding();
			if (isGenericRuntimeParameterIdiom(node.resolveBinding(), parameterType)) {

				// System.Type <p.name> = typeof(<T>);
				method.body().addStatement(
				        new CSDeclarationStatement(p.getStartPosition(), new CSVariableDeclaration(identifier(p
				                .getName()), new CSTypeReference("System.Type"), new CSTypeofExpression(
				                genericRuntimeTypeIdiomType(parameterType)))));

			} else {

				mapParameter(p, method);
			}
		}
	}

	private CSTypeReferenceExpression genericRuntimeTypeIdiomType(ITypeBinding parameterType) {
		return mappedTypeReference(parameterType.getTypeArguments()[0]);
	}

	private boolean isGenericRuntimeParameterIdiom(IMethodBinding method, ITypeBinding parameterType) {
		if (!parameterType.isParameterizedType()) {
			return false;
		}
		if (!"java.lang.Class".equals(qualifiedName(parameterType))) {
			return false;
		}
		// detecting if the T in Class<T> comes from the method itself
		final ITypeBinding classTypeArgument = parameterType.getTypeArguments()[0];
		return classTypeArgument.getDeclaringMethod() == method;
	}

	private CSTypeReferenceExpression mappedReturnType(MethodDeclaration node) {
		IMethodBinding overriden = getOverridedMethod(node);
		if (overriden != null)
			return mappedTypeReference (overriden.getReturnType());
		return mappedTypeReference(node.getReturnType2());
	}

	private void processEventDeclaration(MethodDeclaration node) {
		CSTypeReference eventHandlerType = new CSTypeReference(getEventHandlerTypeName(node));
		CSEvent event = createEventFromMethod(node, eventHandlerType);
		mapMetaMemberAttributes(node, event);
		if (_currentType.isInterface())
			return;

		VariableDeclarationFragment field = getEventBackingField(node);
		CSField backingField = (CSField) _currentType.getMember(field.getName().toString());
		backingField.type(eventHandlerType);

		// clean field
		backingField.initializer(null);
		backingField.removeModifier(CSFieldModifier.Readonly);

		final CSBlock addBlock = createEventBlock(backingField, "System.Delegate.Combine");
		String onAddMethod = getEventOnAddMethod(node);
		if (onAddMethod != null) {
			addBlock.addStatement(new CSMethodInvocationExpression(new CSReferenceExpression(onAddMethod)));
		}
		event.setAddBlock(addBlock);
		event.setRemoveBlock(createEventBlock(backingField, "System.Delegate.Remove"));
	}

	private String getEventOnAddMethod(MethodDeclaration node) {
		final TagElement onAddTag = javadocTagFor(node, SharpenAnnotations.SHARPEN_EVENT_ON_ADD);
		if (null == onAddTag)
			return null;
		return methodName(JavadocUtility.singleTextFragmentFrom(onAddTag));
	}

	private String getEventHandlerTypeName(MethodDeclaration node) {
		final String eventArgsType = getEventArgsType(node);
		return buildEventHandlerTypeName(node, eventArgsType);
	}

	private void mapMetaMemberAttributes(MethodDeclaration node, CSMetaMember metaMember) {
		mapVisibility(node, metaMember);
		metaMember.modifier(mapMethodModifier(node));
		mapDocumentation(node, metaMember);
	}

	private CSBlock createEventBlock(CSField backingField, String delegateMethod) {
		CSBlock block = new CSBlock();
		block.addStatement(new CSInfixExpression("=", new CSReferenceExpression(backingField.name()),
		        new CSCastExpression(backingField.type(), new CSMethodInvocationExpression(new CSReferenceExpression(
		                delegateMethod), new CSReferenceExpression(backingField.name()), new CSReferenceExpression(
		                "value")))));
		return block;
	}

	private static final class CheckVariableUseVisitor extends ASTVisitor {

		private final IVariableBinding _var;
		private boolean _used;

		private CheckVariableUseVisitor(IVariableBinding var) {
			this._var = var;
		}

		@Override
		public boolean visit(SimpleName name) {
			IBinding binding = name.resolveBinding();
			if(binding == null){
				return false;
			}
			if (binding.equals(_var)) {
				_used = true;
			}

			return false;
		}

		public boolean used() {
			return _used;
		}
	}

	private static final class FieldAccessFinder extends ASTVisitor {
		public IBinding field;

		@Override
		public boolean visit(SimpleName node) {
			field = node.resolveBinding();
			return false;
		}
	}

	private VariableDeclarationFragment getEventBackingField(MethodDeclaration node) {
		FieldAccessFinder finder = new FieldAccessFinder();
		node.accept(finder);
		return findDeclaringNode(finder.field);
	}

	private CSEvent createEventFromMethod(MethodDeclaration node, CSTypeReference eventHandlerType) {
		String eventName = methodName(node);
		CSEvent event = new CSEvent(eventName, eventHandlerType);
		_currentType.addMember(event);
		return event;
	}

	private String methodName(MethodDeclaration node) {
		return methodName(node.getName().toString());
	}

	private String unqualifiedName(String typeName) {
		int index = typeName.lastIndexOf('.');
		if (index < 0)
			return typeName;
		return typeName.substring(index + 1);
	}

	private String buildEventHandlerTypeName(ASTNode node, String eventArgsTypeName) {
		if (!eventArgsTypeName.endsWith("EventArgs")) {
			warning(node, SharpenAnnotations.SHARPEN_EVENT + " type name must end with 'EventArgs'");
			return eventArgsTypeName + "EventHandler";
		}

		return "System.EventHandler<" + eventArgsTypeName + ">";
	}

	private String getEventArgsType(MethodDeclaration node) {
		TagElement tag = eventTagFor(node);
		if (null == tag)
			return null;
		return mappedTypeName(JavadocUtility.singleTextFragmentFrom(tag));
	}

	private TagElement eventTagFor(MethodDeclaration node) {
		return effectiveAnnotationFor(node, SharpenAnnotations.SHARPEN_EVENT);
	}

	private TagElement effectiveAnnotationFor(BodyDeclaration node, final String annotation) {
	    return my(Annotations.class).effectiveAnnotationFor(node, annotation);
	}

	private <T extends ASTNode> T findDeclaringNode(IBinding binding) {
		return (T) my(Bindings.class).findDeclaringNode(binding);		
	}

	private void visitBodyDeclarationBlock(BodyDeclaration node, Block block, CSMethodBase method) {
		CSMethodBase saved = _currentMethod;
		_currentMethod = method;

		processDisableTags(node, method);		
		processBlock(node, block, method.body());
		_currentMethod = saved;
	}

	private void processDisableTags(PackageDeclaration packageDeclaration, CSNode csNode) {
		TagElement tag = javadocTagFor(packageDeclaration, SharpenAnnotations.SHARPEN_IF);
		if (null == tag)
			return;

		csNode.addEnclosingIfDef(JavadocUtility.singleTextFragmentFrom(tag));
	}

	private void processDisableTags(BodyDeclaration node, CSNode csNode) {
		TagElement tag = javadocTagFor(node, SharpenAnnotations.SHARPEN_IF);
		if (null == tag)
			return;

		csNode.addEnclosingIfDef(JavadocUtility.singleTextFragmentFrom(tag));
	}

	private void processBlock(BodyDeclaration node, Block block, final CSBlock targetBlock) {
		if (containsJavadoc(node, SharpenAnnotations.SHARPEN_REMOVE_FIRST)) {
			block.statements().remove(0);
		}
		
		BodyDeclaration savedDeclaration = _currentBodyDeclaration;
		_currentBodyDeclaration = node;

		if (Modifier.isSynchronized(node.getModifiers())) {
			CSLockStatement lock = new CSLockStatement(node.getStartPosition(), getLockTarget(node));
			targetBlock.addStatement(lock);
			visitBlock(lock.body(), block);
		} else {
			visitBlock(targetBlock, block);
		}
		_currentBodyDeclaration = savedDeclaration;
	}

	private CSExpression getLockTarget(BodyDeclaration node) {
		return Modifier.isStatic(node.getModifiers()) ? new CSTypeofExpression(new CSTypeReference(_currentType.name()))
		        : new CSThisExpression();
	}

	public boolean visit(ConstructorInvocation node) {
		addChainedConstructorInvocation(new CSThisExpression(), node.arguments());
		return false;
	}

	private void addChainedConstructorInvocation(CSExpression target, List arguments) {
		CSConstructorInvocationExpression cie = new CSConstructorInvocationExpression(target);
		mapArguments(cie, arguments);
		((CSConstructor) _currentMethod).chainedConstructorInvocation(cie);
	}

	public boolean visit(SuperConstructorInvocation node) {
		if (null != node.getExpression()) {
			notImplemented(node);
		}
		addChainedConstructorInvocation(new CSBaseExpression(), node.arguments());
		return false;
	}

	private <T extends ASTNode> void visitBlock(CSBlock block, T node) {
		if (null == node) {
			return;
		}

		CSBlock saved = _currentBlock;
		_currentBlock = block;
		
		_currentContinueLabel = null;
		
		node.accept(this);
		_currentBlock = saved;
	}

	public boolean visit(VariableDeclarationExpression node) {
		pushExpression(new CSDeclarationExpression(createVariableDeclaration((VariableDeclarationFragment) node
		        .fragments().get(0))));
		return false;
	}

	public boolean visit(VariableDeclarationStatement node) {
		for (Object f : node.fragments()) {
			VariableDeclarationFragment variable = (VariableDeclarationFragment) f;
			addStatement(new CSDeclarationStatement(node.getStartPosition(), createVariableDeclaration(variable)));
		}
		return false;
	}

	private CSVariableDeclaration createVariableDeclaration(VariableDeclarationFragment variable) {
		IVariableBinding binding = variable.resolveBinding();
		ITypeBinding saved = pushExpectedType(binding.getType());
		CSExpression initializer = mapExpression(variable.getInitializer());
		popExpectedType(saved);
		return createVariableDeclaration(binding, initializer);
	}

	private CSVariableDeclaration createVariableDeclaration(IVariableBinding binding, CSExpression initializer) {
		String name = binding.getName();
		if (_blockVariables.size() > 0) {
			if (_blockVariables.peek().contains(name)) {
				int count = 1;
				while (_blockVariables.peek().contains(name + "_" + count)) {
					count++;
				}
				_renamedVariables.peek().put(name, name + "_" + count);
				name = name + "_" + count;
			}
			_localBlockVariables.peek().add(name);
			for (Set<String> s : _blockVariables)
				s.add(name);
		}
		return new CSVariableDeclaration(identifier(name), mappedTypeReference(binding.getType()),
		        initializer);
	}

	public boolean visit(ExpressionStatement node) {
		if (isRemovedMethodInvocation(node.getExpression())) {
			return false;
		}

		addStatement(new CSExpressionStatement(node.getStartPosition(), mapExpression(node.getExpression())));
		return false;
	}

	private boolean isRemovedMethodInvocation(Expression expression) {
		if (!(expression instanceof MethodInvocation)) {
			return false;
		}

		MethodInvocation invocation = (MethodInvocation) expression;
		return isTaggedMethodInvocation(invocation, SharpenAnnotations.SHARPEN_REMOVE)
		        || isRemoved(invocation.resolveMethodBinding());

	}
	
	public boolean isEnumOrdinalMethodInvocation (MethodInvocation node) {
		return node.getName().getIdentifier().equals("ordinal") && 
			node.getExpression() != null &&
			node.getExpression().resolveTypeBinding().isEnum();
	}

	public boolean isEnumNameMethodInvocation (MethodInvocation node) {
		return node.getName().getIdentifier().equals("name") && 
			node.getExpression() != null &&
			node.getExpression().resolveTypeBinding().isEnum();
	}

	public boolean visit(IfStatement node) {
		Expression expression = node.getExpression();

		Object constValue = constValue(expression);
		if (null != constValue) {
			// dead branch elimination
			if (isTrue(constValue)) {
				node.getThenStatement().accept(this);
			} else {
				if (null != node.getElseStatement()) {
					node.getElseStatement().accept(this);
				}
			}
		} else {
			CSIfStatement stmt = new CSIfStatement(node.getStartPosition(), mapExpression(expression));
			visitBlock(stmt.trueBlock(), node.getThenStatement());
			visitBlock(stmt.falseBlock(), node.getElseStatement());
			addStatement(stmt);
		}
		return false;
	}

	private boolean isTrue(Object constValue) {
		return ((Boolean) constValue).booleanValue();
	}

	private Object constValue(Expression expression) {
		switch (expression.getNodeType()) {
		case ASTNode.PREFIX_EXPRESSION:
			return constValue((PrefixExpression) expression);
		case ASTNode.SIMPLE_NAME:
		case ASTNode.QUALIFIED_NAME:
			return constValue((Name) expression);
		}
		return null;
	}

	public Object constValue(PrefixExpression expression) {
		if (PrefixExpression.Operator.NOT == expression.getOperator()) {
			Object value = constValue(expression.getOperand());
			if (null != value) {
				return isTrue(value) ? Boolean.FALSE : Boolean.TRUE;
			}
		}
		return null;
	}

	public Object constValue(Name expression) {
		IBinding binding = expression.resolveBinding();
		if (IBinding.VARIABLE == binding.getKind()) {
			return ((IVariableBinding) binding).getConstantValue();
		}
		return null;
	}

	public boolean visit(final WhileStatement node) {
		consumeContinueLabel(new Function<CSBlock>() {
			public CSBlock apply() {
				CSWhileStatement stmt = new CSWhileStatement(node.getStartPosition(), mapExpression(node.getExpression()));
				visitBlock(stmt.body(), node.getBody());
				addStatement(stmt);
				return stmt.body();
			}
		});
		return false;
	}

	public boolean visit(final DoStatement node) {
		consumeContinueLabel(new Function<CSBlock>() {
			public CSBlock apply() {
				CSDoStatement stmt = new CSDoStatement(node.getStartPosition(), mapExpression(node.getExpression()));
				visitBlock(stmt.body(), node.getBody());
				addStatement(stmt);
				return stmt.body();
			}
		});
		return false;
	}

	public boolean visit(TryStatement node) {
		CSTryStatement stmt = new CSTryStatement(node.getStartPosition());
		CSBlock body = stmt.body();
		for (Object resource : node.resources()) {
			VariableDeclarationExpression expression = (VariableDeclarationExpression) resource;
			expression.accept(this);
			CSUsingStatement using = new CSUsingStatement(expression.getStartPosition(), popExpression());
			body.addStatement(using);
			body = using.body();
		}
		visitBlock(body, node.getBody());
		for (Object o : node.catchClauses()) {
			CatchClause clause = (CatchClause) o;
			if (!_configuration.isIgnoredExceptionType(qualifiedName(clause.getException().getType().resolveBinding()))) {
				stmt.addCatchClause(mapCatchClause(clause));
			}
		}
		if (null != node.getFinally()) {
			CSBlock finallyBlock = new CSBlock();
			visitBlock(finallyBlock, node.getFinally());
			stmt.finallyBlock(finallyBlock);
		}

		if (null != stmt.finallyBlock() || !stmt.catchClauses().isEmpty()) {
			addStatement(stmt);
		} else {
			_currentBlock.addAll(stmt.body());
		}
		return false;
	}

	private CSCatchClause mapCatchClause(CatchClause node) {
		IVariableBinding oldExceptionVariable = _currentExceptionVariable;
		_currentExceptionVariable = node.getException().resolveBinding();
		try {
			CheckVariableUseVisitor check = new CheckVariableUseVisitor(_currentExceptionVariable);
			node.getBody().accept(check);

			// The exception variable is declared in a new scope
			pushScope();
			
			CSCatchClause clause;
			if (isEmptyCatch(node, check)) {
				clause = new CSCatchClause();
			} else {
				clause = new CSCatchClause(createVariableDeclaration(_currentExceptionVariable, null));
			}
			clause.anonymous(!check.used());
			visitBlock(clause.body(), node.getBody());
			return clause;
		} finally {
			_currentExceptionVariable = oldExceptionVariable;
			popScope();
		}
	}

	private boolean isEmptyCatch(CatchClause clause, CheckVariableUseVisitor check) {
		if (check.used())
			return false;
		return isThrowable(clause.getException().resolveBinding().getType());
	}

	private boolean isThrowable(ITypeBinding declaringClass) {
		return "java.lang.Throwable".equals(qualifiedName(declaringClass));
	}

	public boolean visit(ThrowStatement node) {
		addStatement(mapThrowStatement(node));
		return false;
	}

	private CSThrowStatement mapThrowStatement(ThrowStatement node) {
		Expression exception = node.getExpression();
		if (isCurrentExceptionVariable(exception)) {
			return new CSThrowStatement(node.getStartPosition(), null);
		}
		return new CSThrowStatement(node.getStartPosition(), mapExpression(exception));
	}

	private boolean isCurrentExceptionVariable(Expression exception) {
		if (!(exception instanceof SimpleName)) {
			return false;
		}
		return ((SimpleName) exception).resolveBinding() == _currentExceptionVariable;
	}

	public boolean visit(BreakStatement node) {
		SimpleName labelName = node.getLabel();
		if(labelName != null){
			addStatement(new CSGotoStatement(node.getStartPosition(), breakLabel(labelName.getIdentifier())));
			return false;
		}
		addStatement(new CSBreakStatement(node.getStartPosition()));
		return false;
	}

	public boolean visit(ContinueStatement node) {
		SimpleName labelName = node.getLabel();
		if(labelName != null){
			addStatement(new CSGotoStatement(node.getStartPosition(), continueLabel(labelName.getIdentifier())));
			return false;
		}
		addStatement(new CSContinueStatement(node.getStartPosition()));
		return false;
	}

	public boolean visit(SynchronizedStatement node) {
		CSLockStatement stmt = new CSLockStatement(node.getStartPosition(), mapExpression(node.getExpression()));
		visitBlock(stmt.body(), node.getBody());
		addStatement(stmt);
		return false;
	}

	public boolean visit(ReturnStatement node) {
		addStatement(new CSReturnStatement(node.getStartPosition(), mapExpression(node.getExpression())));
		return false;
	}

	public boolean visit(NumberLiteral node) {

		String token = node.getToken();
		CSExpression literal = new CSNumberLiteralExpression(token);

		if (expectingType ("byte") && token.startsWith("-")) {
			literal = uncheckedCast ("byte",literal);
		}
		else if (token.startsWith("0x")) {
			if (token.endsWith("l") || token.endsWith("L")) {
				literal = uncheckedCast("long", literal);
			} else {
				literal = uncheckedCast("int", literal);
			}

		} else if (token.startsWith("0") && token.indexOf('.') == -1 && Character.isDigit(token.charAt(token.length() - 1))) {
			try {
				int n = Integer.parseInt(token, 8);
				if (n != 0)
					literal = new CSNumberLiteralExpression("0x" + Integer.toHexString(n));
			} catch (NumberFormatException ex){
			}
 		}

		pushExpression(literal);
		return false;
	}

	private CSUncheckedExpression uncheckedCast(String type, CSExpression expression) {
		return new CSUncheckedExpression(new CSCastExpression(new CSTypeReference(type), new CSParenthesizedExpression(
		        expression)));
	}

	public boolean visit(StringLiteral node) {
		String value = node.getLiteralValue();
		if (value != null && value.length() == 0) {
			pushExpression(new CSReferenceExpression("string.Empty"));
		} else {
			pushExpression(new CSStringLiteralExpression(fixEscapedNumbers (node.getEscapedValue())));
		}
		return false;
	}
	
	String fixEscapedNumbers (String literal) {
		StringBuffer s = new StringBuffer ();
		for (int n=0; n<literal.length(); n++) {
			if (literal.charAt(n) == '\\') {
				int i = n + 1;
				if (i < literal.length() && literal.charAt(i) == '\\') {
					s.append("\\\\");
					n = i;
					continue;
				}
				while (i < literal.length() && Character.isDigit(literal.charAt(i)))
					i++;
				if (i != n + 1) {
					int num = Integer.parseInt(literal.substring(n + 1, i));
					s.append("\\x" + Integer.toHexString(num));
					n = i - 1;
					continue;
				}
			}
			s.append(literal.charAt(n));
		}
		return s.toString();
	}

	public boolean visit(CharacterLiteral node) {
		CSExpression expr = new CSCharLiteralExpression(node.getEscapedValue());
		if (expectingType("byte")) {
			expr = new CSCastExpression(new CSTypeReference("byte"), new CSParenthesizedExpression(
			        expr));
		}
		pushExpression(expr);
		return false;
	}
	
	private boolean expectingType (String name) {
		return (_currentExpectedType != null && _currentExpectedType.getName().equals(name));
	}

	public boolean visit(NullLiteral node) {
		pushExpression(new CSNullLiteralExpression());
		return false;
	}

	public boolean visit(BooleanLiteral node) {
		pushExpression(new CSBoolLiteralExpression(node.booleanValue()));
		return false;
	}

	public boolean visit(ThisExpression node) {
		pushExpression(new CSThisExpression());
		return false;
	}

	public boolean visit(ArrayAccess node) {
		pushExpression(new CSIndexedExpression(mapExpression(node.getArray()), mapExpression(node.getIndex())));
		return false;
	}

	public boolean visit(ArrayCreation node) {
		ITypeBinding saved = pushExpectedType (node.getType().getElementType().resolveBinding());
		if (node.dimensions().size() > 1) {
			if (null != node.getInitializer()) {
				notImplemented(node);
			}
			pushExpression(unfoldMultiArrayCreation(node));
		} else {
			pushExpression(mapSingleArrayCreation(node));
		}
		popExpectedType(saved);
		return false;
	}

	/**
	 * Unfolds java multi array creation shortcut "new String[2][3][2]" into
	 * explicitly array creation "new string[][][] { new string[][] { new
	 * string[2], new string[2], new string[2] }, new string[][] { new
	 * string[2], new string[2], new string[2] } }"
	 */
	private CSArrayCreationExpression unfoldMultiArrayCreation(ArrayCreation node) {
		return unfoldMultiArray((ArrayType) node.getType().getComponentType(), node.dimensions(), 0);
	}

	private CSArrayCreationExpression unfoldMultiArray(ArrayType type, List dimensions, int dimensionIndex) {
		final CSArrayCreationExpression expression = new CSArrayCreationExpression(mappedTypeReference(type));
		expression.initializer(new CSArrayInitializerExpression());
		int length = resolveIntValue(dimensions.get(dimensionIndex));
		if (dimensionIndex < lastIndex(dimensions) - 1) {
			for (int i = 0; i < length; ++i) {
				expression.initializer().addExpression(
				        unfoldMultiArray((ArrayType) type.getComponentType(), dimensions, dimensionIndex + 1));
			}
		} else {
			Expression innerLength = (Expression) dimensions.get(dimensionIndex + 1);
			CSTypeReferenceExpression innerType = mappedTypeReference(type.getComponentType());
			for (int i = 0; i < length; ++i) {
				expression.initializer().addExpression(
				        new CSArrayCreationExpression(innerType, mapExpression(innerLength)));
			}
		}
		return expression;
	}

	private int lastIndex(List<?> dimensions) {
		return dimensions.size() - 1;
	}

	private int resolveIntValue(Object expression) {
		return ((Number) ((Expression) expression).resolveConstantExpressionValue()).intValue();
	}

	private CSArrayCreationExpression mapSingleArrayCreation(ArrayCreation node) {
		CSArrayCreationExpression expression = new CSArrayCreationExpression(mappedTypeReference(componentType(node
		        .getType())));
		if (!node.dimensions().isEmpty()) {
			expression.length(mapExpression((Expression) node.dimensions().get(0)));
		}
		expression.initializer(mapArrayInitializer(node));
		return expression;
	}

	private CSArrayInitializerExpression mapArrayInitializer(ArrayCreation node) {
		return (CSArrayInitializerExpression) mapExpression(node.getInitializer());
	}

	public boolean visit(ArrayInitializer node) {
		if (isImplicitelyTypedArrayInitializer(node)) {
			CSArrayCreationExpression ace = new CSArrayCreationExpression(mappedTypeReference(node.resolveTypeBinding()
			        .getComponentType()));
			ITypeBinding saved = pushExpectedType(node.resolveTypeBinding().getElementType());
			ace.initializer(mapArrayInitializer(node));
			popExpectedType(saved);
			pushExpression(ace);
			return false;
		}

		pushExpression(mapArrayInitializer(node));
		return false;
	}

	private CSArrayInitializerExpression mapArrayInitializer(ArrayInitializer node) {
		CSArrayInitializerExpression initializer = new CSArrayInitializerExpression();
		for (Object e : node.expressions()) {
			initializer.addExpression(mapExpression((Expression) e));
		}
		return initializer;
	}

	private boolean isImplicitelyTypedArrayInitializer(ArrayInitializer node) {
		return !(node.getParent() instanceof ArrayCreation);
	}

	public ITypeBinding componentType(ArrayType type) {
		return type.getComponentType().resolveBinding();
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		CSForEachStatement stmt = new CSForEachStatement(node.getStartPosition(), mapExpression(node.getExpression()));
		stmt.variable(createParameter(node.getParameter()));
		visitBlock(stmt.body(), node.getBody());
		addStatement(stmt);
		return false;
	}

	public boolean visit(final ForStatement node) {
		consumeContinueLabel(new Function<CSBlock>() {
			public CSBlock apply() {
				ArrayList<CSExpression> initializers = new ArrayList<CSExpression> ();
				for (Object i : node.initializers()) {
					initializers.add(mapExpression((Expression) i));
				}
				CSForStatement stmt = new CSForStatement(node.getStartPosition(), mapExpression(node.getExpression()));
				for (CSExpression i : initializers) {
					stmt.addInitializer(i);
				}
				for (Object u : node.updaters()) {
					stmt.addUpdater(mapExpression((Expression) u));
				}
				visitBlock(stmt.body(), node.getBody());
				addStatement(stmt);
				return stmt.body();
			}
		});
		return false;
	}

	private void consumeContinueLabel(Function<CSBlock> func) {
		CSLabelStatement label = _currentContinueLabel;
		_currentContinueLabel = null;
		CSBlock body = func.apply();
		if(label != null){
			body.addStatement(label);
		}
	}

	private boolean isSwitchCaseClosingStatement(CSStatement stmt) {
		return stmt instanceof CSThrowStatement
			|| stmt instanceof CSReturnStatement
			|| stmt instanceof CSBreakStatement
			|| stmt instanceof CSGotoStatement
			|| stmt instanceof CSContinueStatement;
	}

	public boolean visit(SwitchStatement node) {
		_currentContinueLabel = null;
		CSBlock saved = _currentBlock;

		ITypeBinding switchType = node.getExpression().resolveTypeBinding();
		CSSwitchStatement mappedNode = new CSSwitchStatement(node.getStartPosition(), mapExpression(node.getExpression()));
		addStatement(mappedNode);

		CSCaseClause defaultClause = null;
		CSCaseClause current = null;
		CSBlock openCaseBlock = null;
		_currentBlock = null;
		for (ASTNode element : Types.<Iterable<ASTNode>>cast(node.statements())) {
			if (ASTNode.SWITCH_CASE == element.getNodeType()) {
				if (null == current) {
					if (_currentBlock != null) {
						List<CSStatement> stats = _currentBlock.statements();
						CSStatement lastStmt = stats.size() > 0 ? stats.get(stats.size()-1) : null;
						if (!isSwitchCaseClosingStatement(lastStmt))
							openCaseBlock = _currentBlock;
					}
					current = new CSCaseClause();
					mappedNode.addCase(current);
					_currentBlock = current.body();
				}
				SwitchCase sc = (SwitchCase) element;
				if (sc.isDefault()) {
					defaultClause = current;
					current.isDefault(true);
					if (openCaseBlock != null)
						openCaseBlock.addStatement(new CSGotoStatement (Integer.MIN_VALUE, "default"));
				} else {
					ITypeBinding stype = pushExpectedType (switchType);
					CSExpression caseExpression = mapExpression(sc.getExpression());
					current.addExpression(caseExpression);
					popExpectedType(stype);
					if (openCaseBlock != null)
						openCaseBlock.addStatement(new CSGotoStatement (Integer.MIN_VALUE, caseExpression));
				}
				openCaseBlock = null;
			} else {
				element.accept(this);
				current = null;
			}
		}
		
		if (_currentBlock != null) {
			List<CSStatement> stats = _currentBlock.statements();
			CSStatement lastStmt = stats.size() > 0 ? stats.get(stats.size()-1) : null;
			if (lastStmt == null || !isSwitchCaseClosingStatement(lastStmt)) 
				openCaseBlock = _currentBlock;
		}

		if (openCaseBlock != null)
			openCaseBlock.addStatement(new CSBreakStatement (Integer.MIN_VALUE));

		if (null != defaultClause) {
			List<CSStatement> stats = defaultClause.body().statements();
			
			CSStatement lastStmt = stats.size() > 0 ? stats.get(stats.size()-1) : null;
			if (!isSwitchCaseClosingStatement(lastStmt)) {
				defaultClause.body().addStatement(new CSBreakStatement(Integer.MIN_VALUE));
			}
		}

		_currentBlock = saved;
		return false;
	}

	public boolean visit(CastExpression node) {
		pushExpression(new CSCastExpression(mappedTypeReference(node.getType()), mapExpression(node.getExpression())));
		// Make all byte casts unchecked
		if (node.getType().resolveBinding().getName().equals("byte"))
			pushExpression(new CSUncheckedExpression (popExpression()));
		return false;
	}

	public boolean visit(PrefixExpression node) {
		CSExpression expr;
		expr = new CSPrefixExpression(node.getOperator().toString(), mapExpression(node.getOperand()));
		if (expectingType ("byte") && node.getOperator() == PrefixExpression.Operator.MINUS) {
			expr = uncheckedCast ("byte", expr);
		}
		pushExpression(expr);
		return false;
	}

	public boolean visit(PostfixExpression node) {
		pushExpression(new CSPostfixExpression(node.getOperator().toString(), mapExpression(node.getOperand())));
		return false;
	}

	public boolean visit(InfixExpression node) {

		CSExpression left = mapExpression(node.getLeftOperand());
		CSExpression right = mapExpression(node.getRightOperand());
		String type = node.getLeftOperand().resolveTypeBinding().getQualifiedName();
		if (node.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
			if (type.equals ("byte")) {
				pushExpression(new CSInfixExpression(">>", left, right));
			} else {
				CSExpression cast = new CSCastExpression (new CSTypeReference ("u" + type), left);
				cast = new CSParenthesizedExpression (cast);
				CSExpression shiftResult = new CSInfixExpression(">>", cast, right);
				shiftResult = new CSParenthesizedExpression (shiftResult);
				pushExpression(new CSCastExpression (new CSTypeReference (type), shiftResult));
			}
			return false;
		}
		if (type.equals("byte") && (node.getOperator() == InfixExpression.Operator.LESS || node.getOperator() == InfixExpression.Operator.LESS_EQUALS)) {
			left = new CSCastExpression (new CSTypeReference ("sbyte"), left);
			left = new CSParenthesizedExpression (left);
		}
		String operator = node.getOperator().toString();
		pushExpression(new CSInfixExpression(operator, left, right));
		pushExtendedOperands(operator, node);
		return false;
	}

	private void pushExtendedOperands(String operator, InfixExpression node) {
		for (Object x : node.extendedOperands()) {
			pushExpression(new CSInfixExpression(operator, popExpression(), mapExpression((Expression) x)));
		}
	}

	public boolean visit(ParenthesizedExpression node) {
		pushExpression(new CSParenthesizedExpression(mapExpression(node.getExpression())));
		return false;
	}

	public boolean visit(ConditionalExpression node) {
		pushExpression(new CSConditionalExpression(mapExpression(node.getExpression()), mapExpression(node
		        .getThenExpression()), mapExpression(node.getElseExpression())));
		return false;
	}

	public boolean visit(InstanceofExpression node) {
		pushExpression(new CSInfixExpression("is", mapExpression(node.getLeftOperand()), mappedTypeReference(node
		        .getRightOperand().resolveBinding())));
		return false;
	}

	@Override
	public boolean visit(AssertStatement node) {
		List<CSExpression> args = new ArrayList<CSExpression>();
		args.add(mapExpression(node.getExpression()));
		if (node.getMessage() != null) {
			args.add(mapExpression(node.getMessage()));
		}

		CSExpression assertMethod = new CSMemberReferenceExpression(new CSTypeReference("System.Diagnostics.Debug"), "Assert");
		addStatement(new CSExpressionStatement(node.getStartPosition(), new CSMethodInvocationExpression(assertMethod, args.toArray(new CSExpression[args.size()]))));
		return false;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		if (SharpenAnnotations.hasIgnoreAnnotation(node)) {
			return false;
		}

		ITypeBinding fieldType = ((EnumDeclaration)node.getParent()).resolveBinding();
		CSTypeReferenceExpression typeName = mappedTypeReference(fieldType);
		CSVisibility visibility = mapVisibility(node);

		ITypeBinding saved = pushExpectedType(fieldType);

		CSMethodInvocationExpression initializer;
		Configuration.MemberMapping mappedConstructor = effectiveMappingFor(node.resolveConstructorBinding());
		if (null == mappedConstructor) {
			initializer = new CSConstructorInvocationExpression(mappedTypeReference(fieldType));
		} else {
			final String mappedName = mappedConstructor.name;
			if (mappedName.length() == 0) {
				throw new UnsupportedOperationException();
			} else if (mappedName.startsWith("System.Convert.To")) {
				throw new UnsupportedOperationException();
			} else {
				initializer = new CSMethodInvocationExpression(new CSReferenceExpression(methodName(mappedName)));
			}
		}

		mapArguments(initializer, node.arguments());

		CSField field = new CSField(fieldName(node), typeName, visibility, initializer);
		field.addModifier(CSFieldModifier.Static);
		field.addModifier(CSFieldModifier.Readonly);
		mapAnnotations(node, field);
		mapDocumentation(node, field);

		popExpectedType(saved);
		adjustVisibility(fieldType, field);
		_currentType.addMember(field);

		return false;
	}

	public boolean visit(Assignment node) {
		Expression lhs = node.getLeftHandSide();
		Expression rhs = node.getRightHandSide();
		ITypeBinding lhsType = lhs.resolveTypeBinding();
		ITypeBinding saved = pushExpectedType (lhsType);
		
		if (node.getOperator() == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
			String type = lhsType.getQualifiedName();
			if (type == "byte") {
				pushExpression(new CSInfixExpression(">>", mapExpression(lhs), mapExpression(lhs
				        .resolveTypeBinding(), rhs)));
			} else {
				CSExpression mappedLhs = mapExpression(lhs);
				CSExpression cast = new CSCastExpression (new CSTypeReference ("u" + type), mappedLhs);
				cast = new CSParenthesizedExpression (cast);
				CSExpression shiftResult = new CSInfixExpression(">>", cast, mapExpression(rhs));
				shiftResult = new CSParenthesizedExpression (shiftResult);
				shiftResult = new CSCastExpression (new CSTypeReference (type), shiftResult);
				pushExpression(new CSInfixExpression("=", mappedLhs, shiftResult));
			}
		} else {
			pushExpression(new CSInfixExpression(node.getOperator().toString(), mapExpression(lhs), mapExpression(lhs
					.resolveTypeBinding(), rhs)));
		}
		popExpectedType(saved);
		return false;
	}

	private CSExpression mapExpression(ITypeBinding expectedType, Expression expression) {
		if (expectedType != null)
			return castIfNeeded(expectedType, expression.resolveTypeBinding(), mapExpression(expression));
		else
			return mapExpression (expression);
	}

	private CSExpression castIfNeeded(ITypeBinding expectedType, ITypeBinding actualType, CSExpression expression) {
		if (!_configuration.mapIteratorToEnumerator() && expectedType.getName().startsWith("Iterable<") && isGenericCollection (actualType)) {
			return new CSMethodInvocationExpression (new CSMemberReferenceExpression (expression, "AsIterable"));
		}
		if (expectedType != actualType && isSubclassOf (expectedType, actualType))
			return new CSCastExpression(mappedTypeReference(expectedType), expression);

		ITypeBinding charType = resolveWellKnownType("char");
		if (expectedType != charType)
			return expression;
		if (actualType == expectedType)
			return expression;
		return new CSCastExpression(mappedTypeReference(expectedType), expression);
	}
	
	private boolean isGenericCollection (ITypeBinding t) {
		return t.getName().startsWith("List<") || t.getName().startsWith("Set<");
	}
	
	private boolean isSubclassOf (ITypeBinding t, ITypeBinding tbase) {
		while (t != null) {
			if (t.isEqualTo(tbase))
				return true;
			t = t.getSuperclass();
		}
		return false;
	}

	public boolean visit(ClassInstanceCreation node) {
		if (null != node.getAnonymousClassDeclaration()) {
			node.getAnonymousClassDeclaration().accept(this);
			return false;
		}

		CSMethodInvocationExpression expression = mapConstructorInvocation(node);
		if (null == expression) {
			return false;
		}

		if (isNonStaticNestedTypeCreation(node)) {
			expression.addArgument(new CSThisExpression());
		}

		mapArguments(expression, node.arguments());
		pushExpression(expression);
		return false;
	}

	private boolean isNonStaticNestedTypeCreation(ClassInstanceCreation node) {
		return isNonStaticNestedType(node.resolveTypeBinding());
	}

	private CSMethodInvocationExpression mapConstructorInvocation(ClassInstanceCreation node) {
		Configuration.MemberMapping mappedConstructor = effectiveMappingFor(node.resolveConstructorBinding());
		if (null == mappedConstructor) {
			return new CSConstructorInvocationExpression(mappedTypeReference(node.resolveTypeBinding()));
		}
		final String mappedName = mappedConstructor.name;
		if (mappedName.length() == 0) {
			pushExpression(mapExpression((Expression)node.arguments().get(0)));
			return null;
		}
		if (mappedName.startsWith("System.Convert.To")) {
			if (optimizeSystemConvert(mappedName, node)) {
				return null;
			}
		}
		return new CSMethodInvocationExpression(new CSReferenceExpression(methodName(mappedName)));
	}

	private boolean optimizeSystemConvert(String mappedConstructor, ClassInstanceCreation node) {
		String typeName = _configuration.getConvertRelatedWellKnownTypeName(mappedConstructor);
		if (null != typeName) {
			assert 1 == node.arguments().size();
			Expression arg = (Expression) node.arguments().get(0);
			if (arg.resolveTypeBinding() == resolveWellKnownType(typeName)) {
				arg.accept(this);
				return true;
			}
		}
		return false;
	}

	public boolean visit(TypeLiteral node) {
		
		if (isReferenceToRemovedType(node.getType())) {
			pushExpression(new CSRemovedExpression(node.toString()));
			return false;
		}
		
		pushTypeOfExpression(mappedTypeReference(node.getType()));
		return false;
	}

	private boolean isReferenceToRemovedType(Type node) {
	    BodyDeclaration typeDeclaration = findDeclaringNode(node.resolveBinding());
	    if (null == typeDeclaration)
	    	return false;
		return hasRemoveAnnotation(typeDeclaration);
    }

	private void pushTypeOfExpression(CSTypeReferenceExpression type) {
		if (_configuration.nativeTypeSystem()) {
			pushExpression(new CSTypeofExpression(type));
		} else {
			pushGetClassForTypeExpression(type);
		}
	}

	private void pushGetClassForTypeExpression(final CSTypeReferenceExpression typeName) {
		CSMethodInvocationExpression mie = new CSMethodInvocationExpression(new CSReferenceExpression(
		        methodName(_configuration.getRuntimeTypeName() + ".getClassForType")));
		mie.addArgument(new CSTypeofExpression(typeName));
		pushExpression(mie);
	}

	public boolean visit(MethodInvocation node) {
		
		IMethodBinding binding = originalMethodBinding(node.resolveMethodBinding());
		Configuration.MemberMapping mapping = mappingForInvocation(node, binding);

		if (null != mapping) {
			processMappedMethodInvocation(node, binding, mapping);
		} else {
			processUnmappedMethodInvocation(node);
		}
		
		return false;
	}
	
	public boolean visit(SuperMethodInvocation node) {
		if (null != node.getQualifier()) {
			notImplemented(node);
		}

		IMethodBinding binding = originalMethodBinding(node.resolveMethodBinding());
		Configuration.MemberMapping mapping = mappingForInvocation(node, binding);
		CSExpression target = new CSMemberReferenceExpression(new CSBaseExpression(), mappedMethodName(binding));

		if (mapping != null && mapping.kind != MemberKind.Method) {
			pushExpression(target);
			return false;
		}

		CSMethodInvocationExpression mie = new CSMethodInvocationExpression(target);
		mapArguments(mie, node.arguments());
		pushExpression(mie);
		return false;
	}
	
	private Configuration.MemberMapping mappingForInvocation(ASTNode node, IMethodBinding binding) {
		Configuration.MemberMapping mapping = effectiveMappingFor(binding);

		if (null == mapping) {
			if (isIndexer(binding)) {
				mapping = new MemberMapping(null, MemberKind.Indexer);
			} else if (isTaggedMethodInvocation(binding, SharpenAnnotations.SHARPEN_EVENT)) {
				mapping = new MemberMapping(binding.getName(), MemberKind.Property);
			} else if (isTaggedMethodInvocation(binding, SharpenAnnotations.SHARPEN_PROPERTY)) {
				mapping = new MemberMapping(propertyName(binding), MemberKind.Property);
			}
		}
		return mapping;
	}

	private boolean isIndexer(final IMethodBinding binding) {
		return isTaggedMethod(binding, SharpenAnnotations.SHARPEN_INDEXER);
	}

	private boolean isTaggedMethod(final IMethodBinding binding, final String tag) {
	    final BodyDeclaration declaration = declaringNode(binding);
		if (null == declaration) {
			return false;
		}
		return isTaggedDeclaration(declaration, tag);
    }

	private IMethodBinding originalMethodBinding(IMethodBinding binding) {
		IMethodBinding original = BindingUtils.findMethodDefininition(binding, my(CompilationUnit.class).getAST());
		if (null != original)
			return original;
		return binding;
	}

	private void processUnmappedMethodInvocation(MethodInvocation node) {

		if (isMappedEventSubscription(node)) {
			processMappedEventSubscription(node);
			return;
		}

		if (isEventSubscription(node)) {
			processEventSubscription(node);
			return;
		}

		if (isRemovedMethodInvocation(node)) {
			processRemovedInvocation(node);
			return;
		}
		
		if (isUnwrapInvocation(node)) {
			processUnwrapInvocation(node);
			return;
		}
		
		if (isMacro(node)) {
			processMacroInvocation(node);
			return;
		}
		
		if (isEnumOrdinalMethodInvocation (node)) {
			processEnumOrdinalMethodInvocation (node);
			return;
		}
		
		if (isEnumNameMethodInvocation (node)) {
			processEnumNameMethodInvocation (node);
			return;
		}

		processOrdinaryMethodInvocation(node);
	}

	private boolean isMacro(MethodInvocation node) {
	    return isTaggedMethodInvocation(node, SharpenAnnotations.SHARPEN_MACRO);
    }

	private void processMacroInvocation(MethodInvocation node) {
		final MethodDeclaration declaration = (MethodDeclaration)declaringNode(node.resolveMethodBinding());
		final TagElement macro = effectiveAnnotationFor(declaration, SharpenAnnotations.SHARPEN_MACRO);
		final CSMacro code = new CSMacro(JavadocUtility.singleTextFragmentFrom(macro));
		
		code.addVariable("expression", mapExpression(node.getExpression()));
		code.addVariable("arguments", mapExpressions(node.arguments()));
		
		pushExpression(new CSMacroExpression(code));
    }

	private List<CSExpression> mapExpressions(List expressions) {
		final ArrayList<CSExpression> result = new ArrayList<CSExpression>(expressions.size());
		for (Object expression : expressions) {
			result.add(mapExpression((Expression) expression));
		}
		return result;
    }

	private boolean isUnwrapInvocation(MethodInvocation node) {
	    return isTaggedMethodInvocation(node, SharpenAnnotations.SHARPEN_UNWRAP);
    }

	private void processUnwrapInvocation(MethodInvocation node) {
	    final List arguments = node.arguments();
	    if (arguments.size() != 1) {
	    	unsupportedConstruct(node, SharpenAnnotations.SHARPEN_UNWRAP + " only works against single argument methods.");
	    }
	    pushExpression(mapExpression((Expression) arguments.get(0)));
    }

	private void processOrdinaryMethodInvocation(MethodInvocation node) {
		IMethodBinding method = node.resolveMethodBinding();
		CSExpression targetExpression = mapMethodTargetExpression(node);
		if ((method.getModifiers() & Modifier.STATIC) != 0 && !(targetExpression instanceof CSTypeReferenceExpression) && node.getExpression() != null)
			targetExpression = mappedTypeReference(node.getExpression().resolveTypeBinding());
		
		String name = resolveTargetMethodName(targetExpression, node);
		CSExpression target = null == targetExpression
				? new CSReferenceExpression(name)
				: new CSMemberReferenceExpression(targetExpression, name);
		CSMethodInvocationExpression mie = new CSMethodInvocationExpression(target);
		mapMethodInvocationArguments(mie, node);
		mapTypeArguments(mie, node);
		
		IMethodBinding base = getOverridedMethod(method);
		if (base != null && base.getReturnType() != method.getReturnType() && !(node.getParent() instanceof ExpressionStatement))
			pushExpression (new CSParenthesizedExpression (new CSCastExpression (mappedTypeReference(method.getReturnType()), mie)));
		else
			pushExpression(mie);
    }
	
	private String resolveTargetMethodName(CSExpression targetExpression, MethodInvocation node) {
		final IMethodBinding method = staticImportMethodBinding(node.getName(), _ast.imports());
		if(method != null && targetExpression == null){
			return mappedTypeName(method.getDeclaringClass()) + "." + mappedMethodName(node.resolveMethodBinding());
		}
		return mappedMethodName(node.resolveMethodBinding());
	}

	private void mapTypeArguments(CSMethodInvocationExpression mie, MethodInvocation node) {
	    for (Object o : node.typeArguments()) {
			mie.addTypeArgument(mappedTypeReference((Type)o));
		}
    }

	private void processMappedEventSubscription(MethodInvocation node) {

		final MethodInvocation event = (MethodInvocation) node.getExpression();
		final String eventArgsType = _configuration.mappedEvent(qualifiedName(event));
		final String eventHandlerType = buildEventHandlerTypeName(node, eventArgsType);
		mapEventSubscription(node, eventArgsType, eventHandlerType);
	}

	private void processRemovedInvocation(MethodInvocation node) {
		TagElement element = javadocTagFor(declaringNode(node.resolveMethodBinding()), SharpenAnnotations.SHARPEN_REMOVE);

		String exchangeValue = JavadocUtility.singleTextFragmentFrom(element);
		pushExpression(new CSReferenceExpression(exchangeValue));
	}

	private void processEnumOrdinalMethodInvocation (MethodInvocation node)
	{
		CSExpression exp = mapExpression(node.getExpression());
		pushExpression(new CSCastExpression (new CSTypeReference ("int"), new CSParenthesizedExpression (exp)));
	}
	
	private void processEnumNameMethodInvocation (MethodInvocation node)
	{
		CSExpression exp = mapExpression(node.getExpression());
		pushExpression(new CSMethodInvocationExpression(new CSMemberReferenceExpression (exp, "ToString")));
	}
	
	private void mapMethodInvocationArguments(CSMethodInvocationExpression mie, MethodInvocation node) {
		final List arguments = node.arguments();
		final IMethodBinding actualMethod = node.resolveMethodBinding();
		final ITypeBinding[] actualTypes = actualMethod.getParameterTypes();
		final IMethodBinding originalMethod = actualMethod.getMethodDeclaration();
		final ITypeBinding[] originalTypes = originalMethod.getParameterTypes();
		for (int i = 0; i < arguments.size(); ++i) {
			final Expression arg = (Expression) arguments.get(i);
			if (i < originalTypes.length && isGenericRuntimeParameterIdiom(originalMethod, originalTypes[i])
			        && isClassLiteral(arg)) {
				mie.addTypeArgument(genericRuntimeTypeIdiomType(actualTypes[i]));
			} else {
				addArgument(mie, arg, i < actualTypes.length ? actualTypes[i] : null);
			}
		}
		adjustJUnitArguments (mie, node);
	}
	
	private void adjustJUnitArguments (CSMethodInvocationExpression mie, MethodInvocation node) {
		if (!_configuration.junitConversion())
			return;
		ITypeBinding t = node.resolveMethodBinding().getDeclaringClass();
		if (t.getQualifiedName().equals("junit.framework.Assert") || t.getQualifiedName().equals("org.junit.Assert")) {
			String method = node.getName().getIdentifier();
			int np = -1;
			
			if (method.equals("assertTrue") || method.equals("assertFalse")
				|| method.equals("assertNull") || method.equals("assertNotNull"))
				np = 1;
			else if (method.equals("fail"))
				np = 0;
			else if (method.startsWith("assert"))
				np = 2;
			
			if (np == -1)
				return;
			
			if (mie.arguments().size() == np + 1) {
				// Move the comment argument to the end
				mie.addArgument(mie.arguments().get(0));
				mie.removeArgument(0);
			}
			
			if (method.equals("assertSame")) {
				boolean useEquals = false;
				final List arguments = node.arguments();
				for (int i = 0; i < arguments.size(); ++i) {
					final Expression arg = (Expression) arguments.get(i);
					ITypeBinding b = arg.resolveTypeBinding();
					if (b.isEnum()) {
						useEquals = true;
						break;
					}
				}
				if (useEquals) {
					CSReferenceExpression mref = (CSReferenceExpression) mie.expression();
					mref.name("NUnit.Framework.Assert.AreEqual");
				}
			}
		}
	}
	
	private boolean isClassLiteral(Expression arg) {
		return arg.getNodeType() == ASTNode.TYPE_LITERAL;
	}

	private void processEventSubscription(MethodInvocation node) {

		final MethodDeclaration addListener = (MethodDeclaration)declaringNode(node.resolveMethodBinding());
		assertValidEventAddListener(node, addListener);

		final MethodInvocation eventInvocation = (MethodInvocation) node.getExpression();

		final MethodDeclaration eventDeclaration = (MethodDeclaration)declaringNode(eventInvocation.resolveMethodBinding());
		mapEventSubscription(node, getEventArgsType(eventDeclaration), getEventHandlerTypeName(eventDeclaration));
	}

	private void mapEventSubscription(MethodInvocation node, final String eventArgsType, final String eventHandlerType) {
		final CSAnonymousClassBuilder listenerBuilder = mapAnonymousEventListener(node);
		final CSMemberReferenceExpression handlerMethodRef = new CSMemberReferenceExpression(listenerBuilder
		        .createConstructorInvocation(), eventListenerMethodName(listenerBuilder));

		final CSReferenceExpression delegateType = new CSReferenceExpression(eventHandlerType);

		patchEventListener(listenerBuilder, eventArgsType);

		CSConstructorInvocationExpression delegateConstruction = new CSConstructorInvocationExpression(delegateType);
		delegateConstruction.addArgument(handlerMethodRef);

		pushExpression(new CSInfixExpression("+=", mapMethodTargetExpression(node), delegateConstruction));
	}

	private CSAnonymousClassBuilder mapAnonymousEventListener(MethodInvocation node) {
		ClassInstanceCreation creation = (ClassInstanceCreation) node.arguments().get(0);
		return mapAnonymousClass(creation.getAnonymousClassDeclaration());
	}

	private String eventListenerMethodName(final CSAnonymousClassBuilder listenerBuilder) {
		return mappedMethodName(getFirstMethod(listenerBuilder.anonymousBaseType()));
	}

	private void patchEventListener(CSAnonymousClassBuilder listenerBuilder, String eventArgsType) {
		final CSClass type = listenerBuilder.type();
		type.clearBaseTypes();

		final CSMethod handlerMethod = (CSMethod) type.getMember(eventListenerMethodName(listenerBuilder));
		handlerMethod.parameters().get(0).type(OBJECT_TYPE_REFERENCE);
		handlerMethod.parameters().get(0).name("sender");
		handlerMethod.parameters().get(1).type(new CSTypeReference(eventArgsType));

	}

	private IMethodBinding getFirstMethod(ITypeBinding listenerType) {
		return listenerType.getDeclaredMethods()[0];
	}

	private void assertValidEventAddListener(ASTNode source, MethodDeclaration addListener) {
		if (isValidEventAddListener(addListener))
			return;

		unsupportedConstruct(source, SharpenAnnotations.SHARPEN_EVENT_ADD + " must take lone single method interface argument");
	}

	private boolean isValidEventAddListener(MethodDeclaration addListener) {
		if (1 != addListener.parameters().size())
			return false;

		final ITypeBinding type = getFirstParameterType(addListener);
		if (!type.isInterface())
			return false;

		return type.getDeclaredMethods().length == 1;
	}

	private ITypeBinding getFirstParameterType(MethodDeclaration addListener) {
		return parameter(addListener, 0).getType().resolveBinding();
	}

	private SingleVariableDeclaration parameter(MethodDeclaration method, final int index) {
		return (SingleVariableDeclaration) method.parameters().get(index);
	}

	private boolean isEventSubscription(MethodInvocation node) {
		return isTaggedMethodInvocation(node, SharpenAnnotations.SHARPEN_EVENT_ADD);
	}

	private boolean isMappedEventSubscription(MethodInvocation node) {
		return _configuration.isMappedEventAdd(qualifiedName(node));
	}

	private String qualifiedName(MethodInvocation node) {
		return qualifiedName(node.resolveMethodBinding());
	}

	private boolean isTaggedMethodInvocation(MethodInvocation node, final String tag) {
		return isTaggedMethodInvocation(node.resolveMethodBinding(), tag);
	}

	private boolean isTaggedMethodInvocation(final IMethodBinding binding, final String tag) {
		final BodyDeclaration method = declaringNode(originalMethodBinding(binding));
		if (null == method) {
			return false;
		}
		return containsJavadoc(method, tag);
	}

	@SuppressWarnings("unchecked")
	private void processMappedMethodInvocation(MethodInvocation node, IMethodBinding binding,
	        Configuration.MemberMapping mapping) {

		if (mapping.kind == MemberKind.Indexer) {
			processIndexerInvocation(node, binding, mapping);
			return;
		}

		String name = mappedMethodName(binding);
		if (0 == name.length()) {
			final Expression expression = node.getExpression();
			final CSExpression target = expression != null ? mapExpression(expression) : new CSThisExpression(); // see
																													// collections/EntrySet1
			pushExpression(target);
			return;
		}

		boolean isMappingToStaticMethod = isMappingToStaticMember(name);

		List<Expression> arguments = node.arguments();
		CSExpression expression = mapMethodTargetExpression(node);
		CSExpression target = null;

		if (null == expression || isMappingToStaticMethod) {
			target = new CSReferenceExpression(name);
		} else {
			if (BindingUtils.isStatic(binding) && arguments.size() > 0) {
				// mapping static method to instance member
				// typical example is String.valueOf(arg) => arg.ToString()
				target = new CSMemberReferenceExpression(parensIfNeeded(mapExpression(arguments.get(0))), name);
				arguments = arguments.subList(1, arguments.size());
			} else {
				target = new CSMemberReferenceExpression(expression, name);
			}
		}

		if (mapping.kind != MemberKind.Method) {
			IMethodBinding originalBinding = node.resolveMethodBinding();
			if (binding != originalBinding && originalBinding.getReturnType() != binding.getReturnType() && !(node.getParent() instanceof ExpressionStatement))
				target = new CSParenthesizedExpression (new CSCastExpression (mappedTypeReference(originalBinding.getReturnType()), target));
			switch (arguments.size()) {
			case 0:
				pushExpression(target);
				break;

			case 1:
				pushExpression(new CSInfixExpression("=", target, mapExpression(arguments.get(0))));
				break;

			default:
				unsupportedConstruct(node, "Method invocation with more than 1 argument mapped to property");
				break;
			}
			return;
		}

		CSMethodInvocationExpression mie = new CSMethodInvocationExpression(target);
		if (isMappingToStaticMethod && isInstanceMethod(binding)) {
			if (null == expression) {
				mie.addArgument(new CSThisExpression());
			} else {
				mie.addArgument(expression);
			}
		}
		mapArguments(mie, arguments);
		adjustJUnitArguments(mie, node);
		pushExpression(mie);
	}

	private void processIndexerInvocation(MethodInvocation node, IMethodBinding binding, MemberMapping mapping) {
		if (node.arguments().size() == 1) {
			processIndexerGetter(node);
		} else {
			processIndexerSetter(node);
		}
	}

	private void processIndexerSetter(MethodInvocation node) {
		// target(arg0 ... argN) => target[arg0... argN-1] = argN;
		
		final CSIndexedExpression indexer = new CSIndexedExpression(mapIndexerTarget(node));
		final List arguments = node.arguments();
		final Expression lastArgument = (Expression)arguments.get(arguments.size() - 1);
		for (int i=0; i<arguments.size()-1; ++i) {
			indexer.addIndex(mapExpression((Expression) arguments.get(i)));
		}
		pushExpression(CSharpCode.newAssignment(indexer, mapExpression(lastArgument)));
		
    }

	private void processIndexerGetter(MethodInvocation node) {
	    final Expression singleArgument = (Expression) node.arguments().get(0);
	    pushExpression(
	    		new CSIndexedExpression(
	    				mapIndexerTarget(node),
	    				mapExpression(singleArgument)));
    }

	private CSExpression mapIndexerTarget(MethodInvocation node) {
		if (node.getExpression() == null) {
			return new CSThisExpression();
		}
		return mapMethodTargetExpression(node);
	}

	private CSExpression parensIfNeeded(CSExpression expression) {
		if (expression instanceof CSInfixExpression || expression instanceof CSPrefixExpression
		        || expression instanceof CSPostfixExpression) {

			return new CSParenthesizedExpression(expression);
		}
		return expression;
	}

	protected CSExpression mapMethodTargetExpression(MethodInvocation node) {
		return mapExpression(node.getExpression());
	}

	private boolean isInstanceMethod(IMethodBinding binding) {
		return !BindingUtils.isStatic(binding);
	}

	private boolean isMappingToStaticMember(String name) {
		return -1 != name.indexOf('.');
	}

	protected void mapArguments(CSMethodInvocationExpression mie, List arguments) {
		for (Object arg : arguments) {
			addArgument(mie, (Expression) arg, null);
		}
	}

	private void addArgument(CSMethodInvocationExpression mie, Expression arg, ITypeBinding expectedType) {
		mie.addArgument(mapExpression(expectedType, arg));
	}

	public boolean visit(FieldAccess node) {
		String name = mappedFieldName(node);
		if (null == node.getExpression()) {
			pushExpression(new CSReferenceExpression(name));
		} else {
			pushExpression(new CSMemberReferenceExpression(mapExpression(node.getExpression()), name));
		}
		return false;
	}

	String mapVariableName (String name) {
		if (_renamedVariables.size() > 0) {
			String vname = name;
			if (vname.startsWith("@"))
				vname = vname.substring(1);
			String newName = _renamedVariables.peek().get(vname);
			if (newName != null)
				return newName;
		}
		return name;
	}
	private boolean isBoolLiteral(String name) {
		return name.equals("true") || name.equals("false");
	}

	private String mappedFieldName(FieldAccess node) {
		String name = mappedFieldName(node.getName());
		if (null != name)
			return name;
		return identifier(node.getName());
	}

	public boolean visit(SimpleName node) {
		if (isTypeReference(node)) {
			pushTypeReference(node.resolveTypeBinding());
		} else if (_currentExpression == null){
			String ident = mapVariableName (identifier (node));
			IBinding b = node.resolveBinding();
			IVariableBinding vb = b instanceof IVariableBinding ? (IVariableBinding) b : null;
			if (vb != null) {
				ITypeBinding cls = vb.getDeclaringClass();
				if (cls != null) {
					if (isStaticImport(vb, _ast.imports())) {
						if (cls != null) {
							pushExpression(new CSMemberReferenceExpression(mappedTypeReference(cls), ident));
							return false;
						}
					}
					else if (cls.isEnum() && ident.indexOf('.') == -1){
						pushExpression(new CSMemberReferenceExpression(mappedTypeReference(cls), ident));
						return false;
					}
					else if (_configuration.separateInterfaceConstants() && cls.isInterface() && ident.indexOf('.') == -1) {
						pushExpression(new CSMemberReferenceExpression(mappedAuxillaryTypeReference(cls), ident));
						return false;
					}
				}
			}
			pushExpression(new CSReferenceExpression(ident));
		}
		return false;
	}

	private void addStatement(CSStatement statement) {
		_currentBlock.addStatement(statement);
	}

	private void pushTypeReference(ITypeBinding typeBinding) {
		pushExpression(mappedTypeReference(typeBinding));
	}

	protected CSReferenceExpression createTypeReference(ITypeBinding typeBinding) {
		return new CSReferenceExpression(mappedTypeName(typeBinding));
	}

	private boolean isTypeReference(Name node) {
		final IBinding binding = node.resolveBinding();
		if (null == binding) {
			unresolvedTypeBinding(node);
			return false;
		}
		return IBinding.TYPE == binding.getKind();
	}

	public boolean visit(QualifiedName node) {
		if (isTypeReference(node)) {
			pushTypeReference(node.resolveTypeBinding());
		} else {
			String primitiveTypeRef = checkForPrimitiveTypeReference(node);
			if (primitiveTypeRef != null) {
				pushTypeOfExpression(new CSTypeReference(primitiveTypeRef));
			} else {
				handleRegularQualifiedName(node);
			}
		}
		return false;
	}

	private void handleRegularQualifiedName(QualifiedName node) {
		String mapped = mappedFieldName(node);
		if (null != mapped) {
			if (isBoolLiteral(mapped)) {
				pushExpression(new CSBoolLiteralExpression(Boolean.parseBoolean(mapped)));
				return;
			}
			if (isMappingToStaticMember(mapped)) {
				pushExpression(new CSReferenceExpression(mapped));
			} else {
				pushMemberReferenceExpression(node.getQualifier(), mapped);
			}
		} else {
			Name qualifier = node.getQualifier();
			String name = identifier(node.getName().getIdentifier());
			pushMemberReferenceExpression(qualifier, name);
		}
	}

	private String checkForPrimitiveTypeReference(QualifiedName node) {
		String name = qualifiedName(node);
		if (name.equals(JAVA_LANG_VOID_TYPE))
			return "void";
		if (name.equals(JAVA_LANG_BOOLEAN_TYPE))
			return "bool";
		if (name.equals(JAVA_LANG_BYTE_TYPE)) {
			return _configuration.mapByteToSbyte() ?
					"sbyte" :
					"byte";
		}
		if (name.equals(JAVA_LANG_CHARACTER_TYPE))
			return "char";
		if (name.equals(JAVA_LANG_SHORT_TYPE))
			return "short";
		if (name.equals(JAVA_LANG_INTEGER_TYPE))
			return "int";
		if (name.equals(JAVA_LANG_LONG_TYPE))
			return "long";
		if (name.equals(JAVA_LANG_FLOAT_TYPE))
			return "float";
		if (name.equals(JAVA_LANG_DOUBLE_TYPE))
			return "double";
		return null;
	}

	private String qualifiedName(QualifiedName node) {
		IVariableBinding binding = variableBinding(node);
		if (binding == null)
			return node.toString();
		return BindingUtils.qualifiedName(binding);
	}

	private void pushMemberReferenceExpression(Name qualifier, String name) {
		if (_configuration.separateInterfaceConstants()) {
			IBinding binding = qualifier.resolveBinding();
			if (binding instanceof ITypeBinding) {
				ITypeBinding typeBinding = (ITypeBinding)binding;
				if (typeBinding.isInterface()) {
					pushExpression(new CSMemberReferenceExpression(mappedAuxillaryTypeReference(typeBinding), name));
					return;
				}
			}
		}

		pushExpression(new CSMemberReferenceExpression(mapExpression(qualifier), name));
	}

	private IVariableBinding variableBinding(Name node) {
		if (node.resolveBinding() instanceof IVariableBinding) {
			return (IVariableBinding) node.resolveBinding();
		}
		return null;
	}

	private String mappedFieldName(SuperFieldAccess node) {
		if (node.getQualifier() != null) {
			notImplemented(node);
		}

		String name = mappedFieldName(node.getName());
		if (null != name)
			return name;

		return identifier(node.getName());
	}

	private String mappedFieldName(Name node) {
		IVariableBinding binding = variableBinding(node);
		return null == binding ? null : my(Mappings.class).mappedFieldName(binding);
	}

	protected CSExpression mapExpression(Expression expression) {
		if (null == expression)
			return null;

		try {
			expression.accept(this);
			return popExpression();
		} catch (Exception e) {
			unsupportedConstruct(expression, e);
			return null; // we'll never get here
		}
	}

	private void unsupportedConstruct(ASTNode node, Exception cause) {
		unsupportedConstruct(node, "failed to map: '" + node + "'", cause);
	}

	private void unsupportedConstruct(ASTNode node, String message) {
		unsupportedConstruct(node, message, null);
	}

	private void unsupportedConstruct(ASTNode node, final String message, Exception cause) {
		throw new IllegalArgumentException(sourceInformation(node) + ": " + message, cause);
	}
	
	private ITypeBinding pushExpectedType (ITypeBinding type) {
		ITypeBinding old = _currentExpectedType; 
		_currentExpectedType = type;
		return old;
	}
	
	private void popExpectedType (ITypeBinding saved) {
		_currentExpectedType = saved;
	}

	protected void pushExpression(CSExpression expression) {
		if (null != _currentExpression) {
			throw new IllegalStateException();
		}
		_currentExpression = expression;
	}

	private CSExpression popExpression() {
		if (null == _currentExpression) {
			throw new IllegalStateException();
		}
		CSExpression found = _currentExpression;
		_currentExpression = null;
		return found;
	}

	private CSVariableDeclaration createParameter(SingleVariableDeclaration declaration) {
		return createVariableDeclaration(declaration.resolveBinding(), null);
	}

	protected void visit(List nodes) {
		for (Object node : nodes) {
			((ASTNode) node).accept(this);
		}
	}

	private void createInheritedAbstractMemberStubs(AbstractTypeDeclaration node) {
		if (node instanceof TypeDeclaration && ((TypeDeclaration)node).isInterface())
			return;

		ITypeBinding binding = node.resolveBinding();
		if (!Modifier.isAbstract(node.getModifiers()))
			return;

		Set<ITypeBinding> interfaces = new LinkedHashSet<ITypeBinding>();
		collectInterfaces(interfaces, binding);
		for (ITypeBinding baseType : interfaces) {
			createInheritedAbstractMemberStubs(binding, baseType);
		}
	}

	private void collectInterfaces(Set<ITypeBinding> interfaceList, ITypeBinding binding) {
		ITypeBinding[] interfaces = binding.getInterfaces();
		for (int i = 0; i < interfaces.length; ++i) {
			ITypeBinding interfaceBinding = interfaces[i];
			if (interfaceList.contains(interfaceBinding)) {
				continue;
			}
			collectInterfaces(interfaceList, interfaceBinding);
			interfaceList.add(interfaceBinding);
		}
	}

	private void createInheritedAbstractMemberStubs(ITypeBinding type, ITypeBinding baseType) {
		IMethodBinding[] methods = baseType.getDeclaredMethods();
		for (int i = 0; i < methods.length; ++i) {
			IMethodBinding method = methods[i];
			if (!Modifier.isAbstract(method.getModifiers())) {
				continue;
			}
			if (null != BindingUtils.findOverriddenMethodInTypeOrSuperclasses(type, method)) {
				continue;
			}
			if (isIgnored(originalMethodBinding(method))) {
				continue;
			}
			if (stubIsProperty(method)) {
				_currentType.addMember(createAbstractPropertyStub(method));
			} else {
				CSMethod newMethod = createAbstractMethodStub(method);
				//the same method might be defined in multiple interfaces
				//but only a single stub must be created for those
				if( ! _currentType.members().contains(newMethod)) {
					_currentType.addMember(newMethod);
				}
			}
		}
	}

	private boolean isIgnored(IMethodBinding binding) {
		final BodyDeclaration dec = declaringNode(binding);
		return dec != null && SharpenAnnotations.hasIgnoreAnnotation(dec);
	}

	private boolean stubIsProperty(IMethodBinding method) {
		final BodyDeclaration dec = declaringNode(method);
		return dec != null && isProperty(dec);
	}

	private BodyDeclaration declaringNode(IMethodBinding method) {
		return findDeclaringNode(method);
	}

	private CSProperty createAbstractPropertyStub(IMethodBinding method) {
		CSProperty stub = newAbstractPropertyStubFor(method);		
		safeProcessDisableTags(method, stub);		
		
		return stub;
	}

	private CSProperty newAbstractPropertyStubFor(IMethodBinding method) {
		CSProperty stub = new CSProperty(mappedMethodName(method), mappedTypeReference(method.getReturnType()));
		stub.modifier(CSMethodModifier.Abstract);
		stub.visibility(mapVisibility(method.getModifiers()));
		stub.getter(new CSBlock());
		return stub;
	}

	private CSMethod createAbstractMethodStub(IMethodBinding method) {
		CSMethod stub = newAbstractMethodStubFor(method);
		safeProcessDisableTags(method, stub);
		
		return stub;
	}

	private CSMethod newAbstractMethodStubFor(IMethodBinding method) {
		CSMethod stub = new CSMethod(mappedMethodName(method));
		
		stub.modifier(CSMethodModifier.Abstract);
		stub.visibility(mapVisibility(method.getModifiers()));
		stub.returnType(mappedTypeReference(method.getReturnType()));

		ITypeBinding[] parameters = method.getParameterTypes();
		for (int i = 0; i < parameters.length; ++i) {
			stub.addParameter(new CSVariableDeclaration("arg" + (i + 1), mappedTypeReference(parameters[i])));
		}
		return stub;
	}

	private void safeProcessDisableTags(IMethodBinding method, CSMember member) {
		final BodyDeclaration node = declaringNode(method);
		if (node == null) return;
		
		processDisableTags(node, member);
	}

	CSMethodModifier mapMethodModifier(MethodDeclaration method) {
		if (_currentType.isInterface() || method.resolveBinding().getDeclaringClass().isInterface()) {
			return CSMethodModifier.Abstract;
		}
		int modifiers = method.getModifiers();
		if (Modifier.isStatic(modifiers)) {
			return CSMethodModifier.Static;
		}
		if (Modifier.isPrivate(modifiers)) {
			return CSMethodModifier.None;
		}

		boolean override = isOverride(method);
		if (Modifier.isAbstract(modifiers)) {
			return override ? CSMethodModifier.AbstractOverride : CSMethodModifier.Abstract;
		}
		boolean isFinal = Modifier.isFinal(modifiers);
		if (override) {
			return isFinal ? CSMethodModifier.Sealed : modifierIfNewAnnotationNotApplied(method, CSMethodModifier.Override);
		}
		return isFinal || _currentType.isSealed() ? CSMethodModifier.None : CSMethodModifier.Virtual;
	}

	private CSMethodModifier modifierIfNewAnnotationNotApplied(MethodDeclaration method, CSMethodModifier modifier) {
		return containsJavadoc(method, SharpenAnnotations.SHARPEN_NEW) 
						? CSMethodModifier.None 
						: modifier;
	}
	
	private boolean isExtractedNestedType (ITypeBinding type) {
		return _configuration.typeHasMapping(BindingUtils.typeMappingKey(type));
	}

	private boolean isOverride(MethodDeclaration method) {
		return null != getOverridedMethod (method);
	}
	
	private IMethodBinding getOverridedMethod(MethodDeclaration method) {
		return getOverridedMethod (method.resolveBinding());
	}
	private IMethodBinding getOverridedMethod(IMethodBinding methodBinding) {
		ITypeBinding superclass = _ignoreExtends.value() ? resolveWellKnownType("java.lang.Object") : methodBinding
		        .getDeclaringClass().getSuperclass();
		if (null != superclass) {
			IMethodBinding result = BindingUtils.findOverriddenMethodInHierarchy(superclass, methodBinding); 
			if (null != result)
				return result;
		}

		if (!_configuration.separateInterfaceConstants()) {
			ITypeBinding[] baseInterfaces = methodBinding.getDeclaringClass().getInterfaces();
			if (baseInterfaces.length == 1 && !isValidCSInterface(baseInterfaces[0])) {
				// Base interface generated as a class
				return BindingUtils.findOverriddenMethodInType(baseInterfaces[0], methodBinding);
			}
		}

		return null;
	}
	
	private boolean isValidCSInterface (ITypeBinding type) {
		if (type.getTypeDeclaration().getQualifiedName().equals("java.util.Iterator") || type.getTypeDeclaration().getQualifiedName().equals("java.lang.Iterable"))
			return false;
		if (type.getDeclaredFields().length != 0)
			return false;
		for (ITypeBinding ntype : type.getDeclaredTypes()) {
			if (!isExtractedNestedType(ntype))
				return false;
		}
		return true;
	}

	CSClassModifier mapClassModifier(int modifiers) {
		if (Modifier.isAbstract(modifiers)) {
			return CSClassModifier.Abstract;
		}
		if (Modifier.isFinal(modifiers)) {
			return CSClassModifier.Sealed;
		}
		return CSClassModifier.None;
	}
	
	void adjustVisibility (ITypeBinding memberType, CSMember member) {
		if (memberType == null)
			return;
		CSVisibility typeVisibility = mapVisibility(memberType.getModifiers());
		if (typeVisibility == CSVisibility.Protected && member.visibility() == CSVisibility.Internal)
			member.visibility(CSVisibility.Protected);
	}

	CSVisibility mapVisibility(BodyDeclaration node) {
		if (containsJavadoc(node, SharpenAnnotations.SHARPEN_INTERNAL)) {
			return CSVisibility.Internal;
		}
		
		if (containsJavadoc(node, SharpenAnnotations.SHARPEN_PRIVATE)) {
			return CSVisibility.Private;
		}
		
		if (containsJavadoc(node, SharpenAnnotations.SHARPEN_PROTECTED)) {
			return CSVisibility.Protected;
		}	
		
		if (containsJavadoc(node, SharpenAnnotations.SHARPEN_PUBLIC)) {
			return CSVisibility.Public;
		}

		CSVisibility defaultVisibility = CSVisibility.Internal;
		if (node.getParent() instanceof TypeDeclaration && ((TypeDeclaration)node.getParent()).isInterface()) {
			defaultVisibility = CSVisibility.Public;
		} else if (node instanceof EnumConstantDeclaration) {
			defaultVisibility = CSVisibility.Public;
		}

		return mapVisibility(node.getModifiers(), defaultVisibility);
	}

	CSVisibility mapVisibility(int modifiers) {
		return mapVisibility(modifiers, CSVisibility.Internal);
	}

	CSVisibility mapVisibility(int modifiers, CSVisibility defaultVisibility) {
		if (Modifier.isPublic(modifiers)) {
			return CSVisibility.Public;
		}
		if (Modifier.isProtected(modifiers)) {
			return _configuration.mapProtectedToProtectedInternal() ?  
						CSVisibility.ProtectedInternal :
						CSVisibility.Protected;
		}
		if (Modifier.isPrivate(modifiers)) {
			return CSVisibility.Private;
		}
		return defaultVisibility;
	}

	protected CSTypeReferenceExpression mappedTypeReference(Type type) {
		return mappedTypeReference(type.resolveBinding());
	}

	private CSTypeReferenceExpression mappedMacroTypeReference(ITypeBinding typeUsage, final TypeDeclaration typeDeclaration) {
		
	    final CSMacro macro = new CSMacro(JavadocUtility.singleTextFragmentFrom(javadocTagFor(typeDeclaration, SharpenAnnotations.SHARPEN_MACRO)));
	    
	    final ITypeBinding[] typeArguments = typeUsage.getTypeArguments();
	    if (typeArguments.length > 0) {
		    final ITypeBinding[] typeParameters = typeUsage.getTypeDeclaration().getTypeParameters();
			for (int i = 0; i < typeParameters.length; i++) {
				macro.addVariable(typeParameters[i].getName(), mappedTypeReference(typeArguments[i]));
	        }
	    }
	    
	    return new CSMacroTypeReference(macro);
    }

	private boolean isMacroType(final ASTNode declaration) {
	    return declaration instanceof TypeDeclaration
	    	&& containsJavadoc((TypeDeclaration)declaration, SharpenAnnotations.SHARPEN_MACRO);
    }

	protected CSTypeReferenceExpression mappedTypeReference(ITypeBinding type) {
		return mappedTypeReference(type, false);
	}

	protected CSTypeReferenceExpression mappedAuxillaryTypeReference(ITypeBinding type) {
		return mappedTypeReference(type, true);
	}

	protected CSTypeReferenceExpression mappedTypeReference(ITypeBinding type, boolean auxillary) {
		if (auxillary && !type.isInterface()) {
			throw new IllegalArgumentException("Auxillary types are only available for interfaces.");
		}

		final ASTNode declaration = findDeclaringNode(type);
		if (isMacroType(declaration)) {
			return mappedMacroTypeReference(type, (TypeDeclaration) declaration);
		}
		
		if (type.isArray()) {
			return mappedArrayTypeReference(type);
		}
		if (type.isWildcardType()) {
			return mappedWildcardTypeReference(type);
		}

		if (type.isInterface() && auxillary) {
			return new CSTypeReference(type.getName() + "Constants");
		}

		final CSTypeReference typeRef = new CSTypeReference(mappedTypeName(type));
		if (isJavaLangClass(type)) {
			return typeRef;
		}

		for (ITypeBinding arg : type.getTypeArguments()) {
			typeRef.addTypeArgument(mappedTypeReference(arg));
		}
		return typeRef;
	}

	private boolean isJavaLangClass(ITypeBinding type) {
		return type.getErasure() == javaLangClassBinding();
	}

	private ITypeBinding javaLangClassBinding() {
		return resolveWellKnownType("java.lang.Class");
	}

	private CSTypeReferenceExpression mappedWildcardTypeReference(ITypeBinding type) {
		final ITypeBinding bound = type.getBound();
		return bound != null ? mappedTypeReference(bound) : OBJECT_TYPE_REFERENCE;
	}

	private CSTypeReferenceExpression mappedArrayTypeReference(ITypeBinding type) {
		return new CSArrayTypeReference(mappedTypeReference(type.getElementType()), type.getDimensions());

	}

	protected final String mappedTypeName(ITypeBinding type) {
		return my(Mappings.class).mappedTypeName(type);		
	}
	
	private static String qualifiedName(ITypeBinding type) {
		return BindingUtils.qualifiedName(type);
	}

	private String interfaceName(String name) {
		return my(Configuration.class).toInterfaceName(name);
	}

	private String mappedTypeName(String typeName) {
		return mappedTypeName(typeName, typeName);
	}

	private String mappedTypeName(String typeName, String defaultValue) {
		return _configuration.mappedTypeName(typeName, defaultValue);
	}

	private String annotatedRenaming(BodyDeclaration node) {
		return my(Annotations.class).annotatedRenaming(node);
	}

	protected String mappedMethodName(MethodDeclaration node) {
		return mappedMethodName(node.resolveBinding());
	}

	protected final String mappedMethodName(IMethodBinding binding) {
		return my(Mappings.class).mappedMethodName(binding);
	}

	private String qualifiedName(IMethodBinding actual) {
		return BindingUtils.qualifiedName(actual);
	}

	private boolean isEvent(MethodDeclaration declaring) {
		return eventTagFor(declaring) != null;
	}

	private boolean isMappedToProperty(BodyDeclaration original) {
		IMethodBinding binding;
		if (original instanceof MethodDeclaration) {
			binding = ((MethodDeclaration)original).resolveBinding();
		} else if (original instanceof AnnotationTypeMemberDeclaration) {
			binding = ((AnnotationTypeMemberDeclaration)original).resolveBinding();
		} else {
			throw new UnsupportedOperationException();
		}

		final MemberMapping mapping = effectiveMappingFor(binding);
		if (null == mapping)
			return false;
		return mapping.kind == MemberKind.Property;
	}

	private MemberMapping effectiveMappingFor(IMethodBinding binding) {
		return my(Mappings.class).effectiveMappingFor(binding);
	}

	private String methodName(String name) {
		return namingStrategy().methodName(name);
	}

	protected String identifier(SimpleName name) {
		return identifier(name.toString());
	}

	protected String identifier(String name) {
		return namingStrategy().identifier(name);
	}

	private void unresolvedTypeBinding(ASTNode node) {
		warning(node, "unresolved type binding for node: " + node);
	}

	public boolean visit(CompilationUnit node) {
		return true;
	}

	private void warning(ASTNode node, String message) {
		warningHandler().warning(node, message);
	}

	protected final String sourceInformation(ASTNode node) {
		return ASTUtility.sourceInformation(_ast, node);
	}

	@SuppressWarnings("deprecation")
	protected int lineNumber(ASTNode node) {
		return _ast.lineNumber(node.getStartPosition());
	}

	public void setASTResolver(ASTResolver resolver) {
		_resolver = resolver;
	}
	
	private String mappedNamespace(String namespace) {
		return _configuration.mappedNamespace(namespace);
	}
	
	@Override
	public boolean visit(Block node) {
		if (isBlockInsideBlock (node)) {
			CSBlock parent = _currentBlock;
			_currentBlock = new CSBlock ();
			_currentBlock.parent(parent);
			parent.addStatement(_currentBlock);
		}
		_currentContinueLabel = null;
		pushScope ();
		return super.visit(node);
	}
	
	@Override
	public void endVisit(Block node) {
		if (isBlockInsideBlock (node)) {
			_currentBlock = (CSBlock) _currentBlock.parent();
		}
		popScope ();
		super.endVisit(node);
	}
	
	boolean isBlockInsideBlock (Block node) {
		return node.getParent() instanceof Block;
	}
	
	void pushScope () {
		HashSet<String> newLocalVars = new HashSet<String>();
		if (_localBlockVariables.size() > 0)
			newLocalVars.addAll(_localBlockVariables.peek());
		_localBlockVariables.push(newLocalVars);
		
		HashSet<String> newBlockVars = new HashSet<String>();
		newBlockVars.addAll(newLocalVars);
		_blockVariables.push(newBlockVars);
		
		HashMap<String,String> newRenamed = new HashMap<String,String>();
		if (_renamedVariables.size() > 0)
			newRenamed.putAll(_renamedVariables.peek());
		_renamedVariables.push(newRenamed);
	}
	
	void popScope () {
		_blockVariables.pop();
		_localBlockVariables.pop();
		_renamedVariables.pop();
	}
}

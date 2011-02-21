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

	private String getText(int startPosition, int length) {
		try {
			return ((ICompilationUnit) _ast.getJavaElement()).getBuffer().getText(startPosition, length);
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

	public boolean visit(EnumDeclaration node) {
		if (!SharpenAnnotations.hasIgnoreAnnotation(node)) {
			notImplemented(node);
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
		notImplemented(node);
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
		final CSEnum theEnum = new CSEnum(typeName(node));
		mapVisibility(node, theEnum);
		mapJavadoc(node, theEnum);
		addType(theEnum);

		node.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment node) {
				theEnum.addValue(identifier(node.getName()));
				return false;
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				if (node.isConstructor() && isPrivate(node)) {
					return false;
				}
				unsupportedConstruct(node, "Enum can contain only fields and a private constructor.");
				return false;
			}
		});
		return true;
	}

	protected boolean isPrivate(MethodDeclaration node) {
		return Modifier.isPrivate(node.getModifiers());
	}

	private boolean isEnum(TypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_ENUM);
	}

	private boolean processIgnoredType(TypeDeclaration node) {
		if (!hasIgnoreOrRemoveAnnotation(node)) {
			return false;
		}
		if (isMainType(node)) {
			compilationUnit().ignore(true);
		}
		return true;
	}

	private boolean hasIgnoreOrRemoveAnnotation(TypeDeclaration node) {
	    return SharpenAnnotations.hasIgnoreAnnotation(node) || hasRemoveAnnotation(node);
    }

	private void processNonStaticNestedTypeDeclaration(TypeDeclaration node) {
		new NonStaticNestedClassBuilder(this, node);
	}

	protected CSTypeDeclaration processTypeDeclaration(TypeDeclaration node) {
		CSTypeDeclaration type = mapTypeDeclaration(node);
		
		processDisabledType(node, isMainType(node) ? _compilationUnit : type);

		addType(type);

		mapSuperTypes(node, type);

		mapVisibility(node, type);
		mapDocumentation(node, type);
		processConversionJavadocTags(node, type);
		mapMembers(node, type);

		autoImplementCloneable(node, type);
		
		moveInitializersDependingOnThisReferenceToConstructor(type);
	
		return type;
	}

	private void processDisabledType(TypeDeclaration node, CSNode type) {
		final String expression = _configuration.conditionalCompilationExpressionFor(packageNameFor(node));
		if (null != expression) {
			compilationUnit().addEnclosingIfDef(expression);
		}
		
		processDisableTags(node, type);
	}

	private String packageNameFor(TypeDeclaration node) {
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

	private void autoImplementCloneable(TypeDeclaration node, CSTypeDeclaration type) {

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

	private void mapSuperTypes(TypeDeclaration node, CSTypeDeclaration type) {
		if (!_ignoreExtends.value()) {
			mapSuperClass(node, type);
		}
		if (!ignoreImplements(node)) {
			mapSuperInterfaces(node, type);
		}
	}

	private boolean ignoreImplements(TypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_IGNORE_IMPLEMENTS);
	}

	private boolean ignoreExtends(TypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_IGNORE_EXTENDS);
	}

	private void processConversionJavadocTags(TypeDeclaration node, CSTypeDeclaration type) {
		processPartialTagElement(node, type);
	}

	private CSTypeDeclaration mapTypeDeclaration(TypeDeclaration node) {
		CSTypeDeclaration type = typeDeclarationFor(node);
		type.startPosition(node.getStartPosition());
		type.sourceLength(node.getLength());
		mapTypeParameters(node.typeParameters(), type);
		return checkForMainType(node, type);
	}

	private void mapTypeParameters(final List typeParameters, CSTypeParameterProvider type) {
		for (Object item : typeParameters) {
			type.addTypeParameter(mapTypeParameter((TypeParameter) item));
		}
	}

	private CSTypeParameter mapTypeParameter(TypeParameter item) {
		return new CSTypeParameter(identifier(item.getName()));
	}

	private CSTypeDeclaration typeDeclarationFor(TypeDeclaration node) {
		if (node.isInterface()) {
			return new CSInterface(processInterfaceName(node));
		}
		final String typeName = typeName(node);
		if (isStruct(node)) {
			return new CSStruct(typeName);
		}
		return new CSClass(typeName, mapClassModifier(node.getModifiers()));
	}

	private String typeName(TypeDeclaration node) {
		final String renamed = annotatedRenaming(node);
		if (renamed != null)
			return renamed;
		return node.getName().toString();
	}

	private boolean isStruct(TypeDeclaration node) {
		return containsJavadoc(node, SharpenAnnotations.SHARPEN_STRUCT);
	}

	private CSTypeDeclaration checkForMainType(TypeDeclaration node, CSTypeDeclaration type) {
		if (isMainType(node)) {
			setCompilationUnitElementName(type.name());
		}
		return type;
	}

	private void setCompilationUnitElementName(String name) {
		_compilationUnit.elementName(name + ".cs");
	}

	private String processInterfaceName(TypeDeclaration node) {
		String name = node.getName().getFullyQualifiedName();
		return interfaceName(name);
	}

	private boolean isMainType(TypeDeclaration node) {
		return node.isPackageMemberTypeDeclaration() && Modifier.isPublic(node.getModifiers());
	}

	private void mapSuperClass(TypeDeclaration node, CSTypeDeclaration type) {
		if (handledExtends(node, type))
			return;
		
		if (null == node.getSuperclassType())
			return;
		
		final ITypeBinding superClassBinding = node.getSuperclassType().resolveBinding();
		if (null == superClassBinding)
			unresolvedTypeBinding(node.getSuperclassType());
			
		type.addBaseType(mappedTypeReference(superClassBinding));
	}

	private boolean handledExtends(TypeDeclaration node, CSTypeDeclaration type) {
		final TagElement replaceExtendsTag = javadocTagFor(node, SharpenAnnotations.SHARPEN_EXTENDS);
		if (null == replaceExtendsTag)
			return false;
	
		final String baseType = JavadocUtility.singleTextFragmentFrom(replaceExtendsTag);
		type.addBaseType(new CSTypeReference(baseType));		
		return true;
	}

	private void mapSuperInterfaces(TypeDeclaration node, CSTypeDeclaration type) {
		final ITypeBinding serializable = resolveWellKnownType("java.io.Serializable");
		for (Object itf : node.superInterfaceTypes()) {
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

	private void mapMembers(TypeDeclaration node, CSTypeDeclaration type) {
		CSTypeDeclaration saved = _currentType;
		_currentType = type;
		try {
			visit(node.bodyDeclarations());
			createInheritedAbstractMemberStubs(node);
			flushInstanceInitializers(type, 0);
		} finally {
			_currentType = saved;
		}
	}

	private void mapVisibility(BodyDeclaration node, CSMember member) {
		member.visibility(mapVisibility(node));
	}

	private boolean isNonStaticNestedType(ITypeBinding binding) {
		if (binding.isInterface())
			return false;
		if (!binding.isNested())
			return false;
		return !isStatic(binding);
	}

	private boolean isStatic(ITypeBinding binding) {
	    return Modifier.isStatic(binding.getModifiers());
    }

	private void addType(CSType type) {
		if (null != _currentType) {
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
			member.addDoc(mapTagElement(element));
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

	private void processPartialTagElement(TypeDeclaration node, CSTypeDeclaration member) {
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
			member.addDoc(createTagNode("remarks", element));
		} else {
			member.addDoc(createTagNode("summary", element));
		}
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

	private CSDocNode mapTagElement(TagElement element) {
		String tagName = element.getTagName();
		if (TagElement.TAG_PARAM.equals(tagName)) {
			return mapTagParam(element);
		} else if (TagElement.TAG_RETURN.equals(tagName)) {
			return createTagNode("returns", element);
		} else if (TagElement.TAG_LINK.equals(tagName)) {
			return mapTagLink(element);
		} else if (TagElement.TAG_THROWS.equals(tagName)) {
			return mapTagThrows(element);
		} else if (TagElement.TAG_SEE.equals(tagName)) {
			return mapTagWithCRef("seealso", element);
		}
		return createTagNode(tagName.substring(1), element);
	}

	private CSDocNode mapTagThrows(TagElement element) {
		return mapTagWithCRef("exception", element);
	}

	private CSDocNode mapTagLink(TagElement element) {
		return mapTagWithCRef("see", element);
	}

	private CSDocNode mapTagWithCRef(String tagName, TagElement element) {
		final List fragments = element.fragments();
		if (fragments.isEmpty()) {
			return invalidTagWithCRef(element, tagName, element);
		}
		final ASTNode linkTarget = (ASTNode) fragments.get(0);
		String cref = mapCRefTarget(linkTarget);
		if (null == cref) {
			return invalidTagWithCRef(linkTarget, tagName, element);
		}
		CSDocTagNode node = newTagWithCRef(tagName, cref);
		if (fragments.size() > 1) {
			if (isLinkWithSimpleLabel(fragments, linkTarget)) {
				node.addTextFragment(unqualifiedName(cref));
			} else {
				collectFragments(node, fragments, 1);
			}
		} else {
			//TODO: Move the XML encoding to the right place (CSharpPrinter)
			node.addTextFragment(cref.replace("{", "&lt;").replace("}", "&gt;"));
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
	
	private CSDocNode invalidTagWithCRef(final ASTNode linkTarget, String tagName, TagElement element) {
		warning(linkTarget, "Tag '" + element.getTagName() + "' demands a valid cref target.");
		CSDocNode newTag = createTagNode(tagName, element);
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

	private CSDocNode mapTagParam(TagElement element) {
		
		List fragments = element.fragments();
		SimpleName name = (SimpleName) fragments.get(0);
		if (null == name.resolveBinding()) {
			warning(name, "Parameter '" + name + "' not found.");
		}
		
		CSDocTagNode param = isPropertyNode(documentedNodeAttachedTo(element)) 
									? new CSDocTagNode("value") 
									: newCSDocTag(fixIdentifierNameFor(identifier(name), element));
		
		collectFragments(param, fragments, 1);
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

	private void collectFragments(CSDocTagNode node, List fragments, int index) {
		for (int i = index; i < fragments.size(); ++i) {
			node.addFragment(mapTagElementFragment((ASTNode) fragments.get(i)));
		}
	}

	private CSDocNode mapTextElement(TextElement element) {
		final String text = element.getText();
		if (HTML_ANCHOR_PATTERN.matcher(text).find()) {
			warning(element, "Caution: HTML anchors can result in broken links. Consider using @link instead.");
		}
		return new CSDocTextNode(text);
	}

	private CSDocNode createTagNode(String tagName, TagElement element) {
		CSDocTagNode summary = new CSDocTagNode(tagName);
		for (Object f : element.fragments()) {
			summary.addFragment(mapTagElementFragment((ASTNode) f));
		}
		return summary;
	}

	private CSDocNode mapTagElementFragment(ASTNode node) {
		switch (node.getNodeType()) {
		case ASTNode.TAG_ELEMENT:
			return mapTagElement((TagElement) node);
		case ASTNode.TEXT_ELEMENT:
			return mapTextElement((TextElement) node);
		}
		warning(node, "Documentation node not supported: " + node.getClass() + ": " + node);
		return new CSDocTextNode(node.toString());
	}

	public boolean visit(FieldDeclaration node) {

		if (SharpenAnnotations.hasIgnoreAnnotation(node)) {
			return false;
		}

		CSTypeReferenceExpression typeName = mappedTypeReference(node.getType());
		CSVisibility visibility = mapVisibility(node);

		for (Object item : node.fragments()) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) item;
			CSField field = mapFieldDeclarationFragment(node, fragment, typeName, visibility);
			_currentType.addMember(field);
		}

		return false;
	}

	private CSField mapFieldDeclarationFragment(FieldDeclaration node, VariableDeclarationFragment fragment,
	        CSTypeReferenceExpression fieldType, CSVisibility fieldVisibility) {
		CSField field = new CSField(fieldName(fragment), fieldType, fieldVisibility, mapFieldInitializer(fragment));
		if (isConstField(node, fragment)) {
			field.addModifier(CSFieldModifier.Const);
		} else {
			processFieldModifiers(field, node.getModifiers());
		}
		mapDocumentation(node, field);
		mapAnnotations(node, field);
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

	protected CSExpression mapFieldInitializer(VariableDeclarationFragment fragment) {
		return mapExpression(fragment.getInitializer());
	}

	private boolean isConstField(FieldDeclaration node, VariableDeclarationFragment fragment) {
		//
		return Modifier.isFinal(node.getModifiers()) && node.getType().isPrimitiveType() && 
			hasConstValue(fragment) && Modifier.isStatic(node.getModifiers());
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
		return propertyName(declaringNode(binding));
	}
	
	private boolean isProperty(MethodDeclaration node) {
		return isTaggedAsProperty(node)
			|| isMappedToProperty(node);
	}

	private boolean isTaggedAsProperty(MethodDeclaration node) {
	    return isTaggedDeclaration(node, SharpenAnnotations.SHARPEN_PROPERTY);
    }

	private boolean isTaggedDeclaration(MethodDeclaration node, final String tag) {
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
	}

	private void mapMethodParts(MethodDeclaration node, CSMethodBase method) {

		_currentType.addMember(method);

		method.startPosition(node.getStartPosition());
		method.isVarArgs(node.isVarargs());
		mapVisibility(node, method);
		mapParameters(node, method);
		mapDocumentation(node, method);
		mapAnnotations(node, method);
		visitBodyDeclarationBlock(node, node.getBody(), method);
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
		method.addParameter(createParameter(parameter));
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

	private TagElement effectiveAnnotationFor(MethodDeclaration node, final String annotation) {
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
		return createVariableDeclaration(binding, mapExpression(variable.getInitializer()));
	}

	private CSVariableDeclaration createVariableDeclaration(IVariableBinding binding, CSExpression initializer) {
		return new CSVariableDeclaration(identifier(binding.getName()), mappedTypeReference(binding.getType()),
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
		visitBlock(stmt.body(), node.getBody());
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
		CSNumberLiteralExpression literal = new CSNumberLiteralExpression(token);

		if (token.startsWith("0x")) {
			if (token.endsWith("l") || token.endsWith("L")) {
				pushExpression(uncheckedCast("long", literal));
			} else {
				pushExpression(uncheckedCast("int", literal));
			}

		} else {
			pushExpression(literal);
		}

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
			pushExpression(new CSStringLiteralExpression(node.getEscapedValue()));
		}
		return false;
	}

	public boolean visit(CharacterLiteral node) {
		pushExpression(new CSCharLiteralExpression(node.getEscapedValue()));
		return false;
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
		if (node.dimensions().size() > 1) {
			if (null != node.getInitializer()) {
				notImplemented(node);
			}
			pushExpression(unfoldMultiArrayCreation(node));
		} else {
			pushExpression(mapSingleArrayCreation(node));
		}
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
			ace.initializer(mapArrayInitializer(node));
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
				CSForStatement stmt = new CSForStatement(node.getStartPosition(), mapExpression(node.getExpression()));
				for (Object i : node.initializers()) {
					stmt.addInitializer(mapExpression((Expression) i));
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

	public boolean visit(SwitchStatement node) {
		_currentContinueLabel = null;
		CSBlock saved = _currentBlock;

		CSSwitchStatement mappedNode = new CSSwitchStatement(node.getStartPosition(), mapExpression(node.getExpression()));
		addStatement(mappedNode);

		CSCaseClause defaultClause = null;
		CSCaseClause current = null;
		for (ASTNode element : Types.<Iterable<ASTNode>>cast(node.statements())) {
			if (ASTNode.SWITCH_CASE == element.getNodeType()) {
				if (null == current) {
					current = new CSCaseClause();
					mappedNode.addCase(current);
					_currentBlock = current.body();
				}
				SwitchCase sc = (SwitchCase) element;
				if (sc.isDefault()) {
					defaultClause = current;
					current.isDefault(true);
				} else {
					current.addExpression(mapExpression(sc.getExpression()));
				}
			} else {
				current = null;
				element.accept(this);
			}
		}

		if (null != defaultClause) {
			List<CSStatement> stats = defaultClause.body().statements();
			
			CSStatement lastStmt = stats.size() > 0 ? stats.get(stats.size()-1) : null;
			if( ! ( lastStmt instanceof CSThrowStatement) ) {
				defaultClause.body().addStatement(new CSBreakStatement(Integer.MIN_VALUE));
			}
		}

		_currentBlock = saved;
		return false;
	}

	public boolean visit(CastExpression node) {
		pushExpression(new CSCastExpression(mappedTypeReference(node.getType()), mapExpression(node.getExpression())));
		return false;
	}

	public boolean visit(PrefixExpression node) {
		pushExpression(new CSPrefixExpression(node.getOperator().toString(), mapExpression(node.getOperand())));
		return false;
	}

	public boolean visit(PostfixExpression node) {
		pushExpression(new CSPostfixExpression(node.getOperator().toString(), mapExpression(node.getOperand())));
		return false;
	}

	public boolean visit(InfixExpression node) {

		CSExpression left = mapExpression(node.getLeftOperand());
		CSExpression right = mapExpression(node.getRightOperand());
		if (node.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
			String operator = ">>";
			pushExpression(new CSInfixExpression("&", right, new CSNumberLiteralExpression("0x1f")));
			pushExpression(new CSParenthesizedExpression(popExpression()));
			pushExpression(new CSInfixExpression(operator, new CSParenthesizedExpression(left), popExpression()));
		} else {
			String operator = node.getOperator().toString();
			pushExpression(new CSInfixExpression(operator, left, right));
			pushExtendedOperands(operator, node);
		}

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

	public boolean visit(Assignment node) {
		Expression lhs = node.getLeftHandSide();
		pushExpression(new CSInfixExpression(node.getOperator().toString(), mapExpression(lhs), mapExpression(lhs
		        .resolveTypeBinding(), node.getRightHandSide())));
		return false;
	}

	private CSExpression mapExpression(ITypeBinding expectedType, Expression expression) {
		return castIfNeeded(expectedType, expression.resolveTypeBinding(), mapExpression(expression));
	}

	private CSExpression castIfNeeded(ITypeBinding expectedType, ITypeBinding actualType, CSExpression expression) {
		ITypeBinding charType = resolveWellKnownType("char");
		if (expectedType != charType)
			return expression;
		if (actualType == expectedType)
			return expression;
		return new CSCastExpression(mappedTypeReference(expectedType), expression);
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
	    final MethodDeclaration declaration = declaringNode(binding);
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

		processOrdinaryMethodInvocation(node);
	}

	private boolean isMacro(MethodInvocation node) {
	    return isTaggedMethodInvocation(node, SharpenAnnotations.SHARPEN_MACRO);
    }

	private void processMacroInvocation(MethodInvocation node) {
		final MethodDeclaration declaration = declaringNode(node.resolveMethodBinding());
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
		final CSExpression targetExpression = mapMethodTargetExpression(node);
		
		String name = resolveTargetMethodName(node);
		CSExpression target = null == targetExpression
				? new CSReferenceExpression(name)
		        : new CSMemberReferenceExpression(targetExpression, name);
		CSMethodInvocationExpression mie = new CSMethodInvocationExpression(target);
		mapMethodInvocationArguments(mie, node);
		mapTypeArguments(mie, node);
		pushExpression(mie);
    }

	private String resolveTargetMethodName(MethodInvocation node) {
		final IMethodBinding method = staticImportMethodBinding(node.getName(), _ast.imports());
		if(method != null){
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
				addArgument(mie, arg);
			}
		}
	}

	private boolean isClassLiteral(Expression arg) {
		return arg.getNodeType() == ASTNode.TYPE_LITERAL;
	}

	private void processEventSubscription(MethodInvocation node) {

		final MethodDeclaration addListener = declaringNode(node.resolveMethodBinding());
		assertValidEventAddListener(node, addListener);

		final MethodInvocation eventInvocation = (MethodInvocation) node.getExpression();

		final MethodDeclaration eventDeclaration = declaringNode(eventInvocation.resolveMethodBinding());
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
		final MethodDeclaration method = declaringNode(originalMethodBinding(binding));
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
			addArgument(mie, (Expression) arg);
		}
	}

	private void addArgument(CSMethodInvocationExpression mie, Expression arg) {
		mie.addArgument(mapExpression(arg));
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
		} else {
			pushExpression(new CSReferenceExpression(identifier(node)));
		}
		return false;
	}

	private void addStatement(CSStatement statement) {
		_currentBlock.addStatement(statement);
	}

	private void pushTypeReference(ITypeBinding typeBinding) {
		pushExpression(createTypeReference(typeBinding));
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
		pushExpression(new CSMemberReferenceExpression(mapExpression(qualifier), name));
	}

	private IVariableBinding variableBinding(Name node) {
		if (node.resolveBinding() instanceof IVariableBinding) {
			return (IVariableBinding) node.resolveBinding();
		}
		return null;
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

	private void createInheritedAbstractMemberStubs(TypeDeclaration node) {
		if (node.isInterface())
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
		final MethodDeclaration dec = declaringNode(binding);
		return dec != null && SharpenAnnotations.hasIgnoreAnnotation(dec);
	}

	private boolean stubIsProperty(IMethodBinding method) {
		final MethodDeclaration dec = declaringNode(method);
		return dec != null && isProperty(dec);
	}

	private MethodDeclaration declaringNode(IMethodBinding method) {
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
		final MethodDeclaration node = declaringNode(method);
		if (node == null) return;
		
		processDisableTags(node, member);
	}

	CSMethodModifier mapMethodModifier(MethodDeclaration method) {
		if (_currentType.isInterface()) {
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

	private boolean isOverride(MethodDeclaration method) {
		IMethodBinding methodBinding = method.resolveBinding();
		ITypeBinding superclass = _ignoreExtends.value() ? resolveWellKnownType("java.lang.Object") : methodBinding
		        .getDeclaringClass().getSuperclass();
		if (null == superclass)
			return false;
		return null != BindingUtils.findOverriddenMethodInHierarchy(superclass, methodBinding);
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

		return mapVisibility(node.getModifiers());
	}

	CSVisibility mapVisibility(int modifiers) {
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
		return CSVisibility.Internal;
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

	private boolean isMappedToProperty(MethodDeclaration original) {
		final MemberMapping mapping = effectiveMappingFor(original.resolveBinding());
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
		_currentContinueLabel = null;
		return super.visit(node);
	}
	
}

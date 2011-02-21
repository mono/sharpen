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

package sharpen.ui.tests;

import sharpen.core.*;

public class UnclassifiedConversionTestCase extends AbstractConversionTestCase {
	
	public void testHeader() throws Exception {
		Configuration configuration = getConfiguration();
		configuration.setHeader(ResourceUtility.getStringContents("resources/header.txt"));
		
		runResourceTestCase(configuration, "HeaderSupport");
	}
	
	public void testVarArgs() throws Throwable {
		runResourceTestCase("VarArgs");
	}
	
	public void testSharpenEnum() throws Throwable {
		runResourceTestCase("Enum1");
	}
	
	public void testObjectMethodsThroughGenericInterface() throws Throwable {
		runResourceTestCase("ObjectMethods4");
	}
	
	public void testEnhancedFor() throws Throwable {
		runResourceTestCase("ForEach1");
	}
	
	public void testEnhancedForOverGenerics() throws Throwable {
		runResourceTestCase("ForEach2");
	}
	
	public void testStruct() throws Throwable {
		runResourceTestCase("structs/Struct1");
	}

	public void testStaticBlocks() throws Throwable {
		runResourceTestCase("StaticBlocks1");
	}
	
	public void testStringEmpty() throws Throwable {
		runResourceTestCase("StringEmpty");
	}
	
	public void testEmptyCatch() throws Throwable {
		runResourceTestCase("Catch1");
	}
	
	public void testExternalMethodMapping() throws Throwable {
		String jar = JarUtilities.createJar(BindingTestCaseSubject.class, BindingTestCaseSubject.Foo.class, BindingTestCaseSubject.Baz.class);
		_project.addClasspathEntry(jar);
		
		Configuration conf = newPascalCaseIdentifiersConfiguration();
		conf.mapMethod(BindingTestCaseSubject.Foo.class.getCanonicalName() + ".bar", "Mapped");
		runResourceTestCase(conf, "ExternalMethodMapping");
	}
	
	public void testMethodNameMappingInHierarchy() throws Throwable {
		Configuration conf = newPascalCaseIdentifiersConfiguration(); 
		conf.mapMethod("Foo.mappedToBar", "Bar");
		runResourceTestCase(conf, "MethodNameMappingInHierarchy1");
	}
	
	public void testMethodNameMappingInHierarchy2() throws Throwable {
		Configuration conf = newPascalCaseIdentifiersConfiguration();
		conf.mapMethod("Foo.mappedToBar", "Bar");
		runResourceTestCase(conf, "MethodNameMappingInHierarchy2");
	}
	
	public void testMappingByMethodSignature() throws Throwable {
		runResourceTestCase("SignatureMapping");
	}
	
	public void testIntLiterals() throws Throwable {
		runResourceTestCase("IntLiterals1");
	}
	
	public void testUnsignedRightShift() throws Throwable {
		runResourceTestCase("UnsignedRightShift");
	}
	
	public void testSimpleNestedInterface() throws Throwable {
		runResourceTestCase("NestedInterface1");
	}
	
	public void testPartial() throws Throwable {
		runResourceTestCase("Partial");
	}
	
	public void testExtends() throws Throwable {
		runResourceTestCase("Extends");
	}
	
	public void testContinue() throws Throwable {
		runResourceTestCase("Continue1");
	}
	
	public void testTransientField() throws Throwable {
		runResourceTestCase("Transient1");
	}
	
	public void testWrapperTypesMethods() throws Throwable {
		runResourceTestCase("WrapperTypesMethods1");
	}
	
	public void testDeadBranchElimination() throws Throwable {
		runResourceTestCase("DeadBranchElimination1");
	}
	
	public void testStaticFinalField() throws Throwable {
		runResourceTestCase("StaticFinalField1");
	}

	public void testStringMethodsInvocation() throws Throwable {
		runResourceTestCase("StringMethods1");
	}	
	
	public void testStandardConstants() throws Throwable {
		runResourceTestCase("StandardConstants1");
	}
	
	public void testCharLiteral() throws Throwable {
		runResourceTestCase("CharLiteral1");
	}
	
	public void testForWithAssignment() throws Throwable {
		runResourceTestCase("For2");
	}
	
	public void testGetClass() throws Throwable {
		runResourceTestCase("GetClass1");
	}
	
	public void testJavaArray() throws Throwable {
		runResourceTestCase("JavaArray1");
	}
	
	public void testPrintStackTrace() throws Throwable {
		runResourceTestCase("PrintStackTrace1");
	}
	
	public void testWaitNotify() throws Throwable {
		runResourceTestCase("WaitNotify1");
	}
	
	public void testClone() throws Throwable {
		runResourceTestCase("Clone1");
	}
	
	public void testJavaLangSystemToJavaSystem() throws Throwable {
		runResourceTestCase("JavaSystem1");
	}
	
	public void testFinalizeMethod() throws Throwable {
		runResourceTestCase("Finalize1");
	}
	
	public void testPostfixExpressions() throws Throwable {
		runResourceTestCase("PostfixExpressions1");
	}
	
	public void testInterfaceInheritance() throws Throwable {
		runResourceTestCase("InterfaceInheritance1");
	}
	
	public void testSwitchCase() throws Throwable {
		runResourceTestCase("SwitchCase1");
	}

	public void testCascadingSwitchCase() throws Throwable {
		runResourceTestCase("SwitchCase2");
	}

	public void testSwitchCaseDefaultThrows() throws Throwable {
		runResourceTestCase("SwitchCaseDefaultThrows");
	}

	public void testFinalClass() throws Throwable {
		runResourceTestCase("FinalClass1");
	}
	
	public void testAbstractClass() throws Throwable {
		runResourceTestCase("AbstractClass1");
	}
	
	public void testInterfaceImplementationProvidedBySuperclass() throws Throwable {
		runResourceTestCase("InterfaceImplementation7");
	}
	
	public void testAbstractInterfaceHierarchyImpl() throws Throwable {
		runResourceTestCase("InterfaceImplementation6");
	}
	
	public void testInterfaceImplWithFinalMethod() throws Throwable {
		runResourceTestCase("InterfaceImplementation5");
	}
	
	public void testInheritedAbstractInterfaceImpl() throws Throwable {
		runResourceTestCase("InterfaceImplementation4");
	}
	
	public void testAbstractInterfaceImplementation() throws Throwable {
		runResourceTestCase("InterfaceImplementation3");
	}
	
	public void testSerializableSimpleClass() throws Throwable {
		runResourceTestCase("Serializable1");
	}
	
	public void testSerializableInterface() throws Throwable {
		runResourceTestCase("Serializable2");
	}
	
	public void testSerializableClassExtendSerializableInterface() throws Throwable {
		runResourceTestCase("Serializable3");
	}
	
	public void testDoWhile() throws Throwable {
		runResourceTestCase("DoWhile1");
	}
	
	public void testSimpleFinalMethod() throws Throwable {
		runResourceTestCase("FinalMethod1");
	}
	
	public void testExtendedFinalMethod() throws Throwable {
		runResourceTestCase("FinalMethod2");
	}
	
	public void testJavaLang() throws Throwable {
		runResourceTestCase("JavaLang1");
	}
	
	public void testBaseTypeQualifiedName() throws Throwable {
		runResourceTestCase("mp/BaseType1");
	}
	
	public void testTernaryOperator() throws Throwable {
		runResourceTestCase("TernaryOperator1");
	}
	
	public void testLongLine() throws Throwable {
		runResourceTestCase("LongLine1");
	}

	public void testEmptyClassEmptyPackage() throws Throwable {
		runResourceTestCase("EmptyClass");
	}

	public void testEmptyClassWithPackage() throws Throwable {
		runResourceTestCase("mp/Albatross");
	}

	public void testSimpleFields() throws Throwable {
		runResourceTestCase("Fields1");
	}
	
	public void testFieldReferences() throws Throwable {
		runResourceTestCase("Fields2");
	}
	
	public void testSimpleMemberReference() throws Throwable {
		runResourceTestCase("MemberRef1");
	}

	public void testReturnInteger() throws Throwable {
		runResourceTestCase("Return1");
	}

	public void testReturnField() throws Throwable {
		runResourceTestCase("Return2");
	}
	
	public void testSimpleInterface() throws Throwable {
		runResourceTestCase("Interface1");
	}
	
	public void testWhile() throws Throwable {
		runResourceTestCase("While1");
	}
	
	public void testIf() throws Throwable {
		runResourceTestCase("If1");
	}
	
	public void testIfElse() throws Throwable {
		runResourceTestCase("If2");
	}
	
	public void testFor() throws Throwable {
		runResourceTestCase("For1");
	}
	
	public void testSimpleConstString() throws Throwable {
		runResourceTestCase("ConstString1");
	}
	
	public void testConstStringWithEscapedChars() throws Throwable {
		runResourceTestCase("ConstString2");
	}
	
	public void testCastExpression() throws Throwable {
		runResourceTestCase("Cast1");
	}
	
	public void testClassCastExceptionMapping() throws Throwable {
		runResourceTestCase("Cast2");
	}
	
	public void testTryCatch() throws Throwable {
		runResourceTestCase("Try1");
	}
	
	public void testTryFinally() throws Throwable {
		runResourceTestCase("Try2");
	}
	
	public void testThrow() throws Throwable {
		runResourceTestCase("Throw1");
	}
	
	public void testBinaryExpressions() throws Throwable {
		runResourceTestCase("BinaryExpressions1");
	}
	
	public void testInstanceOf() throws Throwable {
		runResourceTestCase("InstanceOf1");
	}
	
	public void testObjectMethods() throws Throwable {
		runResourceTestCase("ObjectMethods1");
	}
	
	public void testObjectMethodsWithInheritance() throws Throwable {
		runResourceTestCase("ObjectMethods2");
	}
	
	public void testObjectMethodsInvocation() throws Throwable {
		runResourceTestCase("ObjectMethods3");
	}
	
	public void testExceptionMapping() throws Throwable {
		runResourceTestCase("ExceptionMapping1");
	}
	
	public void testKeywordMapping() throws Throwable {
		runResourceTestCase("KeywordMapping1");
	}
	
	public void testClassLiterals() throws Throwable {
		runResourceTestCase("ClassLiterals1");
	}
	
	public void testSynchronizedBlock() throws Throwable {
		runResourceTestCase("SynchronizedBlock1");
	}
	
	public void testSynchronizedMethod() throws Throwable {
		runResourceTestCase("SynchronizedMethod1");
	}
	
	public void testSuperMethodInvocation() throws Throwable {
		runResourceTestCase("Super1");
	}
	
	public void testSuperConstructorInvocation() throws Throwable {
		runResourceTestCase("Super2");
	}
	
	public void testDefineEntryPointMethod() throws Throwable {
		runResourceTestCase("EntryPoint1");
	}
	
	public void testInvokeEntryPointMethod() throws Throwable {
		runResourceTestCase("EntryPoint2");
	}
	
	public void testFieldMapping() throws Throwable {
	    runResourceTestCase("FieldMapping");
	}
}


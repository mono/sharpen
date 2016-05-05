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
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import sharpen.core.*;

public class UnclassifiedConversionTestCase extends AbstractConversionTestCase {
	
	@Test
	public void testHeader() throws Exception {
		Configuration configuration = getConfiguration();
		configuration.setHeader(ResourceUtility.getStringContents("resources/header.txt"));
		
		runResourceTestCase(configuration, "HeaderSupport");
	}
	
	@Test
	public void testVarArgs() throws Throwable {
		runResourceTestCase("VarArgs");
	}
	
	@Test
	public void testSharpenEnum() throws Throwable {
		runResourceTestCase("Enum1");
	}
	
	@Test
	public void testObjectMethodsThroughGenericInterface() throws Throwable {
		runResourceTestCase("ObjectMethods4");
	}
	
	@Test
	public void testEnhancedFor() throws Throwable {
		runResourceTestCase("ForEach1");
	}
	
	@Test
	public void testEnhancedForOverGenerics() throws Throwable {
		runResourceTestCase("ForEach2");
	}
	
	@Test
	public void testStruct() throws Throwable {
		runResourceTestCase("structs/Struct1");
	}
	
	@Test
	public void testStaticBlocks() throws Throwable {
		runResourceTestCase("StaticBlocks1");
	}
	
	@Test
	public void testStringEmpty() throws Throwable {
		runResourceTestCase("StringEmpty");
	}
	
	@Test
	public void testEmptyCatch() throws Throwable {
		runResourceTestCase("Catch1");
	}
	
	@Test
	public void testExternalMethodMapping() throws Throwable {
		String jar = JarUtilities.createJar(BindingTestCaseSubject.class, BindingTestCaseSubject.Foo.class, BindingTestCaseSubject.Baz.class);
		List<String> classJar = new ArrayList<String>();
		classJar.add(jar);
		_project.setclassPath(classJar);
		
		Configuration conf = newPascalCaseIdentifiersConfiguration();
		conf.mapMethod(BindingTestCaseSubject.Foo.class.getCanonicalName() + ".bar", "Mapped");
		runResourceTestCase(conf, "ExternalMethodMapping");
	}
	
	@Test
	public void testMethodNameMappingInHierarchy() throws Throwable {
		Configuration conf = newPascalCaseIdentifiersConfiguration(); 
		conf.mapMethod("Foo.mappedToBar", "Bar");
		runResourceTestCase(conf, "MethodNameMappingInHierarchy1");
	}
	
	@Test
	public void testMethodNameMappingInHierarchy2() throws Throwable {
		Configuration conf = newPascalCaseIdentifiersConfiguration();
		conf.mapMethod("Foo.mappedToBar", "Bar");
		runResourceTestCase(conf, "MethodNameMappingInHierarchy2");
	}
	
	@Test
	public void testMappingByMethodSignature() throws Throwable {
		runResourceTestCase("SignatureMapping");
	}
	
	@Test
	public void testIntLiterals() throws Throwable {
		runResourceTestCase("IntLiterals1");
	}
	
	@Test
	public void testUnsignedRightShift() throws Throwable {
		runResourceTestCase("UnsignedRightShift");
	}
	
	@Test
	public void testSimpleNestedInterface() throws Throwable {
		runResourceTestCase("NestedInterface1");
	}
	
	@Test
	public void testPartial() throws Throwable {
		runResourceTestCase("Partial");
	}
	
	@Test
	public void testExtends() throws Throwable {
		runResourceTestCase("Extends");
	}
	
	@Test
	public void testContinue() throws Throwable {
		runResourceTestCase("Continue1");
	}
	
	@Test
	public void testTransientField() throws Throwable {
		runResourceTestCase("Transient1");
	}
	
	@Test
	public void testWrapperTypesMethods() throws Throwable {
		runResourceTestCase("WrapperTypesMethods1");
	}
	
	@Test
	public void testDeadBranchElimination() throws Throwable {
		runResourceTestCase("DeadBranchElimination1");
	}
	
	@Test
	public void testStaticFinalField() throws Throwable {
		runResourceTestCase("StaticFinalField1");
	}

	@Test
	public void testStringMethodsInvocation() throws Throwable {
		runResourceTestCase("StringMethods1");
	}	
	
	@Test
	public void testStandardConstants() throws Throwable {
		runResourceTestCase("StandardConstants1");
	}
	
	@Test
	public void testCharLiteral() throws Throwable {
		runResourceTestCase("CharLiteral1");
	}
	
	@Test
	public void testForWithAssignment() throws Throwable {
		runResourceTestCase("For2");
	}
	
	@Test
	public void testGetClass() throws Throwable {
		runResourceTestCase("GetClass1");
	}
	
	@Test
	public void testJavaArray() throws Throwable {
		runResourceTestCase("JavaArray1");
	}
	
	@Test
	public void testPrintStackTrace() throws Throwable {
		runResourceTestCase("PrintStackTrace1");
	}
	
	@Test
	public void testWaitNotify() throws Throwable {
		runResourceTestCase("WaitNotify1");
	}
	
	@Test
	public void testClone() throws Throwable {
		runResourceTestCase("Clone1");
	}
	
	@Test
	public void testJavaLangSystemToJavaSystem() throws Throwable {
		runResourceTestCase("JavaSystem1");
	}
	
	@Test
	public void testFinalizeMethod() throws Throwable {
		runResourceTestCase("Finalize1");
	}
	
	@Test
	public void testPostfixExpressions() throws Throwable {
		runResourceTestCase("PostfixExpressions1");
	}
	
	@Test
	public void testInterfaceInheritance() throws Throwable {
		runResourceTestCase("InterfaceInheritance1");
	}
	
	@Test
	public void testSwitchCase() throws Throwable {
		runResourceTestCase("SwitchCase1");
	}

	@Test
	public void testCascadingSwitchCase() throws Throwable {
		runResourceTestCase("SwitchCase2");
	}

	@Test
	public void testSwitchCaseDefaultThrows() throws Throwable {
		runResourceTestCase("SwitchCaseDefaultThrows");
	}

	@Test
	public void testFinalClass() throws Throwable {
		runResourceTestCase("FinalClass1");
	}
	
	@Test
	public void testAbstractClass() throws Throwable {
		runResourceTestCase("AbstractClass1");
	}
	
	@Test
	public void testInterfaceImplementationProvidedBySuperclass() throws Throwable {
		runResourceTestCase("InterfaceImplementation7");
	}
	
	@Test
	public void testAbstractInterfaceHierarchyImpl() throws Throwable {
		runResourceTestCase("InterfaceImplementation6");
	}
	
	@Test
	public void testInterfaceImplWithFinalMethod() throws Throwable {
		runResourceTestCase("InterfaceImplementation5");
	}
	
	@Test
	public void testInheritedAbstractInterfaceImpl() throws Throwable {
		runResourceTestCase("InterfaceImplementation4");
	}
	
	@Test
	public void testAbstractInterfaceImplementation() throws Throwable {
		runResourceTestCase("InterfaceImplementation3");
	}
	
	@Test
	public void testSerializableSimpleClass() throws Throwable {
		runResourceTestCase("Serializable1");
	}
	
	@Test
	public void testSerializableInterface() throws Throwable {
		runResourceTestCase("Serializable2");
	}
	
	@Test
	public void testSerializableClassExtendSerializableInterface() throws Throwable {
		runResourceTestCase("Serializable3");
	}
	
	@Test
	public void testDoWhile() throws Throwable {
		runResourceTestCase("DoWhile1");
	}
	
	@Test
	public void testSimpleFinalMethod() throws Throwable {
		runResourceTestCase("FinalMethod1");
	}
	
	@Test
	public void testExtendedFinalMethod() throws Throwable {
		runResourceTestCase("FinalMethod2");
	}
	
	@Test
	public void testJavaLang() throws Throwable {
		runResourceTestCase("JavaLang1");
	}
	
	@Test
	public void testBaseTypeQualifiedName() throws Throwable {
		runResourceTestCase("mp/BaseType1");
	}
	
	@Test
	public void testTernaryOperator() throws Throwable {
		runResourceTestCase("TernaryOperator1");
	}
	
	@Test
	public void testLongLine() throws Throwable {
		runResourceTestCase("LongLine1");
	}

	@Test
	public void testEmptyClassEmptyPackage() throws Throwable {
		runResourceTestCase("EmptyClass");
	}

	@Test
	public void testEmptyClassWithPackage() throws Throwable {
		runResourceTestCase("mp/Albatross");
	}

	@Test
	public void testSimpleFields() throws Throwable {
		runResourceTestCase("Fields1");
	}
	@Test
	public void testFieldReferences() throws Throwable {
		runResourceTestCase("Fields2");
	}
	
	@Test
	public void testSimpleMemberReference() throws Throwable {
		runResourceTestCase("MemberRef1");
	}

	@Test
	public void testReturnInteger() throws Throwable {
		runResourceTestCase("Return1");
	}

	@Test
	public void testReturnField() throws Throwable {
		runResourceTestCase("Return2");
	}
	
	@Test
	public void testSimpleInterface() throws Throwable {
		runResourceTestCase("Interface1");
	}
	
	@Test
	public void testWhile() throws Throwable {
		runResourceTestCase("While1");
	}
	
	@Test
	public void testIf() throws Throwable {
		runResourceTestCase("If1");
	}
	
	@Test
	public void testIfElse() throws Throwable {
		runResourceTestCase("If2");
	}
	
	@Test
	public void testFor() throws Throwable {
		runResourceTestCase("For1");
	}
	
	@Test
	public void testSimpleConstString() throws Throwable {
		runResourceTestCase("ConstString1");
	}
	
	@Test
	public void testConstStringWithEscapedChars() throws Throwable {
		runResourceTestCase("ConstString2");
	}
	
	@Test
	public void testCastExpression() throws Throwable {
		runResourceTestCase("Cast1");
	}
	
	@Test
	public void testClassCastExceptionMapping() throws Throwable {
		runResourceTestCase("Cast2");
	}
	
	@Test
	public void testTryCatch() throws Throwable {
		runResourceTestCase("Try1");
	}
	
	@Test
	public void testTryFinally() throws Throwable {
		runResourceTestCase("Try2");
	}
	
	@Test
	public void testThrow() throws Throwable {
		runResourceTestCase("Throw1");
	}
	
	@Test
	public void testBinaryExpressions() throws Throwable {
		runResourceTestCase("BinaryExpressions1");
	}
	
	@Test
	public void testInstanceOf() throws Throwable {
		runResourceTestCase("InstanceOf1");
	}
	
	@Test
	public void testObjectMethods() throws Throwable {
		runResourceTestCase("ObjectMethods1");
	}
	
	@Test
	public void testObjectMethodsWithInheritance() throws Throwable {
		runResourceTestCase("ObjectMethods2");
	}
	
	@Test
	public void testObjectMethodsInvocation() throws Throwable {
		runResourceTestCase("ObjectMethods3");
	}
	
	@Test
	public void testExceptionMapping() throws Throwable {
		runResourceTestCase("ExceptionMapping1");
	}
	
	@Test
	public void testKeywordMapping() throws Throwable {
		runResourceTestCase("KeywordMapping1");
	}
	
	@Test
	public void testClassLiterals() throws Throwable {
		runResourceTestCase("ClassLiterals1");
	}
	
	@Test
	public void testSynchronizedBlock() throws Throwable {
		runResourceTestCase("SynchronizedBlock1");
	}
	
	@Test
	public void testSynchronizedMethod() throws Throwable {
		runResourceTestCase("SynchronizedMethod1");
	}
	
	@Test
	public void testSuperMethodInvocation() throws Throwable {
		runResourceTestCase("Super1");
	}
	
	@Test
	public void testSuperConstructorInvocation() throws Throwable {
		runResourceTestCase("Super2");
	}
	
	@Test
	public void testDefineEntryPointMethod() throws Throwable {
		runResourceTestCase("EntryPoint1");
	}
	
	@Test
	public void testInvokeEntryPointMethod() throws Throwable {
		runResourceTestCase("EntryPoint2");
	}
	
	@Test
	public void testFieldMapping() throws Throwable {
	    runResourceTestCase("FieldMapping");
	}
}


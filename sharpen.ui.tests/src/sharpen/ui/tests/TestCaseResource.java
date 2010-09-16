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

import java.io.*;

import junit.framework.Assert;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import sharpen.util.*;


/**
 * A test case resource is composed of two files: a java source file (.java.txt extension) and
 * its expected output ({@link #expectedPathSuffix()} extension). 
 * 
 */
public class TestCaseResource {
	
	private final String _originalPath;
	private final String _packageName;
	private final String _simpleName;
	private final String _expectedPath;	

	/**
	 * Create a new test case resource. 
	 * 
	 * @param path relative path to the resource from the root of the project, ex.: EmptyClass, com/db4o/Test1.
	 */
	public TestCaseResource(String originalPath, String expectedPath) {
		
		String parts[] = originalPath.split("/");
		
		_simpleName = parts[parts.length-1];
		_packageName = join(parts, parts.length-1, ".");
		_originalPath = originalPath;
		_expectedPath = expectedPath;
	}
	
	public TestCaseResource(String path) {
		this(path, path);
	}
	
	public void assertExpectedContent(String actualContents) throws IOException {

		StringAssert.assertEqualLines(expectedStringContents(), actualContents);
	}
	
	public String javaFileName() {
		return _simpleName + ".java";
	}
	
	public String actualStringContents() throws IOException {
		return ResourceUtility.getStringContents(_originalPath + actualPathSuffix(), getClass());
	}

	protected String actualPathSuffix() {
		return ".java.txt";
	}
	
	public String expectedStringContents() throws IOException {
		return ResourceUtility.getStringContents(_expectedPath + expectedPathSuffix(), getClass());
	}

	protected String expectedPathSuffix() {
		return ".cs.txt";
	}
	
	public String targetSimpleName() {	
		return _simpleName;
	}
	
	String join(String parts[], int count, String separator) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i<count; ++i) {
			if (i > 0) {
				buffer.append(separator);
			}
			buffer.append(parts[i]);
		}
		return buffer.toString();
	}

	/**
	 * @return
	 */
	public String packageName() {
		return _packageName;
	}

	public void assertFile(IFile actualFile) throws IOException, CoreException {
		if (expectedStringContents().length() == 0) {
			Assert.assertFalse("No content in expected file: "  + actualFile, actualFile.exists());
			return;
		}
		Assert.assertTrue("Expected file: " + actualFile, actualFile.exists());
		StringAssert.assertEqualLines(expectedStringContents(), fileContents(actualFile));
	}
	
	private static String fileContents(IFile file) throws CoreException, IOException {
		InputStream stream = file.getContents();
		try {
			return InputStreamUtility.readString(stream, file.getCharset());
		} finally {
			stream.close();
		}
	}

	public boolean isSupportingLibrary() {
		return false;
	}
}

package sharpen.ui.tests;

import java.io.*;

import junit.framework.Assert;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import wheel.io.*;


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
		return getSimpleName() + ".java";
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
	
	public String getSimpleName() {	
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
}

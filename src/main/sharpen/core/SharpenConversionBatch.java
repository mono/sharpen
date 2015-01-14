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

import java.io.*;

import sharpen.core.csharp.ast.CSCompilationUnit;
import sharpen.core.framework.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

/**
 * Converts a set of java source files to c#.
 * 
 * The c# files are created relative to the targetFolder directory. If no
 * targetFolder is specified the files will be created side by side with the
 * corresponding java source files.
 *  
 */
public class SharpenConversionBatch extends ConversionBatch { 
	
	private String _targetProjectPath;

	private final Configuration _configuration;	

	public SharpenConversionBatch() {
		this(Sharpen.getDefault().configuration());
	}
	
	public SharpenConversionBatch(Configuration configuration) {
		if (null == configuration) {
			throw new IllegalArgumentException("configuration");
		}
		_configuration = configuration;
	}
	
	public void setTargetProject(String projectPath) {
		File fprojectPath = new File(projectPath);
		if (fprojectPath.isDirectory() && !fprojectPath.exists())
		{
			fprojectPath.mkdir();
		}
		_targetProjectPath = projectPath;
	}

	//@Override
	protected void convertCompilationUnit(ASTResolver resolver, String source, CompilationUnit ast)
			throws CoreException, IOException {
		SharpenConversion converter = new SharpenConversion(_configuration);
		final StringWriter writer = new StringWriter();
		converter.setTargetWriter(writer);
		converter.setSource(source);
		converter.setASTResolver(resolver);
		CSCompilationUnit result = converter.run(ast);
		if (writer.getBuffer().length() > 0) {
			saveConvertedFile(source, result, writer);
		}
	}
	/**
	 * @param cu
	 * @throws JavaModelException
	 * @throws CoreException
	 * @throws IOException 
	 */
	private void saveConvertedFile(String cu, CSCompilationUnit csModule, StringWriter convertedContents) throws  IOException, CoreException {
		String newName = csModule.elementName();
		if (newName == null) {
			newName = getNameWithoutExtension(cu) + ".cs";
		}
		String folder = targetFolderForCompilationUnit(cu, csModule.namespace());
		ensureFolder(folder);
		newName = folder + "/" + newName;
		File fnewName = new File(newName);
		fnewName.createNewFile();
        FileWriter fw = new FileWriter(fnewName);
        fw.write(convertedContents.getBuffer().toString());
        fw.close();
	}

	private void ensureFolder(String folder) {
		File ffolder = new File(folder);
		ffolder.mkdirs();
    }

	String targetFolderForCompilationUnit(String cu, String generatedNamespace)
			throws CoreException {

		if (null == _targetProjectPath) {
			throw new IllegalArgumentException("_targetProjectPath");
		}
		
		// compute target folder based on packageName
		String targetFolder = _targetProjectPath;
		
		String cuParent = new File(cu).getParent().replace("\\", "/");
		String packageName = generatedNamespace == null
			? cuParent.substring(cuParent.lastIndexOf("/"))
			: cleanupNamespace(generatedNamespace);
		if (packageName.length() > 0) {
			return getTargetPackageFolder(targetFolder, packageName);
		}
		return targetFolder;
	}

	public static String cleanupNamespace(String generatedNamespace) {
		// remove any keyword markers from the namespace 
		return generatedNamespace.replace("@", "");
	}

	private String getTargetPackageFolder(String targetFolder, String packageName) {
		 targetFolder = targetFolder  +"/"  + packageName.replace('.', '/').toLowerCase();
		 return targetFolder;
	}
	
	private String getNameWithoutExtension(String name) {
		File f = new File(name);
		String filename = f.getName();
		return filename.substring(0,filename.lastIndexOf("."));
	}

	public Configuration getConfiguration() {
		return _configuration;
	}
}
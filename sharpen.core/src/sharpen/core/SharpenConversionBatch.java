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
import sharpen.core.framework.resources.WorkspaceUtilities;

import org.eclipse.core.resources.*;
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
	
	private IProject _targetProject;

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
	
	public void setTargetProject(IProject project) {
		_targetProject = project;
	}

	@Override
	protected void convertCompilationUnit(ASTResolver resolver, ICompilationUnit source, CompilationUnit ast)
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
	 * @throws UnsupportedEncodingException
	 */
	private void saveConvertedFile(ICompilationUnit cu, CSCompilationUnit csModule, StringWriter convertedContents) throws JavaModelException, CoreException, UnsupportedEncodingException {
		String newName = csModule.elementName();
		if (newName == null) {
			newName = getNameWithoutExtension(cu.getElementName()) + ".cs";
		}

		IFolder folder = targetFolderForCompilationUnit(cu, csModule.namespace());
		ensureFolder(folder);
		WorkspaceUtilities.writeText(folder.getFile(newName), convertedContents.getBuffer().toString());
	}

	private void ensureFolder(IFolder folder) throws CoreException {
	    WorkspaceUtilities.initializeTree(folder, null);
    }

	IFolder targetFolderForCompilationUnit(ICompilationUnit cu, String generatedNamespace)
			throws CoreException {

		if (null == _targetProject) {
			// no target folder specified
			// converted files go in the same folder as their corresponding
			// java source files
			return (IFolder) cu.getCorrespondingResource().getParent();
		}
		
		final IPath basePath = cu.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT).getResource().getProjectRelativePath();
		final IFolder targetFolder = _targetProject.getFolder(basePath);

		// compute target folder based on packageName
		String packageName = generatedNamespace == null
			? cu.getParent().getElementName()
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

	private IFolder getTargetPackageFolder(IFolder targetFolder, String packageName)
			throws CoreException {
		return targetFolder.getFolder(packageName.replace('.', '/'));
	}
	
	private String getNameWithoutExtension(String name) {
		return name.split("\\.")[0];
	}

	public Configuration getConfiguration() {
		return _configuration;
	}
}
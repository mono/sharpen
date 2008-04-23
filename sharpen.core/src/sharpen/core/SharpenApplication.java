/* Copyright (C) 2004 - 2008  db4objects Inc.  http://www.db4o.com

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
import java.util.*;

import sharpen.core.io.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

/**
 * Start this application with: <code>
 * java -cp startup.jar org.eclipse.core.launcher.Main -application sharpen.core.application resourcePath
 * </code>
 */
public class SharpenApplication implements IPlatformRunnable {

	private static class ConsoleProgressMonitor extends NullProgressMonitor {
		public void subTask(String name) {
			System.out.println(name);
		}
	}

	private SharpenCommandLine _args;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(final Object arg) throws Exception {
		try {
			_args = SharpenCommandLine.parse((String[]) arg);
			safeRun();
		} catch (Exception x) {
			System.err.println("ERROR: " + x.getMessage());
			x.printStackTrace();
			throw x;
		}
		return IPlatformRunnable.EXIT_OK;
	}

	void safeRun() throws Exception {

		long startTime = System.currentTimeMillis();
		try {
			convert();
		} finally {
			System.out.println(
					"Conversion finished in " + (System.currentTimeMillis()-startTime) + "ms.");
		}
	}

	private void convert() throws CoreException, IOException, InterruptedException {
		JavaProject project = setUpJavaProject();
		
		List<ICompilationUnit> units = sortedByName(project.getAllCompilationUnits());
		
		convertTo(units, resetTargetFolder(project));
	}

	private IFolder resetTargetFolder(JavaProject project) throws CoreException {
		deleteTargetProject(project);
		IFolder targetFolder = createTargetProjectAndFolder(project);
		return targetFolder;
	}

	private void convertTo(List<ICompilationUnit> units, IFolder targetFolder)
			throws IOException, CoreException, InterruptedException {
		BatchConverter converter = new BatchConverter(getConfiguration());		
		converter.setProgressMonitor(getProgressMonitor());
		converter.setTargetFolder(targetFolder);
		converter.setSource(units);
		converter.run();
	}

	private IFolder createTargetProjectAndFolder(JavaProject project)
			throws CoreException {
		return JavaModelUtility
				.getTargetProjectAndFolder(project.getJavaProject(), null);
	}

	private void deleteTargetProject(JavaProject project) throws CoreException {
		JavaModelUtility.deleteTargetProject(project.getJavaProject());
	}

	private Configuration getConfiguration() throws IOException {
		final Configuration configuration = new Configuration(_args.runtimeTypeName);
		
		ods("Pascal case mode: " + _args.pascalCase);
		configuration.setNamingStrategy(_args.pascalCase.getNamingStrategy());
		if (_args.nativeTypeSystem) {
			ods("Native type system mode on.");
			configuration.enableNativeTypeSystem();
		}
		if (_args.nativeInterfaces) {
			ods("Native interfaces mode on.");
			configuration.enableNativeInterfaces();
		}
		if (_args.organizeUsings) {
			ods("Organize usings mode on.");
			configuration.enableOrganizeUsings();
		}
		if (_args.headerFile != null) {
			ods("Header file: " + _args.headerFile);
			configuration.setHeader(IO.readFile(new File(_args.headerFile)));
		}
		if (_args.xmldoc != null) {
			ods("Xml documentation: " + _args.xmldoc);
			configuration.setDocumentationOverlay(new XmlDocumentationOverlay(_args.xmldoc));
		}
		configuration.mapNamespaces(_args.namespaceMappings);
		configuration.mapMembers(_args.memberMappings);
		
		for (String fullyQualifiedType : _args.fullyQualifiedTypes) {
			configuration.addFullyQualifiedTypeName(fullyQualifiedType);
		}
		
		for (Configuration.NameMapping mapping : _args.typeMappings) {
			configuration.mapType(mapping.from, mapping.to);
		}
		return configuration;
	}

	private List<ICompilationUnit> sortedByName(List<ICompilationUnit> units) {
		Collections.sort(units, new Comparator<ICompilationUnit>() {
			public int compare(ICompilationUnit o1, ICompilationUnit o2) {
				return o1.getElementName().compareTo(o2.getElementName());
			}
		});
		return units;
	}

	private IProgressMonitor getProgressMonitor() {
		return new ConsoleProgressMonitor();
	}

	JavaProject setUpJavaProject() throws CoreException {
		ods("project: " + _args.project);
		JavaProject project = new JavaProject(_args.project);
		initializeClassPath(project);
		initializeSourceFolders(project);
		return project;
	}

	private void initializeSourceFolders(JavaProject project) throws CoreException {
		for (String srcFolder : _args.sourceFolders) {
			ods("source folder: " + srcFolder);
			project.addSourceFolder(srcFolder);
		}
	}

	private void initializeClassPath(JavaProject project) throws JavaModelException {
		for (String cp : _args.classpath) {
			ods("classpath entry: " + cp);
			if (!new File(cp).exists()) throw new IllegalArgumentException("'" + cp + "' not found.");
			project.addClasspathEntry(cp);
		}
	}
	
	private void ods(String message) {
		System.out.println(message);
	}	
}
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
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.app.*;
import org.eclipse.jdt.core.*;

import sharpen.core.framework.*;
import sharpen.core.io.*;

/**
 * Start this application with: <code>
 * java -cp startup.jar org.eclipse.core.launcher.Main -application sharpen.core.application resourcePath
 * </code>
 */
public class SharpenApplication implements IApplication {

	private SharpenCommandLine _args;

	public Object start(IApplicationContext context) throws Exception {
		try {
			String[] args = argv(context);
			_args = SharpenCommandLine.parse(args);
			System.err.println("Configuration Class: " + _args.configurationClass);
			Sharpen.getDefault().configuration(ConfigurationFactory.newConfiguration(_args.configurationClass, _args.runtimeTypeName));
			safeRun();
		} catch (Exception x) {
			System.err.println("ERROR: " + x.getMessage());
			x.printStackTrace();
			throw x;
		}
		return IApplication.EXIT_OK;
	}

	private String[] argv(IApplicationContext context) {
		return (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
	}
	
	public void stop() {
		
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

	private IProject resetTargetFolder(JavaProject project) throws CoreException {
		return deleteTargetProject(project);
	}

	private void convertTo(List<ICompilationUnit> units, IProject targetFolder)
			throws IOException, CoreException, InterruptedException {
		SharpenConversionBatch converter = new SharpenConversionBatch(getConfiguration());		
		converter.setProgressMonitor(newProgressMonitor());
		converter.setTargetProject(targetFolder);
		converter.setSource(units);
		converter.run();
	}

	private IProject deleteTargetProject(JavaProject project) throws CoreException {
		return JavaModelUtility.deleteTargetProject(project.getJavaProject());
	}

	private Configuration getConfiguration() throws IOException {
		final Configuration configuration = Sharpen.getDefault().configuration();
		
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
		configuration.mapEventAdds(_args.eventAddMappings);
		configuration.mapEvents(_args.eventMappings);
		configuration.mapNamespaces(_args.namespaceMappings);
		configuration.mapMembers(_args.memberMappings);
		configuration.conditionalCompilation(_args.conditionalCompilation);
		
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

	private IProgressMonitor newProgressMonitor() {
		return new ConsoleProgressMonitor();
	}

	JavaProject setUpJavaProject() throws CoreException {
		ods("project: " + _args.project);
		return new JavaProject.Builder(newProgressMonitor(), _args.project)
			.classpath(_args.classpath)
			.sourceFolders(_args.sourceFolders)
			.project;
	}
	
	private static void ods(String message) {
		System.out.println(message);
	}	
}
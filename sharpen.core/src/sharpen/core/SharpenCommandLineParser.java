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

/* Copyright (C) 2004 - 2006 Versant Inc. http://www.db4o.com */

package sharpen.core;

import sharpen.core.framework.*;
import sharpen.core.io.IO;

class SharpenCommandLineParser extends CommandLineParser {
	
	private final SharpenCommandLine _cmdLine;
	
	public SharpenCommandLineParser(String[] args) {
		this(args, new SharpenCommandLine());
	}
	
	private SharpenCommandLineParser(String[] args, SharpenCommandLine cmdLine) {
		super(args);		
		_cmdLine = cmdLine;
		parse();
	}

	@Override
	protected void processResponseFile(String arg) {
		new SharpenCommandLineParser(
				IO.linesFromFile(arg.substring(1)),
				_cmdLine);
	}

	@Override
	protected void validate() {
		if((_cmdLine.project == null) && (_cmdLine.help == false)) {
			System.out.println("Error:unspecified source folder. Please check help.");
			_cmdLine.help =true;
		}
	}

	@Override
	protected void processArgument(String arg) {
		//Making compatible both for Unix & Window based directory structure
		arg = arg.replace("\\", "/");
		if (_cmdLine.project != null) {
			illegalArgument(arg);
		}
		
		if (arg.indexOf('/') > -1) {
			//String projectName = arg.split("/")[0];
			//String srcFolder = arg.substring(projectName.length() + 1);
			
			String srcFolder = arg.substring(arg.lastIndexOf("/")+1);
			String projectPath =arg.substring(0,arg.lastIndexOf("/"));
			String projectName = projectPath.substring(projectPath.lastIndexOf("/")+1);
			
			_cmdLine.project = projectName;
			_cmdLine.projectPath = projectPath;
			_cmdLine.sourceFolders.add(srcFolder);
		} else {
			_cmdLine.project = arg;
		}
	}

	@Override
	protected void processOption(String arg) {
		if (areEqual(arg, "-pascalCase")) {
			_cmdLine.pascalCase = SharpenCommandLine.PascalCaseOptions.Identifiers;
		} else if (areEqual(arg, "-pascalCase+")) {
			_cmdLine.pascalCase = SharpenCommandLine.PascalCaseOptions.NamespaceAndIdentifiers;			 
		} else if (areEqual(arg, "-cp")) {
			_cmdLine.classpath.add(consumeNext());
		} else if (areEqual(arg, "-srcFolder")) {
			_cmdLine.sourceFolders.add(consumeNext());
		} else if (areEqual(arg, "-paramCountFileNames")) {
			_cmdLine.paramCountFileNames = true;
		} else if (areEqual(arg, "-nativeTypeSystem")) {
			_cmdLine.nativeTypeSystem = true;
		} else if (areEqual(arg, "-indentWithSpaces")) {
			_cmdLine.indentWithSpaces = true;
		} else if (areEqual(arg, "-indentSize")) {
			_cmdLine.indentSize = Integer.parseInt(consumeNext());
		} else if (areEqual(arg, "-maxColumns")) {
			_cmdLine.maxColumns = Integer.parseInt(consumeNext());
		} else if (areEqual(arg, "-nativeInterfaces")) {
			_cmdLine.nativeInterfaces = true;
		} else if (areEqual(arg, "-separateInterfaceConstants")) {
			_cmdLine.separateInterfaceConstants = true;
		} else if (areEqual(arg, "-organizeUsings")) {
			_cmdLine.organizeUsings = true;
		} else if (areEqual(arg, "-continueOnError")) {
			_cmdLine.continueOnError = true;
		} else if (areEqual(arg, "-fullyQualify")) {
			_cmdLine.fullyQualifiedTypes.add(consumeNext());
		} else if (areEqual(arg, "-namespaceMapping")) {
			_cmdLine.namespaceMappings.add(consumeNameMapping());
		} else if (areEqual(arg, "-methodMapping")) {
			String from = consumeNext();
			String to = consumeNext();
			_cmdLine.memberMappings.put(from, new Configuration.MemberMapping(to, MemberKind.Method));
		} else if (areEqual(arg, "-typeMapping")) {
			_cmdLine.typeMappings.add(consumeNameMapping());
		} else if (areEqual(arg, "-propertyMapping")) {
			String from = consumeNext();
			String to = consumeNext();
			_cmdLine.memberMappings.put(from, new Configuration.MemberMapping(to, MemberKind.Property));
		} else if (areEqual(arg, "-fieldMapping")) {
			String from = consumeNext();
			String to = consumeNext();
			_cmdLine.memberMappings.put(from, new Configuration.MemberMapping(to, MemberKind.Field));
		} else if (areEqual(arg, "-indexerMapping")) {
			String from = consumeNext();
			_cmdLine.memberMappings.put(from, new Configuration.MemberMapping(null, MemberKind.Indexer));
		} else if (areEqual(arg, "-makePartial")){
			_cmdLine.partialTypes.add (consumeNext());
		} else if (areEqual(arg, "-runtimeTypeName")){
			_cmdLine.runtimeTypeName = consumeNext();
		} else if (areEqual(arg, "-header")){
			_cmdLine.headerFile = consumeNext();
		} else if (areEqual(arg, "-xmldoc")){
			_cmdLine.xmldoc = consumeNext();
		} else if (areEqual(arg, "-eventMapping")){
			_cmdLine.eventMappings.add(consumeNameMapping());
		} else if (areEqual(arg, "-eventAddMapping")){
			_cmdLine.eventAddMappings.add(consumeNext());
		} else if (areEqual(arg, "-conditionalCompilation")) {
			_cmdLine.conditionalCompilation.put(consumeNext(), consumeNext());
		} else if (areEqual(arg, "-configurationClass")) {
			_cmdLine.configurationClass = consumeNext();		
		} else if (areEqual(arg, "-junitConversion")) {
			_cmdLine.junitConversion = true;		
		} else if (areEqual(arg, "-sharpenNamespace")) {
			_cmdLine.sharpenNamespace = consumeNext();	
		} else if (areEqual(arg, "-help")) {
			_cmdLine.help = true;		
		} else {
			_cmdLine.help = true;
		}
	}

	private Configuration.NameMapping consumeNameMapping() {
		final String from = consumeNext();
		final String to = consumeNext();
		final Configuration.NameMapping nameMapping = new Configuration.NameMapping(from, to);
		return nameMapping;
	}

	public SharpenCommandLine commandLine() {
		return _cmdLine;
	}

}

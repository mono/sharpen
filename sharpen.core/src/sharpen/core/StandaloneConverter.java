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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sharpen.core.csharp.ast.CSCompilationUnit;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class StandaloneConverter extends SharpenConversion {
	
	private ASTParser _parser;
	
	public StandaloneConverter(Configuration configuration) {
		super(configuration);
		_parser = ASTParser.newParser(AST.JLS4);
		_parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		@SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_7);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		_parser.setCompilerOptions(options);
	}
	
	public CSCompilationUnit run() throws IOException {
		if (null == _writer || null == _source) {
			throw new IllegalStateException("source and writer must be set");
		}
		File sourceFile = new File(_source);
		List<String> sourcefilePaths = new ArrayList<String>(); 
		sourcefilePaths.add(sourceFile.getPath());
		String []sourcefilearr = new String[sourcefilePaths.size()];
		sourcefilePaths.toArray(sourcefilearr);
		_parser.setEnvironment(null,sourcefilearr,
				null, true);
		_parser.setSource(ReadFileToCharArray(_source));
		_parser.setResolveBindings(true);
        CompilationUnit ast = (CompilationUnit) _parser.createAST(null);
        return run(ast);
	}
	
	private char[] ReadFileToCharArray(String filePath) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return  fileData.toString().toCharArray();	
	}
}

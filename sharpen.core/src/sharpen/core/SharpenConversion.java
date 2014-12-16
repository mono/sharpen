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

import java.io.IOException;
import java.io.Writer;
import sharpen.core.csharp.CSharpPrinter;
import sharpen.core.csharp.ast.CSCompilationUnit;
import sharpen.core.framework.*;
import org.eclipse.jdt.core.dom.*;

public class SharpenConversion {

	private CSharpPrinter _printer;
	protected String _source;
	protected Writer _writer;
	protected final Configuration _configuration;
	private ASTResolver _resolver = new ASTResolver() {
		public ASTNode findDeclaringNode(IBinding binding) {
			return null;
		}
	};

	public SharpenConversion(Configuration configuration) {
		_configuration = configuration;
	}

	public void setSource(String source) {
		_source = source;
	}

	public void setTargetWriter(Writer writer) {
		_writer = writer;
	}

	public Writer getTargetWriter() {
		return _writer;
	}

	public void setPrinter(CSharpPrinter printer) {
		_printer = printer;
	}

	private CSharpPrinter getPrinter() {
		if (null == _printer) {
			_printer = new CSharpPrinter();
		}
		return _printer;
	}

	public Configuration getConfiguration() {
		return _configuration;
	}

	protected void print(CSCompilationUnit unit) {
		printHeader();
		printTree(unit);
	}

	private void printHeader() {
		try {
			_writer.write(_configuration.header());
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	private void printTree(CSCompilationUnit unit) {
		CSharpPrinter printer = getPrinter();
		printer.setWriter(_writer, _configuration.getIndentString(), _configuration.getMaxColumns());
		printer.print(unit);
	}

	protected CSCompilationUnit run(final CompilationUnit ast) {
		processProblems(ast);
		prepareForConversion(ast);		
		CSCompilationUnit cs = convert(ast);
		if (!cs.ignore() && !cs.types().isEmpty()) {
			print(cs);
		}
		return cs;
	}

	protected void processProblems(CompilationUnit ast) {
		ASTUtility.checkForProblems(ast, !ignoringErrors());
	}

	private CSCompilationUnit convert(final CompilationUnit ast) {
		final CSCompilationUnit compilationUnit = new CSCompilationUnit();
		final Environment environment = Environments.newConventionBasedEnvironment(ast, _configuration, _resolver, compilationUnit);
		Environments.runWith(environment, new Runnable() { public void run() {
			CSharpBuilder builder = new CSharpBuilder();
			builder.run();
		}});
		
		return compilationUnit;
	}
	
	private boolean ignoringErrors() {
		return _configuration.getIgnoreErrors();
	}

	private void prepareForConversion(final CompilationUnit ast) {
		WarningHandler warningHandler = new WarningHandler() {
			public void warning(ASTNode node, String message) {
				System.err.println(getSourcePath() + "(" + ASTUtility.lineNumber(ast, node) + "): " + message);
			}
		};
		_configuration.setWarningHandler(warningHandler);
	}

	private String getSourcePath() {

		return _source.substring(0, _source.lastIndexOf("/")-1);
	}
	
	public ASTResolver getASTResolver() {
		return _resolver;
	}

	public void setASTResolver(ASTResolver resolver) {
		_resolver = resolver;
	}
}
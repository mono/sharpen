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
import java.util.HashMap;
import java.util.Map;

import sharpen.core.csharp.CSharpPrinter;
import sharpen.core.csharp.ast.CSCompilationUnit;
import sharpen.core.framework.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;

public class SharpenConversion {

	private CSharpPrinter _printer;
	protected ICompilationUnit _source;
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

	public void setSource(ICompilationUnit source) {
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
		printer.setWriter(_writer);
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
		deleteProblemMarkers();
		WarningHandler warningHandler = new WarningHandler() {
			public void warning(ASTNode node, String message) {
				createProblemMarker(ast, node, message);
				System.err.println(getSourcePath() + "(" + ASTUtility.lineNumber(ast, node) + "): " + message);
			}
		};
		_configuration.setWarningHandler(warningHandler);
	}

	private void deleteProblemMarkers() {
		if (createProblemMarkers()) {
			try {
				_source.getCorrespondingResource().deleteMarkers(Sharpen.PROBLEM_MARKER, false, IResource.DEPTH_ONE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void createProblemMarker(CompilationUnit ast, ASTNode node, String message) {
		if (!createProblemMarkers()) {
			return;
		}			
		try {
			IMarker marker = _source.getCorrespondingResource().createMarker(Sharpen.PROBLEM_MARKER);			
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put(IMarker.MESSAGE, message);
			attributes.put(IMarker.CHAR_START, new Integer(node.getStartPosition()));
			attributes.put(IMarker.CHAR_END, new Integer(node.getStartPosition() + node.getLength()));
			attributes.put(IMarker.TRANSIENT, Boolean.TRUE);
			attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			attributes.put(IMarker.LINE_NUMBER, ASTUtility.lineNumber(ast, node));			
			marker.setAttributes(attributes);			
		} catch (CoreException e) {			
			e.printStackTrace();
		}
	}

	private boolean createProblemMarkers() {
		return _configuration.createProblemMarkers();
	}

	private String getSourcePath() {
		try {
			return _source.getCorrespondingResource().getFullPath().toString();
		} catch (JavaModelException e) {			
			e.printStackTrace();
			return "";
		}
	}
	
	public ASTResolver getASTResolver() {
		return _resolver;
	}

	public void setASTResolver(ASTResolver resolver) {
		_resolver = resolver;
	}
}
package sharpen.core.framework;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public abstract class ConversionBatch {

	private ICompilationUnit[] _source;

	private IProgressMonitor _progressMonitor = new NullProgressMonitor();

	private final ASTParser _parser;

	public ConversionBatch() {
		_parser = ASTParser.newParser(AST.JLS3);
		_parser.setKind(ASTParser.K_COMPILATION_UNIT);

	}

	/**
	 * Defines the set of java source files to be converted.
	 * 
	 * @param source
	 *            iterator of ICompilationUnit instances
	 */
	public void setSource(ICompilationUnit... source) {
		if (null == source || 0 == source.length) {
			throw new IllegalArgumentException("source");
		}
		_source = source;
	}

	public void setSource(List<ICompilationUnit> source) {
		if (null == source || source.isEmpty()) {
			throw new IllegalArgumentException("source");
		}
		_source = source.toArray(new ICompilationUnit[source.size()]);
	}
	
	public void setProgressMonitor(IProgressMonitor monitor) {
		if (null == monitor) {
			throw new IllegalArgumentException("monitor");
		}
		_progressMonitor = monitor;
	}

	/**
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * @throws InterruptedException 
	 * @throws IllegalStateException
	 *             when source is not set
	 */
	public void run() throws CoreException, IOException {
	
		if (null == _source) {
			throw new IllegalStateException("source was not set");
		}
		
		final ArrayList<CompilationUnitPair> pairs = parseCompilationUnits();
		final ASTResolver resolver = new DefaultASTResolver(pairs);
		
		_progressMonitor.beginTask("converting", pairs.size());
		for (final CompilationUnitPair pair : pairs) {
			if (_progressMonitor.isCanceled()) return;
			convertPair(resolver, pair);
		}
	}

	private void convertPair(final ASTResolver resolver, final CompilationUnitPair pair) throws CoreException,
			IOException {
		try {
			_progressMonitor.subTask(pair.source.getElementName());
			convertCompilationUnit(resolver, pair.source, pair.ast);
		} finally {
			_progressMonitor.worked(1);
		}
	}
	
	protected abstract void convertCompilationUnit(ASTResolver resolver, ICompilationUnit source,
			CompilationUnit ast) throws CoreException, IOException;

	private ArrayList<CompilationUnitPair> parseCompilationUnits() {
		final ArrayList<CompilationUnitPair> pairs = new ArrayList<CompilationUnitPair>(_source.length);
		ASTRequestor requestor = new ASTRequestor() {
			@Override
			public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
				pairs.add(new CompilationUnitPair(source, ast));
			}
		};
		_parser.setProject(_source[0].getJavaProject());
		_parser.setResolveBindings(true);
		_parser.createASTs(_source, new String[0], requestor, _progressMonitor);
		return pairs;
	}

}
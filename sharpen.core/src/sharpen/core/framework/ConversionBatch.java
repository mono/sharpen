package sharpen.core.framework;

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public abstract class ConversionBatch {

	private String[] _sourceFiles;
	private String[] _sourcePathEntries;
	private String[] _classPathEntries;

	private IProgressMonitor _progressMonitor = new NullProgressMonitor();

	private final ASTParser _parser;
	private boolean _continueOnError;

	public ConversionBatch() {
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

	public boolean isContinueOnError() {
		return _continueOnError;
	}

	public void setContinueOnError(boolean continueOnError) {
		this._continueOnError = continueOnError;
	}

	/**
	 * Defines the set of java source files to be converted.
	 * 
	 * @param source
	 *            iterator of sourceFiles instances
	 */
	public void setsourceFiles(String... sourceFiles) {
		if (null == sourceFiles || 0 == sourceFiles.length) {
			throw new IllegalArgumentException("sourceFiles");
		}
		_sourceFiles = sourceFiles;
	}

	public void setsourceFiles(List<String> sourceFiles) {
		if (null == sourceFiles || sourceFiles.isEmpty()) {
			throw new IllegalArgumentException("sourceFiles");
		}
		_sourceFiles = sourceFiles.toArray(new String[sourceFiles.size()]);
	}
	
	/**
	 * Defines the set of java source files path to be converted.
	 * 
	 * @param source
	 *            iterator of sourcePathEntries instances
	 */
	public void setsourcePathEntries(String... sourcePathEntries) {
		if (null == sourcePathEntries || 0 == sourcePathEntries.length) {
			throw new IllegalArgumentException("sourcePathEntries");
		}
		_sourcePathEntries = sourcePathEntries;
	}

	public void setsourcePathEntries(List<String> sourcePathEntries) {
		if (null == sourcePathEntries || sourcePathEntries.isEmpty()) {
			throw new IllegalArgumentException("sourcePathEntries");
		}
		_sourcePathEntries= sourcePathEntries.toArray(new String[sourcePathEntries.size()]);
	}
	/**
	 * Defines the set of java executable files path to be converted.
	 * 
	 * @param source
	 *            iterator of classPathEntries instances
	 */
	public void setclassPathEntries(String... classPathEntries) {
		if (null == classPathEntries || 0 == classPathEntries.length) {
			throw new IllegalArgumentException("classPathEntries");
		}
		_classPathEntries = classPathEntries;
	}

	public void setclassPathEntries(List<String> classPathEntries) {
		if (null != classPathEntries || classPathEntries.isEmpty() ==false) {
			_classPathEntries= classPathEntries.toArray(new String[classPathEntries.size()]);
		}
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
	
		if (null == _sourceFiles) {
			throw new IllegalStateException("source was not set");
		}
		
		final ArrayList<CompilationUnitPair> pairs = parseCompilationUnits();
		final ASTResolver resolver = new DefaultASTResolver(pairs);
		
		_progressMonitor.beginTask("converting", pairs.size());
		for (final CompilationUnitPair pair : pairs) {
			if (_progressMonitor.isCanceled()) return;

			try {
				convertPair(resolver, pair);
			} catch (RuntimeException ex) {
				if (!isContinueOnError()) {
					throw ex;
				}

				if (ex instanceof IllegalArgumentException
					|| ex instanceof ClassCastException) {
					// we still want to notify the user about the problem
					ex.printStackTrace(System.err);
				} else {
					// not a recoverable exception
					throw ex;
				}
			}
		}
	}

	private void convertPair(final ASTResolver resolver, final CompilationUnitPair pair) throws CoreException,
			IOException {
		try {
			_progressMonitor.subTask(pair.source.replace("\\", "/"));
			convertCompilationUnit(resolver, pair.source.replace("\\", "/"), pair.ast);
		} finally {
			_progressMonitor.worked(1);
		}
	}
	
	protected abstract void convertCompilationUnit(ASTResolver resolver, String sourceFiles,
			CompilationUnit ast) throws CoreException, IOException;

	private ArrayList<CompilationUnitPair> parseCompilationUnits() {
		final ArrayList<CompilationUnitPair> pairs = new ArrayList<CompilationUnitPair>(_sourceFiles.length);
		FileASTRequestor  requestor = new FileASTRequestor () {
			@Override
			public void acceptAST(String  source, CompilationUnit ast) {
				pairs.add(new CompilationUnitPair(source, ast));
			}
		};
		_parser.setEnvironment(_classPathEntries, _sourcePathEntries, null, true);
		_parser.setResolveBindings(true);
		_parser.createASTs(_sourceFiles, null, new String[0], requestor, _progressMonitor);
		return pairs;
	}

}
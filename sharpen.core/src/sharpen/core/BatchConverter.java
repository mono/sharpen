package sharpen.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import sharpen.core.csharp.ast.CSCompilationUnit;

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
public class BatchConverter { 
	
	private ICompilationUnit[] _source;

	private IFolder _targetFolder;

	private IProgressMonitor _progressMonitor = new NullProgressMonitor();

	private ASTParser _parser;

	private final Configuration _configuration;	

	public BatchConverter() {
		this(new Configuration());
	}
	
	public BatchConverter(Configuration configuration) {
		if (null == configuration) {
			throw new IllegalArgumentException("configuration");
		}
		_configuration = configuration;
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

	public void setTargetFolder(IFolder folder) {
		_targetFolder = folder;
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

	private void convertPair(final ASTResolver resolver, final CompilationUnitPair pair) throws CoreException, IOException {
		try {
			_progressMonitor.subTask(pair.source.getElementName());
			convertCompilationUnit(resolver, pair.source, pair.ast);
		} finally {
			_progressMonitor.worked(1);
		}
	}

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

	private void convertCompilationUnit(ASTResolver resolver, ICompilationUnit source, CompilationUnit ast)
			throws CoreException, IOException {
		Converter converter = new Converter(_configuration);
		final StringWriter writer = new StringWriter();
		converter.setTargetWriter(writer);
		converter.setSource(source);
		converter.setASTResolver(resolver);
		CSCompilationUnit result = converter.run(ast);
		if (!result.ignore()) {
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

		IFolder folder = getTargetFolderForCompilationUnit(cu, csModule.namespace());
		IFile file = folder.getFile(newName);

		ByteArrayInputStream stream = new ByteArrayInputStream(convertedContents
				.getBuffer().toString().getBytes(file.getCharset()));
		if (file.exists()) {
			file.setContents(stream, true, false, null);
		} else {
			file.create(stream, true, null);
		}
	}

	IFolder getTargetFolderForCompilationUnit(ICompilationUnit cu, String generatedNamespace)
			throws CoreException {

		if (null == _targetFolder) {
			// no target folder specified
			// converted files go in the same folder as their corresponding
			// java source files
			return (IFolder) cu.getCorrespondingResource().getParent();
		}

		// compute target folder based on packageName
		String packageName = generatedNamespace == null
			? cu.getParent().getElementName()
			: cleanupNamespace(generatedNamespace);
		if (packageName.length() > 0) {
			return getTargetPackageFolder(packageName);
		}
		return _targetFolder;
	}

	public static String cleanupNamespace(String generatedNamespace) {
		// remove any keyword markers from the namespace 
		return generatedNamespace.replace("@", "");
	}

	private IFolder getTargetPackageFolder(String packageName)
			throws CoreException {
		String[] parts = packageName.split("\\.");
		IFolder folder = _targetFolder;
		synchronized(_targetFolder) {
			for (int i = 0; i < parts.length; ++i) {
				folder = folder.getFolder(parts[i]);
				if (!folder.exists()) {
					folder.create(false, true, null);
				}
			}
		}
		return folder;
	}
	
	private String getNameWithoutExtension(String name) {
		return name.split("\\.")[0];
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		if (null == monitor) {
			throw new IllegalArgumentException("monitor");
		}
		_progressMonitor = monitor;
	}

	public Configuration getConfiguration() {
		return _configuration;
	}
}
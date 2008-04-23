package sharpen.core;

import sharpen.core.csharp.ast.CSCompilationUnit;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * @author Rodrigo B. de Oliveira
 *
 */
public class StandaloneConverter extends Converter {
	
	private ASTParser _parser;
	
	public StandaloneConverter(Configuration configuration) {
		super(configuration);
		_parser = ASTParser.newParser(AST.JLS3);
	}
	
	public CSCompilationUnit run() {
		if (null == _writer || null == _source) {
			throw new IllegalStateException("source and writer must be set");
		}
		
		_parser.setSource(_source);
		_parser.setResolveBindings(true);
        
        CompilationUnit ast = (CompilationUnit) _parser.createAST(null);
        return run(ast);
	}
}

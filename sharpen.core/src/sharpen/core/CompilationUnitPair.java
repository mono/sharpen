package sharpen.core;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CompilationUnitPair {
	
	public CompilationUnitPair(ICompilationUnit source, CompilationUnit ast) {
		this.source = source;
		this.ast = ast;
	}
	
	public ICompilationUnit source;
	public CompilationUnit ast;
}

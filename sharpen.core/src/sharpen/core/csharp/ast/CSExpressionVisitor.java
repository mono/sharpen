package sharpen.core.csharp.ast;

public class CSExpressionVisitor extends CSVisitor {
	
	@Override
	public void visit(CSConstructorInvocationExpression node) {
		visit((CSMethodInvocationExpression)node);
	}
	
	@Override
	public void visit(CSMethodInvocationExpression node) {
		visitNode(node.expression());
		visitList(node.arguments());
	}

	private void visitNode(CSNode expression) {
		if (null == expression) return;
		expression.accept(this);
    }

}

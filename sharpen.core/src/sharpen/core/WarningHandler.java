/**
 * 
 */
package sharpen.core;

import org.eclipse.jdt.core.dom.ASTNode;

public class WarningHandler {
	public void warning(@SuppressWarnings("unused")
	ASTNode node, String message) {
		System.err.println("WARNING: " + message);
	}
}
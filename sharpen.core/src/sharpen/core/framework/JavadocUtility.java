package sharpen.core.framework;

import org.eclipse.jdt.core.dom.*;

public class JavadocUtility {

	public static TagElement getJavadocTag(BodyDeclaration node, String tagName) {
		Javadoc javadoc = node.getJavadoc();
		if (null != javadoc) {
			for (Object tag : javadoc.tags()) {
				TagElement element = (TagElement)tag;
				if (tagName.equals(element.getTagName())) {
					return element;
				}
			}
		}
		return null;
	}

	public static boolean containsJavadoc(BodyDeclaration node, final String tag) {
		return null != getJavadocTag(node, tag);
	}

}

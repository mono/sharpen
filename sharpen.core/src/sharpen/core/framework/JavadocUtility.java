package sharpen.core.framework;

import java.util.*;

import org.eclipse.jdt.core.dom.*;

public class JavadocUtility {

	public static TagElement getJavadocTag(BodyDeclaration node, String tagName) {
		final List<TagElement> found = getJavadocTags(node, tagName);
		return found.isEmpty() ? null : found.get(0);
	}

	public static boolean containsJavadoc(BodyDeclaration node, final String tag) {
		return null != getJavadocTag(node, tag);
	}

	public static List<TagElement> getJavadocTags(BodyDeclaration node, String tagName) {
		final Javadoc javadoc = node.getJavadoc();
		if (null == javadoc) {
			return Collections.emptyList();
		}
		
		final ArrayList<TagElement> found = new ArrayList<TagElement>();
		for (Object tag : javadoc.tags()) {
			TagElement element = (TagElement)tag;
			if (tagName.equals(element.getTagName())) {
				found.add(element);
			}
		}
		return found;
	}

	public static String textFragment(List fragments, final int index) {
		String text = ((TextElement)fragments.get(index)).getText();
		return text.trim();
	}

}

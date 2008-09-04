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
		final List<TagElement> tags = Types.cast(javadoc.tags());
		return collectTags(tags, tagName, new ArrayList<TagElement>());
	}

	public static ArrayList<TagElement> collectTags(final List<TagElement> tags, String tagName, final ArrayList<TagElement> accumulator) {
		for (TagElement element : tags) {
			if (tagName.equals(element.getTagName())) {
				accumulator.add(element);
			}
		}
		return accumulator;
	}

	public static String textFragment(List fragments, final int index) {
		String text = ((TextElement)fragments.get(index)).getText();
		return text.trim();
	}

}

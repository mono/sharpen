/* Copyright (C) 2006 db4objects Inc. http://www.db4o.com */

package sharpen.core;

/**
 * @exclude
 */
public class PascalCaseNamingStrategy extends PascalCaseIdentifiersNamingStrategy {
	
	public static final NamingStrategy DEFAULT = new PascalCaseNamingStrategy();
	
	@Override
	public String identifier(String name) {
		if (isAllUpper(name)) {
			return super.identifier(fromJavaConstantName(name));
		}
		return super.identifier(name);
	}

	private boolean isAllUpper(String name) {
		for (int i=0; i<name.length(); ++i) {
			char ch = name.charAt(i);
			if (Character.isLetter(ch) && Character.isLowerCase(ch)) {
				return false;
			}
		}
		return true;
	}

	private String fromJavaConstantName(String name) {
		final String[] parts = name.split("_");
		final StringBuilder buffer = new StringBuilder();
		for (String part : parts) {
			buffer.append(title(part));
		}
		return buffer.toString();
	}

	private String title(String name) {
		return name.length() > 1
			? name.substring(0, 1) + name.substring(1).toLowerCase()
			: name;
	}

	@Override
	protected String namespacePart(String part) {
		return part.length() < 3
			? part.toUpperCase()
			: toPascalCase(part);
	}

}

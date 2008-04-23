package sharpen.core;

import java.util.*;

public class NamingStrategy {
	
	public static final NamingStrategy DEFAULT = new NamingStrategy();
	
	static Map<String, String> _identifierMappings = new HashMap<String, String>();
	
	static {
		mapIdentifier("ref");
		mapIdentifier("out");
		mapIdentifier("as");
		mapIdentifier("is");
		mapIdentifier("in");
		mapIdentifier("object");
		mapIdentifier("string");
		mapIdentifier("bool");
		mapIdentifier("using");
		mapIdentifier("lock", "Lock");
		mapIdentifier("params");
		mapIdentifier("delegate", "delegate_");
		mapIdentifier("override");
		mapIdentifier("event");
		mapIdentifier("operator");
		mapIdentifier("internal");
	}
	
	private static void mapIdentifier(String value) {
		mapIdentifier(value, "@" + value);
	}
	
	private static void mapIdentifier(String from, String to) {
		_identifierMappings.put(from, to);
	}
	
	public String identifier(String name) {
		String mapped = _identifierMappings.get(name);
		return mapped != null ? mapped : name;
	}

	public String methodName(String name) {
		return namespacePart(name);
	}

	public String namespace(String name) {
		StringBuilder builder = new StringBuilder();
		for (String part : name.split("\\.")) {
			if (builder.length() > 0) builder.append('.');
			builder.append(namespacePart(part));
		}
		return builder.toString();
	}

	protected String namespacePart(String part) {
		return identifier(part);
	}
}

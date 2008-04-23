package sharpen.core;

public class NullDocumentationOverlay implements DocumentationOverlay {
	
	public static final DocumentationOverlay DEFAULT = new NullDocumentationOverlay();

	public String forType(String fullName) {
		return null;
	}

	public String forMember(String fullTypeName, String signature) {
		return null;
	}

}

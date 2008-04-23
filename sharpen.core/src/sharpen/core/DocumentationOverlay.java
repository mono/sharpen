package sharpen.core;

public interface DocumentationOverlay {
	
	String forType(String fullName);

	String forMember(String fullTypeName, String signature);

}

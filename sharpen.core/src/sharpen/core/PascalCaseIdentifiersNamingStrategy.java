package sharpen.core;


public class PascalCaseIdentifiersNamingStrategy extends NamingStrategy {
	
	public static final NamingStrategy DEFAULT = new PascalCaseIdentifiersNamingStrategy();
	
	public String methodName(String name) {
		int nameStart = name.lastIndexOf('.');
		return nameStart < 0
			? identifier(toPascalCase(name))
			: name.substring(0, nameStart+1) + toPascalCase(name.substring(nameStart+1)); 
	}

	protected String toPascalCase(String name) {
		return name.length() > 1
			? name.substring(0, 1).toUpperCase() + name.substring(1)
			: name.toUpperCase();
	}

}

package sharpen.core;

import java.util.*;

public class JavaToCSharpCommandLine {
	
	public static JavaToCSharpCommandLine parse(String[] args) {
		return new JavaToCSharpCommandLineParser(args).commandLine();
	}
	
	public static enum PascalCaseOptions {
		None {
			@Override
			public NamingStrategy getNamingStrategy() {
				return NamingStrategy.DEFAULT;
			}
		},
		Identifiers{
			@Override
			public NamingStrategy getNamingStrategy() {
				return PascalCaseIdentifiersNamingStrategy.DEFAULT;
			}
		},
		NamespaceAndIdentifiers{
			@Override
			public NamingStrategy getNamingStrategy() {
				return PascalCaseNamingStrategy.DEFAULT;
			}
		};

		public abstract NamingStrategy getNamingStrategy();
	}

	public String runtimeTypeName = Configuration.DEFAULT_RUNTIME_TYPE_NAME;
	public boolean nativeTypeSystem;
	public PascalCaseOptions pascalCase = PascalCaseOptions.None;
	public String project;
	final public List<String> classpath = new ArrayList<String>();
	final public List<String> sourceFolders = new ArrayList<String>();
	final public List<Configuration.NameMapping> namespaceMappings = new ArrayList<Configuration.NameMapping>();
	final public List<Configuration.NameMapping> typeMappings = new ArrayList<Configuration.NameMapping>();
	final public Map<String, Configuration.MemberMapping> memberMappings = new HashMap<String, Configuration.MemberMapping>();
	public boolean nativeInterfaces;
	public boolean organizeUsings;
	final public List<String> fullyQualifiedTypes = new ArrayList<String>();
	public String headerFile;
	public String xmldoc;
}

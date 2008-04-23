package sharpen.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Rodrigo B. de Oliveira
 */
public class JavaToCSharp extends Plugin {
	
	public static final String PLUGIN_ID = "sharpen.core";
	
	public static final String PROBLEM_MARKER = PLUGIN_ID + ".problem";
	
	//The shared instance.
	private static JavaToCSharp plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	/**
	 * The constructor.
	 */
	public JavaToCSharp() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("sharpen.JavaToCSharpPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static JavaToCSharp getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = JavaToCSharp.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}

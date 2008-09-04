package sharpen.util;


import java.io.*;


public class ResourceLoader {
	
	public static String getStringContents(final Class<?> anchor, String resourceName) throws IOException {
		InputStream stream = anchor.getResourceAsStream(resourceName);
		if (null == stream) ResourceLoader.resourceNotFound(resourceName);
		try {
			return InputStreamUtility.readString(stream);			
		} finally {
			stream.close();
		}
	}

	public static void resourceNotFound(String resourceName) {
		throw new IllegalArgumentException("Resource '" + resourceName + "' not found");
	}

}

/* Copyright (C) 2004 - 2006 db4objects Inc. http://www.db4o.com */

package sharpen.ui.tests;

import java.io.*;
import java.net.*;

import wheel.io.*;


public class ResourceUtility {
	
	public static String getStringContents(String resourceName) throws IOException {
		return getStringContents(resourceName, ResourceUtility.class);
	}

	public static String getStringContents(String resourceName, Class<?> relativeTo) throws IOException {
		return ResourceLoader.getStringContents(relativeTo, "/" + resourceName);
	}	

	public static String getResourceUri(String resourceName) {
		URL url = ResourceUtility.class.getResource("/" + resourceName);
		if (null == url) ResourceLoader.resourceNotFound(resourceName);
		try {
			return url.toURI().toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}

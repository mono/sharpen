/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */
package sharpen.core;

import org.eclipse.core.runtime.IProgressMonitor;
import sharpen.core.framework.NameUtility;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationFactory {

	public static final String DEFAULT_RUNTIME_TYPE_NAME = "Sharpen.Runtime";
	
	private ConfigurationFactory() {}
	
	public static Configuration defaultConfiguration() {
		return newConfiguration(null);
	}
	
	public static Configuration newConfiguration(String configurationClass) {
		return newConfiguration(configurationClass, DEFAULT_RUNTIME_TYPE_NAME);
	}
	
	public static Configuration newConfiguration(String configurationClass, String runtimeTypeName) {
		runtimeTypeName = evalRuntimeType(runtimeTypeName);

		if (configurationClass == null) {
			return new DefaultConfiguration(runtimeTypeName);
		}

		try {
			Constructor<?> ctor = Class.forName(configurationClass).getDeclaredConstructor(String.class);
			ctor.setAccessible(true);
			return (Configuration) ctor.newInstance(runtimeTypeName);
		} catch (Exception e) {
			throw new IllegalArgumentException("Cannot instantiate configuration class: " + configurationClass,  e);
		}
	}

	public static Configuration newExternalConfiguration(String configurationClass, String runtimeTypeName, IProgressMonitor progressMonitor) throws Exception {
		if (configurationClass == null) {
			return null;
		}

		runtimeTypeName = evalRuntimeType(runtimeTypeName);

		String configJar = NameUtility.unqualify(configurationClass)+ ".sharpenconfig.jar";

		try {
			URI currentDirectoryURI = getCurrentDirectoryURI();
			File currentDirectory = new File(currentDirectoryURI);
			Path configPath = Paths.get(currentDirectory.getPath(), configJar);
			URI jarURI = configPath.toUri();
			File configFile = configPath.toFile();
			if(!configFile.exists()){
				progressMonitor.subTask("Configuration library " + configJar + " not found");
				return null;
			}

			return createConfigFromJar(jarURI, configurationClass, runtimeTypeName);
		}
		catch (Exception ex){
			throw new Exception("External configuration library error : " + ex.getMessage(), ex);
		}
	}

	public static URI getCurrentDirectoryURI() throws URISyntaxException {
		return ConfigurationFactory.class.getProtectionDomain().getCodeSource().getLocation().toURI();
	}

	private static String evalRuntimeType(String runtimeTypeName) {
		return runtimeTypeName == null ? DEFAULT_RUNTIME_TYPE_NAME : runtimeTypeName;
	}

	private static Configuration createConfigFromJar(URI jarURI, String className, String runtimeTypeName) throws Exception {
		URLClassLoader classLoader = new URLClassLoader(new URL[]{ jarURI.toURL() }, ConfigurationFactory.class.getClassLoader());
		Class configurationClass = Class.forName (className, true, classLoader);
		if(!Configuration.class.isAssignableFrom(configurationClass)){
			throw new Exception("Configuration class must extend " + Configuration.class.getName());
		}
		Constructor<?> ctor = configurationClass.getDeclaredConstructor(String.class);
		if(!Modifier.isPublic(ctor.getModifiers())){
			throw new Exception("Configuration class constructor must have public modifier");
		}
		return (Configuration) ctor.newInstance(runtimeTypeName);
	}
}

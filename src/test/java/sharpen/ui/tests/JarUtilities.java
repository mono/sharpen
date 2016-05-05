/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

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

package sharpen.ui.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarUtilities {
	
	public static String createJar(Class<?>...cookies) throws Exception {
		File file = java.io.File.createTempFile("sharpen", ".jar");
		java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
		try {
			fillJar(fos, cookies);
		} finally {
			fos.close();
		}
		return file.getAbsolutePath();
	}
	
	private static void fillJar(java.io.FileOutputStream fos, Class<?>... cookies) throws Exception {
		ZipOutputStream out = new ZipOutputStream(fos);
		try {
			for (Class<?> clazz : cookies) {
				writeJarEntry(out, clazz);
			}
		} finally {
			out.close();
		}
	}

	private static void writeJarEntry(ZipOutputStream out, Class<?> clazz) throws Exception {
		ZipEntry ze = new ZipEntry(fileName(clazz));
		out.putNextEntry(ze);
		out.write(readBytes(clazz));
		out.closeEntry();
	}

	private static byte[] readBytes(Class<?> clazz) throws IOException {
		String resourceName = "/"+ fileName(clazz);
		InputStream stream = clazz.getResourceAsStream(resourceName);
		try {
			byte[] buffer = new byte[stream.available()];
			stream.read(buffer);
			return buffer;
		} finally {
			stream.close();
		}
	}

	private static String fileName(Class<?> clazz) {
		return clazz.getName().replace('.', '/') + ".class";
	}
}

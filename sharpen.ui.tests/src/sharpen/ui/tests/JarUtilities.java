package sharpen.ui.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class JarUtilities {
	
	public static String createJar(Class...cookies) throws Exception {
		File file = java.io.File.createTempFile("sharpen", ".jar");
		java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
		try {
			fillJar(fos, cookies);
		} finally {
			fos.close();
		}
		return file.getAbsolutePath();
	}
	
	private static void fillJar(java.io.FileOutputStream fos, Class... cookies) throws Exception {
		ZipOutputStream out = new ZipOutputStream(fos);
		try {
			for (Class clazz : cookies) {
				writeJarEntry(out, clazz);
			}
		} finally {
			out.close();
		}
	}

	private static void writeJarEntry(ZipOutputStream out, Class clazz) throws Exception {
		ZipEntry ze = new ZipEntry(fileName(clazz));
		out.putNextEntry(ze);
		out.write(readBytes(clazz));
		out.closeEntry();
	}

	private static byte[] readBytes(Class clazz) throws IOException {
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

	private static String fileName(Class clazz) {
		return clazz.getName().replace('.', '/') + ".class";
	}
}

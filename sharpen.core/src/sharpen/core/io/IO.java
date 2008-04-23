package sharpen.core.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

public class IO {

	public static void writeFile(File file, String contents) throws IOException {
		FileWriter writer = new FileWriter(file);
		try {
			writer.append(contents);
		} finally {
			writer.close();
		}
	}
	
	public static String readFile(File file) throws IOException {
		FileReader reader = new FileReader(file);
		try {
			StringWriter writer = new StringWriter();
			char[] buffer = new char[32*1024];
			int read = 0;
			while ((read = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, read);
			}
			return writer.toString();
		} finally {
			reader.close();
		}
	}

	public static void collectLines(ArrayList<String> lines, BufferedReader reader) throws IOException {
		String line = null;
		while (null != (line = reader.readLine())) {
			line = line.trim();
			if (line.length() > 0) {
				for (String arg : line.split("\\s+")) {
					lines.add(arg);
				}
			}
		}
	}

	public static String[] linesFromFile(String fname) {
		try {
			java.io.FileReader reader = new java.io.FileReader(fname);
			try {
				ArrayList<String> lines = new ArrayList<String>();
				collectLines(lines, new BufferedReader(reader));
				return lines.toArray(new String[lines.size()]);
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}

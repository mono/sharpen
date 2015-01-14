package sharpen.util;


import java.io.*;

public class InputStreamUtility {

	public static String readString(java.io.InputStream stream)
			throws IOException {
		return readString(new InputStreamReader(stream));
	}

	public static String readString(InputStream stream, String charset)
			throws IOException {
		return readString(new InputStreamReader(stream, charset));
	}

	public static String readString(InputStreamReader reader)
			throws IOException {
		
		final BufferedReader bufferedReader = new BufferedReader(reader);
		final StringWriter writer = new StringWriter();
		String line = null;
		while (null != (line = bufferedReader.readLine())) {
			writer.write(line);
			writer.write("\n");
		}
		return writer.toString();
	}
}
package sharpen.core.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

public class IndentedWriter {

	private static final int MAX_COLUMNS = 80;

	String _lineSeparator = System.getProperty("line.separator");

	String _indentString = "\t";

	int _indentLevel = 0;

	private int _column;

	private Writer _delegate;

	private String _prefix;

	public IndentedWriter(Writer writer) {
		_delegate = writer;
	}

	public void indent() {
		++_indentLevel;
	}

	public void outdent() {
		--_indentLevel;
	}

	public void writeIndented(String s) {
		writeIndentation();
		write(s);
	}

	public void writeIndentedLine(String s) {
		writeIndentation();
		writeLine(s);

	}

	public void write(String s) {
		if (_column > MAX_COLUMNS) {
			writeLine();
			writeIndented(_indentString);
		}
		writeBlock(s);
	}

	/**
	 * write a block of text without checking columns.
	 */
	public void writeBlock(String s) {
		uncheckedWrite(s);
		_column += s.length();
	}

	public void writeLine() {
		writeLine("");
	}

	public void writeLine(String s) {
		try {
			_delegate.write(s);
			_delegate.write(_lineSeparator);
			_column = 0;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void uncheckedWrite(String s) {
		try {
			_delegate.write(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void writeIndentation() {
		for (int i = 0; i < _indentLevel; ++i) {
			uncheckedWrite(_indentString);
		}
		if (null != _prefix) {
			uncheckedWrite(_prefix);
		}
	}

	public Writer delegate() {
		return _delegate;
	}

	public void writeLines(String lines) {
		BufferedReader lineReader = new BufferedReader(new StringReader(lines));
		String line;
		try {
			while (null != (line = lineReader.readLine())) {
				if (line.trim().length() > 0) {
					writeIndentedLine(line);
				} else {
					writeLine();
				}
			}
		} catch (java.io.IOException x) {
			throw new RuntimeException(x);
		}
	}

	public void linePrefix(String prefix) {
		_prefix = prefix;
	}
}

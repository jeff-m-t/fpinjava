package fpinjava.json;

import fpinjava.util.parsing.Parser;

public class JsonParser extends Parser<JSON> {
	public final static Parser<String> whitespaceChar = string(" ").or(string("\t")).or(string("\n"));
}

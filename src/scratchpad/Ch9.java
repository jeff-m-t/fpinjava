package scratchpad;

import fpinjava.data.List;
import fpinjava.data.Pair;
import fpinjava.util.parsing.Parser;
import fpinjava.util.parsing.ParserFactory;
import fpinjava.util.parsing.impl.ParseState;
import fpinjava.util.parsing.impl.ReferenceParserFactory;
import fpinjava.json.JSON;
import fpinjava.json.JsonParser;
public class Ch9<A> {

	public static void main(String[] args) {
		
		ParserFactory<ParseState> pf = new ReferenceParserFactory();
		
		Parser<ParseState,String> p1 = pf.string("aa");
		
		System.out.println(p1.parse("aa"));
		System.out.println(p1.parse("ab"));

		Parser<ParseState,Pair<String,String>> p2 = pf.string("aa").andThen(() -> pf.string("ab"));
		
		System.out.println(p2.parse("aaab"));
		System.out.println(p2.parse("aaac"));
		
		Parser<ParseState,Pair<List<String>,String>> p3 = pf.string("aA").many().andThen(() -> pf.string("aB"));
		
		System.out.println(p3.parse("aAaAaAaB"));
		System.out.println(p3.parse("aB"));
		System.out.println(p3.parse("aA"));

		Parser<ParseState,List<String>> p4 = pf.string("aA").many();
		System.out.println(p4.parse("aAaAaB"));
		System.out.println(p4.parse("aA"));
		System.out.println(p4.parse(""));
		System.out.println(p4.parse("aB"));
		
		JsonParser jp = new JsonParser(pf);
		
		System.out.println(jp.jvalue().parse("\"foo\""));
		System.out.println(jp.jvalue().parse("12345 "));
		
		System.out.println(jp.jarray().parse("[\"foo\",[12,34]]"));
		
		System.out.println(jp.jobject().parse("{\n  \"foo\":123,\n  \"bar\":[true,false,4]\n}"));

		
	}

}

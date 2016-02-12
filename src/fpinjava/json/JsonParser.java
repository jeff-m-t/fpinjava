package fpinjava.json;

import fpinjava.data.Pair;
import fpinjava.util.parsing.Parser;
import fpinjava.util.parsing.ParserFactory;
import fpinjava.util.parsing.impl.ParseState;
import static fpinjava.json.JSON.*;

public class JsonParser {
	private final ParserFactory<ParseState> pf;
	
	public JsonParser(ParserFactory<ParseState> pf) {
		this.pf = pf;
	}
	
	public Parser<ParseState,String> tok(String s) {
		return pf.token(pf.string(s));
	}
	
	public Parser<ParseState,JSON> jstring() {
		return pf.token(pf.quoted()).map(s -> new JString(s));
	}
	
	public Parser<ParseState,JSON> jnumber() {
		return pf.token(pf.fpnumber()).map(n -> new JNumber(n));
	}
	
	public Parser<ParseState,JSON> jnull() { 
		return tok("null").map(s -> new JNull()); 
	}
	
	public Parser<ParseState,JSON> jtrue() {
		return tok("true").map(s -> new JBool(true)); 
	}

	public Parser<ParseState,JSON> jfalse() {
		return tok("false").map(s -> new JBool(false)); 
	}

	public Parser<ParseState,JSON> jarray() {		
		return jvalue().sep(pf.token(pf.string(",")))
				       .surroundedBy(tok("["), tok("]"))
					   .map(l -> new JArray(l));
	}
	
	public Parser<ParseState,JSON> jliteral() {
		return jstring().or(()->
			   jnumber()).or(()->
			   jtrue()).or(()->
			   jfalse()).or(()->
			   jnull());
	}
	
	public Parser<ParseState, Pair<String,JSON>> jfield() {
		return pf.token(pf.quoted()).andThenSkip(() -> pf.token(pf.string(":"))).andThen(() -> jvalue());
	}
	
	public Parser<ParseState,JSON> jobject() {
		return jfield().sep(tok(","))
				      .surroundedBy(tok("{"), tok("}"))
				      .map(fields -> new JObject(fields));
	}
	
	public Parser<ParseState,JSON> jvalue() {
		return jliteral().or(()->jarray()).or(()->jobject());
	}
	
	public Parser<ParseState,JSON> topLevel() {
		return jobject().or(()->jarray());
	}
}

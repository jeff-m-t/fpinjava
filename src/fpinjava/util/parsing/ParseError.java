package fpinjava.util.parsing;

import fpinjava.data.List;
import fpinjava.data.Pair;

public class ParseError {
	public final List<Pair<Location,String>> stack;
	
	public ParseError(List<Pair<Location,String>> stack) {
		this.stack = stack;
	}
	
	public ParseError push(Location loc, String msg) {
		return new ParseError(this.stack.cons(Pair.of(loc,msg)));
	}
}


package fpinjava.util.parsing;

import java.util.Optional;

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
	
	public ParseError label(String msg) {
		return new ParseError(latestLoc().map(loc -> List.of(Pair.of(loc,msg))).orElse(List.nil()));
	}
	
	public Optional<Pair<Location,String>> latest() {
		return stack.lastOption();
	}
	
	public Optional<Location> latestLoc() {
		return latest().map(pair -> pair.fst);
	}
	
	@Override
	public String toString() {
		return stack.map(pe -> "At "+pe.fst+" but expected "+pe.snd).toString();
	}
}


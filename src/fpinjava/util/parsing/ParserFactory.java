package fpinjava.util.parsing;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import fpinjava.data.List;
import fpinjava.data.Pair;

//TODO: Reduce duplication between Parser and ParserFactory
public interface ParserFactory<IN> {
	public <B> Parser<IN,B> succeed(B b);
	public Parser<IN,String> string(String s);
	public Parser<IN,String> regex(Pattern pat);	
	public <B> Parser<IN,B> or(Parser<IN,B> p1, Supplier<Parser<IN,B>> p2);
	public <B,C> Parser<IN,C> flatMap(Parser<IN,B> pa, Function<B,Parser<IN,C>> f);	
	public <B> Parser<IN,String> slice(Parser<IN,B> pa);
	public <B> Parser<IN,B> label(String msg, Parser<IN,B> pa);
	public <B> Parser<IN,B> scope(String name, Parser<IN,B> pa);
	public <B> Parser<IN,B> attempt(Parser<IN,B> pa);

	// Derived
	public default <B,C,D> Parser<IN,D> map2(Parser<IN,B> pa, Supplier<Parser<IN,C>> pb, BiFunction<B,C,D> f) {
		return pa.flatMap(a -> pb.get().map(b -> f.apply(a,b)));
	}

	public default <B> Parser<IN,List<B>> many(Parser<IN,B> pb) {
		return map2(pb, ()->many(pb),(b,bs) -> bs.cons(b)).or(()->succeed(List.nil()));
	}
	
	public default <B> Parser<IN,List<B>> many1(Parser<IN,B> pa) {
		return map2(pa,() -> many(pa), (a,as) -> as.cons(a));
	}
	
	public default <B,C> Parser<IN,Pair<B,C>> product(Parser<IN,B> pa, Supplier<Parser<IN,C>> pb) {
		return map2(pa,pb,(a,b) -> Pair.of(a,b));
	}

	public default <B> Parser<IN,List<B>> listOfN(int n, Parser<IN,B> pa) {
		if(n == 0) return succeed(List.nil());
		else return map2(pa,() -> listOfN(n-1, pa),(a,as) -> as.cons(a));
	}
	
	public default <B> Parser<IN,B> skipL(Parser<IN,?> p1, Supplier<Parser<IN,B>> p2) {
		return map2(slice(p1), p2, (a,b) -> b);
	}
	
	public default <B> Parser<IN,B> skipR(Parser<IN,B> p1, Supplier<Parser<IN,?>> p2) {
		return map2(p1,() -> slice(p2.get()),(a,b) -> a);
	}
	
	public default <B> Parser<IN,Optional<B>> opt(Parser<IN,B> pa) {
		return pa.map(a -> Optional.of(a)).or(()->succeed(Optional.empty()));
	}
	
	public default Parser<IN,String> whitespace() {
		return regex(Pattern.compile("\\s*"));
	}
	
	public default <B> Parser<IN,B> token(Parser<IN,B> pa) {
		return skipR(attempt(pa),() -> whitespace());
	}
	
	public default Parser<IN,String> digits() {
		return regex(Pattern.compile("\\d+"));
	}
	
	public default Parser<IN,String> thru(String s) {
		return regex(Pattern.compile(".*?"+Pattern.quote(s)));
	}
	
	public default Parser<IN,String> quoted() {
		return skipL(string("\""),() -> thru("\"")).map(s -> s.substring(0,s.length()-1));
	}
	
	// Really just unescaped 
	public default Parser<IN,String> escapedQuoted() {
		return token(quoted().label("String Literal"));
	}
	
	public default Parser<IN,String> doubleString() {
		return regex(Pattern.compile("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?"));
	}
	
	public default Parser<IN,Double> fpnumber() {
		return doubleString().map(ds -> Double.valueOf(ds));
	}
	
	 /** Zero or more repetitions of `p`, separated by `p2`, whose results are ignored. */
	public default <B> Parser<IN,List<B>> sep(Parser<IN,B> pa, Parser<IN,?> sep) {
		return sep1(pa,sep).or(()->succeed(List.nil()));
	}
	
	/** One or more repetitions of `p`, separated by `p2`, whose results are ignored. */
	public default <B> Parser<IN,List<B>> sep1(Parser<IN,B> pa, Parser<IN,?> sep) {
		return map2(pa, ()->many(skipL(sep,()->pa)), (a,as) -> as.cons(a));
	}
	
	// baffled
	/** Parses a sequence of left-associative binary operators with the same precedence. */
	public default <B> Parser<IN,B> opL(Parser<IN,B> p, Parser<IN,BiFunction<B,B,B>> op) {
		return map2(p,()->many(product(op,() -> p)), (h,t) -> t.foldLeft(h,(a,b) -> b.fst.apply(a,b.snd)));
	}
	
	/** Wraps `p` in before/after delimiters. */
	public default <B> Parser<IN,B> surround(Parser<IN,?> before, Parser<IN,?> after, Parser<IN,B> pa) {
		return skipL(before, ()->skipR(pa,()->after));
	}
	
	public default Parser<IN,String> eof() {
		return regex(Pattern.compile("\\z")).label("unexpected trailing characters");
	}
	
	public default <B> Parser<IN,B> root(Parser<IN,B> pb) {
		return skipR(pb,()->eof());
	}	
	
}

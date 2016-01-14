package fpinjava.util.parsing;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import fpinjava.data.List;
import fpinjava.data.Pair;

public interface Parser<A> {
	
	public Result<A> run(Parser<A> pa, Location input);
	
	public <B> Parser<B> succeed(B a);
	
	public Parser<String> string(String s);
	
	public Parser<String> regex(Pattern pat);
	
	public <B> Parser<B> or(Parser<B> p1, Supplier<Parser<B>> p2);
	
	public <B,C> Parser<C> flatMap(Parser<B> pa, Function<B,Parser<C>> f);
	
	public <B> Parser<String> slice(Parser<B> pa);
	
	public <B> Parser<B> label(String msg, Parser<B> pa);
	
	public <B> Parser<B> scope(String name, Parser<B> pa);
	
	public <B> Parser<B> attempt(Parser<B> pa);

	/* Derived functions */
	public default <B> Parser<B> defaultSucceed(B b) {
		return string("").map(s -> b);
	}

	public default Parser<Character> character(char c) {
		return string(String.valueOf(c)).map(s -> s.charAt(0));
	}
	
	public default <B,C> Parser<C> map(Parser<B> pa, Function<B,C> f) {
		return flatMap(pa,a -> succeed(f.apply(a)));
	}
	
	public default <B> Parser<List<B>> many(Parser<B> pa) {
		return map2(pa, ()->many(pa),(a,as) -> as.cons(a)).or(succeed(List.nil()));
	}
	
	public default <B> Parser<List<B>> many1(Parser<B> pa) {
		return map2(pa,() -> many(pa), (a,as) -> as.cons(a));
	}
	
	public default <B,C,D> Parser<D> map2(Parser<B> pa, Supplier<Parser<C>> pb, BiFunction<B,C,D> f) {
		return pa.flatMap(a -> pb.get().map(b -> f.apply(a,b)));
	}

	public default <B,C> Parser<Pair<B,C>> product(Parser<B> pa, Supplier<Parser<C>> pb) {
		return map2(pa,pb,(a,b) -> Pair.of(a,b));
	}

	public default <B> Parser<List<B>> listOfN(int n, Parser<B> pa) {
		if(n == 0) return succeed(List.nil());
		else return map2(pa,() -> listOfN(n-1, pa),(a,as) -> as.cons(a));
	}
	
	public default <B> Parser<B> skipL(Parser<?> p1, Supplier<Parser<B>> p2) {
		return map2(slice(p1), p2, (a,b) -> b);
	}
	
	public default <B> Parser<B> skipR(Parser<B> p1, Supplier<Parser<?>> p2) {
		return map2(p1,() -> slice(p2.get()),(a,b) -> a);
	}
	
	public default <B> Parser<Optional<B>> opt(Parser<B> pa) {
		return pa.map(a -> Optional.of(a)).or(succeed(Optional.empty()));
	}
	
	public default Parser<String> whitespace() {
		return regex(Pattern.compile("\\s+"));
	}
	
	public default <B> Parser<B> token(Parser<B> pa) {
		return skipR(attempt(pa),() -> whitespace());
	}
	
	public default Parser<String> digits() {
		return regex(Pattern.compile("\\d+"));
	}
	
	public default Parser<String> thru(String s) {
		return regex(Pattern.compile(".*?"+Pattern.quote(s)));
	}
	
	public default Parser<String> quoted() {
		return skipL(string("\""),() -> thru("\"")).map(s -> s.substring(0,s.length()-1));
	}
	
	// Really just unescaped 
	public default Parser<String> escapedQuoted() {
		return token(quoted().label("String Literal"));
	}
	
	public default Parser<String> doubleString() {
		return regex(Pattern.compile("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?"));
	}
	
	public default Parser<Double> fpnumber() {
		return doubleString().map(ds -> Double.valueOf(ds));
	}
	
	 /** Zero or more repetitions of `p`, separated by `p2`, whose results are ignored. */
	public default <B> Parser<List<B>> sep(Parser<B> pa, Parser<?> sep) {
		return sep1(pa,sep).or(succeed(List.nil()));
	}
	
	/** One or more repetitions of `p`, separated by `p2`, whose results are ignored. */
	public default <B> Parser<List<B>> sep1(Parser<B> pa, Parser<?> sep) {
		return map2(pa, ()->many(skipL(sep,()->pa)), (a,as) -> as.cons(a));
	}
	
	// baffled
	/** Parses a sequence of left-associative binary operators with the same precedence. */
	public default <B> Parser<B> opL(Parser<B> p, Parser<BiFunction<B,B,B>> op) {
		return map2(p,()->many(product(op,() -> p)), (h,t) -> t.foldLeft(h,(a,b) -> b.fst.apply(a,b.snd)));
	}
	
	/** Wraps `p` in before/after delimiters. */
	public default <B> Parser<B> surround(Parser<?> before, Parser<?> after, Parser<B> pa) {
		return skipL(before, ()->skipR(pa,()->after));
	}
	
	public default Parser<String> eof() {
		return regex(Pattern.compile("\\z")).label("unexpected trailing characters");
	}
	
	public default Parser<A> root(Parser<A> pa) {
		return skipR(pa,()->eof());
	}	
	
	/* Instance methods */
	public default Parser<A> or(Parser<A> other) {
		return or(this,()-> other);
	}
	
	public default Parser<List<A>> many() {
		return many(this);
	}
	
	public default <B> Parser<B> map(Function<A,B> f) {
		return map(this,f);
	}
	
	public default <B> Parser<B> flatMap(Function<A,Parser<B>> f) {
		return flatMap(this,f);
	}
	
	public default Parser<String> slice() {
		return slice(this);
	}
	
	public default Parser<A> label(String msg) {
		return label(msg,this);
	}
	
	public default Parser<A> scope(String msg) {
		return scope(msg,this);
	}

	public default Parser<A> token() {
		return token(this);
	}
	
	public default Parser<List<A>> sep(Parser<?> s) {
		return sep(this,s);
	}
	
	public default Parser<List<A>> sep1(Parser<?> s) {
		return sep1(this,s);
	}
	
	public default Parser<A> opL(Parser<BiFunction<A,A,A>> op) {
		return opL(this,op);
	}
	
	public default <B> Parser<Pair<A,B>> andThen(Parser<B> pb) {
		return product(this,()->pb);
	}
	
	public default <B> Parser<B> skippedAndThen(Parser<B> pb) {
		return skipL(this,()->pb);
	}
	
	public default Parser<A> andThenSkip(Parser<?> pb) {
		return skipR(this,()->pb);
	}
}

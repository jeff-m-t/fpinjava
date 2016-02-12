package fpinjava.util.parsing;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import fpinjava.data.List;
import fpinjava.data.Pair;

// TODO: Reduce duplication between Parser and ParserFactory
public class Parser<IN,A> {
		
	public final ParserImpl<IN,A> impl;
	
	public Parser(ParserImpl<IN,A> impl) {
		this.impl = impl;
	}
	
	public Result<A> parse(String inp) {
		return impl.run(impl.inputFor(inp));
	}
	
	public static <IN,OUT> Parser<IN,OUT> of(ParserImpl<IN,OUT> impl) {
		return new Parser<IN,OUT>(impl);
	}
	
	/* Derived functions */
	public <B> Parser<IN,B> defaultSucceed(B b) {
		return Parser.of(impl.string("")).map(s -> b);
	}

	public Parser<IN,Character> character(char c) {
		return Parser.of(impl.string(String.valueOf(c))).map(s -> s.charAt(0));
	}
	
	public <B,C> Parser<IN,C> map(Parser<IN,B> pb, Function<B,C> f) {
		return pb.flatMap(b -> Parser.of(impl.succeed(f.apply(b))));
	}
	
	public <B> Parser<IN,List<B>> many(Parser<IN,B> pb) {
		return map2(pb, () -> pb.many(),(b,bs) -> bs.cons(b)).or(()->Parser.of(impl.succeed(List.nil())));
	}
	
	public <B> Parser<IN,List<B>> many1(Parser<IN,B> pa) {
		return map2(pa,() -> pa.many(), (a,as) -> as.cons(a));
	}
	
	public <B,C,D> Parser<IN,D> map2(Parser<IN,B> pa, Supplier<Parser<IN,C>> pb, BiFunction<B,C,D> f) {
		return pa.flatMap(a -> pb.get().map(b -> f.apply(a,b)));
	}

	public <B,C> Parser<IN,Pair<B,C>> product(Parser<IN,B> pa, Supplier<Parser<IN,C>> pb) {
		return map2(pa,pb,(a,b) -> Pair.of(a,b));
	}

	public <B> Parser<IN,List<B>> listOfN(int n, Parser<IN,B> pa) {
		if(n == 0) return Parser.of(impl.succeed(List.nil()));
		else return map2(pa,() -> listOfN(n-1, pa),(a,as) -> as.cons(a));
	}
	
	public <B> Parser<IN,B> skipL(Parser<IN,?> p1, Supplier<Parser<IN,B>> p2) {
		return map2(Parser.of(impl.slice(p1.impl)), p2, (a,b) -> b);
	}
	
	public <B> Parser<IN,B> skipR(Parser<IN,B> p1, Supplier<Parser<IN,?>> p2) {
		return map2(p1,() -> Parser.of(impl.slice(p2.get().impl)),(a,b) -> a);
	}
	
	public <B> Parser<IN,Optional<B>> opt(Parser<IN,B> pa) {
		return pa.map(a -> Optional.of(a)).or(()->Parser.of(impl.succeed(Optional.empty())));
	}
	
	public Parser<IN,String> whitespace() {
		return Parser.of(impl.regex(Pattern.compile("\\s+")));
	}
	
	public <B> Parser<IN,B> token(Parser<IN,B> pa) {
		return skipR(Parser.of(impl.attempt(pa.impl)),() -> whitespace());
	}
	
	public Parser<IN,String> digits() {
		return Parser.of(impl.regex(Pattern.compile("\\d+")));
	}
	
	public Parser<IN,String> thru(String s) {
		return Parser.of(impl.regex(Pattern.compile(".*?"+Pattern.quote(s))));
	}
	
	public Parser<IN,String> quoted() {
		return skipL(Parser.of(impl.string("\"")),() -> thru("\"")).map(s -> s.substring(0,s.length()-1));
	}
	
	// Really just unescaped 
	public Parser<IN,String> escapedQuoted() {
		return token(quoted().label("String Literal"));
	}
	
	public Parser<IN,String> doubleString() {
		return Parser.of(impl.regex(Pattern.compile("[-+]?([0-9]*\\.)?[0-9]+([eE][-+]?[0-9]+)?")));
	}
	
	public Parser<IN,Double> fpnumber() {
		return doubleString().map(ds -> Double.valueOf(ds));
	}
	
	 /** Zero or more repetitions of `p`, separated by `p2`, whose results are ignored. */
	public <B> Parser<IN,List<B>> sep(Parser<IN,B> pa, Parser<IN,?> sep) {
		return sep1(pa,sep).or(()->Parser.of(impl.succeed(List.nil())));
	}
	
	/** One or more repetitions of `p`, separated by `p2`, whose results are ignored. */
	public <B> Parser<IN,List<B>> sep1(Parser<IN,B> pa, Parser<IN,?> sep) {
		return map2(pa, ()->many(skipL(sep,()->pa)), (a,as) -> as.cons(a));
	}
	
	// baffled
	/** Parses a sequence of left-associative binary operators with the same precedence. */
	public <B> Parser<IN,B> opL(Parser<IN,B> p, Parser<IN,BiFunction<B,B,B>> op) {
		return map2(p,()->many(product(op,() -> p)), (h,t) -> t.foldLeft(h,(a,b) -> b.fst.apply(a,b.snd)));
	}
	
	/** Wraps `p` in before/after delimiters. */
	public <B> Parser<IN,B> surround(Parser<IN,?> before, Parser<IN,?> after, Parser<IN,B> pa) {
		return skipL(before, ()->skipR(pa,()->after));
	}
	
	public Parser<IN,String> eof() {
		return Parser.of(impl.regex(Pattern.compile("\\z"))).label("unexpected trailing characters");
	}
	
	public Parser<IN,A> root(Parser<IN,A> pa) {
		return skipR(pa,()->eof());
	}	
	
	/* Instance methods */
	public Parser<IN,A> or(Supplier<Parser<IN,A>> other) {
		return Parser.of(impl.or(this.impl,()->other.get().impl));
	}
	
	public Parser<IN,List<A>> many() {
		return many(this);
	}
	
	public <B> Parser<IN,B> map(Function<A,B> f) {
		return map(this,f);
	}
	
	public <B> Parser<IN,B> flatMap(Function<A,Parser<IN,B>> f) {
		return Parser.of(impl.flatMap(this.impl,f.andThen(p -> p.impl)));
	}
	
	public Parser<IN,String> slice() {
		return Parser.of(impl.slice(this.impl));
	}
	
	public Parser<IN,A> label(String msg) {
		return Parser.of(impl.label(msg,this.impl));
	}
	
	public Parser<IN,A> scope(String msg) {
		return Parser.of(impl.scope(msg,this.impl));
	}

	public Parser<IN,A> token() {
		return token(this);
	}
	
	public Parser<IN,List<A>> sep(Parser<IN,?> s) {
		return sep(this,s);
	}
	
	public Parser<IN,A> surroundedBy(Parser<IN,?> before, Parser<IN,?> after) {
		return surround(before,after,this);
	}
	
	public Parser<IN,List<A>> sep1(Parser<IN,?> s) {
		return sep1(this,s);
	}
	
	public Parser<IN,A> opL(Parser<IN,BiFunction<A,A,A>> op) {
		return opL(this,op);
	}
	
	public <B> Parser<IN,Pair<A,B>> andThen(Supplier<Parser<IN,B>> pb) {
		return product(this,pb);
	}
	
	public <B> Parser<IN,B> skippedAndThen(Supplier<Parser<IN,B>> pb) {
		return skipL(this,pb);
	}
	
	public Parser<IN,A> andThenSkip(Supplier<Parser<IN,?>> pb) {
		return skipR(this,pb);
	}
}

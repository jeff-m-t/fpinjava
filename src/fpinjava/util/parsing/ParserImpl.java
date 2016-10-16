package fpinjava.util.parsing;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import fpinjava.data.List;

public interface ParserImpl<IN,OUT> {
	public Result<OUT> run(IN inp);
	
	public IN inputFor(String imp);
	
	public <B> ParserImpl<IN,B> succeed(B a);
	
	public ParserImpl<IN,String> string(String s);
	
	public ParserImpl<IN,String> regex(Pattern pat);
	
	public <B> ParserImpl<IN,B> or(ParserImpl<IN,B> p1, Supplier<ParserImpl<IN,B>> p2);
	
	public <B,C> ParserImpl<IN,C> flatMap(ParserImpl<IN,B> pa, Function<B,ParserImpl<IN,C>> f);
	
	public <B> ParserImpl<IN,String> slice(ParserImpl<IN,B> pa);
	
	public <B> ParserImpl<IN,B> label(String msg, ParserImpl<IN,B> pa);
	
	public <B> ParserImpl<IN,B> scope(String name, ParserImpl<IN,B> pa);
	
	public <B> ParserImpl<IN,B> attempt(ParserImpl<IN,B> pa);
}

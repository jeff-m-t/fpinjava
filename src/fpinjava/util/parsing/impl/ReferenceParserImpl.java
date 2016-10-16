package fpinjava.util.parsing.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fpinjava.util.parsing.Location;
import fpinjava.util.parsing.Parser;
import fpinjava.util.parsing.ParserImpl;
import fpinjava.util.parsing.Result;

public class ReferenceParserImpl<OUT> implements ParserImpl<ParseState,OUT> {

	public final Function<ParseState, Result<OUT>> run;
	
	public ReferenceParserImpl(Function<ParseState,Result<OUT>> run) {
		this.run = run;
	}

	@Override
	public Result<OUT> run(ParseState in) {
		return this.run.apply(in);
	}
	
	@Override 
	public ParseState inputFor(String inp) {
		return new ParseState(new Location(inp,0));
	}

	
	@Override
	public <B> ReferenceParserImpl<B> succeed(B b) {
		return new ReferenceParserImpl<B>( s -> Result.success(b,0) );
	}

	@Override
	public ReferenceParserImpl<String> string(String w) {
		return new ReferenceParserImpl<String>( s -> {
			int i = firstNonMatchingIndex(s.loc.input, w, s.loc.offset);
			if(i == -1) return Result.success(w,w.length());
			else return Result.failure(s.loc.advanceBy(i).toError("'"+w+"'"),i != 0);
		});	
	}

	@Override
	public ReferenceParserImpl<String> regex(Pattern pat) {
		String msg = "regex: "+pat.pattern();
		return new ReferenceParserImpl<String>( s -> {
			Matcher m = pat.matcher(s.input());
			if(m.lookingAt()) {
				MatchResult mr = m.toMatchResult();
				String matched = mr.group();
				return Result.success(matched,matched.length());
			}
			else {
				return Result.failure(s.loc.toError(msg),false);
			}
		});
	}

	@Override
	public <B> ReferenceParserImpl<B> or(ParserImpl<ParseState,B> p1, Supplier<ParserImpl<ParseState,B>> p2) {
		return new ReferenceParserImpl<B>(s -> {
			Result<B> first = p1.run(s);
			if(first.isSuccess()) return first;
			else return p2.get().run(s);
		});
	}

	@Override
	public <B, C> ReferenceParserImpl<C> flatMap(ParserImpl<ParseState,B> pb, Function<B, ParserImpl<ParseState,C>> f) {
		return new ReferenceParserImpl<C>( s -> {
			Result<C> res = pb.run(s).fold(
				success -> {
					B b = success.get;
					int n = success.charsConsumed;
							
					return f.apply(b).run(s.advanceBy(n)).addCommit(n != 0).advanceSuccess(n);
					
				}, 
				failure -> new Result.Failure<C>(failure.get, failure.isCommitted)
			);
			
			return res;
		});
	}

	@Override
	public <B> ReferenceParserImpl<String> slice(ParserImpl<ParseState,B> pb) {
		return new ReferenceParserImpl<String>(s ->
			pb.run(s).fold(
				success -> new Result.Success<String>(s.slice(success.charsConsumed),success.charsConsumed), 
				failure -> new Result.Failure<String>(failure.get,failure.isCommitted)
			)
		);
	}

	@Override
	public <B> ReferenceParserImpl<B> label(String msg, ParserImpl<ParseState,B> pb) {
		return new ReferenceParserImpl<B>( s -> pb.run(s).mapError(er -> er.label(msg)) );
	}

	@Override
	public <B> ReferenceParserImpl<B> scope(String name, ParserImpl<ParseState,B> pb) {
		return new ReferenceParserImpl<B>( s -> 
			pb.run(s).mapError(er -> er.push(s.loc, name))
		);
	}

	@Override
	public <B> ReferenceParserImpl<B> attempt(ParserImpl<ParseState,B> pb) {
		return new ReferenceParserImpl<B>( s -> pb.run(s).uncommit());
	}

	public int firstNonMatchingIndex(String s1, String s2, int offset) {
		int i = 0;
		while (i < s1.length() && i < s2.length()) {
			if (s1.charAt(i+offset) != s2.charAt(i)) return i;
			i += 1;
		}
		if (s1.length()-offset >= s2.length()) return -1;
		else return s1.length()-offset;		
	}
}

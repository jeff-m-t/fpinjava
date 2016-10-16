package fpinjava.util.parsing.impl;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fpinjava.util.parsing.Location;
import fpinjava.util.parsing.Parser;
import fpinjava.util.parsing.ParserFactory;
import fpinjava.util.parsing.ParserImpl;
import fpinjava.util.parsing.Result;

public class ReferenceParserFactory implements ParserFactory<ParseState> {
	
	@Override
	public <B> Parser<ParseState, B> succeed(B b) {
		return Parser.of(new ReferenceParserImpl<B>(s -> Result.success(b,0)));
	}

	@Override
	public Parser<ParseState, String> string(String w) {
		return Parser.of(new ReferenceParserImpl<String>( s -> {
			int i = firstNonMatchingIndex(s.loc.input, w, s.loc.offset);
			if(i == -1) return Result.success(w,w.length());
			else return Result.failure(s.loc.advanceBy(i).toError("'"+w+"'"),i != 0);
		}));
	}

	@Override
	public Parser<ParseState,String> regex(Pattern pat) {
		String msg = "regex: "+pat.pattern();
		return Parser.of(new ReferenceParserImpl<String>( s -> {
			Matcher m = pat.matcher(s.input());
			if(m.lookingAt()) {
				MatchResult mr = m.toMatchResult();
				String matched = mr.group();
				return Result.success(matched,matched.length());
			}
			else {
				return Result.failure(s.loc.toError(msg),false);
			}
		}));
	}

	@Override
	public <B> Parser<ParseState,B> or(Parser<ParseState,B> p1, Supplier<Parser<ParseState,B>> p2) {
		return Parser.of(new ReferenceParserImpl<B>(s -> {
			Result<B> first = p1.impl.run(s);
			if(first.isSuccess()) return first;
			else return p2.get().impl.run(s);
		}));
	}

	@Override
	public <B, C> Parser<ParseState,C> flatMap(Parser<ParseState,B> pb, Function<B, Parser<ParseState,C>> f) {
		return Parser.of(new ReferenceParserImpl<C>( s -> {
			Result<C> res = pb.impl.run(s).fold(
				success -> {
					B b = success.get;
					int n = success.charsConsumed;
							
					return f.apply(b).impl.run(s.advanceBy(n)).addCommit(n != 0).advanceSuccess(n);
					
				}, 
				failure -> new Result.Failure<C>(failure.get, failure.isCommitted)
			);
			
			return res;
		}));
	}

	@Override
	public <B> Parser<ParseState,String> slice(Parser<ParseState,B> pb) {
		return Parser.of(new ReferenceParserImpl<String>(s -> 
			pb.impl.run(s).fold(
				success -> new Result.Success<String>(s.slice(success.charsConsumed),success.charsConsumed), 
				failure -> new Result.Failure<String>(failure.get,failure.isCommitted)
			)
		));
	}

	@Override
	public <B> Parser<ParseState,B> label(String msg, Parser<ParseState,B> pb) {
		return Parser.of(new ReferenceParserImpl<B>( s -> pb.impl.run(s).mapError(er -> er.label(msg))));
	}

	@Override
	public <B> Parser<ParseState,B> scope(String name, Parser<ParseState,B> pb) {
		return Parser.of(new ReferenceParserImpl<B>( s -> 
			pb.impl.run(s).mapError(er -> er.push(s.loc, name))
		));
	}

	@Override
	public <B> Parser<ParseState,B> attempt(Parser<ParseState,B> pb) {
		return Parser.of(new ReferenceParserImpl<B>( s -> pb.impl.run(s).uncommit()));
	}

	public int firstNonMatchingIndex(String s1, String s2, int offset) {
		int i = 0;
		while ((i+offset < s1.length()) && (i < s1.length()) && (i < s2.length())) {
			if (s1.charAt(i+offset) != s2.charAt(i)) return i;
			i += 1;
		}
		if (s1.length()-offset >= s2.length()) return -1;
		else return s1.length()-offset;		
	}
}

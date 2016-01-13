package fpinjava.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import fpinjava.data.Pair;

public class Functions {
	public static <A,B,C> Function<A,Function<B,C>> curry(BiFunction<A,B,C> f) {
		return a -> b -> f.apply(a,b); 
	}

	public static <A,B,C> BiFunction<A,B,C> uncurry(Function<A,Function<B,C>> f) {
		return (a, b) -> f.apply(a).apply(b);
	}
	
	public static <A,B,C> Function<Pair<A,B>,C> tupled(BiFunction<A,B,C> f) {
		return ab -> f.apply(ab.fst,ab.snd);
	}
}

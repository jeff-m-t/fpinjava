package fpinjava.util;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class OptionalOps {
	public static <A,B> Function<Optional<A>,Optional<B>> lift(Function<A,B> f) {
		return oa -> oa.map(f);
	}
	
	public static <A,B,C> Optional<C> map2(Optional<A> a, Optional<B> b, BiFunction<A,B,C> f) {
		return a.flatMap(aa -> b.map(bb -> f.apply(aa,bb)));
	}
}

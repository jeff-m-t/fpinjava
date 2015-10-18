package scratchpad;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.*;

public class Ch2 {

	public static void main(String[] args) throws Exception {
		System.out.println("Hello World");

		System.out.println("Is Sorted: "+isSorted(new Integer[] {1,2,3,4,5}, (a,b) -> a <= b ));
		System.out.println("Is Sorted: "+isSorted(new Integer[] {1,2,4,3,5}, (a,b) -> a <= b ));
		
		BiFunction<String,String,String> f = (a,b) -> a.toString() + " : " + b.toString();
		
		Function<String,Function<String,String>> g = curry(f);
		
		BiFunction<String,String,String> f2 = uncurry(g);
		
		System.out.println("Orig: "+f.apply("foo","bar"));
		System.out.println("Curried: "+g.apply("foo").apply("bar"));
		System.out.println("Uncurried: "+f2.apply("foo","bar"));
		
		Optional<String> empty = Optional.empty();
		Optional<String> nonEmpty = Optional.of("Non-empty");
		
		System.out.println("Non-empty Optional: "+nonEmpty.map(s -> s.length()));
		System.out.println("Empty Optional: "+empty);

		Function<Optional<String>,Optional<Integer>> maybeLength = lift( s -> s.length() );
		
		System.out.println("Lifted: "+maybeLength.apply(nonEmpty));
		
		System.out.println("Map2: "+map2(Optional.of("foo"),Optional.of("bar"),f));
		System.out.println("Map2 (None): "+map2(Optional.of("foo"),Optional.empty(),f));
		
	}
	
	public static <A> boolean isSorted(A[] as, BiPredicate<A,A>  ordered) {
		if(as.length < 2) {
			return true;
		}
		else {
			return ordered.test(as[0],as[1]) && isSorted(Arrays.copyOfRange(as, 2, as.length),ordered);
		}	
	}
	
	public static <A,B,C> Function<A,Function<B,C>> curry(BiFunction<A,B,C> f) {
		return a -> b -> f.apply(a,b); 
	}

	public static <A,B,C> BiFunction<A,B,C> uncurry(Function<A,Function<B,C>> f) {
		return (a, b) -> f.apply(a).apply(b);
	}
	
	public static <A,B,C> Function<A,C> compose(Function<B,C> bc, Function<A,B> ab) {
		return a -> bc.apply(ab.apply(a));
	}
	
	public static <A,B> Function<Optional<A>,Optional<B>> lift(Function<A,B> f) {
		return oa -> oa.map(f);
	}
	
	public static <A,B,C> Optional<C> map2(Optional<A> a, Optional<B> b, BiFunction<A,B,C> f) {
		Optional<Function<B, C>> appliedToA = lift(curry(f)).apply(a);
		return appliedToA.flatMap(curried -> lift(curried).apply(b));
	}
	

}

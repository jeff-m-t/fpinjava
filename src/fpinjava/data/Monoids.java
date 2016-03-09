package fpinjava.data;

import java.util.function.BiFunction;
import java.util.function.Function;

import fpinjava.concurrent.Par;
import fpinjava.test.Gen;
import fpinjava.test.Prop;
import fpinjava.util.Functions;

public class Monoids {

	public static Monoid<String> stringMonoid = new Monoid<String>() {
		@Override public String op(String a1, String a2) { return a1 + a2; }
		@Override public String zero() { return ""; }
	};
	
	public static <A> Monoid<List<A>> listMonoid() {
		return new Monoid<List<A>>() {		
			@Override public List<A> op(List<A> a1, List<A> a2) { return a1.append(a2); }
			@Override public List<A> zero() { return List.nil(); }
		};
	}
	
	public static Monoid<Integer> intAddition = new Monoid<Integer>() {
		@Override public Integer op(Integer a1, Integer a2) { return a1 + a2; }
		@Override public Integer zero() { return 0; }
	};

	public static Monoid<Integer> intMultiplication = new Monoid<Integer>() {
		@Override public Integer op(Integer a1, Integer a2) { return a1 * a2; }
		@Override public Integer zero() { return 1; }
	};
	
	public static Monoid<Boolean> booleanOr = new Monoid<Boolean>() {
		@Override public Boolean op(Boolean a1, Boolean a2) { return a1 || a2; }
		@Override public Boolean zero() { return false; }
	};

	public static Monoid<Boolean> booleanAnd = new Monoid<Boolean>() {
		@Override public Boolean op(Boolean a1, Boolean a2) { return a1 && a2; }
		@Override public Boolean zero() { return true; }
	};
	
	public static <A> Monoid<Option<A>> optionMonoid() {
		return new Monoid<Option<A>>() {
			@Override public Option<A> op(Option<A> a1, Option<A> a2) { return a1.orElse(a2); }
			@Override public Option<A> zero() { return Option.none(); }
		};
	}
	
	public static <A> Monoid<Function<A,A>> endoMonoid() {
		return new Monoid<Function<A,A>>() {
			@Override public Function<A,A> op(Function<A,A> e1, Function<A,A> e2) { return a -> e1.apply(e2.apply(a)); }
			@Override public Function<A,A> zero() { return a -> a; }
		};
	}
	
	public static <A,B> B foldMap(List<A> as, Monoid<B> m, Function<A,B> f) {
		return as.map(f).foldLeft(m.zero(), m::op); // This is List's foldLeft
	}
	
	public static <A,B> B foldRight(List<A> as, B zero, BiFunction<A,B,B> f) {
		Function<A,Function<B,B>> curried = Functions.curry(f);
		Monoid<Function<B,B>> endoMonoid = Monoids.endoMonoid();
		
		Function<B,B> folded = foldMap(as, endoMonoid, curried);
		
		return folded.apply(zero);
	}
	
	public static <A,B> B foldLeft(List<A> as, B zero, BiFunction<B,A,B> f) {
		Function<A,Function<B,B>> curried = a -> b -> f.apply(b,a);
		Monoid<Function<B,B>> endoMonoid = Monoids.endoMonoid();
		
		Function<B,B> folded = foldMap(as, endoMonoid, curried);
		
		return folded.apply(zero);
	}
	
	public static <A,B> B foldMapV(A[] as, Monoid<B> m, Function<A,B> f) {
		return foldMapV(as,0,as.length-1,m,f);
	}

	public static <A,B> B foldMapV(A[] as, int start, int end, Monoid<B> m, Function<A,B> f) {
		if(end == start) {
			return f.apply(as[start]);
		}
		else if(end == start + 1) {
			return m.op(f.apply(as[start]),f.apply(as[end]));
		}
		else {
			int split = (end - start)/2;
			return m.op(foldMapV(as,start,split,m,f), foldMapV(as,split+1,end,m,f));
		}
	}
	
	public static <A> Monoid<Par<A>> par(Monoid<A> m) {
		return new Monoid<Par<A>>() {
			@Override
			public Par<A> op(Par<A> pa1, Par<A> pa2) {
				return Par.map2(pa1, pa2, (a1,a2) -> m.op(a1, a2));
			}

			@Override
			public Par<A> zero() {
				return Par.unit(m.zero());
			}			
		};
	}
	
	public static <A,B> Par<B> parFoldMap(A[] as, Monoid<B> m, Function<A,B> f) {
		Monoid<Par<B>> pm = par(m);
		return foldMapV(as, pm, a -> Par.lazyUnit(() -> f.apply(a)));
	}

//	public static <A,B> Par<B> parFoldMap2(A[] as, Monoid<B> m, Function<A,B> f) {
//		Par<List<B>> pbs = Par.parMap(List.of(as), f);
//		return pbs.flatMap(bs -> {
//			B[] bsa = null; // We need an indexed sequence type for this
//			return foldMapV(bsa, par(m), b -> Par.lazyUnit(() -> b));
//		});
//	}

	public static class Laws {
		public static <A> Prop monoidLaws(Monoid<A> m, Gen<A> gen) {
			Prop identity = Prop.forAll(gen, a -> m.op(a,m.zero()).equals(a) && m.op(m.zero(),a).equals(a));
			
			Gen<List<A>> trippleGen = Gen.listOfN(3, gen);
			
			Prop associativity = Prop.forAll(trippleGen, as -> {
				A a1 = as.head(), a2 = as.tail().head(), a3 = as.tail().tail().head();
				return m.op(a1, m.op(a2,a3)).equals(m.op(m.op(a1,a2),a3));
			});
			
			return identity.and(associativity);
		}
	}
}

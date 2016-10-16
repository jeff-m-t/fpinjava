package fpinjava.test;

import java.util.function.Function;

import fpinjava.data.List;
import fpinjava.data.Pair;
import fpinjava.data.State;
import fpinjava.util.RNG;

public class Gen<A> {
	public final State<RNG,A> sample;

	public Gen(State<RNG,A> sample) {
		this.sample = sample;
	}

	public static Gen<Integer> choose(int start, int stopExclusive) {
		State<RNG,Integer> init = new State<RNG,Integer>(rng -> RNG.nonNegativeInt(rng));
		return new Gen<Integer>(init.map(i -> start + (i % (stopExclusive - start))));
	}
	
	public static <B> Gen<B> unit(B b) {
		return new Gen<B>(State.unit(b));
	}
	
	public static Gen<Boolean> booleanValue() {
		return new Gen<Boolean>(new State<RNG,Boolean>(rng -> RNG.nextBoolean(rng)));
	}
	
	public static Gen<Double> doubleValue() {
		return new Gen<Double>(new State<RNG,Double>(rng -> RNG.nextDouble(rng)));
	}

	public static <B> Gen<List<B>> listOfN(int n, Gen<B> genB) {
		return new Gen<List<B>>(
			State.sequence(List.fill(n, genB.sample))
		);
	}
	
	public <B> Gen<B> flatMap(Function<A,Gen<B>> f) {
		return new Gen<B>(sample.flatMap(a -> f.apply(a).sample));
	}

	public <B> Gen<B> map(Function<A,B> f) {
		return this.flatMap(a -> unit(f.apply(a)));
	}
	
	public Gen<List<A>> listOfN(Gen<Integer> size) {
		return size.flatMap(n -> Gen.listOfN(n, this));
	}
	
	public static <A> Gen<A> union(Gen<A> g1, Gen<A> g2) {
		return booleanValue().flatMap(choice -> choice ? g1 : g2);
	}
	
	public static <A> Gen<A> weighted(Pair<Gen<A>,Double> g1, Pair<Gen<A>,Double> g2) {
		double fractionForFirst = g1.snd / (g1.snd + g2.snd);
		return doubleValue().flatMap(sample -> sample <= fractionForFirst ? g1.fst : g2.fst);
	}
	
	
}

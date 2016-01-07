package scratchpad;

import fpinjava.data.List;
import fpinjava.data.Pair;
import fpinjava.test.Gen;
import fpinjava.test.Prop;
import fpinjava.util.RNG;

public class Ch8 {

	public static void main(String[] args) {

		RNG rng = new RNG.SimpleRNG(System.currentTimeMillis());
		
		Gen<List<Integer>> g1 = Gen.listOfN(3, Gen.choose(0,5));
		
		System.out.println(g1.sample.run(rng).snd);
		
		System.out.println("");
		
		Gen<List<Double>> g2 = Gen.doubleValue().listOfN(Gen.choose(0, 10));
		
		for(int i=0; i < 10; i++) {
			Pair<RNG, List<Double>> res = g2.sample.run(rng);
			System.out.println(res.snd);
			rng = res.fst;
		}
		
		System.out.println("");
		
		Gen<List<Integer>> g3 = Gen.choose(20, 30).listOfN(Gen.unit(0));
		
		System.out.println(g3.sample.run(rng).snd);
		
		Prop p1 = Prop.forAll(Gen.choose(0,10), n -> -(-n) == n);
		
		System.out.println(p1.run.apply(10, rng));
		
		Prop p2 = Prop.forAll(Gen.choose(0,10), n -> n % 2 == 0);
		
		System.out.println(p2.run.apply(10,rng));
		
		Prop.run(p1.and(p2));
		Prop.run(p1.or(p2));
		Prop.run(p2.or(p1));
		
	}
	
}

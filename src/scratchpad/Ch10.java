package scratchpad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import fpinjava.concurrent.Par;
import fpinjava.data.List;
import fpinjava.data.Monoid;
import fpinjava.data.Monoids.Laws;
import fpinjava.data.Option;
import fpinjava.test.Gen;
import fpinjava.test.Prop;
import static fpinjava.data.Monoids.*;

public class Ch10 {

	public static void main(String[] args) {
		List<Integer> l1 = List.of(1,2);
		List<Integer> l2 = List.of(3,4);

		Monoid<List<Integer>> lm = listMonoid();
		
		System.out.println(lm.op(l1, l2));
		
		Monoid<Option<Integer>> om = optionMonoid();
		
		System.out.println(om.op(Option.some(5), Option.some(7)));
		
		Prop p = Laws.monoidLaws(intAddition, Gen.choose(-10,10));
		
		Prop.run(p);
		
		Gen<Option<Integer>> optionGen = Gen.booleanValue().flatMap(b -> 
			b ? Gen.choose(-10,10).map(i -> Option.some(i)) : Gen.unit(Option.none())
		);

		Prop.run(Laws.monoidLaws(optionMonoid(), optionGen));
		
		System.out.println(foldLeft(List.of(1,2,3,4), "", (s,i) -> i+" - "+s));
		
		Integer[] ints = {1,2,3,4}; // using int[] spoils type inference
		System.out.println(foldMapV(ints, stringMonoid, i -> "*"+i+"*"));
		
		Monoid<String> noisyStringMonoid = new Monoid<String> () {
			@Override public String op(String a1, String a2) {
				System.out.println("Combining Strings: '"+a1+"' & '"+a2+"' - "+Thread.currentThread().getName());
				return a1 + a2;
			}

			@Override public String zero() { return ""; }			
		};
	
		String[] strings = {"lorem", "ipsum", "dolor", "sit"};
		System.out.println(foldMapV(strings, noisyStringMonoid, s -> " "+s));
		
		ThreadFactory daemonThreadFactory = new ThreadFactory() {
			@Override public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
			
		};
		
		ExecutorService es = Executors.newFixedThreadPool(4, daemonThreadFactory);
		
		Par<String> ps = parFoldMap(strings, noisyStringMonoid, s -> s);
		
		System.out.println(Par.run(es,ps));

	}
}

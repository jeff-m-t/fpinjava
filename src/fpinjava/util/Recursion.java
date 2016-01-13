package fpinjava.util;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import fpinjava.data.List;
import fpinjava.data.Pair;


public class Recursion {
	public static <ACC,REC> BiFunction<REC,ACC,ACC> tailRec(Predicate<REC> isBaseCase, BiFunction<REC,ACC,ACC> baseCase, BiFunction<REC,ACC,Pair<REC,ACC>> recurseCase) {
		return (REC inp, ACC initAcc) -> {
			ACC acc = initAcc;
			REC rec = inp;
			while(! isBaseCase.test(rec)) {
				Pair<REC,ACC> step = recurseCase.apply(rec,acc);
				rec = step.fst;
				acc = step.snd;
			}
			return baseCase.apply(rec,acc);
		};
	}

	public static List<Integer> range(int start, int end) {
		BiFunction<Integer,List<Integer>,List<Integer>> tr = tailRec(
			counter -> counter == start,
			(counter,acc) -> acc.cons(counter),
			(counter,acc) -> Pair.of(counter-1, acc.cons(counter))
		);
		
		return tr.apply(end,List.nil());
	}
	
	public static <A> int size(List<A> inp) {
		BiFunction<List<A>,Integer,Integer> tr = tailRec(
			xs -> xs.isEmpty(),
			(xs,acc) -> acc,
			(xs,acc) -> Pair.of(xs.tail(), acc + 1)
		);
		
		return tr.apply(inp,0);
	}
	
	public static void main(String[] args) throws Exception {
		List<Integer> inp = List.of(1,2,3,4,5);
		
		BiFunction<List<Integer>,Integer,Integer> tr = tailRec(
			as -> as.isEmpty(), 
			(rec, acc) -> acc, 
			(rec, acc) -> Pair.of(rec.tail(), acc + rec.head())
		);
		
		System.out.println("Result: "+tr.apply(inp,0));
		
		List<Integer> tailRec = range(0,10000);
		System.out.println("Got list of size "+size(tailRec));
	}
}

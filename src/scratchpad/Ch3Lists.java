package scratchpad;

import fpinjava.data.List;
import fpinjava.data.List.*;

public class Ch3Lists {

	public static void main(String[] args) {
		List<Integer> ints = List.of(1,2,3,4,5);
		
		System.out.println(ints);
		System.out.println(ints.take(3));
		System.out.println(ints.take(7));
		System.out.println(ints.drop(2));
		System.out.println(ints.drop(7));
		System.out.println(ints.takeWhile(a -> a < 4));
		System.out.println(ints.dropWhile(a -> a < 4));
		System.out.println(ints.cons(25));
		System.out.println(ints.append(ints));
		System.out.println(ints.init());

		System.out.println(ints.foldRight(0,(a,b) -> a+b));
		System.out.println(ints.foldRight(new StringBuilder(),(a,b) -> b.append(String.valueOf(a))));
		List<Integer> zero = List.nil();
		System.out.println(ints.foldRight(zero,(a,b) -> new Cons<Integer>(a,b)));
		System.out.println(ints.foldRight(0,(a,b)->b+1));

		System.out.println(ints.foldLeft(0,(b,a) -> a+b));
		System.out.println(ints.foldLeft(new StringBuilder(),(b,a) -> b.append(String.valueOf(a))));
		System.out.println(ints.foldLeft(zero,(b,a) -> new Cons<Integer>(a,b)));
		System.out.println(ints.foldLeft(0,(b,a)->b+1));

		System.out.println(ints.foldRight(ints, (a,b) -> new Cons<Integer>(a,b)));
		
		System.out.println(ints.map(a -> String.valueOf(a)));
		
		System.out.println(ints.filter(a -> a % 2 == 0));
		
		System.out.println(ints.flatMap(a -> List.of(a,a)));
		
		System.out.println(ints.zipWith(ints, (a,b) -> a * b));
		
		System.out.println(ints.forAll(a -> a < 5));
		
		System.out.println(ints.exists(a -> a < 5));
		
		System.out.println(ints.find(a -> a % 2 == 0));
		
		System.out.println(ints.find(a -> a > 5));
	}

}

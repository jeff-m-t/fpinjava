package scratchpad;

import fpinjava.data.Either;
import fpinjava.data.List;

public class Ch4Either {

	public static void main(String[] args) {

		Either<String,Integer> rightInt = Either.right(5);
		Either<String,Integer> leftString = Either.left("Boom!");
		
		System.out.println(rightInt.map(i -> "The value is "+i));
		System.out.println(leftString.map(i -> "The value is "+i));
	
		System.out.println(Either.sequence(List.of(rightInt,rightInt)));
		System.out.println(Either.sequence(List.of(rightInt,leftString)));
		
		System.out.println(rightInt.map2(rightInt,(i,j) -> i + j));

		System.out.println(Either.traverse(List.of("123","456","789"),Ch4Either::parse));
		System.out.println(Either.traverse(List.of("123","456","foo"),Ch4Either::parse));
		
	}

	private static Either<String,Integer> parse(String input) {
		try {
			return Either.right(Integer.valueOf(input));
		}
		catch(Exception e) {
			return Either.left(e.getClass().getSimpleName()+": "+e.getMessage());
		}
	}

}

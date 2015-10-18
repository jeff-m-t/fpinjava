package scratchpad;

import fpinjava.data.List;
import fpinjava.data.Option;

public class Ch4Option {

	public static void main(String[] args) {
		Option<Integer> someInt = Option.some(5);
		Option<Integer> noneInt = Option.none();
		
		System.out.println(someInt.map(i -> "The value is "+i));
		System.out.println(noneInt.map(i -> "The value is "+i));
		
		System.out.println(Option.sequence(List.of(someInt,someInt)));
		System.out.println(Option.sequence(List.of(someInt,noneInt)));
		
		System.out.println(Option.traverse(List.of("123","456","7809"),Ch4Option::parse));
		System.out.println(Option.traverse(List.of("123","456","foo"),Ch4Option::parse));
	}
	
	private static Option<Integer> parse(String input) {
		try {
			return Option.some(Integer.valueOf(input));
		}
		catch(Exception e) {
			return Option.none();
		}
	}
}

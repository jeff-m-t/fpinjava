package scratchpad;

import fpinjava.data.Tree;
import fpinjava.data.Tree.*;
public class Ch3Trees {

	public static void main(String[] args) {
		
		Tree<Character> bottom = new Branch<Character>(new Leaf<Character>('d'),new Leaf<Character>('e'));
		Tree<Character> left = new Branch<Character>(new Leaf<Character>('a'),new Leaf<Character>('b'));
		Tree<Character> right = new Branch<Character>(new Leaf<Character>('c'),bottom);
		Tree<Character> tree = new Branch<Character>(left,right);

		System.out.println(tree);
		System.out.println(tree.size());
		System.out.println(tree.max((Character a,Character b) -> (int)a -(int)b ));
		System.out.println(tree.depth());
		System.out.println(tree.map(a -> "The char is: "+a));
	}

}

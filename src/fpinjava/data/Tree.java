package fpinjava.data;

import java.util.Comparator;
import java.util.function.Function;

public abstract class Tree<A> {
	
	private Tree() {};
	
	public abstract boolean isLeaf();
	
	public int size() {
		return fold(l -> 1, b -> 1 + b.left().size() + b.right().size());
	}
		
	public A max(Comparator<A> comp) {
		return fold(
			l -> l.value(), 
			b -> {
				A leftMax = b.left().max(comp);
				A rightMax = b.right().max(comp);
				return comp.compare(leftMax,rightMax) > 0 ? leftMax : rightMax;
			}
		);
	}
	
	public int depth() {
		return fold(l -> 1,b -> 1 + Math.max(b.left().depth(), b.right().depth()));
	}
	
	public <B> Tree<B> map(Function<A,B> f) {
		return fold(l -> new Leaf<B>(f.apply(l.value())),b -> new Branch<B>(b.left().map(f),b.right().map(f)));
	}
	
	public <RES> RES fold(Function<Leaf<A>,RES> l, Function<Branch<A>,RES> b) {
		return (this instanceof Leaf) ? l.apply((Leaf<A>)this) : b.apply((Branch<A>)this);
	}
	
	public static final class Branch<A> extends Tree<A> {
		private final Tree<A> left;
		private final Tree<A> right;
		
		public Branch(Tree<A> left, Tree<A> right) {
			this.right = right;
			this.left = left;
		}
		
		public boolean isLeaf() { return false; }
		
		public Tree<A> left() { return left; }
		public Tree<A> right() { return right; }
		
		@Override
		public String toString() {
			return new StringBuilder("Branch(")
			              .append(left.toString())
			              .append(',')
			              .append(right.toString())
			              .append(')')
			              .toString();
		}
	}
	
	public static final class Leaf<A> extends Tree<A> {
		private final A value;
		
		public Leaf(A a) {
			this.value = a;
		}
		
		public boolean isLeaf() { return true; } 
		
		public A value() { return value; }
		
		@Override
		public String toString() {
			return new StringBuilder("Leaf(")
			               .append(value.toString())
			               .append(')')
			               .toString();
		}
	}
}

package fpinjava.data;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class List<A> {

	private List() {};
	
	public abstract A head();
	public abstract List<A> tail();
	protected abstract StringBuilder buildString(StringBuilder sb);
	
	@Override public String toString() { 
		StringBuilder sb = new StringBuilder("List(");
		return buildString(sb).append(")").toString();			
	};
	
	public boolean isEmpty() { return this instanceof Nil; }
	public boolean isNotEmpty() { return ! isEmpty(); }
	
	public List<A> cons(A a) {
		return new Cons<A>(a,this);
	}
	
	public List<A> append(List<A> other) {
		return foldRight(other, (a,b) -> new Cons<A>(a,b));
	}
	
	public List<A> take(int num) {
		if(num ==0 || isEmpty()) return nil();
		else return new Cons<A>(head(), tail().take(num - 1));
	}
	
	public List<A> drop(int num) {
		if(num == 0 || isEmpty()) return this;
		else return tail().drop(num-1);
	}
	
	public List<A> takeWhile(Predicate<A> condition) {
		if(isEmpty() || !condition.test(head())) return nil();
		else return new Cons<A>(head(), tail().takeWhile(condition));		
	}
	
	public List<A> dropWhile(Predicate<A> condition) {
		if(isEmpty() || !condition.test(head())) return this;
		else return tail().dropWhile(condition);
	}
	
	public List<A> init() {
		if(isEmpty() || tail().isEmpty()) return nil();
		else return new Cons<A>(head(),tail() .init());
	}
	
	public <B> B foldRight(B zero, BiFunction<A,B,B> f) {
		if(isEmpty()) return zero;
		else return f.apply(head(),tail().foldRight(zero, f));
	}
	
	public <B> B foldLeft(B zero, BiFunction<B,A,B> f) {
		if(isEmpty()) return zero;
		else return tail().foldLeft(f.apply(zero,head()),f);
	}
	
	public <B> B fold(Function<Nil<A>,B> nil, Function<Cons<A>,B> cons) {
		if(isEmpty()) return nil.apply((Nil<A>)this);
		else return cons.apply((Cons<A>)this);
	}
	
	public <B> List<B> map(Function<A,B> f) {
		if(isEmpty()) return nil();
		else return new Cons<B>(f.apply(head()),tail().map(f));
	}
	
	public List<A> filter(Predicate<A> p) {
		return flatMap(a -> p.test(a) ? List.of(a) : nil());
	}
	
	public <B> List<B> flatMap(Function<A,List<B>> f) {
		if(isEmpty()) return nil();
		else return f.apply(head()).append(tail().flatMap(f));
	}
	
	public <B,C> List<C> zipWith(List<B> other, BiFunction<A,B,C> f) {
		if(isEmpty() || other.isEmpty()) return nil();
		else return new Cons<C>(f.apply(head(),other.head()), tail().zipWith(other.tail(),f));
	}
	
	public boolean forAll(Predicate<A> p) {
		return foldLeft(true, (b,a) -> p.test(a) && b);
	}
	
	public boolean exists(Predicate<A> p) {
		if(isEmpty()) return false;
		else if(p.test(head())) return true;
		else return tail().exists(p);
	}
	
	public Optional<A> find(Predicate<A> p) {
		if(isEmpty()) return Optional.empty();
		else if(p.test(head())) return Optional.of(head());
		else return tail().find(p);
	}
	
	@SuppressWarnings("unchecked")
	public static <A> List<A> nil() {
		return (Nil<A>) Nil.INSTANCE;
	}
	
	private static final class Nil<A> extends List<A> {
		private static Nil<?> INSTANCE = new Nil<Object>();
		
		public A head() { throw new Error("head on empty list"); }
		public List<A> tail() { throw new Error("tail on empty list."); }
		
		protected StringBuilder buildString(StringBuilder sb) { 
			if(sb.length() == 0 || sb.charAt(sb.length()-1) != ',') return sb;
			else return sb.deleteCharAt(sb.length() -1);
		}		
	}
	
	public static class Cons<A> extends List<A> {
		public final A head;
		public final List<A> tail;
		
		public Cons(A head, List<A> tail) {
			this.head = head;
			this.tail = tail;
		}
		
		public A head() { return head; }
		public List<A> tail() {return tail; }
		
		protected StringBuilder buildString(StringBuilder sb) { 
			sb.append(head.toString()).append(',');
			return tail.buildString(sb);
		}
	}
	
	
	@SafeVarargs
	public static <A> List<A> of(A... as) {
		if(as.length == 0) return nil();
		else {
			A a = as[0];
			A[] rest = Arrays.copyOfRange(as, 1, as.length);
			return new Cons<A>(a, of(rest));
		}
	}
}

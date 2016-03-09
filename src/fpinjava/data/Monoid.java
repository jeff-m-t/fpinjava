package fpinjava.data;

public interface Monoid<A> {
	public A op(A a1, A a2);
	public A zero();	
}

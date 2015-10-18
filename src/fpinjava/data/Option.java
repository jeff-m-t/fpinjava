package fpinjava.data;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import fpinjava.data.List.*;

public abstract class Option<T> {
	
	private Option() {}  

	// Constructors
	public static <A> Option<A> some(A a) { return new Some<A>(a); }
	@SuppressWarnings("unchecked") public static <A> Option<A> none() { return (None<A>) None.INSTANCE; }
	
	
	// API
	public boolean isEmpty() { return this instanceof None; }
	public boolean isNotEmpty() { return ! isEmpty(); }
	public abstract Option<T> orElse(Option<T> that);
	public abstract T getOrElse(T t);
	public abstract Option<T> filter(Predicate<T> p);
	public abstract <U> Option<U> map(Function<T,U> f);
	public abstract <U> Option<U> flatMap(Function<T,Option<U>> f);
	public abstract <C> C fold(Supplier<C> none, Function<T,C> some);

	// Derived API
	public <B,C> Option<C> map2(Option<B> b, BiFunction<T,B,C> f) {
		return this.flatMap(aa -> b.map(bb -> f.apply(aa,bb)));
	}
	
	public static <A,B> Function<Option<A>,Option<B>> lift(Function<A,B> f) {
		return oa -> oa.map(f);
	}
	
	public static <A,B> Option<List<B>> traverse(List<A> as, Function<A,Option<B>> f) {
		if(as.isEmpty()) return some(List.nil());
		else {
			A h = ((Cons<A>)as).head();
			List<A> t = ((Cons<A>)as).tail();
			return f.apply(h).map2(traverse(t,f),(a,lst) -> lst.cons(a));
		}
	}

	public static <A> Option<List<A>> sequence(List<Option<A>> opts) {
		return traverse(opts,a->a);
	}
	
	// Instances
	public final static class Some<T> extends Option<T> {
		private final T t;
		
		public Some(T t) { this.t = t; }
		
		public Option<T> orElse(Option<T> that) { return this; }
		
		public T getOrElse(T t) { return this.t; }
		
		public Option<T> filter(Predicate<T> p) { return p.test(t) ? this : new None<T>(); }
		
		public <U> Option<U> map(Function<T,U> f) { return new Some<U>(f.apply(t)); }
		
		public <U> Option<U> flatMap(Function<T,Option<U>> f) { return f.apply(t); }
		
		public <C> C fold(Supplier<C> none, Function<T,C> some) { return some.apply(t); }
	
		@Override public String toString() { return "Some("+t+")"; }
	}
		
	public final static class None<T> extends Option<T> {
		private static None<?> INSTANCE = new None<Object>();
		
		public None() {}
		
		public Option<T> orElse(Option<T> that) { return that; }
		
		public T getOrElse(T t) { return t; }
		
		public Option<T> filter(Predicate<T> p) { return this; }
		
		public <U> Option<U> map(Function<T,U> f) { return new None<U>(); }
		
		public <U> Option<U> flatMap(Function<T,Option<U>> f) { return new None<U>(); }
		
		public <C> C fold(Supplier<C> none, Function<T,C> some) { return none.get(); }

		@Override public String toString() { return "None"; }
	}

	// Historical
	public static <A> Option<List<A>> sequence_orig(List<Option<A>> opts) {
		Option<List<A>> zero = some(List.nil());
		return opts.foldRight(zero, (oa,acc) -> {
			Option<List<A>> liftedA = oa.map(a -> List.of(a));
			return liftedA.map2(acc,(la,lb) -> la.append(lb));
		});
	}
	
	public static <A> Option<List<A>> sequence_alt(List<Option<A>> opts) {
		if(opts.isEmpty()) return some(List.nil());
		else {
			Cons<Option<A>> cns = (Cons<Option<A>>)opts;
			Option<A> h = cns.head();
			List<Option<A>> t = cns.tail();
			return h.flatMap(hh -> sequence_alt(t).map(lst -> lst.cons(hh)));
		}
	}
	
	public static <A,B> Option<List<B>> traverse_orig(List<A> as, Function<A,Option<B>> f) {
		Option<List<B>> zero = some(List.nil());
		return as.foldRight(zero, (a,acc) -> {
			Option<B> ob = f.apply(a);
			Option<List<B>> liftedB = ob.map(b -> List.of(b));
			return liftedB.map2(acc,(la,lb) -> la.append(lb));
		});		
	}


}

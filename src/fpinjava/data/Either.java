package fpinjava.data;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import fpinjava.data.List.Cons;

public abstract class Either<E, A> {

	private Either() {};

	// Constructors
	public static <E,A> Either<E,A> right(A a) { return new Right<E,A>(a); }
	public static <E,A> Either<E,A> left(E e) { return new Left<E,A>(e); }
		
	// API
	public boolean isRight() { return this instanceof Right; }
	public boolean isLeft() {return ! isRight(); }
	public abstract <B> Either<E,B> map(Function<A,B> f);
	public abstract <B> Either<E,B> flatMap(Function<A,Either<E,B>> f);
	public abstract Either<E, ? super A> orElse(Supplier<Either<E,? super A>> other);
	public abstract <C> C fold(Function<E,C> left, Function<A,C> right);

	// Derived API
	public <B,C> Either<E, C> map2(Either<E, B> b, BiFunction<A,B,C> f) {
		return this.flatMap(aa -> b.map(bb -> f.apply(aa,bb)));
	}
	
	public static <E,A,B> Function<Either<E,A>,Either<E,B>> lift(Function<A,B> f) {
		return oa -> oa.map(f);
	}
	public static <E,A,B> Either<E, List<B>> traverse(List<A> as, Function<A,Either<E,B>> f) {
		if(as.isEmpty()) return right(List.nil());
		else {
			A h = ((Cons<A>)as).head();
			List<A> t = ((Cons<A>)as).tail();
			return f.apply(h).map2(traverse(t,f),(a,lst) -> lst.cons(a));
		}
	}
	
	public static <E,A> Either<E,List<A>> sequence(List<Either<E,A>> eas) {
		return traverse(eas,a->a);
	}

	// Instances
	public final static class Right<E,A> extends Either<E,A> {
		private final A a;
		public Right(A a) { this.a = a; }
		
		public <B> Either<E, B> map(Function<A, B> f) {
			return new Right<E,B>(f.apply(a));
		}

		public <B> Either<E, B> flatMap(Function<A, Either<E, B>> f) {
			return f.apply(a);
		}

		public Either<E, ? super A> orElse(Supplier<Either<E, ? super A>> other) {
			return this;
		}

		public <C> C fold(Function<E,C> left, Function<A,C> right) { return right.apply(a); }

		@Override public String toString() { return "Right("+a+")"; }
	}
	
	public final static class Left<E,A> extends Either<E,A> {
		private final E e;
		public Left(E e) { this.e = e; }

		public <B> Either<E, B> map(Function<A, B> f) {
			return new Left<E,B>(e);
		}

		public <B> Either<E, B> flatMap(Function<A, Either<E, B>> f) {
			return new Left<E, B>(e);
		}

		public Either<E, ? super A> orElse(Supplier<Either<E, ? super A>> other) {
			return other.get();
		}		

		public <C> C fold(Function<E,C> left, Function<A,C> right) { return left.apply(e); }

		@Override public String toString() { return "Left("+e+")"; }

	}

	// Historical
	public static <E,A> Either<E,List<A>> sequence_orig(List<Either<E,A>> es) {
		Either<E,List<A>> zero = right(List.nil());
		return es.foldRight(zero, (ea,acc) -> {
			Either<E,List<A>> liftedA = ea.map(a -> List.of(a));
			return liftedA.map2(acc,(la,lb) -> la.append(lb));
		});
	}
	
	public static <E,A> Either<E,List<A>> sequence_alt(List<Either<E,A>> eas) {
		if(eas.isEmpty()) return right(List.nil());
		else {
			Cons<Either<E,A>> cns = (Cons<Either<E,A>>)eas;
			Either<E,A> h = cns.head();
			List<Either<E,A>> t = cns.tail();
			return h.flatMap(hh -> sequence_alt(t).map(lst -> lst.cons(hh)));
		}
	}


	public static <E,A,B> Either<E, List<B>> traverse_orig(List<A> as, Function<A,Either<E,B>> f) {
		Either<E,List<B>> zero = right(List.nil());
		return as.foldRight(zero,(a,acc) -> {
			Either<E,B> eb = f.apply(a);
			Either<E,List<B>> liftedB = eb.map(b->List.of(b));
			return liftedB.map2(acc,(la,lb) -> la.append(lb));
		});
	}

}

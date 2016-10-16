package fpinjava.util;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class PartialFunction<A,B> {
	
	public abstract boolean isDefinedAt(A a);
	public abstract B apply(A a);
	
	public PartialFunction<A,B> orElse(PartialFunction<A,B> other) {
		return new PartialFunction<A,B>() {
			public boolean isDefinedAt(A a) { return this.isDefinedAt(a) || other.isDefinedAt(a); }
			public B apply(A a) {
				if(! isDefinedAt(a)) throw new IllegalArgumentException("Match Error");
				else return this.isDefinedAt(a) ? this.apply(a) : other.apply(a);
			}
		};
	}
		
	public static class Case<A,B> extends PartialFunction<A,B> {

		private final Class<A> clazz;
		private final Predicate<A> guard;
		private final Function<A,B> body;
		
		public Case(Class<A> clazz, Predicate<A> guard, Function<A,B> app) {
			this.clazz = clazz;
			this.guard = guard;
			this.body = app;
		}
		
		@Override
		public boolean isDefinedAt(A a) {
			return (clazz.equals(a.getClass())) && guard.test(a);
		}

		@Override
		public B apply(A a) {
			if(! isDefinedAt(a)) throw new IllegalArgumentException("Match Error");
			else return body.apply(a);
		}
		
	}
}

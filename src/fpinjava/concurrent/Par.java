package fpinjava.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.concurrent.Callable;

import fpinjava.data.List;
import fpinjava.data.Option;
import fpinjava.data.Either;
import fpinjava.data.Pair;
import fpinjava.data.Unit;
import fpinjava.data.List.Cons;

public class Par<A> {

	private final Function<ExecutorService,Future<A>> _body;
	
	private Par(Function<ExecutorService,Future<A>> body) {
		_body = body;
	}

	// Constructors
	public static <A> Par<A> unit(A a) {
		return new Par<A>((es) -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) { cb.accept(a); };
		});
	}
	
	public static <A> Par<A> delay(Supplier<A> sa) {
		return new Par<A>((es -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) { cb.accept(sa.get()); };
		}));
	}
	
	public static <A> Par<A> fork(Supplier<Par<A>> a) {
		return new Par<A>((es) -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) {
				Supplier<Unit> supplier = () -> {
					a.get()._body.apply(es).apply(foo -> cb.accept(foo));
					return Unit.unit;
				};
				eval(es,supplier);
			}
		});
	}
	
	public static <A> Par<A> async(Function<Consumer<A>,Unit> f) {
		return new Par<A>((es) -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) { f.apply(cb); }
		});
	}
	
	public static <A> Par<A> lazyUnit(Supplier<A> a) {
		return fork(() -> unit(a.get()));
	}
	
	
	public static <A,B,C> Par<C> map2(Par<A> para, Par<B> parb, BiFunction<A,B,C> f) {
		return new Par<C>((es) -> new Future<C>() {
			@Override
			public void apply(Consumer<C> cb) {
			
				final Actor<Either<A,B>> combiner = Actor.of(es, new Consumer<Either<A,B>> () {
					Option<A> ar = Option.none();
					Option<B> br = Option.none();
					
					@Override public void accept(Either<A, B> eab) {
						eab.fold(
							a -> br.fold(
									() -> { ar = Option.some(a); return Unit.unit(); },
									b -> {
										eval(es,() -> { cb.accept(f.apply(a,b)); return Unit.unit(); });
										return Unit.unit(); }
								 ), 
							b -> ar.fold(
									() -> { br = Option.some(b); return Unit.unit(); },
									a -> {
										eval(es,() -> { cb.accept(f.apply(a,b)); return Unit.unit(); });
										return Unit.unit();
									}
								 )
						);
					}
				});
				
				para._body.apply(es).apply( a -> combiner.tell(Either.left(a))  );
				parb._body.apply(es).apply( b -> combiner.tell(Either.right(b)) );
			}
		});
	}

	public <B,C> Par<C> map2(Par<B> other, BiFunction<A,B,C> f) {
		return map2(this,other,f);
	}

	public static <A,B> Function<A,Par<B>> asyncF(Function<A,B> f) {
		return a -> lazyUnit(() -> f.apply(a)); 
	}
	
	public static <A,B> Par<List<B>> traverse(List<A> as, Function<A,Par<B>> f) {
		if(as.isEmpty()) return unit(List.nil());
		else {
			A h = ((Cons<A>)as).head();
			List<A> t = ((Cons<A>)as).tail();
			return f.apply(h).map2(traverse(t,f),(a,lst) -> lst.cons(a));
		}	
	}
	
	public static <A> Par<List<A>> sequence(List<Par<A>> ps) {
		return traverse(ps,a->a);
	}
	
	public static <A, B> Par<B> map(Par<A> pa, Function<A,B> f) {
		return map2(pa, unit(Unit.unit()), (a,u) -> f.apply(a));
	}
	
	public <B> Par<B> map(Function<A,B> f) {
		return map(this,f);
	}
	
	public static <A,B> Par<List<B>> parMap(List<A> ps, Function<A,B> f) {
		List<Par<B>> fbs = ps.map(asyncF(f));
		return sequence(fbs);
	}

	public static <A> Par<List<A>> parFilter(List<A> as, Predicate<A> f) {
		List<Par<List<A>>> pars = as.map(asyncF(a -> f.test(a) ? List.of(a) : List.nil()));
		return sequence(pars).map(lls -> List.flatten(lls));
	}

	public static <A> Par<A> choice(Par<Boolean> p, Par<A> t, Par<A> f) {
		return flatMap(p, cond -> cond ? t : f);
	}

	public static <A> Par<A> choiceN(Par<Integer> n, List<Par<A>> choices) {
		return flatMap(n, idx -> choices.drop(idx-1).head());
	}
	
	public static <A,B> Par<B> flatMap(Par<A> pa, Function<A,Par<B>> f) {
		return new Par<B>((es) -> new Future<B>() {
			@Override public void apply(Consumer<B> cb) {
				Consumer<A> first = a -> {
					eval(es, () -> { 
						f.apply(a)._body.apply(es).apply(cb); 
						return Unit.unit();
					});
				};
				pa._body.apply(es).apply(first);
			}
		});
	}
	
	public static <A> Par<A> join(Par<Par<A>> ppa) {
		return new Par<A>((es) -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) {
				Consumer<Par<A>> first = pa -> {
					eval(es,() -> {
						pa._body.apply(es).apply(cb); 
						return Unit.unit();
					});
				};
				ppa._body.apply(es).apply(first);
			}
		});
	}

	// Interfaces
	interface Future<A> {
		void apply(Consumer<A> cb);
	}
	
	public static <A> A run(ExecutorService es, Par<A> pa) {
		final AtomicReference<A> ref = new AtomicReference<>();
		final CountDownLatch latch = new CountDownLatch(1);
		
		pa._body.apply(es).apply(a -> { ref.set(a); latch.countDown(); });

		try { latch.await(); } catch(Throwable ex) { throw new RuntimeException("Caught checked exception. Rethrowing", ex); };
		
		return ref.get();
	}
	
	static void eval(ExecutorService es, Supplier<Unit> r) {
		es.submit(new Callable<Unit>() {
			@Override public Unit call() { return r.get(); }
		});
	}
	
	
}

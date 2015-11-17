package fpinjava.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.Callable;

import fpinjava.data.Unit;

public class ParNB<A> {

	interface Future<A> {
		void apply(Consumer<A> cb);
	}
	
	private final Function<ExecutorService,Future<A>> _body;
	
	private ParNB(Function<ExecutorService,Future<A>> body) {
		_body = body;
	}
	
	public static <A> A run(ExecutorService es, ParNB<A> pa) throws InterruptedException {
		final AtomicReference<A> ref = new AtomicReference<>();
		final CountDownLatch latch = new CountDownLatch(1);
		
		pa._body.apply(es).apply(a -> { ref.set(a); latch.countDown(); });

		latch.await();
		
		return ref.get();
	}
	
	protected static void eval(ExecutorService es, Supplier<Unit> r) {
		es.submit(new Callable<Unit>() {
			@Override public Unit call() { return r.get(); }
		});
	}
	
	public static <A> ParNB<A> unit(A a) {
		return new ParNB<A>((es) -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) { cb.accept(a); };
		});
	}
	
	public static <A> ParNB<A> fork(Supplier<ParNB<A>> a) {
		return new ParNB<A>((es) -> new Future<A>() {
			@Override public void apply(Consumer<A> cb) {
				Supplier<Unit> supplier = () -> {
					a.get()._body.apply(es).apply(foo -> cb.accept(foo));
					return Unit.unit;
				};
				eval(es,supplier);
			}
		});
	}
	
	public static <A,B,C> ParNB<C> map2(ParNB<A> para, ParNB<B> parb, BiFunction<A,B,C> f) {
		return new ParNB<C>((es) -> new Future<C>() {
			@Override public void apply(Consumer<C> cb) {
				Future<A> aF = para._body.apply(es);
				Future<B> bF = parb._body.apply(es);
				
				A a = null;
				B b = null;
				
				cb.accept(f.apply(a,b));
			}
		});
	}
}

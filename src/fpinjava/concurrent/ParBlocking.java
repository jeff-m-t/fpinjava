package fpinjava.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.concurrent.Future;

public class ParBlocking<A> {

	private final Function<ExecutorService,Future<A>> _body;
	
	private ParBlocking(Function<ExecutorService,Future<A>> body) {
		_body = body;
	}

	public static <A> Future<A> run(ExecutorService es, ParBlocking<A> pa) {
		return pa._body.apply(es);
	}
	
	public static <A> ParBlocking<A> unit(A a) { 
		return new ParBlocking<A>((es) -> new UnitFuture<A>(a));
	}

	public static <A> ParBlocking<A> fork(Supplier<ParBlocking<A>> a) {
		return new ParBlocking<A>((es) -> es.submit(new Callable<A>() {
			@Override public A call() { 
				try {
					return a.get()._body.apply(es).get(); 
				}
				catch(Throwable ex) {
					throw new RuntimeException("Caught Checked Exception",ex);
				}
			}
		}));
	}
	
	public static <A> ParBlocking<A> lazyUnit(Supplier<A> a) {
		return fork(() -> unit(a.get()));
	}
	
	public static <A,B,C> ParBlocking<C> map2(ParBlocking<A> para, ParBlocking<B> parb, BiFunction<A,B,C> f) {
		return new ParBlocking<C>((es) -> {
			Future<A> fa = para._body.apply(es);
			Future<B> fb = parb._body.apply(es);
			
			try {
				return new UnitFuture<C>(f.apply(fa.get(),fb.get()));
			} 
			catch(Throwable ex) {
				throw new RuntimeException("Caught Checked Exception",ex);
			}
		});
	}
	
	public <B,C> ParBlocking<C> map2(ParBlocking<B> other, BiFunction<A,B,C> f) {
		return map2(this,other,f);
	}

	private static class UnitFuture<A> implements Future<A> {
		private final A value;
		
		public UnitFuture(A a) { value = a; }
		
		@Override public boolean cancel(boolean mayInterruptIfRunning) { return false; }

		@Override public boolean isCancelled() { return false; }

		@Override public boolean isDone() { return true; }

		@Override
		public A get() throws InterruptedException, ExecutionException {
			return value;
		}

		@Override
		public A get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return value;
		}		
	}
	
}

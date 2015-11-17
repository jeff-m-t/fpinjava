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

public class Par<A> {

	private final Function<ExecutorService,Future<A>> _body;
	
	private Par(Function<ExecutorService,Future<A>> body) {
		_body = body;
	}

	public static <A> Future<A> run(ExecutorService es, Par<A> pa) {
		return pa._body.apply(es);
	}
	
	public static <A> Par<A> unit(A a) { 
		return new Par<A>((es) -> new UnitFuture<A>(a));
	}

	public static <A> Par<A> fork(Supplier<Par<A>> a) {
		return new Par<A>((es) -> es.submit(new Callable<A>() {
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
	
	public static <A,B,C> Par<C> map2(Par<A> para, Par<B> parb, BiFunction<A,B,C> f) {
		return new Par<C>((es) -> {
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

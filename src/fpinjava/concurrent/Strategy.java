package fpinjava.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

public abstract class Strategy {
    private Strategy() {}
    
    public abstract <A> Supplier<A> apply(Supplier<A> sup);
    
	public static Strategy fromExecutorService(ExecutorService es) {
    	return new Strategy() {
   		 	@Override
   		 	public <A> Supplier<A> apply(Supplier<A> sup) {
   		 		final Future<A> f = es.submit(new Callable<A>() {
   		 			@Override public A call() throws Exception {
   		 				return sup.get();
   		 			}
				});
   		 		return () -> {
  	   				try {
  	   					return f.get();
   	   				} catch (InterruptedException | ExecutionException e) {
   	   					throw new RuntimeException("Caught a checked exception. Rethrowing", e);
   	   				}
   	   			};   				    		 			
			 };
    	};
    }
    
    public static Strategy sequential() {
   	 return new Strategy() {
   		 @Override
   		 public <A> Supplier<A> apply(Supplier<A> sup) {
   			 final A evaluatedA = sup.get();
   			 return new Supplier<A>() {
   				 @Override public A get() { return evaluatedA; }
   			 };
   		 }
   		 
   	 };
    }
}

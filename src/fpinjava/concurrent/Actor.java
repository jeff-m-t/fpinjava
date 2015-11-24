package fpinjava.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Actor<A> {
    private final Strategy strategy;
    private final Consumer<A> handler;
    private final Consumer<Throwable> onError;
    
    private final AtomicInteger suspended = new AtomicInteger(1);
    private final AtomicReference<Node<A>> tail = new AtomicReference<>(new Node<A>());
    private final AtomicReference<Node<A>> head = new AtomicReference<>(tail.get());
    
    public static <A> Actor<A> of(ExecutorService es, Consumer<A> handler) {
    	final Strategy strategy = Strategy.fromExecutorService(es);
    	final Consumer<Throwable> onError = (ex) -> { throw new RuntimeException("Exception in Actor", ex); };

    	return new Actor<A>(strategy,handler,onError);
    }
    
    public Actor(Strategy strategy, Consumer<A> handler, Consumer<Throwable> onError) {
   	 this.strategy = strategy;
   	 this.handler = handler;
   	 this.onError = onError;   	 
    }
    
    public void tell(A a) {
   	 Node<A> n = new Node<>(a);
   	 head.getAndSet(n).lazySet(n);
   	 trySchedule();
    }
    
    public void apply(A a) {
   	 this.tell(a);
    }
    
    private void trySchedule() {
   	 if (suspended.compareAndSet(1, 0)) {
   		 schedule();
   	 }
    }
    
    private void schedule() {
   	 strategy.apply(() -> act());
    }
    
    private int act() {
   	 Node<A> t = tail.get();
   	 Node<A> n = batchHandle(t,1024);
   	 
   	 if(n != t) {
   		 n.setValue(null);
   		 tail.lazySet(n);
   		 schedule();
   	 }
   	 else {
   		 suspended.set(1);
   		 if(n.getValue() != null) trySchedule();
   	 }
   	 return 0;
    }
    
    // TODO: No Tail Call Elimination so convert to loop
    private Node<A> batchHandle(Node<A> t, int i) {
   	 Node<A> n = t.get();
   	 if(n != null) {
   		 try {
   			 handler.accept(n.getValue());
   		 }
   		 catch(Throwable ex) {
   			 onError.accept(ex);
   		 }
   		 return (i > 0) ? batchHandle(n, i - 1) : n;
   	 }
   	 else {
   		 return t;
   	 }
    }

    public static void sleep(long millis) {
   	 try {
   		 Thread.sleep(millis);
   	 }
   	 catch(InterruptedException ie) {
   		 System.out.println("Interrupted");
   	 }
    }
    
    public static void main(String[] args) throws Exception {
   	 ExecutorService es = Executors.newFixedThreadPool(4);
   	 Strategy strategy = Strategy.fromExecutorService(es);
   	 
   	 Actor<String> actor = new Actor<>(
   		 strategy,
   		 (a) -> System.out.println("I got: "+a+" ("+Thread.currentThread().getName()+")"),
   		 (ex) -> System.out.println("ERROR:"+ex.getMessage())
   	 );
    
   	 System.out.println("Sending messages");
   	 actor.tell("Hello");
   	 actor.tell("World");
    	actor.apply("FOO");
    	actor.apply("BAR");
    	actor.apply("FOO1");
    	actor.apply("BAR1");
    	actor.apply("FOO2");
    	actor.apply("BAR2");

   	 System.out.println("Sent messages");
   	 Thread.sleep(1000);
   	 
   	 System.out.println(strategy.apply(() -> {System.out.println("Thread: "+Thread.currentThread().getName()); return "foo";}).get());
   	 
   	 Thread.sleep(1000L);
   	 es.shutdown();
    }
    
    final private class Node<T> extends AtomicReference<Node<A>> {
		private static final long serialVersionUID = 710892348547358616L;
		private T value;
    	
    	Node() {
    		this(null);
    	}
    	
    	Node(T value) {
    		this.value = value;
    	}
    	
    	T getValue() { return this.value; }
    	void setValue(T value) { this.value = value; }
    }
}

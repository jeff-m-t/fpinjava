package scratchpad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import fpinjava.concurrent.Par;

public class Ch7 {

	public static void main(String[] args) throws Exception {
		ThreadFactory daemonThreadFactory = new ThreadFactory() {
			@Override public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
			
		};
		
		ExecutorService es = Executors.newFixedThreadPool(4, daemonThreadFactory);

		System.out.println("Main Thread: "+Thread.currentThread().getName());
		
		Par<Integer> p1 = Par.unit(5);
		Par<Boolean> p2 = Par.unit(true);
		
		Par<String> p3 = Par.map2(p1,p2,(i,b) -> "It's "+b+" I got a "+i);
		System.out.println(Par.run(es,p3).get());

		
		
		Par<Integer> p4 = Par.fork(() -> { 
			System.out.println("Running (" + Thread.currentThread().getName() + ")");
			sleepySleep(2000); 
			Par<Integer> par = Par.unit(7);
			System.out.println("Done");
			return par;}
		);
		System.out.println(p4);
		
		System.out.println(Par.run(es,p4).get());
		
	}
	
	public static void sleepySleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch(InterruptedException ie) {}
	}

}

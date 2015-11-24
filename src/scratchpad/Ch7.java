package scratchpad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import fpinjava.concurrent.Par;
import fpinjava.data.List;

public class Ch7 {

	public static void main(String[] args) throws Exception {
		ThreadFactory daemonThreadFactory = new ThreadFactory() {
			@Override public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
			
		};
		
		ExecutorService es = Executors.newFixedThreadPool(8, daemonThreadFactory);

		System.out.println("Main Thread: "+Thread.currentThread().getName());
		
		Par<Integer> p1 = Par.lazyUnit(() -> { sleepySleep(200L); return 5; });
		Par<Boolean> p2 = Par.lazyUnit(() -> { sleepySleep(300L); return true;});
		
		Par<String> p3 = Par.map2(p1,p2,(i,b) -> "It's "+b+" I got a "+i);
		
		long start = System.currentTimeMillis();
		System.out.println(Par.run(es,p3));
		long end = System.currentTimeMillis();
		System.out.println("That took "+(end-start)+" milliseconds");

		List<Integer> inp = List.range(1,360);
		Par<List<Double>> p = Par.parMap(inp , i -> {sleepySleep(10); return Math.sqrt(i); });
		
		start = System.currentTimeMillis();
		List<Double> res = Par.run(es, p);
		System.out.println("inp: "+inp.take(10));
		System.out.println("res: "+res.take(10));
		end = System.currentTimeMillis();
		System.out.println("That took "+(end-start)+" milliseconds");
		
		Par<Integer> chooser = Par.choiceN(Par.unit(4), List.of(
				Par.lazyUnit(() -> {sleepySleep(100L); return 1;}),
				Par.lazyUnit(() -> {sleepySleep(200L); return 2;}),
				Par.lazyUnit(() -> {sleepySleep(300L); return 3;}),
				Par.lazyUnit(() -> {sleepySleep(400L); return 4;}),
				Par.lazyUnit(() -> {sleepySleep(500L); return 5;})
		));
		
		System.out.println(timed(() -> Par.run(es,chooser)));
		
		Par<String> ifp = Par.choice(Par.unit(false), Par.unit("TRUE"), Par.unit("FALSE"));
		
		System.out.println(Par.run(es,ifp));
		
		Par<Par<String>> nested = Par.unit(Par.unit("foo"));
		
		System.out.println(Par.run(es, Par.join(nested)));
		
	}
	
	public static <T> T timed(Supplier<T> body) {
		long start = System.currentTimeMillis();
		T res = body.get();
		long timeMillis = System.currentTimeMillis() - start;
		System.out.println("That took "+timeMillis+" milliseconds");
		return res;
	}
	
	public static void sleepySleep(long millis) {
		try {
			Thread.sleep(millis);
		}
		catch(InterruptedException ie) {}
	}

}

package scratchpad;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import fpinjava.concurrent.Actor;
import fpinjava.concurrent.Strategy;
import fpinjava.data.Option;

public class Ch7Actors {

	public static void main(String[] args) {
	   	 ExecutorService es = Executors.newFixedThreadPool(4);
	   	 Strategy strategy = Strategy.fromExecutorService(es);

	   	Actor<String> actor = Actor.of(es,
			new Consumer<String>() {
				private Option<String> value = Option.none();;
				@Override
				public void accept(String t) {
					System.out.println("Value was "+value+" - Setting a value of '"+t+"'");
					value = Option.some(t);
				}
			}
	   	);

	   	actor.tell("foo");
	   	actor.tell("bar");
	   	actor.tell("baz");
	   	
	   	es.shutdown();
	}
}

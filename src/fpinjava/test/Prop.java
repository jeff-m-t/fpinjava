package fpinjava.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.codepoetics.protonpack.Indexed;
import com.codepoetics.protonpack.StreamUtils;

import fpinjava.data.Pair;
import fpinjava.util.RNG;

public class Prop {
	
	public final BiFunction<Integer,RNG,Result> run;
	
	public Prop(BiFunction<Integer,RNG,Result> run) {
		this.run = run;
	}
	
	public Prop and(Prop other) {
		return new Prop((n,rng) -> {
			Result r1 = this.run.apply(n,rng);
			if(r1.isFalsified()) return r1;
			else return other.run.apply(n,rng);
		});
	}
	
	public Prop or(Prop other) {
		return new Prop((n,rng) -> {
			Result r1 = this.run.apply(n,rng);
			if(! r1.isFalsified()) return r1;
			else return other.run.apply(n,rng);
		});
	}
	
	public static void run(Prop p, int n, RNG rng) {
		Result res = p.run.apply(n,rng);
		if(res.isFalsified()) {
			Result.Falsified fail = (Result.Falsified)res;
			System.out.println("! Failsified after " + fail.successes + " passed tests: "+fail.failedCase);
		}
		else {
			System.out.println("+ OK. Passed "+n+" tests.");
		}
	}
	
	public static void run(Prop p, int n) {
		run(p,n,new RNG.SimpleRNG(System.currentTimeMillis()));
	}
	
	public static void run(Prop p) {
		run(p,100,new RNG.SimpleRNG(System.currentTimeMillis()));
	}
	
	public static <A> Prop forAll(Gen<A> as, Predicate<A> f) {
		return new Prop( (n,rng) -> 
			StreamUtils.zipWithIndex(randomStream(as,rng))
				.limit(n)
				.map(ia -> trySample(ia,f))
				.filter(res -> res instanceof Result.Falsified)
				.findFirst()
				.orElse(new Result.Passed())
		);
	}
	
	public static <A> Result trySample(Indexed<A> ia, Predicate<A> pred) {
		A value = ia.getValue();
		int index = (int)ia.getIndex();

		try {
			if(pred.test(value)) {
				return new Result.Passed();
			}
			else {
				return new Result.Falsified(String.valueOf(value), index);
			}
		}
		catch(Exception e) {
			return new Result.Falsified(buildMsg(value,e),index);
		}
	}
	
	public static <A> Stream<A> randomStream(Gen<A> ga, RNG rng) {
		Pair<RNG,A> seed = ga.sample.run(rng);
		Stream<Pair<RNG,A>> pairs = StreamUtils.unfold(seed, pair -> Optional.of(ga.sample.run(pair.fst)));
		return pairs.map(pair -> pair.snd);
	}
	
	public static <A> String buildMsg(A s, Exception ex) {
		PrintWriter pw = new PrintWriter(new StringWriter());
		ex.printStackTrace(pw);
		
		return new StringBuilder("test case: ").append(s).append("\n")
					.append("generated and exception: ").append(ex.getMessage()).append("\n")
					.append("stack trace: ").append("\n").append(pw.toString())
					.toString();
	}
	
	
}

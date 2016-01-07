package fpinjava.data;

import java.util.function.BiFunction;
import java.util.function.Function;

import fpinjava.data.List.Cons;

public class State<S,A> {
	
	private Function<S,Pair<S,A>> _run;
	
	public State(Function<S,Pair<S,A>> run) {
		this._run = run;
	}
	
	public Pair<S,A> run(S state) { 
		return _run.apply(state); 
	}
	
	public static <S> State<S,S> get() {
		return new State<S,S>(s -> new Pair<S,S>(s,s));
	}
	
	public static <S> State<S,Unit> set(S state) {
		return new State<S,Unit>(s -> new Pair<S,Unit>(state,Unit.unit()));
	}
	
	public static <S >State<S,Unit> modify(Function<S,S> f) {
		State<S,S> init = State.get();
		return init.flatMap(s -> State.set(f.apply(s)));
	}
	
	public static <S,A> State<S,A> unit(A a) {
		return new State<S,A>(s -> new Pair<S,A>(s,a));
	}
	
	public <B> State<S,B> map(Function<A,B> f) {
		return new State<S,B>( s -> {
			Pair<S,A> init = _run.apply(s);
			return Pair.of(init.fst, f.apply(init.snd));
		});
	}
	
	public <B> State<S,B> flatMap(Function<A,State<S,B>> f) {
		return new State<S,B>( s -> {
			Pair<S,A> init = _run.apply(s);
			State<S,B> sb = f.apply(init.snd);
			return sb.run(init.fst);
		});
	}
	
	public <B,C> State<S, C> map2(State<S, B> b, BiFunction<A,B,C> f) {
		return this.flatMap(aa -> b.map(bb -> f.apply(aa,bb)));
	}

	public static <S,A,B> State<S, List<B>> traverse(List<A> as, Function<A,State<S,B>> f) {
		if(as.isEmpty()) return State.unit(List.nil());
		else {
			A h = ((Cons<A>)as).head();
			List<A> t = ((Cons<A>)as).tail();
			return f.apply(h).map2(traverse(t,f),(a,lst) -> lst.cons(a));
		}
	}

	public static <S,A> State<S,List<A>> sequence(List<State<S,A>> eas) {
		return traverse(eas,a->a);
	}

}

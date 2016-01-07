package fpinjava.data;

final public class Pair<A,B> {
	
	public final A fst;
	public final B snd;
	
	public Pair(A fst, B snd) {
		this.fst = fst;
		this.snd = snd;
	}
	
	public static <A,B> Pair<A,B> of(A a, B b) {
		return new Pair<A,B>(a,b);
	}

	@Override
	public String toString() {
		return new StringBuilder("(").append(fst).append(",").append(snd).append(")").toString();
	}
}

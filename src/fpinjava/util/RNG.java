package fpinjava.util;

import fpinjava.data.Pair;

public abstract class RNG {
	public abstract Pair<RNG,Integer> nextInt();

	public static class SimpleRNG extends RNG {

		private final long seed;
		
		public SimpleRNG(long seed) {
			this.seed = seed;
		}
		
		@Override
		public Pair<RNG, Integer> nextInt() {
			long newSeed = (seed * 0x5DEECE66DL + 0xBL) & 0xFFFFFFFFFFFFL;
			RNG nextRNG = new SimpleRNG(newSeed);

			int n = (int)(newSeed >>> 16);
			
			return Pair.of(nextRNG, n);
		}		
	}
	
	public static Pair<RNG,Integer> nonNegativeInt(RNG rng) {
		Pair<RNG,Integer> raw = rng.nextInt();
		return Pair.of(raw.fst, raw.snd > 0 ? raw.snd : -(raw.snd + 1));
	}
	
	public static Pair<RNG,Boolean> nextBoolean(RNG rng) {
		Pair<RNG,Integer> raw = rng.nextInt();
		return Pair.of(raw.fst, raw.snd % 2 == 0);
	}
	
	public static Pair<RNG, Double> nextDouble(RNG rng) {
		Pair<RNG,Integer> raw = nonNegativeInt(rng);
		return Pair.of(raw.fst, ((double)raw.snd)/Integer.MAX_VALUE);
	}

}


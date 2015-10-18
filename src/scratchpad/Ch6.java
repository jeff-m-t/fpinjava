package scratchpad;

import java.util.function.Function;

import fpinjava.data.State;
import fpinjava.data.State.StateAction;



public class Ch6 {

	public static void main(String[] args) {

		Function<Integer,StateAction<Integer,Integer>> f =  (i) -> new StateAction<Integer,Integer>(i + 1, i);

		State<Integer,Integer> state = new State<Integer,Integer>(f);
		
		System.out.println(state.run(5));
	}

	public final static class VendingMachineState {
		public final boolean locked;
		public final int numCandies;
		public final int numCoins;
		
		public VendingMachineState(boolean locked, int numCandies, int numCoins) {
			this.locked = locked;
			this.numCandies = numCandies;
			this.numCoins = numCoins;
		}
		
		public VendingMachineState withOneMoreCoin() {
			return new VendingMachineState(locked,numCandies,numCoins+1);
		}
		
		public VendingMachineState withOneLessCandy() {
			return new VendingMachineState(locked,numCandies - 1, numCoins);
		}
		
		public VendingMachineState unlocked() {
			return new VendingMachineState(false,numCandies,numCoins);
		}

		public VendingMachineState locked() {
			return new VendingMachineState(true,numCandies,numCoins);
		}
	}
	
	public static abstract class Input {
		private Input() {}
		
		public static Input coin() { return Coin.INSTANCE; }
		public static Input turn() { return Turn.INSTANCE; }
		
		public static final class Coin extends Input {
			private Coin() {}
			
			public static Coin INSTANCE = new Coin();
		}

		public static final class Turn extends Input {
			private Turn() {}
			
			public static Turn INSTANCE = new Turn();
		}
	}
	
}

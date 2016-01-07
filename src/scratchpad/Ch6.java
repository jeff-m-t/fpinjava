package scratchpad;

import java.util.function.Function;

import fpinjava.data.List;
import fpinjava.data.Pair;
import fpinjava.data.State;
import fpinjava.data.Unit;



public class Ch6 {

	public static void main(String[] args) {

		Function<Integer,Pair<Integer,Integer>> f =  (i) -> new Pair<Integer,Integer>(i + 1, i);

		State<Integer,Integer> state = new State<Integer,Integer>(f);
		
		System.out.println(state.run(5));		

		State<Machine,Machine> test = simulateMachine(List.of(Input.coin(),Input.turn(),Input.coin(),Input.turn()));
		
		System.out.println( test.run(new Machine(true,10,0)).snd );
		
	}
	
	public static State<Machine,Machine> simulateMachine(List<Input> inputs) {
		List<State<Machine,Unit>> transitions = inputs.map(inp -> State.modify((Machine s) -> stepper(inp,s)));
		return State.sequence(transitions).flatMap(s -> State.get());
	}
	
	public static Machine stepper(Input inp, Machine m) {
		if(m.numCandies <= 0) return m;
		else if(inp instanceof Input.Coin && m.locked) return m.unlocked().withOneMoreCoin();
		else if(inp instanceof Input.Turn && ! m.locked) return m.locked().withOneLessCandy();
		else return m;
	}	

	public final static class Machine {
		public final boolean locked;
		public final int numCandies;
		public final int numCoins;
		
		public Machine(boolean locked, int numCandies, int numCoins) {
			this.locked = locked;
			this.numCandies = numCandies;
			this.numCoins = numCoins;
		}
		
		public Machine withOneMoreCoin() {
			return new Machine(locked,numCandies,numCoins + 1);
		}
		
		public Machine withOneLessCandy() {
			return new Machine(locked,numCandies - 1, numCoins);
		}
		
		public Machine unlocked() {
			return new Machine(false,numCandies,numCoins);
		}

		public Machine locked() {
			return new Machine(true,numCandies,numCoins);
		}
		
		@Override
		public String toString() {
			return new StringBuilder("VendingMachine(")
						.append("locked=").append(locked).append(",")
						.append("candies=").append(numCandies).append(",")
						.append("coins=").append(numCoins)
						.append(")")
						.toString();
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

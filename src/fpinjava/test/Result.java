package fpinjava.test;

public abstract class Result {
	public abstract boolean isFalsified();
	
	public static class Passed extends Result {
		public boolean isFalsified() { return false; }
		@Override public String toString() {
			return "Property passed";
		}
	}
	
	public static class Falsified extends Result {
		public final String failedCase;
		public final int successes;
		
		public Falsified(String failedCase, int numSuccesses) {
			this.failedCase = failedCase;
			this.successes = numSuccesses;
		}
		
		public boolean isFalsified() { return true; }
		@Override public String toString() {
			return "Property failed after " + successes + " cases. Failed case: " + failedCase;
		}
	}
}

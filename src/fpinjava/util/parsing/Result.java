package fpinjava.util.parsing;

public abstract class Result<A> {
	private Result() {}

	public abstract boolean isSuccess();
	
	public static class Success<A> extends Result<A> {
		public final A get;
		public final int charsConsumed;
		
		public Success(A  value, int charactersConsumed) {
			this.get = value;
			this.charsConsumed = charactersConsumed;
		}
		
		@Override
		public boolean isSuccess() { return true; }
	}

	public static class Failure<A> extends Result<A> {
		public final ParseError get;
		
		public Failure(ParseError error) {
			this.get = error;
		}
		
		@Override
		public boolean isSuccess() { return false; }
		
	}
}

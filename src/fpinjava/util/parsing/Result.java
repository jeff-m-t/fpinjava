package fpinjava.util.parsing;

import java.util.function.Function;

import fpinjava.data.Either;

public abstract class Result<A> {
	private Result() {}

	public abstract boolean isSuccess();
	
	public <B> B fold(Function<Success<A>,B> success, Function<Failure<A>,B> failure) {
		return isSuccess() ? success.apply((Success<A>)this) : failure.apply((Failure<A>)this);
	}
	
	public Either<ParseError,A> extract() {
		return fold(
			success -> Either.right(success.get),
			failure -> Either.left(failure.get)
		);
	}
	
	public Result<A> uncommit() {
		return fold(
			success -> success,
			failure -> new Failure<A>(failure.get, false)
		);
	}
	
	public Result<A> addCommit(boolean isCommitted) {
		return fold(
			success -> success,
			failure -> new Failure<A>(failure.get, failure.isCommitted || isCommitted)
		);
	}
	
	public Result<A> advanceSuccess(int n) {
		return fold(
			success -> new Success<A>(success.get,success.charsConsumed+n),
			failure -> failure
		);
	}
	
	public Result<A> mapError(Function<ParseError,ParseError> f) {
		return fold(
			success -> success,
			failure -> new Failure<A>(f.apply(failure.get),failure.isCommitted)
		);
	}
	
	public static <A> Result<A> success(A a, int num) {
		return new Success<A>(a,num);
	}
	
	public static <A> Result<A> failure(ParseError error, boolean isCommitted) {
		return new Failure<A>(error,isCommitted);
	}
	
	public static class Success<A> extends Result<A> {
		public final A get;
		public final int charsConsumed;
		
		public Success(A  value, int charactersConsumed) {
			this.get = value;
			this.charsConsumed = charactersConsumed;
		}
		
		@Override
		public boolean isSuccess() { return true; }
		
		@Override
		public String toString() {
			return "Success('"+get+"', "+charsConsumed+")";
		}
	}

	public static class Failure<A> extends Result<A> {
		public final ParseError get;
		public final boolean isCommitted;
		
		public Failure(ParseError error, boolean isCommitted) {
			this.get = error;
			this.isCommitted = isCommitted;
		}
		
		public Failure(ParseError error) {
			this(error,true);
		}
		
		@Override
		public boolean isSuccess() { return false; }
		
		@Override
		public String toString() {
			return new StringBuilder("Failure:\n")
						.append("committed: ").append(isCommitted).append("\n")
						.append("stack: ").append(get).append("\n")
						.toString();
		}
		
	}
}

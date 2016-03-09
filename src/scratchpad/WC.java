package scratchpad;

public abstract class WC {
	private WC() {}
	
	public static class Stub extends WC {
		public final String chars;
		
		public Stub(String chars) {
			this.chars = chars;
		}
	}
	
	public static class Part extends WC {
		public final String lChars;
		public final int words;
		public final String rChars;
		
		public Part(String lChars, int words, String rChars) {
			this.lChars = lChars;
			this.words = words;
			this.rChars = rChars;
		}
	}

}

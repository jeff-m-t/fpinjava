package fpinjava.json;

import fpinjava.data.List;
import fpinjava.data.Pair;

public abstract class JSON {

	private JSON() {}
	
	public static class JNull extends JSON {
		@Override public String toString() { return "JNull"; }
	}
	
	public static class JNumber extends JSON {
		public final double get;
		public JNumber(double value) { this.get = value; }
		@Override public String toString() { return "JNumber("+get+")"; }
	}
	
	public static class JString extends JSON {
		public final String get;
		public JString(String value) { this.get = value; }
		@Override public String toString() { return "JString("+get+")"; }
	}
	
	public static class JBool extends JSON {
		public final boolean get;
		public JBool(boolean value) { this.get = value; }
		@Override public String toString() { return "JBool("+get+")"; }
	}
	
	public static class JArray extends JSON {
		public final List<JSON> get;
		public JArray(List<JSON> value) { this.get = value; }
		@Override public String toString() { return "JArray("+get+")"; }
	}
	
	public static class JObject extends JSON {
		public final List<Pair<String,JSON>> get;
		public JObject(List<Pair<String,JSON>> value) { this.get = value; } 
		@Override public String toString() { return "JObject("+get+")"; }
	}
}

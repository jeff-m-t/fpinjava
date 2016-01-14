package fpinjava.json;

import fpinjava.data.List;
import fpinjava.data.Pair;

public abstract class JSON {

	private JSON() {}
	
	public static class JNull extends JSON {
		
	}
	
	public static class JNumber {
		public final double get;
		public JNumber(double value) { this.get = value; }
	}
	
	public static class JString {
		public final String get;
		public JString(String value) { this.get = value; }
	}
	
	public static class JBool {
		public final boolean get;
		public JBool(boolean value) { this.get = value; }
	}
	
	public static class JArray {
		public final List<JSON> get;
		public JArray(List<JSON> value) { this.get = value; }
	}
	
	public static class JObject {
		public final List<Pair<String,JSON>> get;
		public JObject(List<Pair<String,JSON>> value) { this.get = value; } 
	}
}

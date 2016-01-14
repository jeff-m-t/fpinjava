package fpinjava.util.parsing;

public class Location {
	private final String input;
	private final int offset;
	
	public Location(String input, int offset) {
		this.input = input;
		this.offset = offset;
	}
	
	public Location(String input) {
		this(input,0);
	}
	
	public int line() {
		String prefix = input.substring(0,offset+1);
		
		// Count number of new line chars before the offset
		return prefix.length() - prefix.replace("\n", "").length() + 1;
	}
	
	public int col() {
		int lastNewLine = input.substring(0,offset+1).lastIndexOf("\n");
		return lastNewLine == -1 ? (offset + 1) : (offset - lastNewLine);
	}
}
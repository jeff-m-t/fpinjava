package fpinjava.util.parsing;

import fpinjava.data.List;
import fpinjava.data.Pair;

public class Location {
	public final String input;
	public final int offset;
	
	public Location(String input, int offset) {
		this.input = input;
		this.offset = offset;
	}
	
	public Location(String input) {
		this(input,0);
	}
	
	public Location advanceBy(int n) {
		return new Location(input,offset+n);
	}
	
	public ParseError toError(String msg) {
		return new ParseError(List.of(Pair.of(this, msg)));
	}
	
	public int line() {
		String prefix = (offset+1 < input.length()) ? input.substring(0,offset+1) : input;
		
		// Count number of new line chars before the offset
		return prefix.length() - prefix.replace("\n", "").length() + 1;
	}
	
	public int col() {
		String prefix = (offset+1 < input.length()) ? input.substring(0,offset+1) : input;
		int lastNewLine = prefix.lastIndexOf("\n");
		return lastNewLine == -1 ? (offset + 1) : (offset - lastNewLine);
	}
	
	public String errLine() {
		String prefix = (offset+1 < input.length()) ? input.substring(0,offset+1) : input;
		int lastNewLine = prefix.lastIndexOf("\n");
		int nextNewLine = input.indexOf("\n", offset);
		
		if(lastNewLine == -1 && nextNewLine == -1) return input;
		else if(lastNewLine == -1 && nextNewLine != -1) return input.substring(0,nextNewLine-1);
		else if(lastNewLine != -1 && nextNewLine == -1) return  input.substring(lastNewLine+1,input.length());
		else return input.substring(lastNewLine+1,nextNewLine-1);
	}
	
	@Override 
	public String toString() {
		return "(line: "+line()+", col: "+col()+") - '"+errLine()+"'";
	}
}
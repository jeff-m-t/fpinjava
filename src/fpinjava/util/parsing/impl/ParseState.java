package fpinjava.util.parsing.impl;

import fpinjava.util.parsing.Location;

public class ParseState {
	public final Location loc;
	
	public ParseState(Location loc) {
		this.loc = loc;
	}
	
	public ParseState advanceBy(int n) {
		Location newLoc = new Location(this.loc.input, this.loc.offset + n);
		return new ParseState(newLoc);
	}
	
	public String input() { return loc.input.substring(loc.offset); }
	public String slice(int n) { return loc.input.substring(loc.offset, loc.offset+n); }
}

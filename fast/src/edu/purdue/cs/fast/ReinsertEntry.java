package edu.purdue.cs.fast;

import edu.purdue.cs.fast.helper.Rectangle;
import edu.purdue.cs.fast.messages.MinimalRangeQuery;

public class ReinsertEntry {
	public Rectangle range;
	public MinimalRangeQuery query;
	public ReinsertEntry(Rectangle range, MinimalRangeQuery query) {
		super();
		this.range = range;
		this.query = query;
	}
	

}

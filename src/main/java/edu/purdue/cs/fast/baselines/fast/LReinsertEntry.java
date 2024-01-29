package edu.purdue.cs.fast.baselines.fast;

import edu.purdue.cs.fast.baselines.fast.messages.LMinimalRangeQuery;
import edu.purdue.cs.fast.models.Rectangle;

public class LReinsertEntry {
	public Rectangle range;
	public LMinimalRangeQuery query;
	public LReinsertEntry(Rectangle range, LMinimalRangeQuery query) {
		super();
		this.range = range;
		this.query = query;
	}
	

}

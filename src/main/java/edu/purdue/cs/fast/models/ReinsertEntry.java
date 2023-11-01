package edu.purdue.cs.fast.models;

public class ReinsertEntry {
	public Rectangle range;
	public MinimalRangeQuery query;

	public ReinsertEntry(Rectangle range, MinimalRangeQuery query) {
		super();
		this.range = range;
		this.query = query;
	}
}

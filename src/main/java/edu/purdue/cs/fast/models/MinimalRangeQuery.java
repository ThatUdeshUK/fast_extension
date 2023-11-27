package edu.purdue.cs.fast.models;

import java.util.List;

import edu.purdue.cs.fast.helper.TextualPredicate;

public class MinimalRangeQuery extends Query {
	public Rectangle spatialRange;

	public MinimalRangeQuery(int id, List<String> keywords, Rectangle spatialRange, TextualPredicate predicate, long st, long et) {
		super(id, keywords, predicate, st, et);
		this.spatialRange = spatialRange;
	}

	@Override
	public Rectangle spatialBox() {
		return this.spatialRange;
	}

	@Override
	public String toString() {
		return "MinimalRangeQuery{" +
				"id=" + id +
				", keywords=" + keywords +
				", spatialRange=" + spatialRange +
				", predicate=" + predicate +
				", et=" + et +
				", deleted=" + deleted +
				'}';
	}
}

package edu.purdue.cs.fast.models;

import edu.purdue.cs.fast.helper.TextualPredicate;

import java.util.List;

public abstract class Query {
	public int id;
	public List<String> keywords;
	public TextualPredicate predicate;
	public long st;
	public long et;

	public boolean deleted;

	public Query(int id, List<String> keywords, TextualPredicate predicate, long st, long et) {
		this.id = id;
		this.keywords = keywords;
		this.predicate = predicate;
		this.st = st;
		this.et = et;
	}

	public abstract Rectangle spatialBox();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Query query = (Query) o;
		return id == query.id;
	}
}

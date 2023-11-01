package edu.purdue.cs.fast.models;

import edu.purdue.cs.fast.helper.TextualPredicate;

import java.util.List;

public abstract class Query {
	public int id;
	public List<String> keywords;
	public TextualPredicate predicate;
	public int et;

	public boolean deleted;

	public Query(int id, List<String> keywords, TextualPredicate predicate, int et) {
		this.id = id;
		this.keywords = keywords;
		this.predicate = predicate;
		this.et = et;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Query query = (Query) o;
		return id == query.id;
	}
}

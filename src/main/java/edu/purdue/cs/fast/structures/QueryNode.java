package edu.purdue.cs.fast.structures;

import edu.purdue.cs.fast.models.Query;

public class QueryNode extends TextualNode{
    public Query query;

    public QueryNode(Query query) {
        this.query = query;
    }
}

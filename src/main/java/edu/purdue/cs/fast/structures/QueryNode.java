package edu.purdue.cs.fast.structures;

import edu.purdue.cs.fast.models.Query;
import java.io.Serializable;

public class QueryNode extends TextualNode implements Serializable{
    public Query query;

    public QueryNode(Query query) {
        this.query = query;
    }
}

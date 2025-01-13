package edu.purdue.cs.fast.structures;

import edu.purdue.cs.fast.models.Query;
import java.io.Serializable;

public class QueryListNode extends TextualNode implements Serializable {
    public HybridList queries;

    // TODO - Remove eager init
    public QueryListNode() {
        this.queries = new HybridList();
    }

    public QueryListNode(Query firstQuery) {
        this.queries = new HybridList();
        this.queries.add(firstQuery);
    }
}

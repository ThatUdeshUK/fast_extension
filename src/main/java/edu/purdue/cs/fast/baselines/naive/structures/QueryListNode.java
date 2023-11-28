package edu.purdue.cs.fast.baselines.naive.structures;

import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.structures.TextualNode;

public class QueryListNode extends TextualNode {
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

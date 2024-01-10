package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.baselines.ckqst.models.CkObject;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

public class ObjectIndex {
    private HashMap<String, LinkedList<CkObject>> index;

    public ObjectIndex() {
        this.index = new HashMap<>();
    }

    public void insert(CkObject obj) {
        for (String key: obj.keywords) {
            if (!index.containsKey(key)) {
                // Create new entry in object index.
                LinkedList<CkObject> objs = new LinkedList<>();
                objs.add(obj);

                this.index.put(key, objs);
            } else {
                this.index.get(key).add(obj);
            }
        }
    }

    public List<CkObject> search(Query query) {
        HashSet<CkObject> set = null;
        for (String key : query.keywords) {
            if (index.containsKey(key)) {
                if (set == null) {
                    set = new HashSet<>(index.get(key));
                } else {
                    HashSet<CkObject> other = new HashSet<>(index.get(key));
                    set.retainAll(other);
                }
            }
        }

        List<CkObject> results = new LinkedList<>();
        if (set != null) {
            results.addAll(set);
        }
        return results;
    }
}

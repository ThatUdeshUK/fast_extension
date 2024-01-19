package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

public class ObjectIndex  {
    private final HashMap<String, LinkedList<DataObject>> index;

    public ObjectIndex() {
        this.index = new HashMap<>();
    }

    public Collection<DataObject> search(Query query) {
        HashSet<DataObject> set = null;
        for (String key : query.keywords) {
            if (index.containsKey(key)) {
                if (set == null) {
                    set = new HashSet<>(index.get(key));
                } else {
                    HashSet<DataObject> other = new HashSet<>(index.get(key));
                    set.retainAll(other);
                }
            }
        }

        List<DataObject> results = new LinkedList<>();
        if (set != null) {
            results.addAll(set);
        }
        return results;
    }

    public Collection<Query> insert(DataObject dataObject) {
        for (String key: dataObject.keywords) {
            if (!index.containsKey(key)) {
                // Create new entry in object index.
                LinkedList<DataObject> objs = new LinkedList<>();
                objs.add(dataObject);

                this.index.put(key, objs);
            } else {
                this.index.get(key).add(dataObject);
            }
        }
        return null;
    }
}

package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.L;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.baselines.ckqst.structures.IQuadTree;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Point;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


class ILQuadTreeTest {

    @Test
    public void testInserts() {
        IQuadTree index = new IQuadTree(0, 0, 10, 10, 1, 5);

        index.insert(new DataObject(1, new Point(7.0, 7.0), L.of("k1", "k2"), 0, 0));
        index.insert(new DataObject(2, new Point(1.0, 1.0), L.of("k1", "k2"), 0, 0));
        index.insert(new DataObject(3, new Point(4.0, 4.0), L.of("k1", "k3"), 0, 0));
        index.insert(new DataObject(4, new Point(1.0, 6.0), L.of("k1"), 0, 0));
        index.insert(new DataObject(5, new Point(1.0, 3.0), L.of("k2", "k3"), 0, 0));
        index.insert(new DataObject(6, new Point(0.75, 8.0), L.of("k2", "k3"), 0, 0));
        index.insert(new DataObject(7, new Point(3.0, 7.0), L.of("k3"), 0, 0));
        index.insert(new DataObject(8, new Point(6.0, 2.0), L.of("k3"), 0, 0));
        index.insert(new DataObject(9, new Point(0.5, 5.5), L.of("k3"), 0, 0));
        index.insert(new DataObject(10, new Point(6.0, 8.0), L.of("k1"), 0, 0));
        index.insert(new DataObject(11, new Point(3.0, 9.0), L.of("k2"), 0, 0));

        System.out.println(index.getStatusByMorton("k1", "1010"));
        System.out.println(index.getStatusByMorton("k1", "10"));
        System.out.println(index.search(new CkQuery(1, L.of("k1", "k2"), 3.0, 6.0, 1, 0, 0)));
        System.out.println(index);
    }

}
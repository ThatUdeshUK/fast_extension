package edu.purdue.cs.fast.baselines.ckqst;

import edu.purdue.cs.fast.L;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class CkQSTTest {
    private List<DataObject> objects;
    private List<Query> queries;

    public CkQSTTest() {
        this.queries = L.of(
                new KNNQuery(1, L.of("k1", "k2", "k3"), new Point(5.0, 5.0), 3, null, 0, 100),
                new KNNQuery(2, L.of("k1", "k2", "k4"), new Point(7.0, 7.0), 2, null, 0, 100),
                new KNNQuery(3, L.of("k1", "k2", "k0"), new Point(5.0, 5.0), 3, null, 0, 100),
                new KNNQuery(4, L.of("k3", "k6", "k7"), new Point(7.0, 4.0), 1, null, 0, 100),
                new KNNQuery(5, L.of("k1", "k3"), new Point(5.0, 5.0), 1, null, 0, 100),
                new KNNQuery(6, L.of("k1", "k2", "k3"), new Point(5.0, 5.0), 1, null, 0, 100),
                new KNNQuery(7, L.of("k2", "k3", "k7"), new Point(5.0, 5.0), 1, null, 0, 100),
                new KNNQuery(8, L.of("k2"), new Point(5.0, 5.0), 1, null, 0, 100),
                new KNNQuery(9, L.of("k1", "k3"), new Point(5.0, 5.0), 1, null, 0, 100),
                new KNNQuery(10, L.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 0, 100),
                new KNNQuery(11, L.of("k1"), new Point(8.0, 5.0), 3, null,0, 100),
                new KNNQuery(12, L.of("k2", "k3"), new Point(1.0, 7.0), 2, null, 0, 100)
        );

        this.objects = L.of(
                new DataObject(1, new Point(7.0, 8.0), L.of("k1", "k2", "k3"), 5, 7),
                new DataObject(2, new Point(5.0, 5.0), L.of("k1", "k2", "k3"), 6, 9),
                new DataObject(3, new Point(2.0, 6.0), L.of("k1", "k2", "k3"), 7, 10),
                new DataObject(4, new Point(1.0, 1.0), L.of("k1", "k2", "k4"), 8, 10),
                new DataObject(5, new Point(5.0, 6.0), L.of("k1", "k2", "k0"), 9, 11),
                new DataObject(6, new Point(7.0, 9.0), L.of("k1", "k2", "k0"), 10, 11)
//                new DataObject(7, new Point(7.0, 8.0), L.of("k1", "k2"), 7, 100) // Fails as it's a tie-breaker
        );
    }

    @Test
    void addContinuousQuery() {
        CkQST testCkQST = new CkQST();

        for (Query query : this.queries) {
            testCkQST.insertQuery(query);
        }
        testCkQST.printIndex();

        System.out.println("-----Search Test-----");
        for (int i = 0; i < objects.size(); i++) {
            System.out.println(objects.get(i));
            List<Integer> fastAns = testCkQST.insertObject(objects.get(i)).stream().map((Query query) -> query.id).collect(Collectors.toList());
            System.out.println(fastAns);

//            testCkQST.printIndex();
            System.out.println("------------\n");
        }
    }
}
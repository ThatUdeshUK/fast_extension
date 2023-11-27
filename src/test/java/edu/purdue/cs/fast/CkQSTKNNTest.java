package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class CkQSTKNNTest {
    private final List<KNNQuery> queries;
    private final List<DataObject> objects;
    private final List<List<Integer>> answers;

    public CkQSTKNNTest() {
        this.queries = List.of(
                new KNNQuery(1, List.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 0, 100),
                new KNNQuery(2, List.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 0, 100),
                new KNNQuery(3, List.of("k1"), new Point(8.0, 5.0), 3, null, 0, 100),
                new KNNQuery(4, List.of("k2", "k3"), new Point(1.0, 7.0), 2, null, 0, 100)
        );

        // TODO - Address tie-breaking
        this.objects = List.of(
                new DataObject(1, new Point(7.0, 8.0), List.of("k1", "k2"), 1, 100),
                new DataObject(2, new Point(5.0, 5.0), List.of("k1", "k2"), 2, 100),
                new DataObject(3, new Point(2.0, 6.0), List.of("k1", "k2"), 3, 100),
                new DataObject(4, new Point(1.0, 1.0), List.of("k1", "k2"), 4, 100),
                new DataObject(5, new Point(5.0, 6.0), List.of("k1", "k2"), 5, 100),
                new DataObject(6, new Point(7.0, 8), List.of("k1", "k2"), 6, 100)
        );

        this.answers = List.of(
                List.of(1, 2, 3),
                List.of(1, 2, 3),
                List.of(1, 3),
                List.of(),
                List.of(1, 2, 3),
                List.of(2)
        );
    }

    @Test
    public void testInserts() {
        CkQST testCkQST = new CkQST();

        for (KNNQuery query : this.queries) {
            testCkQST.addContinuousQuery(query);
        }

        testCkQST.printIndex();
    }

    @Test
    public void testSearch() {
        CkQST testCkQST = new CkQST();

        for (KNNQuery query : this.queries) {
            testCkQST.addContinuousQuery(query);
        }

        System.out.println("-----Search Test-----");
        for (int i = 0; i < answers.size(); i++) {
            System.out.println(objects.get(i));
            List<Integer> fastAns = testCkQST.searchQueries(objects.get(i)).stream().map((Query query) -> query.id).toList();
            System.out.println(fastAns + " | " + answers.get(i).toString());

            testCkQST.printIndex();
            System.out.println("------------\n");

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).toArray());
        }
    }
}
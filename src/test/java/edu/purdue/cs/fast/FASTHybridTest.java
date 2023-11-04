package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class FASTHybridTest {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<List<Integer>> answers;

    public FASTHybridTest() {
        this.queries = List.of(
                new MinimalRangeQuery(1, List.of("k1", "k2"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(2, List.of("k1", "k2"), new Rectangle(.0, .0, 8.0, 10.0), null, 100),
                new MinimalRangeQuery(3, List.of("k1", "k2"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(4, List.of("k3", "k6"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(5, List.of("k1", "k3"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(6, List.of("k1", "k2", "k3"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(7, List.of("k2", "k3", "k7"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(8, List.of("k2"), new Rectangle(.0, .0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(9, List.of("k1", "k3"), new Rectangle(6.0, 7.0, 10.0, 10.0), null, 100),
                new MinimalRangeQuery(10, List.of("k1", "k3"), new Rectangle(.0, .0, 4.0, 4.0), null, 100),
                new MinimalRangeQuery(11, List.of("k1", "k3"), new Rectangle(6.0, .0, 10.0, 4.0), null, 100),
                new KNNQuery(12, List.of("k1", "k2"), new Point(5.0, 5.0), 3, null,100),
                new KNNQuery(14, List.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 100)
        );

        this.objects = List.of(
                new DataObject(1, new Point(7.0, 8.0), List.of("k1", "k2"), 1),
                new DataObject(2, new Point(5.0, 5.0), List.of("k1", "k2"), 2),
                new DataObject(3, new Point(2.0, 6.0), List.of("k1", "k2"), 3),
                new DataObject(4, new Point(1.0, 1.0), List.of("k1", "k2"), 4),
                new DataObject(5, new Point(5.0, 6.0), List.of("k1", "k2"), 5)
        );

        this.answers = List.of(
                List.of(1, 2, 3, 8, 12, 14),
                List.of(1, 2, 3, 8, 12, 14),
                List.of(1, 2, 3, 8, 12),
                List.of(1, 2, 3, 8),
                List.of(1, 2, 3, 8, 12, 14)
        );
    }

    @Test
    public void testInserts() {
        FAST testFAST = new FAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(10.1, 10.1)
                ),
                2,
                1
        );

        for (Query query : this.queries) {
            testFAST.addContinuousQuery(query);
        }

        testFAST.printFrequencies();
        testFAST.printIndex();
    }

    @Test
    public void testSearch() {
        FAST testFAST = new FAST(
                new Rectangle(
                        new Point(0.0, 0.0),
                        new Point(10.1, 10.1)
                ),
                2,
                1
        );

        for (Query query : this.queries) {
            testFAST.addContinuousQuery(query);
        }

        System.out.println("-----Search Test-----");
        for (int i = 0; i < answers.size(); i++) {
            List<Integer> fastAns = testFAST.searchQueries(objects.get(i)).stream().map((Query query) -> query.id).toList();
            System.out.println(fastAns + " | " + answers.get(i).toString());

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).toArray());
        }
    }
}
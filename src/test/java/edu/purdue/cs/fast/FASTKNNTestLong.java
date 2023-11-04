package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class FASTKNNTestLong {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<List<Integer>> answers;

    public FASTKNNTestLong() {
        this.queries = List.of(
                new KNNQuery(1, List.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 100),
                new KNNQuery(2, List.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 100),
                new KNNQuery(3, List.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 100),
                new KNNQuery(4, List.of("k3", "k6"), new Point(7.0, 4.0), 1, null, 100),
                new KNNQuery(5, List.of("k1", "k3"), new Point(5.0, 5.0), 1, null, 100),
                new KNNQuery(6, List.of("k1", "k2", "k3"), new Point(5.0, 5.0), 1, null, 100),
                new KNNQuery(7, List.of("k2", "k3", "k7"), new Point(5.0, 5.0), 1, null, 100),
                new KNNQuery(8, List.of("k2"), new Point(5.0, 5.0), 1, null, 100),
                new KNNQuery(9, List.of("k1", "k3"), new Point(5.0, 5.0), 1, null, 100),
                new KNNQuery(10, List.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 100),
                new KNNQuery(11, List.of("k1"), new Point(8.0, 5.0), 3, null,100),
                new KNNQuery(12, List.of("k2", "k3"), new Point(1.0, 7.0), 2, null, 100)
        );

        this.objects = List.of(
                new DataObject(1, new Point(7.0, 8.0), List.of("k1", "k2"), 1),
                new DataObject(2, new Point(5.0, 5.0), List.of("k1", "k2"), 2),
                new DataObject(3, new Point(2.0, 6.0), List.of("k1", "k2"), 3),
                new DataObject(4, new Point(1.0, 1.0), List.of("k1", "k2"), 4),
                new DataObject(5, new Point(5.0, 6.0), List.of("k1", "k2"), 5)
        );

        this.answers = List.of(
                List.of(1, 2, 3, 8, 10, 11),
                List.of(1, 2, 3),
                List.of(1, 3),
                List.of(),
                List.of(1, 2, 3)
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

            testFAST.printIndex();
            System.out.println("------------\n");

//            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).toArray());
        }
    }
}
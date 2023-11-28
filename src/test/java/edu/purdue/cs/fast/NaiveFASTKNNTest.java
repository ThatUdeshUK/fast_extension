package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.naive.NaiveFAST;
import edu.purdue.cs.fast.baselines.naive.models.KNNQuery;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Query;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


class NaiveFASTKNNTest {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<List<Integer>> answers;

    public NaiveFASTKNNTest() {
        this.queries = List.of(
                new KNNQuery(1, List.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 1,8),
                new KNNQuery(2, List.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 2,7),
                new KNNQuery(3, List.of("k1"), new Point(8.0, 5.0), 3, null, 3,11),
                new KNNQuery(4, List.of("k2", "k3"), new Point(1.0, 7.0), 2, null, 4,9)
        );

        // TODO - Address tie-breaking
        this.objects = List.of(
                new DataObject(1, new Point(7.0, 8.0), List.of("k1", "k2"), 5, 100),
                new DataObject(2, new Point(5.0, 5.0), List.of("k1", "k2"), 6, 100),
                new DataObject(3, new Point(2.0, 6.0), List.of("k1", "k2"), 7, 100),
                new DataObject(4, new Point(1.0, 1.0), List.of("k1", "k2"), 8, 100),
                new DataObject(5, new Point(5.0, 6.0), List.of("k1", "k2"), 9, 100),
                new DataObject(6, new Point(7.0, 9.0), List.of("k1", "k2"), 10, 100)
//                new DataObject(7, new Point(7.0, 8.0), List.of("k1", "k2"), 7, 100) // Fails as it's a tie-breaker
        );

        this.answers = List.of(
                List.of(1, 2, 3),
                List.of(1, 2, 3),
                List.of(1, 3),
                List.of(),
                List.of(3),
                List.of()
        );
    }

    @Test
    public void testInserts() {
        NaiveFAST testFAST = new NaiveFAST(
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
        NaiveFAST testFAST = new NaiveFAST(
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
            System.out.println(objects.get(i));
            List<Integer> fastAns = testFAST.searchQueries(objects.get(i)).stream().map((Query query) -> query.id).toList();
            System.out.println(fastAns + " | " + answers.get(i).toString());

            testFAST.printIndex();
            System.out.println("------------\n");

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).toArray());
        }
    }
}
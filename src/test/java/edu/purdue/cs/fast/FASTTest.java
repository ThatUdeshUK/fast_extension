package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;


class FASTTest {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<Answer> answers;

    public FASTTest() {
        this.queries = L.of(
                new MinimalRangeQuery(1, L.of("k1", "k2"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(2, L.of("k1", "k2"), new Rectangle(.0, .0, 8.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(3, L.of("k1", "k2"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(4, L.of("k3", "k6"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(5, L.of("k1", "k3"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(6, L.of("k1", "k2", "k3"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(7, L.of("k2", "k3", "k7"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(8, L.of("k2"), new Rectangle(.0, .0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(9, L.of("k1", "k3"), new Rectangle(6.0, 7.0, 10.0, 10.0), null, 0, 100),
                new MinimalRangeQuery(10, L.of("k1", "k3"), new Rectangle(.0, .0, 4.0, 4.0), null, 0, 100),
                new MinimalRangeQuery(11, L.of("k1", "k3"), new Rectangle(6.0, .0, 10.0, 4.0), null, 0, 100)
        );

        this.objects = L.of(
                new DataObject(1, new Point(8.0, 4.0), L.of("k3", "k6"), 1, 100),
                new DataObject(2, new Point(2.0, 6.0), L.of("k1", "k2"), 2, 100),
                new DataObject(3, new Point(8.0, 2.0), L.of("k1", "k3"), 3, 100),
                new DataObject(4, new Point(0.0, 2.0), L.of("k1", "k3"), 4, 100),
                new DataObject(5, new Point(7.0, 8.0), L.of("k1", "k3"), 5, 100)
        );

        this.answers = L.of(
                new Answer(4),
                new Answer(1, 2, 3, 8),
                new Answer(5, 11),
                new Answer(5, 10),
                new Answer(5, 9)
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
            testFAST.insertQuery(query);
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
            testFAST.insertQuery(query);
        }

        System.out.println("-----Search Test-----");
        for (int i = 0; i < answers.size(); i++) {
            List<Integer> fastAns = testFAST.insertObject(objects.get(i)).stream().map((Query query) -> query.id).collect(Collectors.toList());
            System.out.println(fastAns + " | " + answers.get(i).toString());

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).answers.toArray());
        }
    }
}
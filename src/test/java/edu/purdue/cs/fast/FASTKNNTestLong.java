package edu.purdue.cs.fast;

import edu.purdue.cs.fast.config.Config;
import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;


class FASTKNNTestLong {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<Answer> answers;

    public FASTKNNTestLong() {
        this.queries = L.of(
                new KNNQuery(1, L.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 0, 100),
                new KNNQuery(2, L.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 0, 100),
                new KNNQuery(3, L.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 0, 100),
                new KNNQuery(4, L.of("k3", "k6"), new Point(7.0, 4.0), 1, null, 0, 100),
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
                new DataObject(1, new Point(7.0, 8.0), L.of("k1", "k2"), 13, 15),
                new DataObject(2, new Point(5.0, 5.0), L.of("k1", "k2"), 14, 16),
                new DataObject(3, new Point(2.0, 6.0), L.of("k1", "k2"), 15, 100),
                new DataObject(4, new Point(1.0, 1.0), L.of("k1", "k2"), 16, 100),
                new DataObject(5, new Point(5.0, 6.0), L.of("k1", "k2"), 17, 100),
                new DataObject(6, new Point(1.0, 1.0), L.of("k1", "k2"), 18, 100),
                new DataObject(7, new Point(5.0, 5.0), L.of("k1", "k2"), 19, 100),
                new DataObject(8, new Point(5.0, 6.0), L.of("k1", "k2"), 20, 100)
        );

        this.answers = L.of(
                new Answer(1, 2, 3, 8, 10, 11),
                new Answer(1, 2, 3, 8, 10, 11),
                new Answer(1, 3, 11),
                new Answer(2, 10, 11),
                new Answer(1, 2, 3, 8, 10, 11),
                new Answer(),
                new Answer(1, 2, 3, 8, 10, 11),
                new Answer(1, 2, 3, 10, 11)
                );
    }

    @Test
    public void testInserts() {
        FAST testFAST = new FAST(
                new Config(),
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
                new Config(),
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
            System.out.println("Searching: " + objects.get(i));
            List<Query> fastAnsRaw = testFAST.insertObject(objects.get(i));
            List<Integer> fastAns = fastAnsRaw.stream().map((Query query) -> query.id).collect(Collectors.toList());
            System.out.println(fastAns + " | " + answers.get(i).toString());

            testFAST.printIndex();
            System.out.println("------------\n");

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).answers.toArray());
        }
    }
}
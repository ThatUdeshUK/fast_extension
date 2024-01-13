package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;


class FASTKNNTest {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<Answer> answers;

    public FASTKNNTest() {
        this.queries = L.of(
                new KNNQuery(1, L.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 1,8),
                new KNNQuery(2, L.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 2,7),
                new KNNQuery(3, L.of("k1"), new Point(8.0, 5.0), 3, null, 3,11),
                new KNNQuery(4, L.of("k2", "k3"), new Point(1.0, 7.0), 2, null, 4,9)
        );

        // TODO - Address tie-breaking
        this.objects = L.of(
                new DataObject(1, new Point(7.0, 8.0), L.of("k1", "k2"), 5, 7),
                new DataObject(2, new Point(5.0, 5.0), L.of("k1", "k2"), 6, 9),
                new DataObject(3, new Point(2.0, 6.0), L.of("k1", "k2"), 7, 10),
                new DataObject(4, new Point(1.0, 1.0), L.of("k1", "k2"), 8, 10),
                new DataObject(5, new Point(5.0, 6.0), L.of("k1", "k2"), 9, 11),
                new DataObject(6, new Point(7.0, 9.0), L.of("k1", "k2"), 10, 11)
//                new DataObject(7, new Point(7.0, 8.0), L.of("k1", "k2"), 7, 100) // Fails as it's a tie-breaker
        );

        this.answers = L.of(
                new Answer(1, 2, 3),
                new Answer(1, 2, 3),
                new Answer(1, 3),
                new Answer(),
                new Answer(3),
                new Answer()
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
            System.out.println(objects.get(i));
            List<Integer> fastAns = testFAST.insertObject(objects.get(i)).stream().map((Query query) -> query.id).collect(Collectors.toList());
            System.out.println(fastAns + " | " + answers.get(i).toString());

            testFAST.printIndex();
            System.out.println("------------\n");

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).answers.toArray());
        }
    }
}
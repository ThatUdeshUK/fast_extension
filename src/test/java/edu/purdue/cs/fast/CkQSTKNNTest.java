package edu.purdue.cs.fast;

import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;


class CkQSTKNNTest {
    private final List<Query> queries;
    private final List<DataObject> objects;
    private final List<Answer> answers;

    public CkQSTKNNTest() {
        this.queries = L.of(
                new KNNQuery(1, L.of("k1", "k2"), new Point(5.0, 5.0), 3, null, 0, 100),
                new KNNQuery(2, L.of("k1", "k2"), new Point(7.0, 7.0), 2, null, 0, 100),
                new KNNQuery(3, L.of("k1"), new Point(8.0, 5.0), 3, null, 0, 100),
                new KNNQuery(4, L.of("k2", "k3"), new Point(1.0, 7.0), 2, null, 0, 100)
        );

        // TODO - Address tie-breaking
        this.objects = L.of(
                new DataObject(1, new Point(7.0, 8.0), L.of("k1", "k2"), 1, 100),
                new DataObject(2, new Point(5.0, 5.0), L.of("k1", "k2"), 2, 100),
                new DataObject(3, new Point(2.0, 6.0), L.of("k1", "k2"), 3, 100),
                new DataObject(4, new Point(1.0, 1.0), L.of("k1", "k2"), 4, 100),
                new DataObject(5, new Point(5.0, 6.0), L.of("k1", "k2"), 5, 100),
                new DataObject(6, new Point(7.0, 8), L.of("k1", "k2"), 6, 100)
        );

        this.answers = L.of(
                new Answer(1, 2, 3),
                new Answer(1, 2, 3),
                new Answer(1, 3),
                new Answer(),
                new Answer(1, 2, 3),
                new Answer(2)
        );
    }

    @Test
    public void testInserts() {
        CkQST testCkQST = new CkQST();

        for (Query query : this.queries) {
            testCkQST.insertQuery(query);
        }

        testCkQST.printIndex();
    }

    @Test
    public void testSearch() {
        CkQST testCkQST = new CkQST();

        for (Query query : this.queries) {
            testCkQST.insertQuery(query);
        }

        System.out.println("-----Search Test-----");
        for (int i = 0; i < answers.size(); i++) {
            System.out.println(objects.get(i));
            List<Integer> fastAns = testCkQST.insertObject(objects.get(i)).stream().map((Query query) -> query.id).collect(Collectors.toList());
            System.out.println(fastAns + " | " + answers.get(i).toString());

            testCkQST.printIndex();
            System.out.println("------------\n");

            Assertions.assertArrayEquals(fastAns.stream().sorted().toArray(), answers.get(i).answers.toArray());
        }
    }

//    private static ArrayList<KNNQuery> listOf() {
//        ArrayList<KNNQuery> queries = new ArrayList<>();
//        return null;
//    }
}
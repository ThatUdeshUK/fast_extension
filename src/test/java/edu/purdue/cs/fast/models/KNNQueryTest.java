package edu.purdue.cs.fast.models;

import edu.purdue.cs.fast.FAST;
import org.junit.jupiter.api.Test;

import java.util.List;

class KNNQueryTest {

    @Test
    void pushUntilK() {
        KNNQuery dummyQuery = new KNNQuery(0, List.of("k1", "k2"), new Point(5, 5), 3, null, 0, 100);

        List<DataObject> objects = List.of(
                new DataObject(0, new Point(5, 6), List.of("k1", "k2"), 0, 100),
                new DataObject(1, new Point(7, 6), List.of("k1", "k2"), 0, 100),
                new DataObject(2, new Point(1, 1), List.of("k1", "k2"), 0, 100),
                new DataObject(3, new Point(4, 5), List.of("k1", "k2"), 0, 100)
        );

        for (DataObject object : objects.subList(0, 3)) {
            dummyQuery.pushUntilK(object);
        }
        assertListEqualUnordered(objects.subList(0, 3), dummyQuery.getMonitoredObjects().stream().toList());
        assert 5.656854249492381 == dummyQuery.ar;

        dummyQuery.pushUntilK(objects.get(3));
        assert 2.23606797749979 == dummyQuery.ar;
        assertListEqualUnordered(objects.stream().filter((a) -> a.id != 2).toList(), dummyQuery.getMonitoredObjects().stream().toList());
    }

    @Test
    void calcMinSpatialLevel() {
        int bound = 1024;
        int gridGranularity = 512;
        FAST.localXstep = (double) bound / gridGranularity;
        int maxLevel = (int) (Math.log(gridGranularity) / Math.log(2));
        for (int i = maxLevel; i >= 0; i--) {
            int granI = (int) (512 / Math.pow(2, i));
            System.out.println("Level: " + i + ", Granularity: " + granI + ", Cell Size:" + bound/granI);
        }

        KNNQuery dummyQuery = new KNNQuery(0, List.of("k1", "k2"), new Point(192, 192), 3, null, 0, 100);

        List<DataObject> objects = List.of(
                new DataObject(0, new Point(128, 128), List.of("k1", "k2"), 0, 100),
                new DataObject(1, new Point(200, 210), List.of("k1", "k2"), 0, 100),
                new DataObject(2, new Point(192, 384), List.of("k1", "k2"), 0, 100),
                new DataObject(3, new Point(4, 5), List.of("k1", "k2"), 0, 100)
        );

        for (DataObject object : objects.subList(0, 3)) {
            dummyQuery.pushUntilK(object);
        }

        System.out.println("---------------------------------------");
        System.out.println("Answer Region: " + dummyQuery.ar);

        int minLevel = dummyQuery.calcMinSpatialLevel();
        System.out.println("Min level: " + minLevel);
        assert minLevel == 6;
    }

    private void assertListEqualUnordered(List<DataObject> a, List<DataObject> b) {
        assert a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
    }
}
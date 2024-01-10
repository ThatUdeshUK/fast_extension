package edu.purdue.cs.fast.unit;

import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.L;
import edu.purdue.cs.fast.config.Context;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.Point;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class KNNQueryTest {

    @Test
    void pushUntilK() {
        KNNQuery dummyQuery = new KNNQuery(0, L.of("k1", "k2"), new Point(5, 5), 3, null, 0, 100);

        List<DataObject> objects = L.of(
                new DataObject(0, new Point(5, 6), L.of("k1", "k2"), 0, 100),
                new DataObject(1, new Point(7, 6), L.of("k1", "k2"), 0, 100),
                new DataObject(2, new Point(1, 1), L.of("k1", "k2"), 0, 100),
                new DataObject(3, new Point(4, 5), L.of("k1", "k2"), 0, 100)
        );

        for (DataObject object : objects.subList(0, 3)) {
            dummyQuery.pushUntilK(object);
        }
        assertListEqualUnordered(objects.subList(0, 3), new ArrayList<>(dummyQuery.getMonitoredObjects()));
        assert 5.656854249492381 == dummyQuery.ar;

        dummyQuery.pushUntilK(objects.get(3));
        assert 2.23606797749979 == dummyQuery.ar;
        assertListEqualUnordered(
                objects.stream().filter((a) -> a.id != 2).collect(Collectors.toList()),
                new ArrayList<>(dummyQuery.getMonitoredObjects())
        );
    }

    @Test
    void calcMinSpatialLevel() {
        Rectangle bound = new Rectangle(0, 0, 1024, 1024);
        FAST.context = new Context(bound, 512, Integer.MAX_VALUE);
        for (int i = FAST.context.maxLevel; i >= 0; i--) {
            int granI = (int) (512 / Math.pow(2, i));
            System.out.println("Level: " + i + ", Granularity: " + granI + ", Cell Size:" + FAST.context.globalXRange / granI);
        }

        KNNQuery dummyQuery = new KNNQuery(0, L.of("k1", "k2"), new Point(192, 192), 3, null, 0, 100);

        List<DataObject> objects = L.of(
                new DataObject(0, new Point(128, 128), L.of("k1", "k2"), 0, 100),
                new DataObject(1, new Point(200, 210), L.of("k1", "k2"), 0, 100),
                new DataObject(2, new Point(192, 384), L.of("k1", "k2"), 0, 100),
                new DataObject(3, new Point(4, 5), L.of("k1", "k2"), 0, 100)
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
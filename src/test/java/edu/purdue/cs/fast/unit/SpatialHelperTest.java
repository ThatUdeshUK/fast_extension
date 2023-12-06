package edu.purdue.cs.fast.unit;

import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.models.Rectangle;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SpatialHelperTest {

    @Test
    public void rectRectCoverEntire() {
        Rectangle a = new Rectangle(0, 0, 10, 10);
        Rectangle b = new Rectangle(2, 2, 6, 6);

        boolean overlap = SpatialHelper.coversSpatially(a, b);
        assertTrue(overlap);
    }

    @Test
    public void rectRectCoverPartial() {
        Rectangle a = new Rectangle(0, 0, 10, 10);
        Rectangle b = new Rectangle(2, 2, 16, 16);

        boolean overlap = SpatialHelper.coversSpatially(a, b);
        assertFalse(overlap);
    }

    @Test
    public void rectRectCoverOutside() {
        Rectangle a = new Rectangle(0, 0, 10, 10);
        Rectangle b = new Rectangle(12, 12, 16, 16);

        boolean overlap = SpatialHelper.coversSpatially(a, b);
        assertFalse(overlap);
    }

    @Test
    public void rectRectCoverInverse() {
        Rectangle a = new Rectangle(0, 0, 10, 10);
        Rectangle b = new Rectangle(2, 2, 6, 6);

        boolean overlap = SpatialHelper.coversSpatially(b, a);
        assertFalse(overlap);
    }
}
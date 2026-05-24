package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathCommittedFloorTest {

    @Test
    public void isValidInterimPathAlignment_withinTwoTiles() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3200, 3200, 0),
                new WorldPoint(3202, 3200, 0));

        WorldPoint click = new WorldPoint(3201, 3200, 0);
        assertTrue(Rs2Walker.isValidInterimPathAlignment(path, 1, click));
    }

    @Test
    public void isValidInterimPathAlignment_falseWhenMisaligned() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3200, 3200, 0),
                new WorldPoint(3220, 3200, 0));

        WorldPoint click = new WorldPoint(3205, 3205, 0);
        assertFalse(Rs2Walker.isValidInterimPathAlignment(path, 1, click));
    }

    @Test
    public void isValidInterimPathAlignment_falseOnWrongPlane() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3200, 3200, 0),
                new WorldPoint(3201, 3201, 0));

        WorldPoint click = new WorldPoint(3201, 3201, 1);
        assertFalse(Rs2Walker.isValidInterimPathAlignment(path, 1, click));
    }
}

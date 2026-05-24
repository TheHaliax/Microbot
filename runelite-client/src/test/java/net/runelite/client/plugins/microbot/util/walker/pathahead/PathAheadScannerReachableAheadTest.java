package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathAheadScannerReachableAheadTest {

    @Test
    public void findReachableAheadIndex_emptyPath_returnsNone() {
        WorldPoint player = new WorldPoint(3200, 3200, 0);
        assertEquals(PathAheadQueuedAction.NONE,
                PathAheadScanner.findReachableAheadIndex(Collections.emptyList(), 3, 8, player, 20));
    }

    @Test
    public void findReachableAheadIndex_fromBeyondPath_returnsNone() {
        List<WorldPoint> path = pathLine(5);
        WorldPoint player = new WorldPoint(3200, 3200, 0);
        assertEquals(PathAheadQueuedAction.NONE,
                PathAheadScanner.findReachableAheadIndex(path, 10, 8, player, 20));
    }

    @Test
    public void findFirstUnreachableAhead_startAtEnd_returnsNone() {
        List<WorldPoint> path = pathLine(3);
        WorldPoint player = new WorldPoint(3200, 3200, 0);
        assertEquals(PathAheadQueuedAction.NONE,
                PathAheadScanner.findFirstUnreachableAhead(path, 2, player, 20));
    }

    private static List<WorldPoint> pathLine(int count) {
        List<WorldPoint> path = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            path.add(new WorldPoint(3200 + i, 3200, 0));
        }
        return path;
    }
}

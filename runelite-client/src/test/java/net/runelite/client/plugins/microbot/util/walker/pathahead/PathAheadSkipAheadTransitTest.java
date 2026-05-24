package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathAheadSkipAheadTransitTest {

    @After
    public void tearDown() {
        PathAheadHandlerState.clear();
        PathAheadHandlerState.clearSkipAheadTransit();
        PathAheadLookahead.clear();
    }

    @Test
    public void skipAheadTransit_beginAndClear() {
        assertFalse(PathAheadHandlerState.isSkipAheadTransit());
        WorldPoint skip = new WorldPoint(3240, 3273, 0);
        PathAheadHandlerState.beginSkipAheadTransit(skip, 20);
        assertTrue(PathAheadHandlerState.isSkipAheadTransit());
        assertTrue(PathAheadHandlerState.isDuplicateSkipAhead(skip));
        PathAheadHandlerState.clearSkipAheadTransit();
        assertFalse(PathAheadHandlerState.isSkipAheadTransit());
    }

    @Test
    public void refresh_duringTransit_clearsQueue() {
        PathAheadHandlerState.beginSkipAheadTransit(new WorldPoint(3240, 3273, 0), 20);
        PathAheadLookahead.refresh(pathLine(30));
        assertFalse(PathAheadLookahead.getQueued().hasWork());
    }

    @Test
    public void shouldEnd_whenPlayerNearSkipTile() {
        WorldPoint skip = new WorldPoint(3240, 3273, 0);
        PathAheadHandlerState.beginSkipAheadTransit(skip, 20);
        WorldPoint player = new WorldPoint(3241, 3274, 0);
        assertTrue(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(player, 5));
    }

    @Test
    public void shouldEnd_whenClosestIndexPassesSkip() {
        WorldPoint skip = new WorldPoint(3240, 3273, 0);
        PathAheadHandlerState.beginSkipAheadTransit(skip, 12);
        assertTrue(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(null, 12));
        assertTrue(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(null, 15));
    }

    @Test
    public void shouldEnd_afterMaxAge() {
        WorldPoint skip = new WorldPoint(3240, 3273, 0);
        PathAheadHandlerState.beginSkipAheadTransit(skip, 20);
        PathAheadHandlerState.setSkipAheadStartedMsForTest(
                System.currentTimeMillis() - 16_000L);
        assertTrue(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(null, 0));
    }

    @Test
    public void shouldNotEnd_whenFarAndIndexBehind() {
        WorldPoint skip = new WorldPoint(3240, 3273, 0);
        PathAheadHandlerState.beginSkipAheadTransit(skip, 20);
        WorldPoint player = new WorldPoint(3239, 3261, 0);
        assertFalse(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(player, 5));
    }

    private static List<WorldPoint> pathLine(int count) {
        List<WorldPoint> path = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            path.add(new WorldPoint(3200 + i, 3200, 0));
        }
        return path;
    }
}

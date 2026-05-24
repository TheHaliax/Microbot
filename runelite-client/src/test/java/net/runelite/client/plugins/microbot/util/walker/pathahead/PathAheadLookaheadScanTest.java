package net.runelite.client.plugins.microbot.util.walker.pathahead;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathAheadLookaheadScanTest {

    @Test
    public void emptyQueue_hasNoWork() {
        assertFalse(PathAheadQueuedAction.empty().hasWork());
    }

    @Test
    public void skipAheadQueued_hasWork() {
        PathAheadQueuedAction q = PathAheadQueuedAction.builder().skipAheadPathIndex(12).build();
        assertTrue(q.hasWork());
    }

    @Test
    public void forwardScanEndIndex_clampsToPath() {
        assertEquals(41, PathAheadLookahead.forwardScanEndIndex(13, 28, 185));
        assertEquals(-1, PathAheadLookahead.forwardScanEndIndex(13, 28, 0));
    }

    @Test
    public void mainScanPathIndices_minOf28AndRender() {
        assertEquals(28, PathAheadLookahead.mainScanPathIndices(50));
        assertEquals(20, PathAheadLookahead.mainScanPathIndices(20));
        assertEquals(15, PathAheadLookahead.mainScanPathIndices(15));
    }

    @Test
    public void probeBoundsBoundedScanEndIndex_stopsAtFirstOutOfBounds() {
        boolean[] inBounds = new boolean[30];
        for (int i = 0; i < inBounds.length; i++) {
            inBounds[i] = i < 20;
        }
        assertEquals(19, PathAheadLookahead.probeBoundsBoundedScanEndIndex(null, 5, 25, i -> inBounds[i]));
    }

    @Test
    public void probeBoundsBoundedScanEndIndex_allInBounds_returnsMaxEnd() {
        assertEquals(25, PathAheadLookahead.probeBoundsBoundedScanEndIndex(null, 5, 25, i -> true));
    }

    @Test
    public void probeBoundsBoundedScanEndIndex_noForwardProbe_returnsStart() {
        assertEquals(10, PathAheadLookahead.probeBoundsBoundedScanEndIndex(null, 10, 25, i -> false));
        assertEquals(10, PathAheadLookahead.probeBoundsBoundedScanEndIndex(null, 10, 8, i -> true));
    }
}

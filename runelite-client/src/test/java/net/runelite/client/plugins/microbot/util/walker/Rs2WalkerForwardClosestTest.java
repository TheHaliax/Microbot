package net.runelite.client.plugins.microbot.util.walker;

import net.runelite.api.coords.WorldPoint;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import net.runelite.client.plugins.microbot.util.walker.pathahead.PathAheadHandlerState;
import net.runelite.client.plugins.microbot.util.walker.pathahead.PathAheadScanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Rs2WalkerForwardClosestTest {

    @After
    public void tearDown() {
        Rs2Walker.clearWalkerDedupeForTesting();
        Rs2Walker.clearInterimCheckpoint();
        Rs2Walker.setPathCommittedFloorIdxForTesting(-1);
        Rs2Walker.setLastSkipHandoffWalkSizeForTesting(-1);
        Rs2Walker.clearSkipAheadRecalcTestState();
        PathAheadHandlerState.clearSkipAheadTransit();
    }

    @Test
    public void closestTileIndexByDistance_respectsMinIndex() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3240, 3300, 0),
                new WorldPoint(3241, 3302, 0),
                new WorldPoint(3242, 3305, 0),
                new WorldPoint(3250, 3320, 0));
        WorldPoint player = new WorldPoint(3241, 3303, 0);
        assertEquals(1, Rs2Walker.closestTileIndexByDistance(path, player, 0));
        assertEquals(2, Rs2Walker.closestTileIndexByDistance(path, player, 2));
    }

    @Test
    public void pathScanStartIndex_withFloor_neverBelowFloor() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3200, 3200, 0),
                new WorldPoint(3201, 3200, 0),
                new WorldPoint(3202, 3200, 0),
                new WorldPoint(3203, 3200, 0));
        Rs2Walker.setPathCommittedFloorIdxForTesting(2);
        assertTrue(Rs2Walker.getPathScanStartIndex(path) >= 2);
    }

    @Test
    public void isPathIndexBehindProgress_belowFloor() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3200, 3200, 0),
                new WorldPoint(3201, 3200, 0),
                new WorldPoint(3202, 3200, 0));
        Rs2Walker.setPathCommittedFloorIdxForTesting(2);
        assertTrue(Rs2Walker.isPathIndexBehindProgress(path, 1));
        assertFalse(Rs2Walker.isPathIndexBehindProgress(path, 2));
    }

    @Test
    public void tryRecoverUnreachable_skipsIndexBehindFloor() {
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3240, 3300, 0),
                new WorldPoint(3241, 3302, 0),
                new WorldPoint(3250, 3320, 0),
                new WorldPoint(3260, 3330, 0));
        Rs2Walker.setPathCommittedFloorIdxForTesting(2);
        assertTrue(Rs2Walker.isPathIndexBehindProgress(path, 1));
        assertFalse(PathAheadScanner.tryRecoverUnreachable(path, new WorldPoint(3260, 3330, 0), 1, 13));
    }

    @Test
    public void resolvePathIndexForTile_exactAndFuzzy() {
        WorldPoint skip = new WorldPoint(3265, 3325, 0);
        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3200, 3200, 0),
                new WorldPoint(3265, 3325, 0),
                new WorldPoint(3270, 3330, 0));
        assertEquals(1, Rs2Walker.resolvePathIndexForTile(path, skip));
        assertEquals(1, Rs2Walker.resolvePathIndexForTile(path, new WorldPoint(3266, 3326, 0)));
        assertEquals(-1, Rs2Walker.resolvePathIndexForTile(path, new WorldPoint(3300, 3400, 0)));
    }

    @Test
    public void resetWalkerProgressForSkipRecalc_clearsFloorAndSetsPending() {
        Rs2Walker.setPathCommittedFloorIdxForTesting(106);
        Rs2Walker.setLastSkipHandoffWalkSizeForTesting(262);
        PathAheadHandlerState.beginSkipAheadTransit(new WorldPoint(3265, 3325, 0), 50);
        Rs2Walker.resetWalkerProgressForSkipRecalc();
        assertEquals(-1, Rs2Walker.getPathCommittedFloorIdxForTesting());
        assertEquals(-1, Rs2Walker.getLastSkipHandoffWalkSizeForTesting());
        assertTrue(Rs2Walker.isSkipAheadHandoffPending());
    }

    @Test
    public void skipHandoffPlayerAnchor_notStaleEndFloor() {
        List<WorldPoint> path = new java.util.ArrayList<>();
        for (int i = 0; i < 131; i++) {
            path.add(new WorldPoint(3222, 3218 + i, 0));
        }
        WorldPoint playerAtGate = new WorldPoint(3222, 3218, 0);
        Rs2Walker.setPathCommittedFloorIdxForTesting(106);
        int playerIdx = Rs2Walker.closestTileIndexByDistance(path, playerAtGate, 0);
        assertTrue("playerIdx=" + playerIdx, playerIdx >= 0 && playerIdx < 20);
        assertTrue("stale floor pins scan near path end",
                Rs2Walker.getPathScanStartIndex(path) >= 100);
        Rs2Walker.setPathCommittedFloorIdxForTesting(playerIdx);
        assertTrue(Rs2Walker.getPathScanStartIndex(path) < 20);
    }

    @Test
    public void shouldEndSkipAheadTransit_whenForwardClosestPassesSkipIndex() {
        WorldPoint skip = new WorldPoint(3265, 3325, 0);
        PathAheadHandlerState.beginSkipAheadTransit(skip, 50);
        WorldPoint nearSkip = new WorldPoint(3266, 3326, 0);
        WorldPoint farFromSkip = new WorldPoint(3239, 3261, 0);
        assertTrue(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(nearSkip, 55));
        assertFalse(PathAheadHandlerState.shouldEndSkipAheadTransitForClosestIndex(farFromSkip, 40));
    }
}

package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.List;

/**
 * Volatile gate while a path-ahead handler (transport, blob interact, passage door)
 * is settling. Suppresses interim minimap churn and duplicate scans.
 *
 * <p>Skip-ahead transit: one canvas commit + in-flight path recalc; blocks raw-path
 * re-scan until player reaches skip tile or new path catches up.
 */
public final class PathAheadHandlerState {

    private static final int SKIP_AHEAD_CLOSE_TILES = 2;
    private static final long SKIP_AHEAD_TRANSIT_MAX_MS = 15_000L;
    /** Suppress raw-path scene scan briefly after skip-ahead / interim-forward commit. */
    private static final long PATH_AHEAD_COMMIT_COOLDOWN_MS = 400L;

    private static volatile long settlingUntilMs = 0L;
    private static volatile long pathAheadCommitAtMs = 0L;
    private static volatile WorldPoint anchorTile = null;

    private static volatile WorldPoint skipAheadTile = null;
    private static volatile int skipAheadPathIndex = -1;
    private static volatile long skipAheadStartedMs = 0L;

    private PathAheadHandlerState() {
    }

    public static boolean isSettling() {
        return System.currentTimeMillis() < settlingUntilMs;
    }

    public static WorldPoint getAnchorTile() {
        return anchorTile;
    }

    public static void beginSettle(WorldPoint anchor, int waitTicks) {
        anchorTile = anchor;
        long tickMs = 600L;
        settlingUntilMs = System.currentTimeMillis() + Math.max(tickMs, waitTicks * tickMs);
    }

    public static void clear() {
        settlingUntilMs = 0L;
        anchorTile = null;
    }

    public static boolean isSkipAheadTransit() {
        return skipAheadTile != null && skipAheadStartedMs > 0L;
    }

    public static WorldPoint getSkipAheadTile() {
        return skipAheadTile;
    }

    public static int getSkipAheadPathIndex() {
        return skipAheadPathIndex;
    }

    public static void beginSkipAheadTransit(WorldPoint tile, int pathIndex) {
        skipAheadTile = tile;
        skipAheadPathIndex = pathIndex;
        skipAheadStartedMs = System.currentTimeMillis();
        markPathAheadCommit();
    }

    /** After skip-ahead handoff remaps skip tile onto a shorter recalculated path. */
    public static void remapSkipAheadPathIndex(int pathIndex) {
        if (pathIndex >= 0) {
            skipAheadPathIndex = pathIndex;
        }
    }

    /** After minimap or canvas forward commit; blocks immediate raw-path re-scan burst. */
    public static void markPathAheadCommit() {
        pathAheadCommitAtMs = System.currentTimeMillis();
    }

    public static boolean isWithinPathAheadCommitCooldown() {
        long at = pathAheadCommitAtMs;
        if (at <= 0L) {
            return false;
        }
        long age = System.currentTimeMillis() - at;
        return age >= 0L && age < PATH_AHEAD_COMMIT_COOLDOWN_MS;
    }

    public static void clearSkipAheadTransit() {
        skipAheadTile = null;
        skipAheadPathIndex = -1;
        skipAheadStartedMs = 0L;
    }

    /**
     * True when skip-ahead transit should end and normal path-ahead may resume.
     */
    public static boolean shouldEndSkipAheadTransit(WorldPoint player, List<WorldPoint> path) {
        if (!isSkipAheadTransit()) {
            return false;
        }
        int minIdx = Math.max(0, skipAheadPathIndex);
        int closest = path == null ? -1 : Rs2Walker.getForwardClosestTileIndex(path, minIdx);
        return shouldEndSkipAheadTransitForClosestIndex(player, closest);
    }

    /**
     * Test hook: supply precomputed forward-closest index (see {@link #shouldEndSkipAheadTransit(WorldPoint, List)}).
     */
    public static boolean shouldEndSkipAheadTransitForClosestIndex(WorldPoint player, int closestPathIndex) {
        if (!isSkipAheadTransit()) {
            return false;
        }
        long ageMs = System.currentTimeMillis() - skipAheadStartedMs;
        if (ageMs > SKIP_AHEAD_TRANSIT_MAX_MS) {
            return true;
        }
        WorldPoint tile = skipAheadTile;
        if (player != null && tile != null
                && tile.getPlane() == player.getPlane()
                && player.distanceTo2D(tile) <= SKIP_AHEAD_CLOSE_TILES) {
            return true;
        }
        return closestPathIndex >= 0 && closestPathIndex >= skipAheadPathIndex;
    }

    /**
     * Dedupe: already in transit toward this exact skip tile.
     */
    static boolean isDuplicateSkipAhead(WorldPoint tile) {
        if (!isSkipAheadTransit() || tile == null) {
            return false;
        }
        WorldPoint committed = skipAheadTile;
        return committed != null && committed.equals(tile);
    }

    /** Unit tests only. */
    static void setSkipAheadStartedMsForTest(long startedMs) {
        skipAheadStartedMs = startedMs;
    }
}

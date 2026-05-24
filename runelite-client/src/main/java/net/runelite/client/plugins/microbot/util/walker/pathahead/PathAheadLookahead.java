package net.runelite.client.plugins.microbot.util.walker.pathahead;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.List;
import java.util.function.IntPredicate;

/**
 * Fast forward scan of raw path (up to render distance). Queues transport, skip-ahead, and door-like blob.
 */
@Slf4j
public final class PathAheadLookahead {

    static final int LOOKAHEAD_MIN_TILES = 20;
    /** Max path indices for unified transport + unreachable forward scan (with render distance). */
    static final int UNREACHABLE_AHEAD_MAX_TILES = 28;
    /** Inset from loaded WorldView edge; collision BFS unreliable in outer chunk ring. */
    static final int SCENE_PROBE_INSET = Constants.CHUNK_SIZE;
    static final int SKIP_AHEAD_STEP = 8;
    /** Path-ahead queue distance from sticky interim (~half minimap reach). */
    public static final int INTERIM_QUEUE_LEGS = 6;
    static final int BLOB_RADIUS = 5;

    private static volatile PathAheadQueuedAction queued = PathAheadQueuedAction.empty();

    private PathAheadLookahead() {
    }

    public static PathAheadQueuedAction getQueued() {
        return queued;
    }

    public static void clear() {
        queued = PathAheadQueuedAction.empty();
    }

    /**
     * Read-only forward scan; no sleeps. Refreshes {@link #queued}.
     */
    public static void refresh(List<WorldPoint> path) {
        if (PathAheadHandlerState.isSkipAheadTransit()) {
            clear();
            return;
        }
        if (path == null || path.size() < 2) {
            clear();
            return;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            clear();
            return;
        }

        int start = Rs2Walker.getPathScanStartIndex(path);
        if (start < 0) {
            clear();
            return;
        }

        int maxScan = maxScanTiles();
        int end = effectiveForwardScanEnd(path, start, mainScanPathIndices(), player);

        int transportIdx = PathAheadQueuedAction.NONE;
        int firstUnreachable = PathAheadQueuedAction.NONE;
        for (int i = start + 1; i <= end; i++) {
            WorldPoint tile = path.get(i);
            if (tile == null || tile.getPlane() != player.getPlane()) {
                continue;
            }
            if (!isPathTileInProbeBounds(tile, player)) {
                continue;
            }
            if (transportIdx < 0 && TransportPathUtil.hasExplicitTransportStep(path, i)) {
                transportIdx = i;
            }
            if (firstUnreachable < 0 && !Rs2Tile.isTileReachable(tile)) {
                if (TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, i)) {
                    continue;
                }
                firstUnreachable = i;
            }
        }

        PathAheadQueuedAction.PathAheadQueuedActionBuilder q = PathAheadQueuedAction.builder()
                .scannedThroughIndex(end)
                .transportPathIndex(transportIdx);

        if (firstUnreachable >= 0) {
            int skipIdx = PathAheadScanner.findReachableAheadIndex(
                    path, firstUnreachable, SKIP_AHEAD_STEP, player, maxScan, true);
            if (skipIdx >= 0) {
                q.skipAheadPathIndex(skipIdx);
                q.doorPathIndex(PathAheadQueuedAction.NONE);
            } else {
                q.doorPathIndex(firstUnreachable);
                WorldPoint doorAnchor = path.get(firstUnreachable);
                q.doorHit(PathAheadBlobInteract.tryDoorLikeAction(path, firstUnreachable, doorAnchor, BLOB_RADIUS));
            }
        }

        queued = q.build();
    }

    /**
     * Execute queued action when interim is absent or player within {@link #INTERIM_QUEUE_LEGS} of interim.
     *
     * @return true if an action ran
     */
    public static boolean tryExecute(List<WorldPoint> path, WorldPoint target) {
        if (path == null || !queued.hasWork()) {
            return false;
        }
        if (Rs2Walker.hasUpcomingOriginlessTeleport(path, Rs2Walker.getPathScanStartIndex(path))) {
            return false;
        }
        if (PathAheadHandlerState.isSettling()) {
            return false;
        }
        if (!mayExecuteAtInterim()) {
            return false;
        }

        PathAheadQueuedAction q = queued;
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }

        if (q.getSkipAheadPathIndex() >= 0) {
            PathAheadScanner.commitSkipAhead(path, q.getSkipAheadPathIndex(), target);
            return false;
        }

        if (q.getTransportPathIndex() >= 0
                && TransportPathUtil.hasExplicitTransportStep(path, q.getTransportPathIndex())) {
            WorldPoint anchor = path.get(q.getTransportPathIndex());
            if (Rs2Walker.handleTransportsAtPathIndex(path, q.getTransportPathIndex())) {
                PathAheadScanner.finishHandler(path, anchor, player,
                        Rs2Walker.getClosestTileIndex(path), q.getTransportPathIndex(), false);
                clear();
                return true;
            }
        }

        if (q.getDoorHit() != null && PathAheadBlobInteract.interact(q.getDoorHit())) {
            WorldPoint anchor = q.getDoorHit().getAnchor();
            int doorIdx = q.getDoorPathIndex();
            PathAheadScanner.finishHandler(path, anchor, player,
                    Rs2Walker.getClosestTileIndex(path), doorIdx, true);
            int reachableIdx = doorIdx >= 0
                    ? PathAheadScanner.closestReachablePathIndexBefore(path, doorIdx, player)
                    : PathAheadQueuedAction.NONE;
            if (reachableIdx < 0 && doorIdx >= 0) {
                reachableIdx = doorIdx;
            }
            if (reachableIdx >= 0) {
                PathAheadScanner.commitForwardToLastReachable(path, reachableIdx, target);
            }
            clear();
            return true;
        }

        return false;
    }

    static boolean mayExecuteAtInterim() {
        if (PathAheadHandlerState.isSkipAheadTransit()) {
            WorldPoint skip = PathAheadHandlerState.getSkipAheadTile();
            WorldPoint interim = Rs2Walker.getInterimTargetWp();
            if (skip != null && interim != null && skip.equals(interim)) {
                return false;
            }
        }
        WorldPoint interim = Rs2Walker.getInterimTargetWp();
        if (interim == null) {
            return true;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null || interim.getPlane() != player.getPlane()) {
            return true;
        }
        if (interim.distanceTo2D(player) <= INTERIM_QUEUE_LEGS) {
            return true;
        }
        return !Rs2Walker.isInterimMakingRecentProgress();
    }

    /** Path-index span for unified transport + unreachable scan: min(28, render distance). */
    static int mainScanPathIndices() {
        if (Microbot.getClientThread() == null) {
            return UNREACHABLE_AHEAD_MAX_TILES;
        }
        return mainScanPathIndices(maxScanTiles());
    }

    static int mainScanPathIndices(int maxScanTiles) {
        return Math.min(UNREACHABLE_AHEAD_MAX_TILES, maxScanTiles);
    }

    /** Inclusive end path index when scanning forward from {@code start} by {@code span} indices. */
    static int forwardScanEndIndex(int start, int span, int pathSize) {
        if (pathSize <= 0 || start < 0) {
            return -1;
        }
        return Math.min(pathSize - 1, start + span);
    }

    /**
     * Min of index-span cap and inset probe bounds (stop at first path tile outside safe collision ring).
     */
    static int effectiveForwardScanEnd(List<WorldPoint> path, int start, int indexSpan, WorldPoint player) {
        if (path == null || player == null || start < 0) {
            return start;
        }
        int byIndices = forwardScanEndIndex(start, indexSpan, path.size());
        if (byIndices < 0) {
            return start;
        }
        return probeBoundsBoundedScanEndIndex(path, start, byIndices, player);
    }

    /**
     * Inclusive last path index at or before {@code maxEnd} inside inset bounds; stops at first OOB tile.
     */
    static int probeBoundsBoundedScanEndIndex(List<WorldPoint> path, int start, int maxEnd, WorldPoint player) {
        if (path == null || player == null || start < 0 || maxEnd < start) {
            return start;
        }
        WorldView worldView = resolveTopLevelWorldView();
        if (worldView == null) {
            return maxEnd;
        }
        return probeBoundsBoundedScanEndIndex(path, start, maxEnd, i -> {
            WorldPoint tile = path.get(i);
            return isPathTileInProbeBounds(tile, player, worldView);
        });
    }

    static int probeBoundsBoundedScanEndIndex(List<WorldPoint> path, int start, int maxEnd, IntPredicate inBoundsAtIndex) {
        if (start < 0 || maxEnd < start) {
            return start;
        }
        int capped = start;
        for (int i = start + 1; i <= maxEnd; i++) {
            if (!inBoundsAtIndex.test(i)) {
                break;
            }
            capped = i;
        }
        return capped;
    }

    /** True when tile is on player plane and inside WorldView inset by {@link #SCENE_PROBE_INSET}. */
    static boolean isPathTileInProbeBounds(WorldPoint tile, WorldPoint player) {
        if (tile == null || player == null || tile.getPlane() != player.getPlane()) {
            return false;
        }
        WorldView worldView = resolveTopLevelWorldView();
        if (worldView == null) {
            return false;
        }
        return isPathTileInProbeBounds(tile, player, worldView);
    }

    static boolean isPathTileInProbeBounds(WorldPoint tile, WorldPoint player, WorldView worldView) {
        if (tile == null || player == null || worldView == null || tile.getPlane() != player.getPlane()) {
            return false;
        }
        int minX = worldView.getBaseX() + SCENE_PROBE_INSET;
        int minY = worldView.getBaseY() + SCENE_PROBE_INSET;
        int maxX = worldView.getBaseX() + worldView.getSizeX() - SCENE_PROBE_INSET;
        int maxY = worldView.getBaseY() + worldView.getSizeY() - SCENE_PROBE_INSET;
        if (minX >= maxX || minY >= maxY) {
            return false;
        }
        int x = tile.getX();
        int y = tile.getY();
        return x >= minX && x < maxX && y >= minY && y < maxY;
    }

    static WorldView resolveTopLevelWorldView() {
        var clientThread = Microbot.getClientThread();
        if (clientThread == null) {
            return null;
        }
        return clientThread.runOnClientThreadOptional(() -> {
            Client client = Microbot.getClient();
            return client != null ? client.getTopLevelWorldView() : null;
        }).orElse(null);
    }

    static int maxScanTiles() {
        Integer drawDistance = Microbot.getClientThread().invoke(() -> {
            Client client = Microbot.getClient();
            if (client == null || client.getScene() == null) {
                return null;
            }
            return client.getScene().getDrawDistance();
        });
        int render = (drawDistance != null && drawDistance > 0)
                ? drawDistance
                : Constants.SCENE_SIZE / 2;
        return Math.max(LOOKAHEAD_MIN_TILES, render);
    }
}

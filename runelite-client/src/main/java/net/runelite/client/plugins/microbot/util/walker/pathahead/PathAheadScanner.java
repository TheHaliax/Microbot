package net.runelite.client.plugins.microbot.util.walker.pathahead;



import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.walker.WebWalkLog;
import net.runelite.client.plugins.microbot.util.walker.passage.PassageDoorHandler;



import java.util.List;
import java.util.Set;



/**
 * Scans raw path ahead for transport blobs and unreachable tiles. Replaces legacy door-ahead probing.
 */
@Slf4j
public final class PathAheadScanner {



    private static final int BLOB_RADIUS = 5;
    private static final int STEP_BACK_TILES = 2;
    private static final int APPROACH_CLOSE_CHEBYSHEV = 2;
    private static final int MINIMAP_REACH_EUCLIDEAN = 13;
    private static final long INTERIM_CHAIN_COOLDOWN_MS = 500L;
    private static volatile long lastInterimChainAtMs = 0L;



    private PathAheadScanner() {
    }



    /**
     * Indices scanned in the unreachable step-back loop (unreachable, -2, -4, ... until 0).
     */
    static List<Integer> stepBackAnchorIndices(int unreachableIdx) {
        java.util.List<Integer> indices = new java.util.ArrayList<>();
        if (unreachableIdx < 0) {
            return indices;
        }
        for (int scanIdx = unreachableIdx; scanIdx >= 0; scanIdx -= STEP_BACK_TILES) {
            indices.add(scanIdx);
        }
        return indices;
    }



    /**
     * Door must share the unreachable path vertex (Chebyshev 0), not merely sit nearby.
     */
    public static boolean isPassageDoorOnPathFocus(List<WorldPoint> path, int focusIdx, WorldPoint doorLoc) {
        if (path == null || doorLoc == null || focusIdx < 0 || focusIdx >= path.size()) {
            return false;
        }
        WorldPoint focus = path.get(focusIdx);
        return focus != null
                && focus.getPlane() == doorLoc.getPlane()
                && doorLoc.distanceTo2D(focus) == 0;
    }



    /**
     * @return true if a handler ran (transport, blob interact, skip-ahead, or passage fallback)
     */
    public static boolean tryHandle(List<WorldPoint> path, WorldPoint target, int handlerRange) {
        if (path == null || path.size() < 2) {
            return false;
        }
        if (PathAheadHandlerState.isSkipAheadTransit()) {
            return false;
        }
        if (PathAheadHandlerState.isSettling()) {
            return false;
        }



        PathAheadLookahead.refresh(path);
        if (PathAheadLookahead.tryExecute(path, target)) {
            return true;
        }



        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }



        int unreachableIdx = scanFirstUnreachableAhead(path, player);
        if (unreachableIdx < 0) {
            return false;
        }
        if (trySkipAheadFromUnreachable(path, unreachableIdx, target, player)) {
            return false;
        }
        return tryRecoverAtIndex(path, unreachableIdx, target, handlerRange);
    }



    /**
     * Recovery entry for segment-loop unreachable tiles.
     */
    public static boolean tryRecoverUnreachable(List<WorldPoint> path, WorldPoint target, int unreachableIdx,
                                                int handlerRange) {
        if (path == null) {
            return false;
        }
        if (unreachableIdx >= 0 && Rs2Walker.isPathIndexBehindProgress(path, unreachableIdx)) {
            return false;
        }
        if (PathAheadHandlerState.isSkipAheadTransit()) {
            return false;
        }
        if (PathAheadHandlerState.isSettling()) {
            return false;
        }
        PathAheadLookahead.refresh(path);
        if (PathAheadLookahead.tryExecute(path, target)) {
            return true;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }
        int focusIdx = unreachableIdx >= 0 ? unreachableIdx : scanFirstUnreachableAhead(path, player);
        if (focusIdx < 0) {
            return false;
        }
        if (trySkipAheadFromUnreachable(path, focusIdx, target, player)) {
            return false;
        }
        return tryRecoverAtIndex(path, focusIdx, target, handlerRange);
    }



    static boolean tryRecoverAtIndex(List<WorldPoint> path, int unreachableIdx, WorldPoint target, int handlerRange) {
        if (PathAheadHandlerState.isSkipAheadTransit()) {
            return false;
        }
        if (Rs2Walker.isPathIndexBehindProgress(path, unreachableIdx)) {
            return false;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }

        WorldPoint unreachableTile = unreachableIdx >= 0 && unreachableIdx < path.size()
                ? path.get(unreachableIdx) : null;
        if (Rs2Walker.isPassageRecentlyHandled(unreachableTile)) {
            return false;
        }

        int closestBefore = Rs2Walker.getClosestTileIndex(path);
        int maxScan = PathAheadLookahead.maxScanTiles();

        if (TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, unreachableIdx)) {
            log.debug("[PathAhead] recover skip transport blob idx={} wp={}",
                    unreachableIdx, path.get(unreachableIdx));
            return false;
        }



        for (int scanIdx : stepBackAnchorIndices(unreachableIdx)) {
            if (Rs2Walker.isPathIndexBehindProgress(path, scanIdx)) {
                continue;
            }
            WorldPoint anchor = path.get(scanIdx);
            if (anchor == null) {
                continue;
            }
            if (Rs2Walker.isPassageRecentlyHandled(anchor)) {
                continue;
            }
            if (TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, scanIdx)) {
                continue;
            }

            if (Rs2Tile.isTileReachable(anchor)) {
                log.debug("[PathAhead] reachable anchor at idx={} wp={}", scanIdx, anchor);
                if (commitForwardToLastReachable(path, scanIdx, target)) {
                    return true;
                }
                continue;
            }

            WebWalkLog.tmark("blob_stepback", Rs2Walker.getWalkSessionElapsedMs(),
                    walkGoal(target), player, "anchorIdx=" + scanIdx);

            PathAheadBlobInteract.BlobHit hit =
                    PathAheadBlobInteract.tryDoorLikeAction(path, scanIdx, anchor, BLOB_RADIUS);
            if (hit != null && PathAheadBlobInteract.interact(hit)) {
                finishHandler(path, anchor, player, closestBefore, unreachableIdx, true);
                int reachableIdx = closestReachablePathIndexBefore(path, scanIdx, player);
                if (reachableIdx < 0) {
                    reachableIdx = scanIdx;
                }
                if (commitForwardToLastReachable(path, reachableIdx, target)) {
                    return true;
                }
                return true;
            }
        }



        int lastReachable = closestReachablePathIndexBefore(path, unreachableIdx, player);
        if (lastReachable >= 0 && commitForwardToLastReachable(path, lastReachable, target)) {
            return true;
        }



        return tryPassageFallback(path, unreachableIdx, player, target, handlerRange, closestBefore, maxScan);
    }



    /**
     * Minimap forward commit toward last reachable path index (interim + optional floor).
     */
    public static boolean commitForwardToLastReachable(List<WorldPoint> path, int reachableIdx, WorldPoint goal) {
        if (path == null || reachableIdx < 0 || reachableIdx >= path.size()) {
            return false;
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return false;
        }
        WorldPoint reachableTile = path.get(reachableIdx);
        if (reachableTile == null || reachableTile.getPlane() != player.getPlane()) {
            return false;
        }
        if (player.distanceTo2D(reachableTile) <= APPROACH_CLOSE_CHEBYSHEV) {
            return false;
        }



        int floor = Math.max(Rs2Walker.getPathCommittedFloorIdx(), 0);
        int clickIdx = Rs2Walker.findFurthestClickableIndex(path, floor, player,
                wp -> {
                    Set<Transport> ts = ShortestPathPlugin.getTransports().get(wp);
                    return ts != null && !ts.isEmpty();
                },
                MINIMAP_REACH_EUCLIDEAN);
        clickIdx = Math.min(clickIdx, reachableIdx);
        clickIdx = Rs2Walker.adjustSolidInterimPathIndex(path, clickIdx);
        WorldPoint nextWp = path.get(clickIdx);
        if (nextWp == null) {
            return false;
        }



        boolean inInstance = Microbot.getClient().getTopLevelWorldView().isInstance();
        WorldPoint clickTarget = inInstance ? nextWp : Rs2Walker.getPointWithWallDistance(nextWp);
        boolean clicked = Rs2Walker.walkMiniMap(clickTarget);
        if (!clicked) {
            clicked = Rs2Walker.walkMiniMapTowardPoint(clickTarget, player, MINIMAP_REACH_EUCLIDEAN - 1);
        }
        if (!clicked) {
            return false;
        }



        commitInterimForward(path, clickIdx, nextWp, goal);
        WebWalkLog.tmark("blob_forward_commit", Rs2Walker.getWalkSessionElapsedMs(),
                walkGoal(goal), Rs2Player.getWorldLocation(),
                "reachableIdx=" + reachableIdx + " clickIdx=" + clickIdx);
        return true;
    }



    static int findReachableAheadIndex(List<WorldPoint> path, int fromIdx, int step, WorldPoint player,
                                       int maxScanTiles) {
        return findReachableAheadIndex(path, fromIdx, step, player, maxScanTiles, false);
    }

    /**
     * Scan forward on path for a reachable tile. When {@code relaxProbeBounds}, skip scene inset cap
     * so skip-ahead can find reachable tiles past gates just outside the loaded collision ring.
     */
    static int findReachableAheadIndex(List<WorldPoint> path, int fromIdx, int step, WorldPoint player,
                                       int maxScanTiles, boolean relaxProbeBounds) {
        if (path == null || fromIdx < 0 || step <= 0 || player == null) {
            return PathAheadQueuedAction.NONE;
        }
        int end = relaxProbeBounds
                ? PathAheadLookahead.forwardScanEndIndex(fromIdx, maxScanTiles, path.size())
                : PathAheadLookahead.effectiveForwardScanEnd(path, fromIdx, maxScanTiles, player);
        for (int i = fromIdx + step; i <= end; i += step) {
            if (TransportPathUtil.hasExplicitTransportStep(path, i)) {
                continue;
            }
            WorldPoint tile = path.get(i);
            if (tile == null || tile.getPlane() != player.getPlane()) {
                continue;
            }
            if (!relaxProbeBounds && !PathAheadLookahead.isPathTileInProbeBounds(tile, player)) {
                continue;
            }
            if (player.distanceTo2D(tile) > maxScanTiles) {
                continue;
            }
            if (Rs2Tile.isTileReachable(tile)) {
                return i;
            }
        }
        return PathAheadQueuedAction.NONE;
    }



    static int scanFirstUnreachableAhead(List<WorldPoint> path, WorldPoint player) {
        int startIdx = Rs2Walker.getPathScanStartIndex(path);
        if (startIdx < 0) {
            return PathAheadQueuedAction.NONE;
        }
        return findFirstUnreachableAhead(path, startIdx, player, PathAheadLookahead.mainScanPathIndices());
    }

    static boolean trySkipAheadFromUnreachable(List<WorldPoint> path, int unreachableIdx, WorldPoint target,
                                               WorldPoint player) {
        int maxScan = PathAheadLookahead.maxScanTiles();
        int skipIdx = findReachableAheadIndex(path, unreachableIdx, PathAheadLookahead.SKIP_AHEAD_STEP, player, maxScan,
                true);
        if (skipIdx >= 0) {
            commitSkipAhead(path, skipIdx, target);
            return true;
        }
        return false;
    }

    static int findFirstUnreachableAhead(List<WorldPoint> path, int startIdx, WorldPoint player, int maxPathIndices) {
        int progressStart = Rs2Walker.getPathScanStartIndex(path);
        int scanStart = progressStart >= 0 ? Math.max(startIdx, progressStart) : startIdx;
        if (scanStart < 0) {
            return PathAheadQueuedAction.NONE;
        }
        int end = PathAheadLookahead.effectiveForwardScanEnd(path, scanStart, maxPathIndices, player);
        for (int i = Math.max(0, scanStart + 1); i <= end; i++) {
            if (Rs2Walker.isPathIndexBehindProgress(path, i)) {
                continue;
            }
            WorldPoint tile = path.get(i);
            if (tile == null || tile.getPlane() != player.getPlane()) {
                continue;
            }
            if (Rs2Walker.isPassageRecentlyHandled(tile)) {
                continue;
            }
            if (!PathAheadLookahead.isPathTileInProbeBounds(tile, player)) {
                continue;
            }
            if (!Rs2Tile.isTileReachable(tile)) {
                if (TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, i)) {
                    continue;
                }
                return i;
            }
        }
        return PathAheadQueuedAction.NONE;
    }



    /**
     * After successful minimap click: sticky interim, path-ahead refresh, optional skip-ahead.
     * Does not restart pathfinder unless skip-ahead commits.
     */
    public static void commitInterimForward(List<WorldPoint> path, int pathIdx, WorldPoint clickTarget,
                                            WorldPoint goal) {
        if (clickTarget == null) {
            return;
        }
        int idx = pathIdx >= 0 ? pathIdx : PathAheadQueuedAction.NONE;
        Rs2Walker.setInterimCheckpoint(clickTarget, idx);
        if (path != null && idx >= 0
                && Rs2Walker.isValidInterimPathAlignment(path, idx, clickTarget)
                && Rs2Walker.isMovingForInterimCommit()) {
            Rs2Walker.advancePathCommittedFloor(path, idx);
        }
        PathAheadHandlerState.markPathAheadCommit();
        if (path != null) {
            PathAheadLookahead.refresh(path);
            PathAheadQueuedAction q = PathAheadLookahead.getQueued();
            if (q.getSkipAheadPathIndex() >= 0) {
                commitSkipAhead(path, q.getSkipAheadPathIndex(), goal);
            }
        }
    }



    /**
     * While interim is in queue range and player is moving, click the next solid minimap checkpoint.
     *
     * @return true when a new interim click was committed
     */
    public static boolean tryChainInterimCheckpoint(List<WorldPoint> path, WorldPoint goal) {
        if (path == null || path.isEmpty()) {
            logInterimChainSkip(goal, "no_path");
            return false;
        }
        if (Rs2Walker.hasUpcomingOriginlessTeleport(path, Rs2Walker.getPathScanStartIndex(path))) {
            logInterimChainSkip(goal, "originless_teleport_pending");
            return false;
        }
        WorldPoint interim = Rs2Walker.getInterimTargetWp();
        int interimIdx = Rs2Walker.getInterimTargetIdx();
        WorldPoint player = Rs2Player.getWorldLocation();
        if (interim == null || interimIdx < 0 || player == null
                || interim.getPlane() != player.getPlane()) {
            logInterimChainSkip(goal, "no_interim");
            return false;
        }
        if (player.distanceTo2D(interim) > PathAheadLookahead.INTERIM_QUEUE_LEGS) {
            logInterimChainSkip(goal, "interim_too_far");
            return false;
        }
        if (!Rs2Walker.isMovingForInterimCommit()) {
            logInterimChainSkip(goal, "not_moving");
            return false;
        }
        long now = System.currentTimeMillis();
        if (PathAheadHandlerState.isWithinPathAheadCommitCooldown()) {
            logInterimChainSkip(goal, "commit_cooldown");
            return false;
        }
        if (lastInterimChainAtMs > 0L && now - lastInterimChainAtMs < INTERIM_CHAIN_COOLDOWN_MS) {
            logInterimChainSkip(goal, "chain_cooldown");
            return false;
        }



        int floor = Math.max(Rs2Walker.getPathCommittedFloorIdx(), interimIdx + 1);
        int nextIdx = Rs2Walker.findFurthestClickableIndex(path, floor, player,
                wp -> {
                    Set<Transport> ts = ShortestPathPlugin.getTransports().get(wp);
                    return ts != null && !ts.isEmpty();
                },
                MINIMAP_REACH_EUCLIDEAN);
        nextIdx = Rs2Walker.adjustSolidInterimPathIndex(path, nextIdx);
        if (nextIdx <= interimIdx) {
            logInterimChainSkip(goal, "nextIdx_le_interim");
            return false;
        }
        WorldPoint nextWp = path.get(nextIdx);
        boolean inInstance = Microbot.getClient().getTopLevelWorldView().isInstance();
        WorldPoint clickTarget = inInstance ? nextWp : Rs2Walker.getPointWithWallDistance(nextWp);
        boolean clicked = Rs2Walker.walkMiniMap(clickTarget);
        if (!clicked) {
            clicked = Rs2Walker.walkMiniMapTowardPoint(clickTarget, player, MINIMAP_REACH_EUCLIDEAN - 1);
        }
        if (!clicked) {
            logInterimChainSkip(goal, "click_failed");
            return false;
        }
        lastInterimChainAtMs = now;
        commitInterimForward(path, nextIdx, nextWp, goal);
        WebWalkLog.tmark("interim_chain_ok", Rs2Walker.getWalkSessionElapsedMs(),
                walkGoal(goal), Rs2Player.getWorldLocation(),
                "idx=" + nextIdx + " to=" + nextWp.getX() + "," + nextWp.getY());
        return true;
    }



    private static void logInterimChainSkip(WorldPoint goal, String reason) {
        WebWalkLog.tmark("interim_chain_skip", Rs2Walker.getWalkSessionElapsedMs(),
                walkGoal(goal), Rs2Player.getWorldLocation(), "reason=" + reason);
    }



    static int closestReachablePathIndexBefore(List<WorldPoint> path, int beforeIdx, WorldPoint player) {
        for (int i = Math.min(beforeIdx - 1, path.size() - 1); i >= 0; i--) {
            WorldPoint tile = path.get(i);
            if (tile == null || tile.getPlane() != player.getPlane()) {
                continue;
            }
            if (Rs2Tile.isTileReachable(tile)) {
                return i;
            }
        }
        return PathAheadQueuedAction.NONE;
    }



    /**
     * One-shot skip-ahead: canvas walk, interim, transit gate, path recalc from skip tile.
     * Always returns false so {@link #tryHandle} does not set raw-path-scene-object-handled.
     */
    static void commitSkipAhead(List<WorldPoint> path, int skipIdx, WorldPoint goal) {
        if (path == null || skipIdx < 0 || skipIdx >= path.size()) {
            return;
        }
        WorldPoint tile = path.get(skipIdx);
        if (tile == null) {
            return;
        }
        if (PathAheadHandlerState.isDuplicateSkipAhead(tile)) {
            return;
        }
        if (!canvasWalkToTile(path, skipIdx)) {
            return;
        }
        PathAheadHandlerState.beginSkipAheadTransit(tile, skipIdx);
        Rs2Walker.resetWalkerProgressForSkipRecalc();
        PathAheadLookahead.clear();
        WorldPoint routeGoal = walkGoal(goal);
        Rs2Walker.restartPathfindingFromSkipAhead(tile, routeGoal);
        log.debug("[PathAhead] skip-ahead transit idx={} wp={} goal={}", skipIdx, tile, routeGoal);
    }



    /**
     * Camera toward tile, then canvas walk + interim checkpoint.
     */
    public static boolean canvasWalkToTile(List<WorldPoint> path, int pathIdx) {
        if (path == null || pathIdx < 0 || pathIdx >= path.size()) {
            return false;
        }
        WorldPoint tile = path.get(pathIdx);
        if (tile == null) {
            return false;
        }
        LocalPoint lp = LocalPoint.fromWorld(Microbot.getClient().getTopLevelWorldView(), tile);
        if (lp == null) {
            return false;
        }
        Microbot.getClientThread().invoke(() -> {
            Rs2Camera.centerTileOnScreen(lp, 10.0);
            return null;
        });
        if (!Rs2Walker.walkFastCanvas(tile)) {
            return false;
        }
        Rs2Walker.setInterimCheckpoint(tile, pathIdx);
        Rs2Walker.markUnreachableRecoveryClick();
        return true;
    }



    private static boolean tryPassageFallback(List<WorldPoint> path, int unreachableIdx, WorldPoint player,
                                              WorldPoint target, int handlerRange, int closestBefore,
                                              int maxScan) {
        if (Rs2Walker.getInterimTargetWp() != null && Rs2Walker.isMovingForInterimCommit()) {
            logPassageSuppressed(target, player, unreachableIdx, "interim_in_flight");
            return false;
        }



        int closestNow = Rs2Walker.getClosestTileIndex(path);
        if (closestNow >= 0 && closestNow < unreachableIdx) {
            logPassageSuppressed(target, player, unreachableIdx, "before_focus_region");
            return false;
        }



        if (TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, unreachableIdx)) {
            logPassageSuppressed(target, player, unreachableIdx, "transport_blob");
            return false;
        }



        if (!Rs2Walker.isStuckTooLongForPassageRecovery()) {
            logPassageSuppressed(target, player, unreachableIdx, "not_stalled");
            return false;
        }



        int approachIdx = closestReachablePathIndexBefore(path, unreachableIdx, player);
        if (approachIdx < 0) {
            log.debug("[PathAhead] no reachable approach tile before unreachable idx={}", unreachableIdx);
            return false;
        }



        WorldPoint unreachableTile = path.get(unreachableIdx);
        if (TransportOriginBlobIndex.shouldSuppressPassageDoor(path, unreachableTile)) {
            logPassageSuppressed(target, player, unreachableIdx, "passage_blob");
            return false;
        }



        int maxTiles = Math.min(handlerRange,
                Math.max(PathAheadDoorActions.DEFAULT_PASSAGE_MAX_TILES,
                        player.distanceTo2D(unreachableTile) + 2));



        if (!PassageDoorHandler.openNearestDoor(maxTiles, unreachableTile, path, unreachableIdx)) {
            return false;
        }



        WebWalkLog.tmark("passage_open", Rs2Walker.getWalkSessionElapsedMs(),
                walkGoal(target), player, "focusIdx=" + unreachableIdx);
        finishHandler(path, unreachableTile, player, closestBefore, unreachableIdx, false);
        return true;
    }



    private static void logPassageSuppressed(WorldPoint target, WorldPoint player, int focusIdx, String reason) {
        WebWalkLog.tmark("passage_suppressed", Rs2Walker.getWalkSessionElapsedMs(),
                walkGoal(target), player, "focusIdx=" + focusIdx + " reason=" + reason);
    }



    private static WorldPoint walkGoal(WorldPoint goal) {
        return goal != null ? goal : Rs2Walker.getCurrentTarget();
    }



    static void finishHandler(List<WorldPoint> path, WorldPoint anchor, WorldPoint playerBefore,
                              int closestBefore, int focusIdx, boolean doorFast) {
        if (doorFast) {
            finishHandlerDoorFast(path, anchor, playerBefore, closestBefore, focusIdx);
            return;
        }
        int dist = playerBefore != null && anchor != null ? playerBefore.distanceTo2D(anchor) : 1;
        int waitTicks = Math.max(1, (int) Math.ceil(dist * 1.5));
        PathAheadHandlerState.beginSettle(anchor, waitTicks);
        Global.sleepTicks(waitTicks);
        if (isPathResumable(path, closestBefore, focusIdx)) {
            PathAheadHandlerState.clear();
        }
    }



    private static void finishHandlerDoorFast(List<WorldPoint> path, WorldPoint anchor, WorldPoint playerBefore,
                                              int closestBefore, int focusIdx) {
        PathAheadHandlerState.beginSettle(anchor, 2);
        final WorldPoint anchorFinal = anchor;
        final WorldPoint before = playerBefore;
        Global.sleepUntil(() -> {
            if (anchorFinal != null && Rs2Tile.isTileReachable(anchorFinal)) {
                return true;
            }
            WorldPoint now = Rs2Player.getWorldLocation();
            if (now != null && before != null && now.distanceTo2D(before) >= 1) {
                return true;
            }
            return !Rs2Player.isAnimating();
        }, 1200);
        if (anchor != null) {
            Rs2Walker.markPassageHandled(anchor);
        }
        if (path != null && focusIdx >= 0 && focusIdx + 1 < path.size()) {
            Rs2Walker.advancePathCommittedFloor(path, focusIdx + 1);
        }
        WorldPoint now = Rs2Player.getWorldLocation();
        if (path != null && now != null && isPathResumable(path, closestBefore, focusIdx)) {
            Rs2Walker.commitWalkingOriginFromPath(path, now, "passage_handled");
            PathAheadHandlerState.clear();
        } else if (isPathResumable(path, closestBefore, focusIdx)) {
            PathAheadHandlerState.clear();
        }
    }



    static boolean isPathResumable(List<WorldPoint> path, int closestBefore, int focusIdx) {
        int closestNow = Rs2Walker.getClosestTileIndex(path);
        if (closestNow > closestBefore) {
            return true;
        }
        if (focusIdx >= 0 && focusIdx < path.size()) {
            WorldPoint focus = path.get(focusIdx);
            if (focus != null && Rs2Tile.isTileReachable(focus)) {
                return true;
            }
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player != null && closestNow >= 0 && closestNow < path.size()) {
            return player.distanceTo2D(path.get(closestNow)) <= 2;
        }
        return false;
    }
}




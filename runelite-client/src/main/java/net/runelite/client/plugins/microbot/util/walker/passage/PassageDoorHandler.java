package net.runelite.client.plugins.microbot.util.walker.passage;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.misc.Rs2UiHelper;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.pathahead.PathAheadDoorActions;
import net.runelite.client.plugins.microbot.util.walker.pathahead.PathAheadQueuedAction;
import net.runelite.client.plugins.microbot.util.walker.pathahead.PathAheadScanner;
import net.runelite.client.plugins.microbot.util.walker.pathahead.TransportOriginBlobIndex;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Nearest door-like tile object: passage action, pick-lock loop, settle idle.
 * Script executor only. Canonical copy lives in the Microbot client; sync microbot-api skill on changes.
 */
@Slf4j
public final class PassageDoorHandler {

    private static final int SETTLE_ANIM_MS = 3_000;
    private static final int SETTLE_MOVE_MS = 5_000;
    private static final int PICK_LOCK_MAX_ATTEMPTS = 5;
    private static final long PICK_LOCK_TIMEOUT_MS = 15_000L;

    private PassageDoorHandler() {
    }

    public static boolean openNearestDoor() {
        return openNearestDoor(PathAheadDoorActions.DEFAULT_PASSAGE_MAX_TILES);
    }

    public static boolean openNearestDoor(int maxTiles) {
        return openNearestDoor(maxTiles, null);
    }

    /**
     * @param near optional bias point (e.g. unreachable path tile); null uses player only
     */
    public static boolean openNearestDoor(int maxTiles, WorldPoint near) {
        return openNearestDoor(maxTiles, near, null);
    }

    /**
     * @param path when non-null, doors on catalog transport blobs without a path transport step are skipped
     */
    public static boolean openNearestDoor(int maxTiles, WorldPoint near, List<WorldPoint> path) {
        return openNearestDoor(maxTiles, near, path, PathAheadQueuedAction.NONE);
    }

    /**
     * @param focusPathIdx when {@code >= 0}, door tile must equal {@code path.get(focusPathIdx)} (Chebyshev 0)
     */
    public static boolean openNearestDoor(int maxTiles, WorldPoint near, List<WorldPoint> path, int focusPathIdx) {
        if (Microbot.getClient().isClientThread()) {
            log.warn("PassageDoorHandler.openNearestDoor must run on script executor, not client thread");
            return false;
        }
        if (maxTiles <= 0) {
            return false;
        }

        Rs2TileObjectModel door = findNearestWalkableDoor(maxTiles, near, path, focusPathIdx);
        if (door == null) {
            log.debug("[PassageDoorHandler] no walkable door within {} tiles near {}", maxTiles, near);
            return false;
        }

        WorldPoint anchor = door.getWorldLocation();
        long pickLockDeadline = System.currentTimeMillis() + PICK_LOCK_TIMEOUT_MS;
        int pickLockAttempts = 0;

        while (isPickLockAction(PathAheadDoorActions.pickWalkDoorAction(door.getObjectComposition()))) {
            if (pickLockAttempts >= PICK_LOCK_MAX_ATTEMPTS || System.currentTimeMillis() >= pickLockDeadline) {
                log.debug("[PassageDoorHandler] pick-lock exhausted at {}", anchor);
                return false;
            }

            String pickLock = PathAheadDoorActions.pickWalkDoorAction(door.getObjectComposition());
            if (pickLock == null || !door.click(pickLock)) {
                log.debug("[PassageDoorHandler] pick-lock click failed at {}", anchor);
                return false;
            }

            pickLockAttempts++;
            waitSettle();

            door = resolveDoorAt(anchor, door.getHash());
            if (door == null) {
                log.debug("[PassageDoorHandler] door vanished after pick-lock at {}", anchor);
                return false;
            }
        }

        ObjectComposition comp = door.getObjectComposition();
        String action = PathAheadDoorActions.pickWalkDoorAction(comp);
        if (action == null) {
            log.debug("[PassageDoorHandler] no passage action at {} name='{}'", anchor, door.getName());
            return false;
        }

        if (PathAheadDoorActions.doorActionPriorityIndex(action) == Integer.MAX_VALUE) {
            log.debug("[PassageDoorHandler] non-standard door action '{}' at {}", action, anchor);
            return false;
        }

        if (!door.click(action)) {
            log.debug("[PassageDoorHandler] click('{}') failed at {}", action, anchor);
            return false;
        }

        waitSettle();
        log.info("[PassageDoorHandler] handled id={} name='{}' action='{}' wp={}",
                door.getId(), door.getName(), action, anchor);
        return true;
    }

    private static Rs2TileObjectModel findNearestWalkableDoor(int maxTiles, WorldPoint near, List<WorldPoint> path,
                                                             int focusPathIdx) {
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null) {
            return null;
        }

        return Microbot.getRs2TileObjectCache().query()
                .within(maxTiles)
                .toList()
                .stream()
                .filter(PassageDoorHandler::isWalkableDoorCandidate)
                .filter(o -> !shouldSuppressDoorOnPath(path, o.getWorldLocation()))
                .filter(o -> focusPathIdx < 0
                        || PathAheadScanner.isPassageDoorOnPathFocus(path, focusPathIdx, o.getWorldLocation()))
                .min(Comparator.comparingInt(o -> scoreDistance(player, near, o.getWorldLocation())))
                .orElse(null);
    }

    private static boolean shouldSuppressDoorOnPath(List<WorldPoint> path, WorldPoint doorLoc) {
        if (path == null || doorLoc == null) {
            return false;
        }
        return TransportOriginBlobIndex.shouldSuppressPassageDoor(path, doorLoc);
    }

    private static int scoreDistance(WorldPoint player, WorldPoint near, WorldPoint objectLoc) {
        if (objectLoc == null) {
            return Integer.MAX_VALUE;
        }
        int score = player.distanceTo(objectLoc);
        if (near != null && near.getPlane() == objectLoc.getPlane()) {
            score = score * 10 + near.distanceTo2D(objectLoc);
        }
        return score;
    }

    private static boolean isWalkableDoorCandidate(Rs2TileObjectModel obj) {
        if (obj == null) {
            return false;
        }
        String name = obj.getName();
        if (!PathAheadDoorActions.isDoorLikeGameObjectName(Rs2UiHelper.stripColTags(name))) {
            return false;
        }
        ObjectComposition comp = obj.getObjectComposition();
        if (comp == null || PathAheadDoorActions.isNullOrPlaceholderObjectName(comp.getName())) {
            return false;
        }
        if (PathAheadDoorActions.doorCompositionSpecifiesOnlyCloseOrShut(comp)) {
            return false;
        }
        String action = PathAheadDoorActions.pickWalkDoorAction(comp);
        return action != null && PathAheadDoorActions.doorActionPriorityIndex(action) != Integer.MAX_VALUE;
    }

    private static Rs2TileObjectModel resolveDoorAt(WorldPoint anchor, long preferredHash) {
        if (anchor == null) {
            return null;
        }

        List<Rs2TileObjectModel> atTile = Microbot.getRs2TileObjectCache().query()
                .where(o -> anchor.equals(o.getWorldLocation()))
                .toList();

        if (atTile.isEmpty()) {
            return null;
        }

        return atTile.stream()
                .filter(o -> o.getHash() == preferredHash)
                .findFirst()
                .orElse(atTile.stream()
                        .filter(o -> PathAheadDoorActions.isDoorLikeGameObjectName(o.getName()))
                        .findFirst()
                        .orElse(atTile.get(0)));
    }

    private static void waitSettle() {
        Rs2Player.waitForAnimation(SETTLE_ANIM_MS);
        Global.sleepUntil(() -> !Rs2Player.isMoving(), SETTLE_MOVE_MS);
    }

    private static boolean isPickLockAction(String action) {
        if (action == null) {
            return false;
        }
        String al = action.toLowerCase(Locale.ROOT).trim();
        return al.startsWith("pick-lock") || al.startsWith("pick lock");
    }
}

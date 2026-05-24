package net.runelite.client.plugins.microbot.util.walker.pathahead;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.api.tileobject.models.Rs2TileObjectModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import java.util.Comparator;
import java.util.List;

/**
 * Radius blob scan for door-like tile objects at a path anchor.
 */
@Slf4j
public final class PathAheadBlobInteract {

    @Value
    public static class BlobHit {
        Rs2TileObjectModel object;
        String action;
        WorldPoint anchor;
    }

    private PathAheadBlobInteract() {
    }

    /**
     * Nearest door-like object at anchor with a walk door action.
     */
    public static BlobHit tryDoorLikeAction(WorldPoint anchor, int radiusTiles) {
        return tryDoorLikeAction(null, PathAheadQueuedAction.NONE, anchor, radiusTiles);
    }

    /**
     * @param path optional raw path for transport-blob suppression at {@code anchorIndex}
     */
    public static BlobHit tryDoorLikeAction(List<WorldPoint> path, int anchorIndex, WorldPoint anchor,
                                            int radiusTiles) {
        if (anchor == null || radiusTiles <= 0) {
            return null;
        }
        if (path != null) {
            int suppressIdx = anchorIndex >= 0
                    ? anchorIndex
                    : TransportOriginBlobIndex.closestPathIndexForTile(path, anchor);
            if (suppressIdx >= 0
                    && TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, suppressIdx)) {
                log.debug("[PathAhead] blob skip transport catalog footprint at {}", anchor);
                return null;
            }
            if (TransportOriginBlobIndex.shouldSuppressPassageDoor(path, anchor)) {
                log.debug("[PathAhead] blob skip passage door at {}", anchor);
                return null;
            }
        }
        WorldPoint player = Rs2Player.getWorldLocation();
        if (player == null || player.getPlane() != anchor.getPlane()) {
            return null;
        }

        List<Rs2TileObjectModel> objects = Microbot.getRs2TileObjectCache().query()
                .within(anchor, radiusTiles)
                .toList();

        return objects.stream()
                .filter(o -> o != null && o.getWorldLocation() != null)
                .filter(o -> o.getWorldLocation().getPlane() == anchor.getPlane())
                .filter(o -> PathAheadDoorActions.isDoorLikeGameObjectName(o.getName()))
                .sorted(Comparator.comparingInt(o -> o.getWorldLocation().distanceTo(anchor)))
                .map(o -> toDoorHit(o, anchor))
                .filter(hit -> hit != null && hit.getAction() != null)
                .findFirst()
                .orElse(null);
    }

    public static boolean interact(BlobHit hit) {
        if (hit == null || hit.getObject() == null || hit.getAction() == null) {
            return false;
        }
        boolean clicked = hit.getObject().click(hit.getAction());
        if (clicked) {
            log.info("[PathAhead] blob interact name='{}' action='{}' at {}",
                    hit.getObject().getName(), hit.getAction(), hit.getAnchor());
        }
        return clicked;
    }

    private static BlobHit toDoorHit(Rs2TileObjectModel object, WorldPoint anchor) {
        ObjectComposition comp = object.getObjectComposition();
        if (comp == null || PathAheadDoorActions.isNullOrPlaceholderObjectName(comp.getName())) {
            return null;
        }
        if (PathAheadDoorActions.doorCompositionSpecifiesOnlyCloseOrShut(comp)) {
            return null;
        }
        String action = PathAheadDoorActions.pickWalkDoorAction(comp);
        if (action == null || PathAheadDoorActions.doorActionPriorityIndex(action) == Integer.MAX_VALUE) {
            return null;
        }
        return new BlobHit(object, action, anchor);
    }
}

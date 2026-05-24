package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.shortestpath.Transport;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nine-tile footprint (origin + eight neighbors) for every transport catalog origin.
 * Used to suppress door/passage recovery on tiles near catalog transports the path does not use.
 */
public final class TransportOriginBlobIndex {

    private static volatile Map<WorldPoint, Set<Transport>> cachedTransportMapRef;
    private static volatile TransportOriginBlobIndex cachedCatalogIndex = new TransportOriginBlobIndex(Collections.emptySet());

    private final Set<WorldPoint> blobTiles;

    private TransportOriginBlobIndex(Set<WorldPoint> blobTiles) {
        this.blobTiles = blobTiles == null ? Collections.emptySet() : blobTiles;
    }

    static TransportOriginBlobIndex forTiles(Set<WorldPoint> tiles) {
        return new TransportOriginBlobIndex(tiles);
    }

    /**
     * Lazy catalog index; rebuilds when the transport map instance changes.
     */
    public static TransportOriginBlobIndex catalogIndex() {
        Map<WorldPoint, Set<Transport>> current = ShortestPathPlugin.getTransports();
        if (current == cachedTransportMapRef && cachedCatalogIndex != null) {
            return cachedCatalogIndex;
        }
        cachedTransportMapRef = current;
        cachedCatalogIndex = fromCatalog(current);
        return cachedCatalogIndex;
    }

    /** Unit tests and direct catalog builds. */
    public static TransportOriginBlobIndex fromCatalog() {
        return fromCatalog(ShortestPathPlugin.getTransports());
    }

    static TransportOriginBlobIndex fromCatalog(Map<WorldPoint, Set<Transport>> transports) {
        if (transports == null || transports.isEmpty()) {
            return new TransportOriginBlobIndex(Collections.emptySet());
        }
        Set<WorldPoint> tiles = new HashSet<>();
        for (WorldPoint origin : transports.keySet()) {
            if (origin == null) {
                continue;
            }
            addNineTileFootprint(tiles, origin);
        }
        return new TransportOriginBlobIndex(tiles);
    }

    public static void addNineTileFootprint(Set<WorldPoint> out, WorldPoint origin) {
        if (out == null || origin == null) {
            return;
        }
        int plane = origin.getPlane();
        int ox = origin.getX();
        int oy = origin.getY();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                out.add(new WorldPoint(ox + dx, oy + dy, plane));
            }
        }
    }

    public boolean isInBlob(WorldPoint point) {
        return point != null && blobTiles.contains(point);
    }

    /**
     * True when this path index is on a catalog transport blob but the walker is not
     * executing that transport edge (door/passage recovery must not run here).
     */
    public static boolean shouldSuppressDoorRecovery(List<WorldPoint> path, int index) {
        return shouldSuppressDoorRecovery(path, index, catalogIndex());
    }

    /**
     * True when a door/passage candidate at {@code doorOrNearTile} should not open
     * (catalog transport blob without an explicit transport step on the path).
     */
    public static boolean shouldSuppressPassageDoor(List<WorldPoint> path, WorldPoint doorOrNearTile) {
        return shouldSuppressPassageDoor(path, doorOrNearTile, catalogIndex());
    }

    static boolean shouldSuppressPassageDoor(List<WorldPoint> path, WorldPoint doorOrNearTile,
                                             TransportOriginBlobIndex blobIndex) {
        if (path == null || doorOrNearTile == null || blobIndex == null) {
            return false;
        }
        int idx = closestPathIndexForTile(path, doorOrNearTile);
        if (idx >= 0) {
            return shouldSuppressDoorRecovery(path, idx, blobIndex);
        }
        return blobIndex.isInBlob(doorOrNearTile);
    }

    /**
     * Nearest path vertex on the same plane within Chebyshev 3 of {@code tile}, or -1.
     */
    static int closestPathIndexForTile(List<WorldPoint> path, WorldPoint tile) {
        if (path == null || tile == null) {
            return -1;
        }
        int best = -1;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < path.size(); i++) {
            WorldPoint p = path.get(i);
            if (p == null || p.getPlane() != tile.getPlane()) {
                continue;
            }
            int d = p.distanceTo2D(tile);
            if (d < bestDist) {
                bestDist = d;
                best = i;
            }
        }
        return bestDist <= 3 ? best : -1;
    }

    static boolean shouldSuppressDoorRecovery(List<WorldPoint> path, int index,
                                              TransportOriginBlobIndex blobIndex) {
        if (path == null || index < 0 || index >= path.size() || blobIndex == null) {
            return false;
        }
        WorldPoint tile = path.get(index);
        if (tile == null || !blobIndex.isInBlob(tile)) {
            return false;
        }
        if (pathUsesCatalogTransportAt(path, index)) {
            return false;
        }
        return true;
    }

    private static boolean pathUsesCatalogTransportAt(List<WorldPoint> path, int index) {
        try {
            if (TransportPathUtil.hasExplicitTransportStep(path, index)) {
                return true;
            }
            if (index > 0 && TransportPathUtil.isCatalogBackedTransportSegment(path, index - 1)) {
                return true;
            }
            return index < path.size() - 1
                    && TransportPathUtil.isCatalogBackedTransportSegment(path, index);
        } catch (NullPointerException e) {
            // ShortestPathPlugin.pathfinderConfig unset (unit tests)
            return false;
        }
    }

    /** Clears lazy cache (unit tests). */
    static void clearCatalogCacheForTest() {
        cachedTransportMapRef = null;
        cachedCatalogIndex = new TransportOriginBlobIndex(Collections.emptySet());
    }
}

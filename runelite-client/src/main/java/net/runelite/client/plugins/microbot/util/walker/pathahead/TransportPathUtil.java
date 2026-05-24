package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.shortestpath.Transport;
import net.runelite.client.plugins.microbot.shortestpath.TransportType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Catalog transport edge matching shared by {@link PathAheadScanner} and {@code Rs2Walker}.
 */
public final class TransportPathUtil {

    private TransportPathUtil() {
    }

    public static boolean hasExplicitTransportStep(List<WorldPoint> path, int index) {
        if (path == null || index < 0 || index >= path.size() - 1) {
            return false;
        }
        return matchesDirectedTransportCatalogEdge(path.get(index), path.get(index + 1));
    }

    public static boolean isCatalogBackedTransportSegment(List<WorldPoint> path, int index) {
        if (path == null || index < 0 || index >= path.size() - 1) {
            return false;
        }
        return isCatalogBackedTransportSegment(path.get(index), path.get(index + 1));
    }

    public static boolean isCatalogBackedTransportSegment(WorldPoint from, WorldPoint to) {
        if (from == null || to == null) {
            return false;
        }
        if (matchesDirectedTransportCatalogEdge(from, to)) {
            return true;
        }
        if (matchesDirectedTransportCatalogEdge(to, from)) {
            return true;
        }
        if (matchesAdjacentOriginShortTransportHop(from, to)) {
            return true;
        }
        return matchesAdjacentOriginShortTransportHop(to, from);
    }

    public static boolean matchesDirectedTransportCatalogEdge(WorldPoint origin, WorldPoint dest) {
        if (origin == null || dest == null) {
            return false;
        }
        Set<Transport> transports = ShortestPathPlugin.getTransports().get(origin);
        if (transports == null || transports.isEmpty()) {
            return false;
        }
        return transports.stream().anyMatch(t -> Objects.equals(t.getDestination(), dest));
    }

    private static boolean matchesAdjacentOriginShortTransportHop(WorldPoint from, WorldPoint to) {
        if (from == null || to == null || from.getPlane() != to.getPlane()) {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                WorldPoint catalogOrigin = new WorldPoint(from.getX() + dx, from.getY() + dy, from.getPlane());
                Set<Transport> transports = ShortestPathPlugin.getTransports().get(catalogOrigin);
                if (transports == null || transports.isEmpty()) {
                    continue;
                }
                for (Transport t : transports) {
                    if (Objects.equals(t.getDestination(), to) && isAdjacentSamePlaneTransport(t)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isAdjacentSamePlaneTransport(Transport transport) {
        if (transport == null || transport.getOrigin() == null || transport.getDestination() == null) {
            return false;
        }
        WorldPoint origin = transport.getOrigin();
        WorldPoint dest = transport.getDestination();
        if (origin.getPlane() != dest.getPlane()) {
            return false;
        }
        return origin.distanceTo2D(dest) <= 1;
    }

    /**
     * Originless teleport (tablet, spell, Leagues area, etc.) that should run before interim/minimap clicks.
     */
    public static boolean isOriginlessTeleportForDefer(Transport transport) {
        if (transport == null || transport.getOrigin() != null) {
            return false;
        }
        String di = transport.getDisplayInfo();
        boolean leaguesArea = transport.getType() == TransportType.SEASONAL_TRANSPORT
                && di != null
                && di.toLowerCase().startsWith("leagues area:");
        return transport.getType() == TransportType.TELEPORTATION_ITEM
                || transport.getType() == TransportType.TELEPORTATION_SPELL
                || leaguesArea
                || TransportType.isTeleport(transport.getType(), null);
    }

    /**
     * First originless teleport on {@code path} whose destination appears at or after {@code fromIndex}.
     */
    public static Transport findNextOriginlessTeleportOnPath(List<WorldPoint> path, int fromIndex) {
        if (path == null || fromIndex < 0 || fromIndex >= path.size()) {
            return null;
        }
        for (int currentIndex = fromIndex; currentIndex < path.size(); currentIndex++) {
            WorldPoint currentPoint = path.get(currentIndex);
            Set<Transport> transportsAtPoint = ShortestPathPlugin.getTransports() != null
                    ? ShortestPathPlugin.getTransports().getOrDefault(currentPoint, Collections.emptySet())
                    : Collections.emptySet();
            for (Transport transport : transportsAtPoint) {
                if (!isOriginlessTeleportForDefer(transport)) {
                    continue;
                }
                WorldPoint dest = transport.getDestination();
                if (dest == null) {
                    continue;
                }
                int destIndex = path.indexOf(dest);
                if (destIndex >= fromIndex) {
                    return transport;
                }
            }
        }
        return null;
    }

    /**
     * Whether a transport at path index {@code i} can be dispatched (includes null-origin at path head).
     */
    public static boolean isAtTransportDispatchPoint(List<WorldPoint> path, int i, int indexOfStartPoint,
                                                      WorldPoint origin, TransportType type) {
        if (path == null || i < 0 || i >= path.size()) {
            return false;
        }
        return Objects.equals(path.get(i), origin)
                || (origin == null && i == indexOfStartPoint
                && TransportType.isTeleport(type, null));
    }
}

package net.runelite.client.plugins.microbot.Hal.util;

import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * ChunkRegionAnalyzer
 *
 * Caches, per 64×64 chunk around the player:
 *   • walkable tile list
 *   • exterior edge segments (WorldPoint[2] pairs)
 *   • a closed Polygon (in canvas coords) of that walkable‐area border
 */
public class ChunkRegionAnalyzer
{
    private static final int CHUNK = Constants.CHUNK_SIZE * Constants.CHUNK_SIZE; // 64
    private static final int[][] DIRS = {
            {0,-1},{1,0},{0,1},{-1,0}
    };
    private static final double[][][] EDGE_OFF = {
            {{0,0},{1,0}},  // north
            {{1,0},{1,1}},  // east
            {{1,1},{0,1}},  // south
            {{0,1},{0,0}}   // west
    };

    private int lastBaseX = Integer.MIN_VALUE, lastBaseY = Integer.MIN_VALUE;
    private final List<WorldPoint> walkableTiles = new ArrayList<>();
    private final List<WorldPoint[]> exteriorEdges = new ArrayList<>();
    private Polygon cachedCanvasPolygon;

    /** Returns the world‐space walkable tiles around playerLoc. */
    public List<WorldPoint> getWalkableTiles(Client client, WorldPoint playerLoc)
    {
        ensureCached(client, playerLoc);
        return walkableTiles;
    }

    /** Returns the raw exterior edges as world‐space segment pairs. */
    public List<WorldPoint[]> getExteriorEdges(Client client, WorldPoint playerLoc)
    {
        ensureCached(client, playerLoc);
        return exteriorEdges;
    }

    /**
     * Returns a closed AWT Polygon in **canvas** coords outlining
     * the walkable‐area border.  Null if chunk not fully loaded.
     */
    public Polygon getWalkableAreaPolygon(Client client, WorldPoint playerLoc)
    {
        ensureCached(client, playerLoc);
        if (exteriorEdges.isEmpty())
            return null;

        // Build adjacency: map each endpoint to the segments starting there
        Map<WorldPoint, List<WorldPoint>> adj = new HashMap<>();
        for (WorldPoint[] seg : exteriorEdges)
        {
            adj.computeIfAbsent(seg[0], k->new ArrayList<>()).add(seg[1]);
            adj.computeIfAbsent(seg[1], k->new ArrayList<>()).add(seg[0]);
        }

        // Find a start point (any)
        WorldPoint start = exteriorEdges.get(0)[0];
        List<WorldPoint> chain = new ArrayList<>();
        Set<WorldPoint> visited = new HashSet<>();
        chain.add(start);
        visited.add(start);

        // Walk the border
        WorldPoint current = start;
        outer:
        while (true)
        {
            List<WorldPoint> neigh = adj.getOrDefault(current, List.of());
            for (WorldPoint next : neigh)
            {
                if (!visited.contains(next) || next.equals(start) && chain.size()>1)
                {
                    chain.add(next);
                    if (next.equals(start)) break outer;
                    visited.add(next);
                    current = next;
                    continue outer;
                }
            }
            // no unvisited neighbor → done
            break;
        }

        // Convert chain of world points to canvas polygon
        Polygon poly = new Polygon();
        int plane = playerLoc.getPlane();
        for (WorldPoint wp : chain)
        {
            LocalPoint lp = LocalPoint.fromWorld(client, wp);
            if (lp == null) return null;  // not loaded
            Point p = Perspective.localToCanvas(client, lp, plane);
            if (p == null) return null;
            poly.addPoint((int) p.getX(), p.getY());
        }

        return poly;
    }

    // ————— internal caching ——————————————————————————————————————————————————————

    private void ensureCached(Client client, WorldPoint playerLoc)
    {
        int baseX = (playerLoc.getX() / CHUNK) * CHUNK;
        int baseY = (playerLoc.getY() / CHUNK) * CHUNK;
        int plane = playerLoc.getPlane();

        if (baseX != lastBaseX || baseY != lastBaseY)
        {
            buildWalkable(client, baseX, baseY, plane);
            buildEdges(client, baseX, baseY, plane);
            // clear any previous canvas polygon
            cachedCanvasPolygon = null;
            lastBaseX = baseX;
            lastBaseY = baseY;
        }
    }

    private void buildWalkable(Client client, int bx, int by, int plane)
    {
        walkableTiles.clear();
        for (int x = bx; x < bx + CHUNK; x++)
        {
            for (int y = by; y < by + CHUNK; y++)
            {
                if (LocalPoint.fromWorld(client, x, y) == null) continue;
                if (canWalk(client, x,y, bx,by,plane))
                    walkableTiles.add(new WorldPoint(x,y,plane));
            }
        }
    }

    private void buildEdges(Client client, int bx, int by, int plane)
    {
        exteriorEdges.clear();
        for (WorldPoint t : walkableTiles)
        {
            int tx = t.getX(), ty = t.getY();
            for (int d = 0; d < DIRS.length; d++)
            {
                int nx = tx + DIRS[d][0], ny = ty + DIRS[d][1];
                if (!canWalk(client, nx,ny, bx,by, plane))
                {
                    double[][] off = EDGE_OFF[d];
                    exteriorEdges.add(new WorldPoint[]{
                            new WorldPoint(tx + (int)off[0][0], ty + (int)off[0][1], plane),
                            new WorldPoint(tx + (int)off[1][0], ty + (int)off[1][1], plane)
                    });
                }
            }
        }
    }

    private boolean canWalk(Client client, int tx, int ty,
                            int bx, int by, int plane)
    {
        if (tx < bx || tx >= bx+CHUNK || ty < by || ty >= by+CHUNK)
            return false;
        if (LocalPoint.fromWorld(client, tx, ty) == null)
            return false;

        WorldArea area = new WorldArea(tx, ty, 1,1, plane);
        for (int[] d : DIRS)
        {
            int nx = tx + d[0], ny = ty + d[1];
            if (nx < bx || nx >= bx+CHUNK || ny < by || ny >= by+CHUNK)
                continue;
            if (LocalPoint.fromWorld(client, nx, ny) == null)
                continue;
            if (area.canTravelInDirection(
                    client.getTopLevelWorldView(), d[0], d[1]))
            {
                return true;
            }
        }
        return false;
    }
}

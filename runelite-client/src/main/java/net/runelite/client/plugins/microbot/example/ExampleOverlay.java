package net.runelite.client.plugins.microbot.example;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExampleOverlay extends Overlay
{
    private static final int CHUNK      = Constants.CHUNK_SIZE * Constants.CHUNK_SIZE; // 64
    private static final float LINE_STROKE = 2f;
    private static final Color WHITE_TRANSLUCENT = new Color(0, 255, 255, 127);
    private static final Color RED_TRANSLUCENT = new Color(255, 0, 0, 127);
    private final ModelOutlineRenderer modelOutlineRenderer;

    // Offsets for N, E, S, W
    private static final int[][] DIRS = {
            {0, -1}, {1, 0}, {0, 1}, {-1, 0}
    };
    // Corresponding tile‐local corner offsets
    private static final double[][][] EDGE = {
            {{0,0},{1,0}},  // north
            {{1,0},{1,1}},  // east
            {{1,1},{0,1}},  // south
            {{0,1},{0,0}}   // west
    };

    private int lastBaseX = Integer.MIN_VALUE, lastBaseY = Integer.MIN_VALUE;
    private final List<WorldPoint> walkableTiles = new ArrayList<>();

    @Inject private Client client;
    @Inject private ExamplePlugin plugin;

    @Inject
    public ExampleOverlay(Client client, ExamplePlugin plugin, ModelOutlineRenderer modelOutlineRenderer)
    {
        super(plugin);
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D g)
    {
        Player pl = client.getLocalPlayer();
        if (pl == null) return null;

        WorldPoint wp = pl.getWorldLocation();
        final int S = CHUNK;
        int baseX = (wp.getX() / S) * S;
        int baseY = (wp.getY() / S) * S;
        int plane = wp.getPlane();

        // rebuild once per chunk
        if (baseX != lastBaseX || baseY != lastBaseY)
        {
            rebuildWalkable(baseX, baseY, plane, S);
            lastBaseX = baseX;
            lastBaseY = baseY;
        }

        // draw walkable-border edges
        g.setColor(WHITE_TRANSLUCENT);
        g.setStroke(new BasicStroke(LINE_STROKE));
        for (WorldPoint p : walkableTiles)
        {
            int tx = p.getX(), ty = p.getY();
            for (int dir = 0; dir < 4; dir++)
            {
                int nx = tx + DIRS[dir][0], ny = ty + DIRS[dir][1];
                if (!isWalkable(nx, ny, baseX, baseY, S, plane))
                {
                    double wx0 = tx + EDGE[dir][0][0], wy0 = ty + EDGE[dir][0][1];
                    double wx1 = tx + EDGE[dir][1][0], wy1 = ty + EDGE[dir][1][1];

                    LocalPoint lp0 = LocalPoint.fromWorld(client, (int) wx0, (int) wy0);
                    LocalPoint lp1 = LocalPoint.fromWorld(client, (int) wx1, (int) wy1);
                    if (lp0 == null || lp1 == null) continue;

                    Point sp0 = Perspective.localToCanvas(client, lp0, plane);
                    Point sp1 = Perspective.localToCanvas(client, lp1, plane);
                    if (sp0 != null && sp1 != null)
                    {
                        g.drawLine(sp0.getX(), sp0.getY(), sp1.getX(), sp1.getY());
                    }
                }
            }
        }

// highlight NPCs inside the walkable area with a model outline
        for (NPC npc : client.getNpcs())
        {
            WorldPoint nwp = npc.getWorldLocation();
            if (nwp.getPlane() != plane) continue;
            if (!walkableTiles.contains(new WorldPoint(nwp.getX(), nwp.getY(), plane)))
                continue;

            // only draw outline if we have a model and it's on screen
            if (npc.getModel() != null)
            {
                try
                {
                    // thickness = 2, inner color = cyan, outer color = red, padding = 4px
                    modelOutlineRenderer.drawOutline(npc, 2, Color.RED, 4);
                }
                catch (Exception ex)
                {
                    Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
                }
            }
        }


        return null;
    }

    private boolean isWalkable(int tx, int ty, int baseX, int baseY, int size, int plane)
    {
        if (tx < baseX || tx >= baseX + size || ty < baseY || ty >= baseY + size)
            return false;
        LocalPoint lp = LocalPoint.fromWorld(client, tx, ty);
        if (lp == null) return false;
        for (int[] d : DIRS)
        {
            int nx = tx + d[0], ny = ty + d[1];
            if (nx < baseX || nx >= baseX + size || ny < baseY || ny >= baseY + size)
                continue;
            if (LocalPoint.fromWorld(client, nx, ny) == null) continue;
            if (new net.runelite.api.coords.WorldArea(tx, ty, 1, 1, plane)
                    .canTravelInDirection(client.getTopLevelWorldView(), d[0], d[1]))
            {
                return true;
            }
        }
        return false;
    }

    private void rebuildWalkable(int baseX, int baseY, int plane, int size)
    {
        walkableTiles.clear();
        for (int x = baseX; x < baseX + size; x++)
        {
            for (int y = baseY; y < baseY + size; y++)
            {
                if (LocalPoint.fromWorld(client, x, y) == null) continue;
                if (isWalkable(x, y, baseX, baseY, size, plane))
                {
                    walkableTiles.add(new WorldPoint(x, y, plane));
                }
            }
        }
    }
}

package net.runelite.client.plugins.microbot.Hal.util;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.aiofighter.model.Monster;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import java.awt.*;
import java.util.List;

/**
 * Single‐entry helper for rendering:
 *   • the walkable‐area border (cached by ChunkRegionAnalyzer)
 *   • a manual safe‐spot
 *   • two lists of NPC outlines
 *   • a list of monsters with optional distance/countdown
 */
public class RegionOverlayHelper
{
    private static final Color AREA_COLOR      = new Color(0, 255, 255, 127);
    private static final Color SAFE_SPOT_COLOR = new Color(255,   0,   0, 127);
    private final ChunkRegionAnalyzer analyzer;
    private final ModelOutlineRenderer outlineRenderer;

    @Inject
    public RegionOverlayHelper(
            ChunkRegionAnalyzer analyzer,
            ModelOutlineRenderer outlineRenderer
    ) {
        this.analyzer        = analyzer;
        this.outlineRenderer = outlineRenderer;
    }

    /**
     * Renders everything in one call.
     *
     * @param g                        the Graphics2D
     * @param client                   RuneLite client
     * @param center                   center WorldPoint of chunk
     * @param safeSpot                 manual safe‐spot WorldPoint
     * @param safeSpotEnabled          whether to draw the safeSpot
     * @param filteredAttackableNpcs   first NPC list
     * @param autoFilteredNpcs         second NPC list
     * @param currentMonstersAttackingUs  monster list
     * @param drawNpcDistance          whether to draw “Xm” for monsters
     * @param drawNpcAttackCountDown   whether to draw the countdown for monsters
     */
    public void render(
            Graphics2D g,
            Client client,
            WorldPoint center,
            boolean safeSpotEnabled,
            WorldPoint safeSpot,
            List<Rs2NpcModel> filteredAttackableNpcs,
            List<Rs2NpcModel> autoFilteredNpcs,
            List<Monster> currentMonstersAttackingUs,
            boolean drawNpcDistance,
            boolean drawNpcAttackCountDown
    ) {
        // 1) border polygon
        Polygon border = analyzer.getWalkableAreaPolygon(client, center);
        if (border != null) {
            g.setColor(AREA_COLOR);
            g.setStroke(new BasicStroke(2f));
            OverlayUtil.renderPolygon(g, border, AREA_COLOR);
        }

        // 2) manual safe‐spot
        if (safeSpotEnabled && safeSpot != null) {
            LocalPoint lp = LocalPoint.fromWorld(client, safeSpot);
            if (lp != null) {
                Polygon p = Perspective.getCanvasTileAreaPoly(client, lp, 1);
                if (p != null) {
                    OverlayUtil.renderPolygon(g, p, SAFE_SPOT_COLOR);
                }
            }
        }

        // 3) outline filteredAttackableNpcs
        outlineList(g, client, filteredAttackableNpcs, false, null);

        // 4) outline autoFilteredNpcs
        outlineList(g, client, autoFilteredNpcs,     false, null);

        // 5) outline monsters + optional distance/countdown
        for (Monster m : currentMonstersAttackingUs) {
            outlineList(
                    g,
                    client,
                    List.of((Rs2NpcModel) m.npc),
                    drawNpcDistance,
                    drawNpcAttackCountDown ? m.lastAttack : null
            );
        }
    }

    private void outlineList(
            Graphics2D g,
            Client client,
            List<Rs2NpcModel> npcs,
            boolean drawDistance,
            Integer countdown
    ) {
        for (Rs2NpcModel npc : npcs) {
            if (npc == null || npc.getCanvasTilePoly() == null) continue;
            try {
                // draw a thick red‐inner, cyan‐outer outline
                outlineRenderer.drawOutline(npc, 2, Color.red, 4);
                g.draw(npc.getCanvasTilePoly());

                if (drawDistance) {
                    int dist = WorldPoint.fromLocal(client, npc.getLocalLocation())
                            .distanceTo(client.getLocalPlayer().getWorldLocation());
                    Point pt = npc.getCanvasTilePoly().getBounds().getLocation();
                    g.drawString(dist + "m", pt.x, pt.y - 5);
                }

                if (countdown != null) {
                    Rectangle b = npc.getCanvasTilePoly().getBounds();
                    g.drawString(
                            countdown.toString(),
                            b.x + b.width/2,
                            b.y + b.height/2
                    );
                }
            } catch (Exception ex) {
                Microbot.logStackTrace(getClass().getSimpleName(), ex);
            }
        }
    }
}

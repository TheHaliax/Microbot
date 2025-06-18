package net.runelite.client.plugins.microbot.runecrafting.gotr2;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Plugin;
import net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Script;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;



public class Gotr2Overlay extends OverlayPanel {

    private final net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Plugin plugin;
    public static Color PUBLIC_TIMER_COLOR = Color.YELLOW;
    public static int TIMER_OVERLAY_DIAMETER = 20;
    private final ProgressPieComponent progressPieComponent = new ProgressPieComponent();

    int sleepingCounter;

    @Inject
    Gotr2Overlay(Gotr2Plugin plugin)
    {
        super(plugin);
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }
    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 300));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("Micro Guardians of the rift V" + net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Script.version)
                    .color(Color.GREEN)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("STATE: " + net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Script.state)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Elemental points: " + net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Script.elementalRewardPoints)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Catalytic points: " + net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Script.catalyticRewardPoints)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Time since portal: " + net.runelite.client.plugins.microbot.runecrafting.gotr2.Gotr2Script.getTimeSincePortal())
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Total time script loop: " + Gotr2Script.totalTime + "ms")
                    .build());

        } catch(Exception ex) {
            Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
        }
        return super.render(graphics);
    }
}

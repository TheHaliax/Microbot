package net.runelite.client.plugins.microbot.Hal.kalphitequeen;

import com.google.inject.Provides;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpcs;

@PluginDescriptor(
        name = PluginDescriptor.Hal + " KalphiteQueen",
        description = "Kalphite Queen Safespot and Flinch",
        tags = {"KQ", "Hal", "Kalphite", "Queen", "Diary", "Desert"},
        enabledByDefault = false
)

public class KalphiteQueenPlugin extends Plugin {
    @Inject
    private KalphiteQueenConfig config;

    @Provides
    KalphiteQueenConfig provideConfig(ConfigManager configManager) { return configManager.getConfig(KalphiteQueenConfig.class); }

    @Inject
    KalphiteQueenScript kalphiteQueenScript;

    @Inject
    KalphiteQueenUtilScript kalphiteQueenUtilScript;

    @Inject
    KalphiteQueenLootScript kalphiteQueenLootScript;

    public KalphiteQueenConfig getConfig() { return config; }

    private Rs2NpcModel kalphiteQueen;

    public static class NpcInfo {
        private final Rs2NpcModel model;
        private final int         distance;
        private final WorldPoint  location;

        public NpcInfo(Rs2NpcModel model, int distance, WorldPoint location) {
            this.model    = model;
            this.distance = distance;
            this.location = location;
        }
        public Rs2NpcModel getModel()    { return model; }
        public int          getDistance() { return distance; }
        public WorldPoint   getLocation() { return location; }
    }


    @Override
    protected void startUp() throws AWTException {
        kalphiteQueenScript.run(config);
        kalphiteQueenUtilScript.run(config);
        kalphiteQueenLootScript.run(config);
        KalphiteQueenScript.workersSetup = false;
        KalphiteQueenScript.queenSetup = false;
        KalphiteQueenScript.queenInteracting = false;
        KalphiteQueenUtilScript.initCheck = false;
        KalphiteQueenScript.inventoryFood = -1;
    }

    @Override
    protected void shutDown() {
        kalphiteQueenScript.shutdown();
        kalphiteQueenUtilScript.shutdown();
        kalphiteQueenLootScript.shutdown();
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {

        String msg = chatMessage.getMessage();

        if (msg.contains("Seems to be burnt out.")) {
            KalphiteQueenScript.queenLairEmpty = true;
        }
        if (msg.contains("Someone has passed here recently.")) {
            KalphiteQueenScript.queenLairEmpty = false;
        }
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        var client = Microbot.getClient();
        var localPlayer = client.getLocalPlayer();
        LocalPoint playerLp = localPlayer.getLocalLocation();
        WorldPoint playerWp = playerLp == null
                ? null
                : WorldPoint.fromLocalInstance(client, playerLp);
        KalphiteQueenScript.currentPlayerLocation = playerWp;

        // 1. Kalphite Queen
        kalphiteQueen = getNpcs(n -> "Kalphite Queen".equals(n.getName()))
                .findFirst().orElse(null);
        if (kalphiteQueen != null && playerLp != null) {
            LocalPoint qLp = kalphiteQueen.getLocalLocation();
            NPCComposition comp = kalphiteQueen.getComposition();
            int size = comp != null ? comp.getSize() : 1;
            if (qLp != null) {
                // SW corner local offset:
                LocalPoint swLp = qLp.plus(
                        -((size - 1) * Perspective.LOCAL_TILE_SIZE / 2),
                        -((size - 1) * Perspective.LOCAL_TILE_SIZE / 2)
                );
                WorldPoint swWp = WorldPoint.fromLocalInstance(client, swLp);
                KalphiteQueenScript.queenLocation = swWp;
                if (playerWp != null) {
                    int dist = swWp.distanceTo(playerWp);
                    KalphiteQueenScript.queenDistance = dist;
                    Microbot.log("Queen → dist=%d, SW at=%s", dist, swWp);
                }
            }
            KalphiteQueenScript.queenInteracting = kalphiteQueen.isInteracting()
                    && Objects.equals(kalphiteQueen.getInteracting(), localPlayer);
        } else {
            KalphiteQueenScript.queenLocation = null;
            KalphiteQueenScript.queenDistance = -1;
            KalphiteQueenScript.queenInteracting = false;
        }

        // 2. Guardians
        List<Rs2NpcModel> found = getNpcs(n -> "Kalphite Guardian".equals(n.getName()))
                .limit(2).collect(Collectors.toList());
        KalphiteQueenScript.kalphiteGuardians.clear();
        KalphiteQueenScript.kalphiteGuardians.addAll(found);
        KalphiteQueenScript.guardianDistances.clear();
        KalphiteQueenScript.guardianLocations.clear();
        KalphiteQueenScript.guardian0Interacting = false;
        KalphiteQueenScript.guardian1Interacting = false;

        for (int i = 0; i < found.size(); i++) {
            Rs2NpcModel g = found.get(i);
            LocalPoint gLp = g.getLocalLocation();
            NPCComposition comp = g.getComposition();
            int size = comp != null ? comp.getSize() : 1;
            if (gLp != null) {
                LocalPoint swLp = gLp.plus(
                        -((size - 1) * Perspective.LOCAL_TILE_SIZE / 2),
                        -((size - 1) * Perspective.LOCAL_TILE_SIZE / 2)
                );
                WorldPoint swWp = WorldPoint.fromLocalInstance(client, swLp);
                KalphiteQueenScript.guardianLocations.add(swWp);
                if (playerWp != null) {
                    int dist = swWp.distanceTo(playerWp);
                    KalphiteQueenScript.guardianDistances.add(dist);
                    Microbot.log("Guardian → dist=%d, SW at=%s", dist, swWp);
                } else {
                    KalphiteQueenScript.guardianDistances.add(-1);
                }
            } else {
                KalphiteQueenScript.guardianLocations.add(null);
                KalphiteQueenScript.guardianDistances.add(-1);
            }
            boolean interacting = g.isInteracting()
                    && Objects.equals(g.getInteracting(), localPlayer);
            if (i == 0) KalphiteQueenScript.guardian0Interacting = interacting;
            if (i == 1) KalphiteQueenScript.guardian1Interacting = interacting;
        }
    }
}

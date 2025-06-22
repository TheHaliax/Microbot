package net.runelite.client.plugins.microbot.kalphitequeen;

import com.google.inject.Provides;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.NpcInfo;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Global;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpcs;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.getRs2WorldPoint;

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
        KalphiteQueenScript.workersSetup = false;
        KalphiteQueenScript.queenSetup = false;
    }

    @Override
    protected void shutDown() {
        kalphiteQueenScript.shutdown();
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {

        String msg = chatMessage.getMessage();

        if (msg.contains("Seems to be burnt out.")) {

        }

        if (msg.contains("Someone has passed here recently.")) {

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

        boolean hasCalculatedAttackSpeed = false;
        if (!hasCalculatedAttackSpeed)
        {
            // We rely on the static timestamps your Hitsplat listener already sets:
            //   KalphiteQueenPlugin.lastMyHitTime  = time of most recent hitsplat
            //   KalphiteQueenPlugin.prevMyHitTime  = time of the hit before that
            // (Assuming you have those; if not, add them exactly as longs updated in your listener.)

            long firstHitTime = -1;
            long lastHitTime = -1;
            if (firstHitTime < 0 && lastHitTime > 0)
            {
                // Capture the first hit timestamp
                firstHitTime = lastHitTime;
            }
            else if (firstHitTime > 0 && lastHitTime > firstHitTime)
            {
                // Second hit: compute interval
                KalphiteQueenScript.attackSpeed = (int)(lastHitTime - firstHitTime);
                Microbot.log("Attack speed: %d ms", KalphiteQueenScript.attackSpeed);
                hasCalculatedAttackSpeed = true;
            }
        }
    }


//    @Subscribe
//    public void onNpcDespawned(NpcDespawned npcDespawned) {
//        NPC npc = npcDespawned.getNpc();
//        if (npc.getId() == 963) {
//            KalphiteQueenScript.queenIsDead = true;
//        }
//    }
//
//    @Subscribe
//    public void onNpcSpawned(NpcSpawned npcSpawned) {
//        NPC npc = npcSpawned.getNpc();
//        if (npc.getId() == 963) {
//            KalphiteQueenScript.queenIsDead = false;
//        }
//    }
}

package net.runelite.client.plugins.microbot.kalphitequeen;

import com.google.inject.Provides;
import net.runelite.api.NPC;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.getNpcs;
import static net.runelite.client.plugins.microbot.util.player.Rs2Player.getRs2WorldPoint;

@PluginDescriptor(
        name = PluginDescriptor.Hal + "KalphiteQueen",
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
    public void onGameTick(GameTick tick)
    {
        KalphiteQueenScript.currentPlayerLocation = Rs2Player.getWorldLocation();

        var client = Microbot.getClient();
        var playerLp = client.getLocalPlayer().getLocalLocation();

        // 1. Track the single Kalphite Queen
        kalphiteQueen = getNpcs(n -> "Kalphite Queen".equals(n.getName()))
                .findFirst()
                .orElse(null);

        if (kalphiteQueen != null)
        {
            KalphiteQueenScript.queenDistance = kalphiteQueen
                    .getLocalLocation()
                    .distanceTo(playerLp);
            KalphiteQueenScript.queenLocation = kalphiteQueen.getWorldLocation();
            Microbot.log("Queen → dist=%d, at=%s", KalphiteQueenScript.queenDistance, KalphiteQueenScript.queenLocation);
        }
        else
        {
            KalphiteQueenScript.queenDistance = -1;
            KalphiteQueenScript.queenLocation = null;
        }

        // 2. Track up to two Kalphite Guardians
        List<Rs2NpcModel> found = getNpcs(n -> "Kalphite Guardian".equals(n.getName()))
                .limit(2)
                .collect(Collectors.toList());

        // refresh your lists
        KalphiteQueenScript.kalphiteGuardians.clear();
        KalphiteQueenScript.kalphiteGuardians.addAll(found);
        KalphiteQueenScript.guardianDistances.clear();
        KalphiteQueenScript.guardianLocations.clear();

        for (Rs2NpcModel g : KalphiteQueenScript.kalphiteGuardians)
        {
            int d = g.getLocalLocation().distanceTo(playerLp);
            KalphiteQueenScript.guardianDistances.add(d);
            KalphiteQueenScript.guardianLocations.add(g.getWorldLocation());
            Microbot.log("Guardian → dist=%d, at=%s", d, g.getWorldLocation());
        }
    }

    public static Stream<NpcInfo> getNpcInfos(Predicate<Rs2NpcModel> predicate) {
        var client   = Microbot.getClient();
        var playerLp = client.getLocalPlayer().getLocalLocation();
        return getNpcs(predicate)
                .map(n -> new NpcInfo(
                        n,
                        n.getLocalLocation().distanceTo(playerLp),
                        n.getWorldLocation()
                ));
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

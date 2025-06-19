package net.runelite.client.plugins.microbot.kalphitequeen;

import com.google.inject.Provides;
import net.runelite.api.NPC;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.runecrafting.gotr.GotrScript;

import javax.inject.Inject;
import java.awt.*;

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


    @Override
    protected void startUp() throws AWTException {
        kalphiteQueenScript.run(config);
    }

    @Override
    protected void shutDown() throws Exception {
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

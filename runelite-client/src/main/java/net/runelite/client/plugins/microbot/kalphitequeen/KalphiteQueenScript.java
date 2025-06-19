package net.runelite.client.plugins.microbot.kalphitequeen;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

public class KalphiteQueenScript extends Script {

    public static String version = "0.0.1";
    static KalphiteQueenConfig config;
    public static boolean queenIsDead;

    private static final WorldPoint WORKER_2                 = new WorldPoint(3508, 9493, 0);
    private static final WorldPoint UNSTACK_WORKER_1         = new WorldPoint(3508, 9492, 0);
    private static final WorldPoint KQ_2                     = new WorldPoint(3505, 9495, 0);
    private static final WorldPoint UNSTACK_WORKER_2         = new WorldPoint(3504, 9496, 0);
    private static final WorldPoint UNSTACK_KQ               = new WorldPoint(3503, 9493, 0);
    private static final WorldPoint WORKER_1                 = new WorldPoint(3499, 9494, 0);
    private static final WorldPoint KQ_1                     = new WorldPoint(3491, 9495, 0);
    private static final WorldPoint KQ_3                     = new WorldPoint(3491, 9491, 0);
    private static final WorldPoint KQ_SAFE_SPOT_1           = new WorldPoint(3490, 9496, 0);
    private static final WorldPoint KQ_SAFE_SPOT_2           = new WorldPoint(3489, 9495, 0);
    private static final WorldPoint KQ_SAFE_SPOT_3           = new WorldPoint(3491, 9497, 0);
    private static final WorldPoint WORKER_SETUP_1           = new WorldPoint(3506, 9492, 0);
    private static final WorldPoint WORKER_SETUP_2           = new WorldPoint(3502, 9491, 0);
    private static final WorldPoint KALPHITE_SETUP_WORKER_1  = new WorldPoint(3499, 9495, 0);
    private static final WorldPoint KALPHITE_SETUP_WORKER_2  = new WorldPoint(3495, 9495, 0);
    private static final WorldPoint KALPHITE_SETUP_QUEEN     = new WorldPoint(3490, 9495, 0);

    boolean initCheck = false;


    public boolean run(KalphiteQueenConfig config) {
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (!initCheck) {
                    if (!Rs2Player.getWorldLocation(WORKER_2))
                    initCheck = true;
                }
            }
        }

        @Override
        public void shutdown() {
            super.shutdown();
        }
    }


}

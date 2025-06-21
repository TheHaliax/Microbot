package net.runelite.client.plugins.microbot.kalphitequeen;

import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.coords.Rs2WorldPoint;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static net.runelite.client.plugins.microbot.runecrafting.gotr.GotrScript.totalTime;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.PROTECT_MELEE;

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
    private static final int    NPC_KG = 962, NPC_KQ = 963;

    boolean initCheck = false;
    boolean workersSetup = false;
    boolean queenSetup = false;
    boolean queenFlinched = false;

    public static WorldPoint currentPlayerLocation;
    public static int          queenDistance;
    public static WorldPoint  queenLocation;
    public static List<Integer>     guardianDistances = new ArrayList<>(2);
    public static List<WorldPoint>  guardianLocations = new ArrayList<>(2);
    public static final List<Rs2NpcModel> kalphiteGuardians = new ArrayList<>(2);


    public boolean run(KalphiteQueenConfig config) {
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                if (!initCheck) {
                    if (!playerOn(WORKER_2)) {
                        shutdown();
                    }
                    initCheck = true;
                }

                if (setupWorkers()) return;

                if (setupQueen()) return;

                if (flinchQueen()) return;

                long endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                Microbot.log("Something went wrong in the KQ Script: " + ex.getMessage() + ". If the script is stuck, please contact us on discord with this log.");
                ex.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean setupWorkers() {
        if (workersSetup) {
            return true;
        }

        return true;
    }

    private boolean setupQueen() {
        if (queenSetup) {
            return true;
        }

        return true;
    }

    private boolean flinchQueen() {
        if (queenFlinched) {
            return true;
        }

        return true;
    }

    private boolean playerOn(WorldPoint target) {
        return currentPlayerLocation != null
                && currentPlayerLocation.equals(target);
    }

    private boolean anyGuardianOn(WorldPoint target) {
        return guardianLocations.stream()
                .anyMatch(loc -> loc.equals(target));
    }

    private boolean guardianOn(int index, WorldPoint target) {
        var locs = guardianLocations;
        return index >= 0
                && index < locs.size()
                && locs.get(index).equals(target);
    }

    private boolean queenOn(WorldPoint target) {
        return queenLocation != null
                && queenLocation.equals(target);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

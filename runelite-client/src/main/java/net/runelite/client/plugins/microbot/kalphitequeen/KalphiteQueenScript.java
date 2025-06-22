package net.runelite.client.plugins.microbot.kalphitequeen;

import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.runecrafting.gotr.GotrScript.totalTime;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.interact;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.*;

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
    private static final WorldPoint WORKER_SETUP_1           = new WorldPoint(3503, 9492, 0);
    private static final WorldPoint WORKER_SETUP_2           = new WorldPoint(3499, 9493, 0);
    private static final WorldPoint KALPHITE_SETUP_WORKER_1  = new WorldPoint(3499, 9495, 0);
    private static final WorldPoint KALPHITE_SETUP_WORKER_2  = new WorldPoint(3495, 9495, 0);
    private static final WorldPoint KALPHITE_SETUP_QUEEN     = new WorldPoint(3490, 9495, 0);
    private static final WorldPoint SAFE_OUTSIDE_LAIR        = new WorldPoint(3506, 9490, 2);
    private static final WorldPoint QUEEN_FAR_ENOUGH         = new WorldPoint(3467, 9488, 0);
    private static final WorldPoint QUEEN_SETUP_PLAYER_SAFE  = new WorldPoint(3491, 9499, 0);
    private static final WorldPoint QUEEN_STUCK_1            = new WorldPoint(3480, 9495, 0);
    private static final WorldPoint QUEEN_STUCK_2            = new WorldPoint(3481, 9494, 0);
    private static final WorldArea  QUEEN_AREA_SAFE          = new WorldArea(QUEEN_FAR_ENOUGH, 13, 11);
    private static final WorldArea OUTSIDE_LAIR              = new WorldArea(SAFE_OUTSIDE_LAIR, 10, 10);
    private static final int    NPC_KG = 962, NPC_KQ = 963;

    boolean initCheck = false;
    static boolean workersSetup = false;
    static boolean queenSetup = false;
    boolean queenFlinched = false;
    static int attackSpeed = -1;
    static boolean guardianAttack;

    public static WorldPoint currentPlayerLocation;
    public static int          queenDistance;
    public static WorldPoint  queenLocation;
    public static List<Integer>     guardianDistances = new ArrayList<>(2);
    public static List<WorldPoint>  guardianLocations = new ArrayList<>(2);
    public static final List<Rs2NpcModel> kalphiteGuardians = new ArrayList<>(2);
    public static boolean guardian0Interacting = false;
    public static boolean guardian1Interacting = false;
    public static boolean queenInteracting     = false;


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
                var kalphiteNpcs = Rs2Npc.getNpcs("Kalphite", false);

                for (Rs2NpcModel kalphiteNpc : kalphiteNpcs.collect(Collectors.toList())) {
                    if (kalphiteNpc == null) continue;
                    int npcAnimation = Rs2Reflection.getAnimation(kalphiteNpc);
                    handleQueenPrayer(npcAnimation);
                }

                Map<Integer, List<String>> animMap = new HashMap<>();

                Rs2Npc.getNpcs(n -> true).forEach(npcModel -> {
                    int anim = npcModel.getAnimation();
                    String desc = npcModel.getName() + "(id=" + npcModel.getId() + ")";
                    animMap.computeIfAbsent(anim, k -> new ArrayList<>()).add(desc);
                });

// Log each animation ID and which NPCs are on it
                animMap.forEach((animId, names) -> {
                    Microbot.log("Animation %d used by: %s", animId, names);
                });

                if (!workersSetup && queenInteracting) {
                    if (fleeQueenWorkerSetup()) return;
                }

                if (!workersSetup) {
                    if (setupWorkers()) return;
                }

                if (!queenSetup) {
                    if (setupQueen()) return;
                }

                if (flinchQueen()) return;

                long endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                Microbot.log("Something went wrong in the KQ Script: " + ex.getMessage() + ". If the script is stuck, please contact us on discord with this log.");
                ex.printStackTrace();
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
        return true;
    }

    private boolean fleeQueenWorkerSetup() {
        Microbot.log("Queen attacked during worker setup!");
        toggle(PROTECT_MAGIC);
        Rs2GameObject.interact("Rope", "Climb-up");
        sleepUntil(isOutsideLair());
        toggle(PROTECT_MELEE);
        Rs2Walker.walkFastCanvas(SAFE_OUTSIDE_LAIR);
        toggle(PROTECT_MELEE);
        return true;
    }

    private boolean setupWorkers() {
        if (queenLocation != null && QUEEN_AREA_SAFE.contains(queenLocation)) {
            if (playerOn(WORKER_2)) {
                sleep(1200, 2400);
                Microbot.log("Player is on Worker 2");
                if ((!guardian0Interacting) || (!guardian1Interacting)) {
                    Microbot.log("Grabbing Worker Aggro!");
                    toggle(PROTECT_MELEE);
                    Rs2Walker.walkFastCanvas(WORKER_1);
                    Rs2Player.waitForWalking();
                    interactAll(kalphiteGuardians, "Attack", 1200);
                    Rs2Player.waitForXpDrop(Skill.HITPOINTS);
                    Rs2Walker.walkFastCanvas(WORKER_2);
                    Rs2Player.waitForWalking(1200);
                    toggle(PROTECT_MELEE);
                    sleep(4800);
                    return false;
                }
            }

            if ((guardian0Interacting) && (guardian1Interacting)) {
                if (guardianLocations.contains(WORKER_SETUP_1)
                        && guardianLocations.contains(WORKER_SETUP_2)) {
                    workersSetup = true;
                    Microbot.log("Workers are Setup");
                    return true;
                } else {
                    Microbot.log("Trying to Unstack Workers 1");
                    Rs2Walker.walkFastCanvas(UNSTACK_WORKER_1);
                    sleep(4800);
                    Rs2Walker.walkFastCanvas(WORKER_2);
                    sleep(4800);
                    if ((guardian0Interacting) || (guardian1Interacting)) {
                        if (guardianLocations.contains(WORKER_SETUP_1)
                                && guardianLocations.contains(WORKER_SETUP_2)) {
                            workersSetup = true;
                            Microbot.log("Workers are Setup");
                            return true;
                        } else {
                            Microbot.log("Trying to Unstack Workers 2");
                            toggle(PROTECT_MELEE);
                            Rs2Walker.walkFastCanvas(UNSTACK_WORKER_2);
                            sleep(4800);
                            Rs2Walker.walkFastCanvas(WORKER_2);
                            sleep(4800);
                            toggle(PROTECT_MELEE);
                            if ((guardian0Interacting) || (guardian1Interacting)) {
                                if (guardianLocations.contains(WORKER_SETUP_1)
                                        && guardianLocations.contains(WORKER_SETUP_2)) {
                                    workersSetup = true;
                                    Microbot.log("Workers are Setup");
                                    return true;
                                }
                            }
                        }
                    }
                }

            }
        }
        Microbot.log("Queen Outside Area");
        return false;
    }

    private boolean setupQueen() {
        if (queenSetup) {
            return true;
        }

        if (!workersSetup) return false;

        if (queenLocation != null && QUEEN_AREA_SAFE.contains(queenLocation)) {
            if (playerOn(WORKER_2)) {
                Microbot.log("Player is on Worker 2, grabbing queen");
                toggle(PROTECT_MELEE);
                Rs2Walker.walkFastCanvas(KQ_1);
                toggle(PROTECT_MAGIC);
                Rs2Player.waitForWalking(4800);
                Rs2Npc.interact(NPC_KQ, "Attack");
                sleep(1200);
            }
        }

        if (queenInteracting) {
            Rs2Walker.walkFastCanvas(KQ_2);
            Rs2Player.waitForWalking();
            if (queenLocation.equals(QUEEN_STUCK_1)) {
                Rs2Walker.walkFastCanvas(UNSTACK_KQ);
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_2);
            }
            if (queenLocation.equals(QUEEN_STUCK_2)) {
                Rs2Walker.walkFastCanvas(UNSTACK_KQ);
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_2);
            }
            if (guardianLocations.contains(KALPHITE_SETUP_WORKER_1)
                    && guardianLocations.contains(KALPHITE_SETUP_WORKER_2)
                    && queenLocation.equals(KALPHITE_SETUP_QUEEN)) {
                Rs2Walker.walkFastCanvas(KQ_3);
                Rs2Player.waitForWalking();
                sleepUntilTrue(guardianAttacking());
                Rs2Walker.walkFastCanvas(QUEEN_SETUP_PLAYER_SAFE);
                Rs2Player.waitForWalking();
                toggle(PROTECT_MAGIC);
                queenSetup = true;
                return true;
            }
        }

        return false;
    }

    private boolean flinchQueen() {
        if (queenFlinched) {
            return true;
        }

        return false;
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

    public static boolean interactAll(List<Rs2NpcModel> npcs, String action, int delayMs) {
        if (npcs == null || npcs.isEmpty()) {
            return false;
        }
        // snapshot to avoid concurrent modification
        List<Rs2NpcModel> targets = new ArrayList<>(npcs);

        boolean allSucceeded = true;
        for (Rs2NpcModel npc : targets) {
            boolean ok = interact(npc, action);
            if (!ok) {
                allSucceeded = false;
                Microbot.log("Failed to %s %s", action, npc.getName());
            }
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return allSucceeded;
    }

    public static BooleanSupplier isOutsideLair() {
        return () -> OUTSIDE_LAIR.contains(currentPlayerLocation);
    }

    private void handleQueenPrayer(int animationId) {
        if (animationId == 6241) {
            guardianAttack = true;
        } else if (animationId != 6241){
            guardianAttack = false;
        }
        if (animationId == 1172) {
            toggle(PROTECT_MAGIC, true);
        } else if (animationId == 6240) {
            toggle(PROTECT_RANGE, true);
        }
    }

    public static BooleanSupplier guardianAttacking() {
        return () -> guardianAttack;
    }
}

package net.runelite.client.plugins.microbot.kalphitequeen;

import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.ObjectComposition;
import net.runelite.api.ObjectID;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2ItemModel;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.runecrafting.gotr.GotrScript.totalTime;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.interact;
import static net.runelite.client.plugins.microbot.util.npc.Rs2Npc.isInAttackRange;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.getBestMeleePrayer;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.*;


public class KalphiteQueenScript extends Script {

    public static String version = "0.0.1";
    public static boolean queenLairEmpty;
    static KalphiteQueenConfig config;


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
    private static final WorldPoint KQ_FLINCH_SPOT_1         = new WorldPoint(3490, 9498, 0);
    private static final WorldPoint KQ_FLINCH_SPOT_2         = new WorldPoint(3489, 9497, 0);
    private static final WorldPoint KQ_FLINCH_SPOT_3         = new WorldPoint(3491, 9499, 0);
    private static final WorldPoint WORKER_SETUP_1           = new WorldPoint(3503, 9492, 0);
    private static final WorldPoint WORKER_SETUP_2           = new WorldPoint(3499, 9493, 0);
    private static final WorldPoint KALPHITE_SETUP_WORKER_1  = new WorldPoint(3499, 9495, 0);
    private static final WorldPoint KALPHITE_SETUP_WORKER_2  = new WorldPoint(3495, 9495, 0);
    private static final WorldPoint KALPHITE_SETUP_QUEEN     = new WorldPoint(3490, 9495, 0);
    private static final WorldPoint SAFE_OUTSIDE_LAIR        = new WorldPoint(3506, 9490, 2);
    private static final WorldPoint QUEEN_FAR_ENOUGH         = new WorldPoint(3467, 9486, 0);
    private static final WorldPoint QUEEN_SETUP_PLAYER_SAFE  = new WorldPoint(3491, 9499, 0);
    private static final WorldPoint QUEEN_STUCK_1            = new WorldPoint(3480, 9495, 0);
    private static final WorldPoint QUEEN_STUCK_2            = new WorldPoint(3481, 9494, 0);
    private static final WorldPoint EDGEFAIRY                = new WorldPoint(3129, 3496, 0);
    private static final WorldPoint BIQ                      = new WorldPoint(3251, 3095,0);
    private static final WorldPoint EDGETELEPORT             = new WorldPoint(3087, 3496, 0);
    private static final WorldPoint EDGEBANK                 = new WorldPoint(3091, 3488, 0);
    private static final WorldPoint OUTSIDELAIR              = new WorldPoint(3226, 3107, 0);
    private static final WorldPoint INSIDELAIR               = new WorldPoint(3483, 9510, 2);
    private static final WorldArea  OUTSIDELAIRAREA          = new WorldArea(OUTSIDELAIR, 4,4);
    private static final WorldArea  EDGEBANKAREA             = new WorldArea(EDGEBANK, 8, 12);
    private static final WorldArea  QUEEN_AREA_SAFE          = new WorldArea(QUEEN_FAR_ENOUGH, 7, 17);
    private static final WorldArea  OUTSIDE_QUEENLAIR        = new WorldArea(SAFE_OUTSIDE_LAIR, 10, 10);
    private static final int    NPC_KG = 962, NPC_KQ = 963, NPC_KQ2 = 965;


    static boolean workersSetup = false;
    static boolean queenSetup = false;
    static int attackSpeed = -1;
    static boolean guardianAttack;
    public static int inventoryFood = -1;
    static boolean initCheck = false;


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
                    getFood();
                    initCheck = true;
                }

                List<Rs2NpcModel> secondQueen = Rs2Npc.getNpcs(npcModel -> npcModel != null && npcModel.getId() == NPC_KQ2)
                        .collect(Collectors.toList());
                boolean phaseTwo = !secondQueen.isEmpty();

                List<Rs2NpcModel> firstQueen = Rs2Npc.getNpcs(npcModel -> npcModel != null && npcModel.getId() == NPC_KQ)
                        .collect(Collectors.toList());
                boolean phaseOne = !firstQueen.isEmpty();

                var kalphiteNpcs = Rs2Npc.getNpcs("Kalphite", false);

                for (Rs2NpcModel kalphiteNpc : kalphiteNpcs.collect(Collectors.toList())) {
                    if (kalphiteNpc == null) continue;
                    int npcAnimation = Rs2Reflection.getAnimation(kalphiteNpc);
                    handleQueenPrayer(npcAnimation);
                }

                if (Rs2Player.eatAt(inventoryFood, true)) {
                    Rs2Inventory.waitForInventoryChanges(1200);
                }

                if (Rs2Player.drinkAntiPoisonPotion()) {
                    Rs2Player.waitForAnimation();
                }

                if (Rs2Player.drinkPrayerPotion()) {
                    Rs2Player.waitForAnimation();
                }

                if (Rs2Player.getWorldLocation().getPlane() == 0) {
                    if (Rs2Player.drinkCombatPotionAt(Skill.STRENGTH)) {
                        Rs2Player.waitForAnimation();
                    }
                    if (Rs2Player.drinkCombatPotionAt(Skill.ATTACK)) {
                        Rs2Player.waitForAnimation();
                    }
                    if (Rs2Player.drinkCombatPotionAt(Skill.DEFENCE)) {
                        Rs2Player.waitForAnimation();
                    }
                }

                if(Rs2Inventory.hasItem(ItemID.VIAL_EMPTY)) {
                    Rs2Inventory.dropAll(ItemID.VIAL_EMPTY);
                    Rs2Inventory.waitForInventoryChanges(1000);
                }

                if (!phaseOne && !phaseTwo) {
                    if (!playerOn(WORKER_2)) {
                        Rs2Walker.walkFastCanvas(WORKER_2);
                        Rs2Player.waitForWalking();
                        fleeQueenWorkerSetup();
                        Rs2Player.waitForWalking();
                        resetPlugin();
                    }
                    return;
                }

                if (!workersSetup && queenInteracting) {
                    if (fleeQueenWorkerSetup()) return;
                }

                if (!workersSetup) {
                    if (setupWorkers()) return;
                }

                if (!queenSetup) {
                    if (setupQueen()) return;
                }

                if (!phaseTwo) {
                    if (flinchQueen1()) return;
                }

                if (!phaseOne) {
                    if (flinchQueen2()) return;
                }

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

    private static void resetPlugin() {
        workersSetup = false;
        queenSetup = false;
        queenInteracting = false;
        initCheck = false;
        inventoryFood = -1;
    }

    private boolean fleeQueenWorkerSetup() {
        Microbot.log("Queen attacked during worker setup!");
        toggle(PROTECT_MAGIC);
        Rs2GameObject.interact("Rope", "Climb-up");
        sleepUntil(isOutsideQueenLair());
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
                if (anyGuardianOn(WORKER_SETUP_1)
                        && anyGuardianOn(WORKER_SETUP_2)) {
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
                        if (anyGuardianOn(WORKER_SETUP_1)
                                && anyGuardianOn(WORKER_SETUP_2)) {
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
                                if (anyGuardianOn(WORKER_SETUP_1)
                                        && anyGuardianOn(WORKER_SETUP_2)) {
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
            if (queenOn(QUEEN_STUCK_1)) {
                Rs2Walker.walkFastCanvas(UNSTACK_KQ);
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_2);
            }
            if (queenOn(QUEEN_STUCK_2)) {
                Rs2Walker.walkFastCanvas(UNSTACK_KQ);
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_2);
            }
            if (anyGuardianOn(KALPHITE_SETUP_WORKER_1)
                    && anyGuardianOn(KALPHITE_SETUP_WORKER_2)
                    && queenOn(KALPHITE_SETUP_QUEEN)) {
                Rs2Walker.walkFastCanvas(KQ_3);
                Rs2Player.waitForWalking();
                sleepUntilTrue(isAnyKalphiteInRange);
                Rs2Walker.walkFastCanvas(QUEEN_SETUP_PLAYER_SAFE);
                Rs2Player.waitForWalking();
                toggle(PROTECT_MAGIC);
                queenSetup = true;
                return true;
            }
        }

        return false;
    }

    private boolean flinchQueen1() {

        if (queenOn(KQ_SAFE_SPOT_1)) {
            Microbot.log("Queen on Safe 1");
            if (!playerOn(KQ_FLINCH_SPOT_1)) {
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_1);
                Rs2Player.waitForWalking();
            }
            if (playerOn(KQ_FLINCH_SPOT_1)) {
                toggle(PROTECT_MELEE);
                toggle(getBestMeleePrayer());
                Rs2Npc.interact(NPC_KQ, "Attack");
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_1);
                Rs2Player.waitForWalking();
                toggle(PROTECT_MELEE);
                toggle(getBestMeleePrayer());
                sleep(4800);
            }
        }

        if (queenOn(KQ_SAFE_SPOT_2)) {
            Microbot.log("Queen on Safe 2");
            if (!playerOn(KQ_FLINCH_SPOT_2)) {
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_2);
                Rs2Player.waitForWalking();
            }
            if (playerOn(KQ_FLINCH_SPOT_2)) {
                toggle(PROTECT_MELEE);
                toggle(getBestMeleePrayer());
                Rs2Npc.interact(NPC_KQ, "Attack");
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_2);
                Rs2Player.waitForWalking();
                toggle(PROTECT_MELEE);
                toggle(getBestMeleePrayer());
                sleep(4800);
            }
        }

        if (queenOn(KQ_SAFE_SPOT_3)) {
            Microbot.log("Queen on Safe 3");
            if (!playerOn(KQ_FLINCH_SPOT_3)) {
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_3);
                Rs2Player.waitForWalking();
            }
            if (playerOn(KQ_FLINCH_SPOT_3)) {
                toggle(PROTECT_MELEE);
                toggle(getBestMeleePrayer());
                Rs2Npc.interact(NPC_KQ, "Attack");
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_3);
                Rs2Player.waitForWalking();
                toggle(PROTECT_MELEE);
                toggle(getBestMeleePrayer());
                sleep(4800);
            }
        }

        return false;
    }

    private boolean flinchQueen2() {

        if (Rs2Combat.getSpecEnergy() >= 250 && Rs2Inventory.hasItem(ItemID.DRAGON_MACE)) {
            if (queenOn(KQ_SAFE_SPOT_1)) {
                Microbot.log("Queen on Safe 1");
                if (!playerOn(KQ_FLINCH_SPOT_1)) {
                    Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_1);
                    Rs2Player.waitForWalking();
                }
                if (playerOn(KQ_FLINCH_SPOT_1)) {
                    Rs2ItemModel currentWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
                    int savedWeaponId = -1;
                    savedWeaponId = currentWeapon.getId();
                    Rs2Inventory.equip(ItemID.DRAGON_MACE);
                    Rs2Inventory.waitForInventoryChanges(1200);
                    Microbot.getMouse().click(Rs2Widget.getWidget(10485795).getBounds());
                    sleep(1200);
                    toggle(getBestMeleePrayer());
                    Rs2Npc.interact(NPC_KQ2, "Attack");
                    sleep(600);
                    Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_1);
                    Rs2Player.waitForWalking();
                    toggle(getBestMeleePrayer());
                    Rs2Inventory.equip(savedWeaponId);
                    sleep(4800);
                }
            }

            if (queenOn(KQ_SAFE_SPOT_2)) {
                Microbot.log("Queen on Safe 2");
                if (!playerOn(KQ_FLINCH_SPOT_2)) {
                    Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_2);
                    Rs2Player.waitForWalking();
                }
                if (playerOn(KQ_FLINCH_SPOT_2)) {
                    Rs2ItemModel currentWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
                    int savedWeaponId = -1;
                    savedWeaponId = currentWeapon.getId();
                    Rs2Inventory.equip(ItemID.DRAGON_MACE);
                    Rs2Inventory.waitForInventoryChanges(1200);
                    Microbot.getMouse().click(Rs2Widget.getWidget(10485795).getBounds());
                    sleep(1200);
                    toggle(getBestMeleePrayer());
                    Rs2Npc.interact(NPC_KQ2, "Attack");
                    sleep(600);
                    Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_2);
                    Rs2Player.waitForWalking();
                    toggle(getBestMeleePrayer());
                    Rs2Inventory.equip(savedWeaponId);
                    sleep(4800);
                }
            }

            if (queenOn(KQ_SAFE_SPOT_3)) {
                Microbot.log("Queen on Safe 3");
                if (!playerOn(KQ_FLINCH_SPOT_3)) {
                    Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_3);
                    Rs2Player.waitForWalking();
                }
                if (playerOn(KQ_FLINCH_SPOT_3)) {
                    Rs2ItemModel currentWeapon = Rs2Equipment.get(EquipmentInventorySlot.WEAPON);
                    int savedWeaponId = -1;
                    savedWeaponId = currentWeapon.getId();
                    Rs2Inventory.equip(ItemID.DRAGON_MACE);
                    Rs2Inventory.waitForInventoryChanges(1200);
                    Microbot.getMouse().click(Rs2Widget.getWidget(10485795).getBounds());
                    sleep(1200);
                    toggle(getBestMeleePrayer());
                    Rs2Npc.interact(NPC_KQ2, "Attack");
                    sleep(600);
                    Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_3);
                    Rs2Player.waitForWalking();
                    toggle(getBestMeleePrayer());
                    Rs2Inventory.equip(savedWeaponId);
                    sleep(4800);
                }
            }
        }

        if (queenOn(KQ_SAFE_SPOT_1)) {
            Microbot.log("Queen on Safe 1");
            if (!playerOn(KQ_FLINCH_SPOT_1)) {
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_1);
                Rs2Player.waitForWalking();
            }
            if (playerOn(KQ_FLINCH_SPOT_1)) {
                toggle(getBestMeleePrayer());
                Rs2Npc.interact(NPC_KQ2, "Attack");
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_1);
                Rs2Player.waitForWalking();
                toggle(getBestMeleePrayer());
                sleep(4800);
            }
        }

        if (queenOn(KQ_SAFE_SPOT_2)) {
            Microbot.log("Queen on Safe 2");
            if (!playerOn(KQ_FLINCH_SPOT_2)) {
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_2);
                Rs2Player.waitForWalking();
            }
            if (playerOn(KQ_FLINCH_SPOT_2)) {
                toggle(getBestMeleePrayer());
                Rs2Npc.interact(NPC_KQ2, "Attack");
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_2);
                Rs2Player.waitForWalking();
                toggle(getBestMeleePrayer());
                sleep(4800);
            }
        }

        if (queenOn(KQ_SAFE_SPOT_3)) {
            Microbot.log("Queen on Safe 3");
            if (!playerOn(KQ_FLINCH_SPOT_3)) {
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_3);
                Rs2Player.waitForWalking();
            }
            if (playerOn(KQ_FLINCH_SPOT_3)) {
                toggle(getBestMeleePrayer());
                Rs2Npc.interact(NPC_KQ2, "Attack");
                sleep(600);
                Rs2Walker.walkFastCanvas(KQ_FLINCH_SPOT_3);
                Rs2Player.waitForWalking();
                toggle(getBestMeleePrayer());
                sleep(4800);
            }
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

    public static BooleanSupplier isOutsideQueenLair() {
        return () -> OUTSIDE_QUEENLAIR.contains(currentPlayerLocation);
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

    public static boolean anyInAttackRange() {
        // Ignore the passed-in stream (which may have been consumed). Always use a fresh stream:
        return Rs2Npc.getNpcs(npcModel -> {
            String name = npcModel.getName();
            return name != null && name.contains("Kalphite");
        }).anyMatch(npc -> {
            if (npc == null) return false;
            try {
                return isInAttackRange(npc);
            } catch (Throwable t) {
                Microbot.log("Range check error for NPC id=%d: %s", npc.getId(), t);
                return false;
            }
        });
    }


    public static final BooleanSupplier isAnyKalphiteInRange = () ->
            Rs2Npc.getNpcs(npc -> {
                String name = npc.getName();
                return name != null && name.contains("Kalphite");
            }).anyMatch(npc -> {
                if (npc == null) return false;
                try {
                    return isInAttackRange(npc);
                } catch (Throwable t) {
                    Microbot.log("Range check error for NPC id=%d: %s", npc.getId(), t);
                    return false;
                }
            });
    public static final BooleanSupplier notAnyKalphiteInRange = () ->
            !isAnyKalphiteInRange.getAsBoolean();


    public static boolean toggleAutoRetaliate() {
        int initValue = Microbot.getVarbitPlayerValue(172);
        if (Microbot.getVarbitPlayerValue(172) == 1) {
            Rs2Tab.switchToCombatOptionsTab();
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.COMBAT, 2000);
            Rs2Widget.clickWidget(38862879);
            Microbot.getVarbitPlayerValue(172);
            int finalValue = Microbot.getVarbitPlayerValue(172);
            return  initValue != finalValue;
        }
        if (Microbot.getVarbitPlayerValue(172) == 0) {
            Rs2Tab.switchToCombatOptionsTab();
            sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.COMBAT, 2000);
            Rs2Widget.clickWidget(38862879);
            Microbot.getVarbitPlayerValue(172);
            int finalValue = Microbot.getVarbitPlayerValue(172);
            return  initValue != finalValue;
        }
        return false;
    }

    void getFood() {
        Map<Integer, Rs2Food> localFoodMap = Arrays.stream(Rs2Food.values())
                .collect(Collectors.toMap(Rs2Food::getId, f -> f));
        Rs2Inventory.getInventoryFood()
                .forEach(itemModel -> {
                    if (itemModel == null) return;
                    Rs2Food food = localFoodMap.get(itemModel.getId());
                    if (food == null) return; // not in our enum list
                    int healAmount = food.getHeal();
                    inventoryFood = 95 - healAmount;
               });
    }

    boolean enterQueenLair(){
        if (playerOn(SAFE_OUTSIDE_LAIR)) {
            Rs2GameObject.interact("Ancient brazier", "Examine");
            if (!queenLairEmpty) {
                Microbot.hopToWorld(Login.getRandomWorld(Rs2Player.isMember()));
                return false;
            }
            if (queenLairEmpty) {
                Rs2Inventory.interact("Rope", "Use");
                Rs2GameObject.interact(23609);
                Rs2Player.waitForAnimation(6000);
                return playerOn(WORKER_2);
            }
        }
        return false;
    }

    boolean enterKalphiteLair() {
        if (playerOn(BIQ)) {
            Rs2Walker.walkFastCanvas(OUTSIDELAIR);
            Rs2Player.waitForWalking();
            Rs2Inventory.interact("Rope", "use");
            Rs2GameObject.interact(3827);
            Rs2Player.waitForAnimation();
            return playerOn(INSIDELAIR);
        }
        if (OUTSIDELAIRAREA.contains(currentPlayerLocation)) {
            Rs2Inventory.interact("Rope", "use");
            Rs2GameObject.interact(3827);
            Rs2Player.waitForAnimation();
            return playerOn(INSIDELAIR);
        }
        return false;
    }

    boolean useEdgeVilleFairy() {
        if (EDGEBANKAREA.contains(currentPlayerLocation)) {
            Rs2Walker.walkFastCanvas(EDGEFAIRY);
            Rs2Player.waitForWalking();
            Rs2Walker.walkTo(BIQ);
            Rs2Player.waitForWalking();
            return playerOn(BIQ);
        }
        if (Rs2Walker.isNear(EDGEFAIRY)) {
            Rs2Walker.walkTo(BIQ);
            Rs2Player.waitForWalking();
            return playerOn(BIQ);
        }
        return false;
    }

    boolean useHouseTablet() {
        Rs2Inventory.use(ItemID.POH_TABLET_TELEPORTTOHOUSE);
        Rs2Inventory.waitForInventoryChanges(1200);
        return sleepUntil(() ->  Microbot.getClient().getTopLevelWorldView().getScene().isInstance() && Rs2GameObject.getGameObject("Portal") != null);
    }

    boolean handleHouse() {
        if (Rs2GameObject.getGameObject("Portal") != null) return false;
        if (Rs2GameObject.getGameObject("pool") != null) {
            Rs2GameObject.interact("pool", "Drink");
        }
        if (Rs2GameObject.getGameObject("Jewellery Box") != null) return false;
        if (Rs2GameObject.interact("Jewellery Box", "Edgeville")) {
            Rs2Player.waitForAnimation();
            return playerOn(EDGETELEPORT);
        }
        if (Rs2GameObject.interact("Jewellery Box", "Teleport Menu")) {
            sleepUntil(() -> Rs2Widget.isWidgetVisible(10551312, 38666240));
            Microbot.getMouse().click(Rs2Widget.getWidget(38666247).getBounds());
            Rs2Player.waitForAnimation();
            return playerOn(EDGETELEPORT);
        }
        return playerOn(EDGETELEPORT);
    }



}

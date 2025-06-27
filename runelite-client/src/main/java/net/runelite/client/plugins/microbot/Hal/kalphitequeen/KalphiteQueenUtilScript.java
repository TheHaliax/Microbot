package net.runelite.client.plugins.microbot.Hal.kalphitequeen;

import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.misc.Rs2Food;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcModel;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.reflection.Rs2Reflection;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.Hal.kalphitequeen.data.Thralls.*;
import static net.runelite.client.plugins.microbot.runecrafting.gotr.GotrScript.totalTime;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.PROTECT_MAGIC;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.PROTECT_RANGE;

public class KalphiteQueenUtilScript extends Script {

    static KalphiteQueenConfig config;


    public static int inventoryFood = -1;
    static boolean initCheck = false;

    public boolean run(KalphiteQueenConfig config) {
        Microbot.pauseAllScripts.compareAndSet(true, false);;
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


                if (Rs2Player.eatAt(inventoryFood, true)) {
                    Rs2Inventory.waitForInventoryChanges(1200);
                }

                if (Rs2Player.drinkAntiPoisonPotion()) {
                    Rs2Player.waitForAnimation();
                }

                if (Rs2Player.drinkPrayerPotion()) {
                    Rs2Player.waitForAnimation();
                }

                if (KalphiteQueenScript.queenLured) {
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

                if (KalphiteQueenScript.queenLured){
                    if (getHighestAvailableThrall() != null) {
                        Rs2Magic.cast(getHighestAvailableThrall());
                        return;
                    }
                }

                var kalphiteNpcs = Rs2Npc.getNpcs("Kalphite", false);

                for (Rs2NpcModel kalphiteNpc : kalphiteNpcs.collect(Collectors.toList())) {
                    if (kalphiteNpc == null) continue;
                    int npcAnimation = Rs2Reflection.getAnimation(kalphiteNpc);
                    handleQueenPrayer(npcAnimation);
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

    private void handleQueenPrayer(int animationId) {
        if (animationId == 1172) {
            toggle(PROTECT_MAGIC, true);
        } else if (animationId == 6240) {
            toggle(PROTECT_RANGE, true);
        }
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

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

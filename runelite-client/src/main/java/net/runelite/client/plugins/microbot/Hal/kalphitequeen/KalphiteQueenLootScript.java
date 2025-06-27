package net.runelite.client.plugins.microbot.Hal.kalphitequeen;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.Hal.kalphitequeen.data.LooterStyle;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.grounditem.LootingParameters;
import net.runelite.client.plugins.microbot.util.grounditem.Rs2GroundItem;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class KalphiteQueenLootScript extends Script {

    static KalphiteQueenConfig config;
    public static boolean lootExists = false;

    public boolean run(KalphiteQueenConfig config) {
        Microbot.pauseAllScripts.compareAndSet(true, false);;
        this.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run()) return;
                if (!Microbot.isLoggedIn() || Rs2Combat.inCombat()) return;

                // Always attempt looting based on style; no world-hop or banking

                LooterStyle style = config.looterStyle();

                if (style == LooterStyle.ITEM_LIST) {
                    lootExists = Arrays.stream(config.listOfItemsToLoot().trim().split(","))
                            .anyMatch(itemName -> Rs2GroundItem.exists(itemName, config.distanceToStray()));
                }
                else if (style == LooterStyle.GE_PRICE_RANGE) {
                    lootExists = Rs2GroundItem.isItemBasedOnValueOnGround(
                            config.minPriceOfItem(), config.distanceToStray());
                }
                else if (style == LooterStyle.MIXED) {
                    lootExists = Arrays.stream(config.listOfItemsToLoot().trim().split(","))
                            .anyMatch(itemName -> Rs2GroundItem.exists(itemName, config.distanceToStray()))
                            || Rs2GroundItem.isItemBasedOnValueOnGround(
                            config.minPriceOfItem(), config.distanceToStray());
                }

                if (lootExists) {
                    // Loot by names if ITEM_LIST or MIXED
                    if (style == LooterStyle.ITEM_LIST || style == LooterStyle.MIXED) {
                        LootingParameters itemLootParams = new LootingParameters(
                                config.distanceToStray(),
                                1,
                                1,
                                // minFreeSlots removed; pass dummy or 0 if required by constructor
                                0,
                                config.toggleDelayedLooting(),
                                config.toggleLootMyItemsOnly(),
                                config.listOfItemsToLoot().split(",")
                        );
                        Rs2GroundItem.lootItemsBasedOnNames(itemLootParams);
                    }
                    // Loot by value if GE_PRICE_RANGE or MIXED
                    if (style == LooterStyle.GE_PRICE_RANGE || style == LooterStyle.MIXED) {
                        LootingParameters valueParams = new LootingParameters(
                                config.minPriceOfItem(),
                                config.maxPriceOfItem(),
                                config.distanceToStray(),
                                1,
                                // minFreeSlots removed; pass dummy or 0
                                0,
                                config.toggleDelayedLooting(),
                                config.toggleLootMyItemsOnly()
                        );
                        Rs2GroundItem.lootItemBasedOnValue(valueParams);
                    }

                }
                // If no loot exists, simply do nothing this cycle; no world-hop

            } catch (Exception ex) {
                Microbot.log("Error in KalphiteQueenLootScript: " + ex.getMessage());
            }
        }, 0, 200, TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}

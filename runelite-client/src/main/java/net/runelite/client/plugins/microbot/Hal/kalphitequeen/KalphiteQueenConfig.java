package net.runelite.client.plugins.microbot.Hal.kalphitequeen;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.Hal.kalphitequeen.data.LooterStyle;
import net.runelite.client.plugins.microbot.Hal.kalphitequeen.data.SpecWeapon;

@ConfigGroup("kalphitequeen")
@ConfigInformation("This Plugin is totally fucked")
public interface KalphiteQueenConfig extends Config {

    @ConfigSection(
            name = "General",
            description = "Configure global plugin settings",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "Configure global plugin settings";

    @ConfigSection(
            name = "Looter",
            description = "Configure Looter Settings",
            position = 1,
            closedByDefault = true
    )
    String looterSection = "Configure Looter Settings";

    @ConfigItem(
            keyName = "usePoH",
            name = "PoH Ornate Jewellery Box",
            description = "use PoH to travel to edgeville, recommended to have fairy ring access and dramen staff.",
            position = 0,
            section = generalSection
    )
    default boolean usePoH() { return false; }

    @ConfigItem(
            keyName = "SpecWeapon",
            name = "Spec Weapon",
            description = "pick your spec attack weapon",
            position = 1,
            section = generalSection
    )
    default SpecWeapon SpecWeapon() { return SpecWeapon.NONE; }

    @ConfigItem(
            name = "Loot Style",
            keyName = "lootStyle",
            position = 0,
            description = "Choose Looting Style",
            section = looterSection
    )
    default LooterStyle looterStyle() {
        return LooterStyle.ITEM_LIST;
    }

    @ConfigItem(
            name = "Distance to Stray",
            keyName = "distanceToStray",
            position = 1,
            description = "Radius of tiles to stray/look for items",
            section = looterSection
    )
    default int distanceToStray() {
        return 20;
    }

    @ConfigItem(
            name = "List of Items",
            keyName = "listOfItemsToLoot",
            position = 2,
            description = "List of items to loot",
            section = looterSection
    )
    default String listOfItemsToLoot() {
        return "Kq head,Dragon,Prayer,Super,rune,Runite,Bucket,Gold,Magic,Uncut,Wine,Potato,Coins,Grapes,Cactus,Weapon,Silver,key";
    }

    @ConfigItem(
            name = "Min. GE price of Item",
            keyName = "minGEPriceOfItem",
            position = 3,
            description = "Minimum GE price of item to loot",
            section = looterSection
    )
    default int minPriceOfItem() {
        return 1000;
    }

    @ConfigItem(
            name = "Max GE price of Item",
            keyName = "maxGEPriceOfItem",
            position = 4,
            description = "Maximum GE price of item to loot",
            section = looterSection
    )
    default int maxPriceOfItem() {
        return Integer.MAX_VALUE;
    }

    @ConfigItem(
            name = "Loot My Items Only",
            keyName = "lootMyItemsOnly",
            position = 5,
            description = "Toggles check for ownership of grounditem",
            section = looterSection
    )
    default boolean toggleLootMyItemsOnly() {
        return false;
    }

    @ConfigItem(
            name = "Delayed Looting",
            keyName = "delayedLooting",
            position = 6,
            description = "Toggles Delayed Looting",
            section = looterSection
    )
    default boolean toggleDelayedLooting() {
        return false;
    }

}

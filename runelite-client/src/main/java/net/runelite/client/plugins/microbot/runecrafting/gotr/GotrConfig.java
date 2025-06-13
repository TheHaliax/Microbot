package net.runelite.client.plugins.microbot.runecrafting.gotr;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigInformation;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.runecrafting.gotr.data.Mode;
import net.runelite.client.plugins.microbot.runecrafting.gotr.data.Combination;

@ConfigGroup(GotrConfig.configGroup)
@ConfigInformation("This plugin is in preview & only supports masses. <br /> The script will not create elemental guardians. <br /> Have fun and don't get banned! <br /> If using NPC Contact to repair pouches, make sure you have Abyssal book in your bank! <br /><br /> <b>NB</b> NPC Contact pouch repair doesn't seem to work; pay Apprentice Cordelia 25 abyssal pearls and have some in your inventory for smooth sailing. ")
public interface GotrConfig extends Config {

    String configGroup = "gotr-combination";

    String mode = "mode";
    String maxFragmentAmount = "maxFragmentAmount";
    String maxAmountEssence = "maxAmountEssence";
    String shouldDepositRunes = "shouldDepositRunes";
    String enabled = "enabled";
    String combination = "combination";
    String air = "air";
    String water = "water";
    String earth = "earth";
    String fire = "fire";
    String noBinding = "noBinding";
    String timeout = "timeout";
    String elemental = "elemental";
    String airAltar = "airAltar";
    String waterAltar = "waterAltar";
    String earthAltar = "earthAltar";
    String fireAltar = "fireAltar";
    String needsDeposit = "needsDeposit";

    @ConfigSection(
            name = "General",
            description = "General Plugin Settings",
            position = 0
    )
    String generalSection = "general";

    @ConfigSection(
            name = "Combination",
            description = "Combination Settings",
            position = 1
    )
    String combinationSection = "combination";

    @ConfigSection(
            name = "Debug",
            description = "Debug settings",
            position = 2
    )
    String debugSection = "debug";

    @ConfigItem(
            keyName = mode,
            name = "Mode",
            description = "Type of mode",
            position = 0,
            section = generalSection
    )
    default Mode Mode() { return Mode.BALANCED; }

    @ConfigItem(
            keyName = maxFragmentAmount,
            name = "Max. amount fragments",
            description = "Max amount fragments to collect",
            position = 1,
            section = generalSection
    )
    default int maxFragmentAmount() {
        return 100;
    }

    @ConfigItem(
            keyName = maxAmountEssence,
            name = "Max. amount essence before using portal",
            description = "If you have more than the threshold defined, the player will not use the portal",
            position = 2,
            section = generalSection
    )
    default int maxAmountEssence() {
        return 20;
    }

    @ConfigItem(
            keyName = shouldDepositRunes,
            name = "Deposit runes?",
            description = "Should you deposit runes into the deposit pool?",
            position = 3,
            section = generalSection
    )
    default boolean shouldDepositRunes() {
        return true;
    }

    @ConfigItem(
            keyName = enabled,
            name = "Enabled",
            description = "Would you like to craft Combination runes?",
            position = 0,
            section = combinationSection
    )
    default boolean enabled() { return false; }

    @ConfigItem(
            keyName = combination,
            name = "Combination",
            description = "Which Combination rune would you like to make",
            position = 1,
            section = combinationSection
    )
    default Combination rune() {return Combination.MIST;}

    @ConfigItem(
            keyName = noBinding,
            name = "noBinding",
            description = "needs to bank?",
            position = 0,
            section = debugSection
    )
    default boolean noBinding() { return false; }

    @ConfigItem(
            keyName = timeout,
            name = "Breaking",
            description = "needs to break?",
            position = 1,
            section = debugSection
    )
    default boolean timeout() { return false; }

    @ConfigItem(
            keyName = elemental,
            name = "Elemental",
            description = "is rune Elemental?",
            position = 2,
            section = debugSection
    )
    default boolean elemental() { return false; }

    @ConfigItem(
            keyName = air,
            name = "Air runes combinable?",
            description = "use air runes for combination?",
            position = 3,
            section = debugSection
    )
    default boolean air() { return false; }

    @ConfigItem(
            keyName = water,
            name = "Water runes combinable?",
            description = "use water runes for combination?",
            position = 4,
            section = debugSection
    )
    default boolean water() { return false; }

    @ConfigItem(
            keyName = earth,
            name = "Earth runes combinable?",
            description = "use earth runes for combination?",
            position = 5,
            section = debugSection
    )
    default boolean earth() { return false; }

    @ConfigItem(
            keyName = fire,
            name = "Fire runes combinable?",
            description = "use fire runes for combination?",
            position = 6,
            section = debugSection
    )
    default boolean fire() { return false; }

    @ConfigItem(
            keyName = airAltar,
            name = "Air Altar?",
            description = "are we at the Air Altar?",
            position = 7,
            section = debugSection
    )
    default boolean airAltar() { return false; }

    @ConfigItem(
            keyName = waterAltar,
            name = "Water Altar?",
            description = "are we at the Water Altar?",
            position = 8,
            section = debugSection
    )
    default boolean waterAltar() { return false; }

    @ConfigItem(
            keyName = earthAltar,
            name = "Earth Altar?",
            description = "are we at the Earth Altar?",
            position = 9,
            section = debugSection
    )
    default boolean earthAltar() { return false; }

    @ConfigItem(
            keyName = fireAltar,
            name = "Fire Altar?",
            description = "are we at the Fire Altar?",
            position = 10,
            section = debugSection
    )
    default boolean fireAltar() { return false; }

    @ConfigItem(
            keyName = needsDeposit,
            name = "needs Deposit?",
            description = "are we at the Fire Altar?",
            position = 11,
            section = debugSection
    )
    default boolean needsDeposit() { return false; }
}

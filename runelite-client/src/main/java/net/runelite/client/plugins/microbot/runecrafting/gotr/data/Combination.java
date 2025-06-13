package net.runelite.client.plugins.microbot.runecrafting.gotr.data;

import lombok.Getter;
import net.runelite.api.ItemID;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum Combination {
    // Format: (id, lvl, name, ItemID)
    NONE(-1, 0, "None"),
    MIST(15, 6, "Mist rune"),
    MUD(16, 13, "Mud rune"),
    DUST(17, 10, "Dust rune"),
    LAVA(18, 23, "Lava rune"),
    STEAM(19, 19, "Steam rune"),
    SMOKE(20, 15, "Smoke rune"),
    ALL(-1, 23, "All");

    private final int id;
    private final int lvl;
    private final String name;
    private final int itemId;

    Combination(int id, int lvl, String name, int itemId) {
        this.id = id;
        this.lvl = lvl;
        this.name = name;
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        if (this == NONE) {
            return "None";
        }
        if (this == ALL) {
            return "All";
        }
        return name + " (" + getlvl() + ")";
    }

    public int getlvl() {
        return lvl;
    }

    // get all ids as a set
    public static Set<Integer> getIds() {
        return Arrays.stream(values()).map(Combination::getId).collect(Collectors.toSet());
    }
}

@Getter
public enum Elemental {
    AIR(22, 1, "Air rune", ItemID.AIR_RUNE),
    WATER(23, 5, "Water rune", ItemID.WATER_RUNE),
    EARTH(24, 9, "Earth rune", ItemID.EARTH_RUNE),
    FIRE(25, 14, "Fire rune", ItemID.FIRE_RUNE);

    private final int id;
    private final int lvl;
    private final String name;
    private final int itemId;

    Elemental(int id, int lvl, String name, int itemId) {
        this.id = id;
        this.lvl = lvl;
        this.name = name;
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return name + " (" + getlvl() + ", " + getItemId() + ")";
    }

    public int getlvl() {
        return lvl;
    }

    public static Set<Integer> getIds() {
        return Arrays.stream(values()).map(Elemental::getId).collect(Collectors.toSet());
    }
}

@Getter
public enum Catalytic {
    MIND(26, 2, "Mind rune", ItemID.MIND_RUNE),
    BODY(27, 20, "Body rune", ItemID.BODY_RUNE),
    COSMIC(28, 27, "Cosmic rune", ItemID.COSMIC_RUNE),
    NATURE(29, 44, "Nature rune", ItemID.NATURE_RUNE),
    LAW(30, 54, "Law rune", ItemID.LAW_RUNE),
    DEATH(31, 65, "Death rune", ItemID.DEATH_RUNE),
    BLOOD(32, 77, "Blood rune", ItemID.BLOOD_RUNE);

    private final int id;
    private final int lvl;
    private final String name;
    private final int itemId;

    Catalytic(int id, int lvl, String name, int itemId) {
        this.id = id;
        this.lvl = lvl;
        this.name = name;
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return name + " (" + getlvl() + ", " + getItemId() + ")";
    }

    public int getlvl() {
        return lvl;
    }

    public static Set<Integer> getIds() {
        return Arrays.stream(values()).map(Catalytic::getId).collect(Collectors.toSet());
    }
}

package net.runelite.client.plugins.microbot.kalphitequeen.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpecWeapon {
    NONE("No Weapon", 2000, false),
    ARCLIGHT("arclight", 500, false),
    BANDOS_GODSWORD("bandos godsword", 500, true),
    BARRELCHEST_ANCHOR("barrelchest anchor", 500, true),
    BONE_DAGGER("bone dagger", 750, false),
    DARKLIGHT("darklight", 500, false),
    DRAGON_MACE("dragon mace", 250, false),
    DRAGON_WARHAMMER("dragon warhammer", 500, false),
    VOIDWAKER("voidwaker", 500, false),
    ;

    private final String name;
    private final int energyRequired;
    private final boolean is2H;

    @Override
    public String toString() {
        return name;
    }

}

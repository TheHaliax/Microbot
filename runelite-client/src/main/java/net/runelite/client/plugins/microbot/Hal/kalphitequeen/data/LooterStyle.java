package net.runelite.client.plugins.microbot.Hal.kalphitequeen.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LooterStyle {
    ITEM_LIST("Item List"),
    GE_PRICE_RANGE("GE Price Range"),
    MIXED("Mixed");

    private final String name;
}

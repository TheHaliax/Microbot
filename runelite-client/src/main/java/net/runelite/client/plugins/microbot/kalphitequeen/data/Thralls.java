package net.runelite.client.plugins.microbot.kalphitequeen.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;

@AllArgsConstructor
@Getter
public enum Thralls {
    RESURRECT_GREATER_GHOST(MagicAction.RESURRECT_GREATER_GHOST),
    RESURRECT_SUPERIOR_GHOST(MagicAction.RESURRECT_SUPERIOR_THRALL), // or correct MagicAction constant
    RESURRECT_LESSER_GHOST(MagicAction.RESURRECT_LESSER_THRALL),
    ;

    private final MagicAction action;

    // Level is delegated:
    public int getLevel() {
        return action.getLevel();
    }

    public static MagicAction getHighestAvailableThrall() {
        if (Rs2Magic.isThrallActive()) {
            return null;
        }
        Thralls bestEntry = null;
        for (Thralls entry : Thralls.values()) {
            MagicAction ma = entry.getAction();
            if (!Rs2Magic.canCast(ma)) {
                continue;
            }
            if (bestEntry == null || entry.getLevel() > bestEntry.getLevel()) {
                bestEntry = entry;
            }
        }
        return bestEntry != null ? bestEntry.getAction() : null;
    }

}

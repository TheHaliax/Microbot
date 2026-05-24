package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.ObjectComposition;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Door-like object heuristics shared by {@link net.runelite.client.plugins.microbot.util.walker.passage.PassageDoorHandler}
 * and path-ahead fallback. Keep in sync with microbot-api skill DoorHandler copy.
 */
public final class PathAheadDoorActions {

    public static final int DEFAULT_PASSAGE_MAX_TILES = 8;

    public static final String[] DOOR_LIKE_NAME_FRAGMENTS = {
            "door", "gate", "barrier", "stile", "portcullis", "archway", "cattlegate", "fence"
    };

    public static final Pattern FENCE_AS_WORD = Pattern.compile("\\bfence\\b", Pattern.CASE_INSENSITIVE);

    public static final List<String> DOOR_ACTION_PRIORITY = List.of(
            "pay-toll", "pick-lock", "walk-through", "go-through", "open", "pass", "enter",
            "push", "climb-over", "climb-through", "squeeze-through", "cross", "force", "exit"
    );

    private PathAheadDoorActions() {
    }

    public static int doorActionPriorityIndex(String action) {
        if (action == null) {
            return Integer.MAX_VALUE;
        }
        String al = action.toLowerCase(Locale.ROOT);
        for (int i = 0; i < DOOR_ACTION_PRIORITY.size(); i++) {
            if (al.startsWith(DOOR_ACTION_PRIORITY.get(i))) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static boolean isDoorCloseOrShutAction(String action) {
        if (action == null) {
            return false;
        }
        String al = action.toLowerCase(Locale.ROOT).trim();
        return al.startsWith("close") || al.startsWith("shut");
    }

    public static boolean doorCompositionSpecifiesOnlyCloseOrShut(ObjectComposition comp) {
        if (comp == null || comp.getActions() == null) {
            return false;
        }
        boolean sawNonNull = false;
        for (String a : comp.getActions()) {
            if (a == null) {
                continue;
            }
            sawNonNull = true;
            if (!isDoorCloseOrShutAction(a)) {
                return false;
            }
        }
        return sawNonNull;
    }

    public static String pickWalkDoorAction(ObjectComposition comp) {
        if (comp == null || comp.getActions() == null) {
            return null;
        }
        return Arrays.stream(comp.getActions())
                .filter(Objects::nonNull)
                .filter(a -> !isDoorCloseOrShutAction(a))
                .min(Comparator.comparingInt(PathAheadDoorActions::doorActionPriorityIndex))
                .orElse(null);
    }

    public static boolean isDoorLikeGameObjectName(String name) {
        if (name == null) {
            return false;
        }
        String n = name.trim().toLowerCase(Locale.ROOT);
        if (n.isEmpty()) {
            return false;
        }
        for (String f : DOOR_LIKE_NAME_FRAGMENTS) {
            if ("fence".equals(f)) {
                if (FENCE_AS_WORD.matcher(n).find()) {
                    return true;
                }
            } else if (n.contains(f)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNullOrPlaceholderObjectName(String name) {
        if (name == null) {
            return true;
        }
        String t = name.trim();
        return t.isEmpty() || "null".equalsIgnoreCase(t);
    }
}

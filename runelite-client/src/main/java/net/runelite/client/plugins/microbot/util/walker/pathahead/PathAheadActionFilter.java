package net.runelite.client.plugins.microbot.util.walker.pathahead;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Filters context-menu actions for path-ahead blob interact. Excluded prefixes are
 * configurable; default skips close/shut so already-open objects are not toggled shut.
 */
public final class PathAheadActionFilter {

    private static final List<String> EXCLUDED_ACTION_PREFIXES = List.of("close", "shut");

    private PathAheadActionFilter() {
    }

    public static boolean isAllowedAction(String action) {
        if (action == null) {
            return false;
        }
        String al = action.toLowerCase(Locale.ROOT).trim();
        if (al.isEmpty()) {
            return false;
        }
        for (String excluded : EXCLUDED_ACTION_PREFIXES) {
            if (al.startsWith(excluded)) {
                return false;
            }
        }
        return true;
    }

  /**
   * First allowed non-null action in array order (client menu order).
   */
    public static String firstAllowedAction(String[] actions) {
        if (actions == null) {
            return null;
        }
        return Arrays.stream(actions)
                .filter(Objects::nonNull)
                .filter(PathAheadActionFilter::isAllowedAction)
                .findFirst()
                .orElse(null);
    }
}

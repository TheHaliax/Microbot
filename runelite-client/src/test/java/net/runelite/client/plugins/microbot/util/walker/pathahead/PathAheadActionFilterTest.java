package net.runelite.client.plugins.microbot.util.walker.pathahead;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PathAheadActionFilterTest {

    @Test
    public void excludesCloseAndShutPrefixes() {
        assertFalse(PathAheadActionFilter.isAllowedAction("Close"));
        assertFalse(PathAheadActionFilter.isAllowedAction("Shut gate"));
        assertTrue(PathAheadActionFilter.isAllowedAction("Open"));
        assertTrue(PathAheadActionFilter.isAllowedAction("Use"));
    }

    @Test
    public void firstAllowedAction_skipsExcluded() {
        String action = PathAheadActionFilter.firstAllowedAction(
                new String[] {"Close", "Open", "Examine"});
        assertEquals("Open", action);
    }

    @Test
    public void firstAllowedAction_nullWhenAllExcluded() {
        assertNull(PathAheadActionFilter.firstAllowedAction(new String[] {"Close", "Shut"}));
    }
}

package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PassageDoorOnPathFocusTest {

    @Test
    public void isPassageDoorOnPathFocus_sameTileAsFocus() {
        List<WorldPoint> path = Collections.singletonList(new WorldPoint(3079, 3258, 0));
        assertTrue(PathAheadScanner.isPassageDoorOnPathFocus(path, 0, new WorldPoint(3079, 3258, 0)));
    }

    @Test
    public void isPassageDoorOnPathFocus_parallelGateOneTileOff() {
        List<WorldPoint> path = Collections.singletonList(new WorldPoint(3079, 3258, 0));
        assertFalse(PathAheadScanner.isPassageDoorOnPathFocus(path, 0, new WorldPoint(3078, 3258, 0)));
    }

    @Test
    public void isPassageDoorOnPathFocus_falseOnWrongPlane() {
        List<WorldPoint> path = Collections.singletonList(new WorldPoint(3079, 3258, 0));
        assertFalse(PathAheadScanner.isPassageDoorOnPathFocus(path, 0, new WorldPoint(3079, 3258, 1)));
    }
}


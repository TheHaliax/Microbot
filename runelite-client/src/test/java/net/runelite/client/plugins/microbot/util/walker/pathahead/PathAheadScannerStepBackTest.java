package net.runelite.client.plugins.microbot.util.walker.pathahead;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathAheadScannerStepBackTest {

    @Test
    public void stepBackAnchorIndices_fromTen_stepsByTwo() {
        List<Integer> indices = PathAheadScanner.stepBackAnchorIndices(10);
        assertEquals(Arrays.asList(10, 8, 6, 4, 2, 0), indices);
    }

    @Test
    public void stepBackAnchorIndices_negativeReturnsEmpty() {
        assertEquals(Collections.emptyList(), PathAheadScanner.stepBackAnchorIndices(-1));
    }

    @Test
    public void stepBackAnchorIndices_oddUnreachable_includesZero() {
        List<Integer> indices = PathAheadScanner.stepBackAnchorIndices(7);
        assertEquals(Arrays.asList(7, 5, 3, 1), indices);
    }
}

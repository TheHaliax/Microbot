package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransportOriginBlobIndexTest {

    @Test
    public void nineTileFootprint_includesOriginAndNeighbors() {
        WorldPoint origin = new WorldPoint(3200, 3200, 0);
        Set<WorldPoint> tiles = new HashSet<>();
        TransportOriginBlobIndex.addNineTileFootprint(tiles, origin);

        assertEquals(9, tiles.size());
        assertTrue(tiles.contains(origin));
        assertTrue(tiles.contains(new WorldPoint(3199, 3199, 0)));
        assertTrue(tiles.contains(new WorldPoint(3201, 3201, 0)));
    }

    @Test
    public void nineTileFootprint_doesNotLeakAcrossPlanes() {
        WorldPoint origin = new WorldPoint(3200, 3200, 1);
        Set<WorldPoint> tiles = new HashSet<>();
        TransportOriginBlobIndex.addNineTileFootprint(tiles, origin);

        assertFalse(tiles.contains(new WorldPoint(3200, 3200, 0)));
        assertTrue(tiles.contains(new WorldPoint(3200, 3200, 1)));
    }

    @Test
    public void isInBlob_emptyIndexNeverMatches() {
        TransportOriginBlobIndex index = TransportOriginBlobIndex.forTiles(new HashSet<>());
        assertFalse(index.isInBlob(new WorldPoint(3200, 3200, 0)));
        assertFalse(index.isInBlob(null));
    }

    @Test
    public void clearCatalogCacheForTest_resetsLazyIndex() {
        TransportOriginBlobIndex.clearCatalogCacheForTest();
        TransportOriginBlobIndex index = TransportOriginBlobIndex.forTiles(new HashSet<>());
        assertFalse(index.isInBlob(new WorldPoint(1, 1, 0)));
    }
}

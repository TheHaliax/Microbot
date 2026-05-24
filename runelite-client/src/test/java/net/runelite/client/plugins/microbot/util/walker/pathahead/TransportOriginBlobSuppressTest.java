package net.runelite.client.plugins.microbot.util.walker.pathahead;

import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransportOriginBlobSuppressTest {

    @Test
    public void shouldSuppressDoorRecovery_onCatalogBlobTile() {
        WorldPoint origin = new WorldPoint(3200, 3200, 0);
        Set<WorldPoint> tiles = new HashSet<>();
        TransportOriginBlobIndex.addNineTileFootprint(tiles, origin);
        TransportOriginBlobIndex index = TransportOriginBlobIndex.forTiles(tiles);

        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3199, 3199, 0),
                origin,
                new WorldPoint(3201, 3201, 0));

        assertTrue(TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, 1, index));
    }

    @Test
    public void shouldSuppressPassageDoor_onBlobTileWithoutTransportStep() {
        WorldPoint origin = new WorldPoint(3084, 3272, 0);
        Set<WorldPoint> tiles = new HashSet<>();
        TransportOriginBlobIndex.addNineTileFootprint(tiles, origin);
        TransportOriginBlobIndex index = TransportOriginBlobIndex.forTiles(tiles);

        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3083, 3271, 0),
                origin,
                new WorldPoint(3084, 3273, 0));

        assertTrue(TransportOriginBlobIndex.shouldSuppressPassageDoor(path, origin, index));
        assertTrue(TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, 1, index));
    }

    @Test
    public void shouldSuppressDoorRecovery_falseOutsideBlob() {
        WorldPoint origin = new WorldPoint(3200, 3200, 0);
        Set<WorldPoint> tiles = new HashSet<>();
        TransportOriginBlobIndex.addNineTileFootprint(tiles, origin);
        TransportOriginBlobIndex index = TransportOriginBlobIndex.forTiles(tiles);

        List<WorldPoint> path = Arrays.asList(
                new WorldPoint(3100, 3100, 0),
                new WorldPoint(3101, 3101, 0));

        assertFalse(TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, 0, index));
    }
}

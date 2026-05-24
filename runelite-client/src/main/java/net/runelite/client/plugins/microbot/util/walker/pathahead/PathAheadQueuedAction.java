package net.runelite.client.plugins.microbot.util.walker.pathahead;

import lombok.Builder;
import lombok.Value;

/**
 * Queued path-ahead actions from a single forward scan (transport, door blob, skip-ahead canvas).
 */
@Value
@Builder
public class PathAheadQueuedAction {

    public static final int NONE = -1;

    @Builder.Default
    int transportPathIndex = NONE;

    @Builder.Default
    int doorPathIndex = NONE;

    @Builder.Default
    int skipAheadPathIndex = NONE;

    PathAheadBlobInteract.BlobHit doorHit;

    @Builder.Default
    int scannedThroughIndex = NONE;

    public static PathAheadQueuedAction empty() {
        return PathAheadQueuedAction.builder().build();
    }

    public boolean hasWork() {
        return transportPathIndex >= 0 || doorPathIndex >= 0 || skipAheadPathIndex >= 0;
    }
}

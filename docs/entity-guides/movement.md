# Movement Gotchas

## 1. Do not recurse on failed minimap clicks without changing the click target

`Rs2Walker.processWalk` holds the walker lock while processing a path. If a minimap click is rejected because the calculated point is outside the minimap clip, immediately recursing with the same target can spin forever while still holding the lock. Shrink the click target toward the player or otherwise change the condition before retrying.

**Why this matters:** Quest steps that walk to a nearby object can repeatedly calculate a valid path but never move, starving other walk requests because the walker lock is never released.

**Pattern to follow:**

```java
WorldPoint clickTarget = getPointWithWallDistance(targetWp);
boolean clicked = Rs2Walker.walkMiniMap(clickTarget);
if (!clicked)
{
	clicked = walkMiniMapToward(clickTarget, playerLoc, MINIMAP_REACH_EUCLIDEAN - 1);
}
```

**Where this applies:** `Rs2Walker`, `Rs2MiniMap`, and shortest-path walking loops.

**Defensive check:** When debugging stalls, compare pathfinder logs with `./microbot-cli state`. A repeating valid path with an unchanged player position usually means the click layer failed after pathing succeeded.

## 2. Walker uses raw BFS path; PathAheadScanner handles obstacles ahead

`Pathfinder.getWalkablePath()` returns the same tile-per-step list as `getPath()` for walker consumption. `PathSmoother` is offline/benchmark only. Every minimap waypoint aligns with a raw path index, so doors and transport blobs are not hidden by diagonal smoothing.

`PathAheadLookahead` scans forward from `Rs2Walker.getPathScanStartIndex` (max of closest index and `pathCommittedFloorIdx`). **Transport + first unreachable** share one forward loop; scan end is **min** of **min(28, render distance)** path indices (render minimum 20) and **8-tile inset** inside loaded `WorldView` (stop at first path tile outside inset; no BFS in outer chunk ring). **Skip-ahead (+8)** after unreachable uses **render distance** path indices (not capped at 28) with same inset. Queued door/transport fires when player is within **6 tiles** of interim (or interim progress stalled). `PathAheadScanner` recovery order: scan unreachable -> skip -> **step-back blob** (`STEP_BACK_TILES=2`) -> `commitForwardToLastReachable` -> `PassageDoorHandler` last. `blob_stepback` WebWalkLog tmark only when blob interact is attempted (not when anchor is already BFS-reachable). Segment unreachable calls `tryRecoverUnreachable` (re-scans; segment index alone is not enough).

**Why this matters:** Smoothed paths could skip door tiles; raw path + path-ahead keeps obstacle tiles on the walked route.

**Pattern to follow:**

```java
if (PathAheadHandlerState.isSettling()) {
    break; // path-ahead-settling — no interim/minimap churn
}
if (handleNearbyRawPathSceneObjects(path, HANDLER_RANGE)) {
  // PathAheadScanner handled transport/blob/passage
}
```

**Where this applies:** `Rs2Walker.processWalk`, `util/walker/pathahead/*`, `PassageDoorHandler`.

**Defensive check:** Stall beside door with complete pathfinder route — confirm path-ahead runs (not only `stuckCount` recovery) and handler settle gate blocks interim re-clicks.

## 3. Path-ahead blob interact: door-like names only

`PathAheadBlobInteract.tryDoorLikeAction(anchor, radius)` queries `.within(anchor, radius)` (not player-centric), filters `PathAheadDoorActions.isDoorLikeGameObjectName`, and uses `pickWalkDoorAction`. Do not use `PathAheadActionFilter.firstAllowedAction` for blob (avoids Logs/Take-axe after gates).

**Why this matters:** First arbitrary allowed action clicked wrong objects after gate opened.

**Pattern to follow:**

```java
PathAheadBlobInteract.BlobHit hit = PathAheadBlobInteract.tryDoorLikeAction(anchor, 5);
if (hit != null && PathAheadBlobInteract.interact(hit)) {
    // finishHandlerDoorFast: 1-2 tick settle, no PassageDoor in same pass
}
```

**Where this applies:** `PathAheadScanner`, `PathAheadLookahead`, `PathAheadDoorActions`.

**Defensive check:** `PathAheadScannerStepBackTest`, `PathAheadLookaheadScanTest`.

## 4. Path-ahead gates: interim does not block lookahead

`isPathAheadBlocked()` is settling + recovery click only (not interim). Lookahead refresh runs every `processWalk` tail. `tryExecute` runs at interim <= 2 tiles. Door blob uses `finishHandlerDoorFast` (cap ~2 ticks).

**Why this matters:** Interim blocked all path-ahead for 9-15s; moving gate stalled until late PassageDoor.

**Pattern to follow:**

```java
PathAheadLookahead.refresh(path);
if (PathAheadLookahead.tryExecute(path, target)) {
    exitReason = "path-ahead-queued-at-interim";
}
```

**Where this applies:** `Rs2Walker.processWalk`, `PathAheadLookahead`, `PathAheadScanner`.

**Defensive check:** One interaction then movement/path progress — not repeated path-ahead logs every tick while moving.

## 4.1 Skip-ahead: one canvas + in-flight path recalc

Unreachable ahead triggers `PathAheadScanner.commitSkipAhead`: one `walkFastCanvas`, `PathAheadHandlerState.beginSkipAheadTransit`, `Rs2Walker.resetWalkerProgressForSkipRecalc()` (clears interim, `pathCommittedFloorIdx`, handoff idempotency), then `Rs2Walker.restartPathfindingFromSkipAhead(skipTile, goal)` (pathfinder start = skip tile, goal unchanged). While transit is active, `PathAheadLookahead.refresh` clears the queue, `allowRawSceneScan` is false, and `tryHandle` / `tryRecoverUnreachable` return false — no burst of `raw-path-scene-object-handled` or inner-fence `PassageDoorHandler`. Transit ends when player is within 2 tiles of the skip tile, **forward** closest path index (from skip index) passes the skip index, or 15s elapses.

Until pathfinder finishes and handoff runs, `Rs2Walker.isSkipAheadHandoffPending()` is true: `processWalk` yields without segment/blob work on the **old** polyline (no stale `floor=106` on a 131-tile list).

When pathfinder delivers the new path (`path_snapshot` + `pathfinder.isDone()` while transit active), `Rs2Walker.applySkipAheadPathHandoff` remaps skip tile onto the new list, sets `pathCommittedFloorIdx` from **player** closest index (not `advancePathCommittedFloor` on the old path), and issues one forward minimap via `PathAheadScanner.commitForwardToLastReachable` (not interim pinned to distant skip when player is still at the gate). Stale interim on old path indices is cleared via `isInterimStaleForPlayer` when index is out of range or misaligned.

**Why this matters:** Re-clicking the skip tile and re-scanning the stale path opened the wrong inner gate and stalled the walk. Without progress reset + player-anchored handoff, `pathCommittedFloorIdx` from the old path (e.g. 106) carried onto a shorter route (131 tiles) — scan started near path end, no minimap click, `tail_max` at the gate.

**Pattern to follow:**

```java
// commitSkipAhead returns false — walker stays on interim-in-flight, not raw-path-scene-handled
PathAheadScanner.commitSkipAhead(path, skipIdx, target);
// Rs2Walker.restartPathfindingFromSkipAhead(skipTile, currentTarget);
// processWalk path_snapshot (pathfinder done): applySkipAheadPathHandoff(path, target);
// while isSkipAheadHandoffPending(): processWalk continues without segment loop
```

**STALL_RECALC:** while skip transit active, call `restartPathfindingFromSkipAhead(skipTile, target)` — not `setTarget` (that clears transit).

**Where this applies:** `PathAheadHandlerState`, `PathAheadScanner`, `PathAheadLookahead`, `Rs2Walker.processWalk`.

**Defensive check:** Logs show one `skip_ahead_recalc` tmark, `skip_ahead_handoff` with low `playerIdx` (not `floor` near `walkSize` at gate), then `blob_forward_commit` or `interim_chain_ok` within ~1s; no segment work on old path size while pending; no `STALL_RECALC` + full `setTarget` during transit.

## 2.1 Banked walk startup: skip compare, early click

`walkWithBankedTransportsAndStateLocked` resolves missing transport items **before** `compareRoutes`. When `!forceBanking` and inventory already has required teleports, it calls `walkWithStateInternal` immediately (no `compare_start` / `compare_done` tmarks).

After `path_snapshot`, `tryStartupMovementClick` issues the first minimap click in the same tail (furthest clickable + `commitInterimForward`) so long paths do not wait for per-segment `dist2d > 9-12` gates.

**Defensive check:** Direct walk with no missing items: no compare tmarks; `path_snapshot` then `first_minimap_click` within a few hundred ms of pathfinder ready.

## 4.2 Catalog transport blob: early detection, not door recovery

`TransportOriginBlobIndex` marks a 3x3 footprint around every transport catalog origin. When the raw path crosses that footprint but **does not** use `TransportPathUtil.hasExplicitTransportStep` (or a catalog-backed segment), path-ahead must **not** treat the tile as unreachable door work (`PathAheadBlobInteract`, `PassageDoorHandler.openNearestDoor(..., path)`, or queued `doorHit`). Use `shouldSuppressPassageDoor(path, doorTile)` for door objects near blob tiles. Skip-ahead and explicit transport handling still apply.

**Why this matters:** Walking past Draynor sewer trapdoor (unused transport on path) used to open a nearby pigpen gate because client reachability failed on the blob tile and door recovery fired.

**Pattern to follow:**

```java
if (TransportOriginBlobIndex.shouldSuppressDoorRecovery(path, pathIndex)) {
    continue; // lookahead scan / recover — not a door anchor
}
PathAheadScanner.commitInterimForward(path, pathIdx, clickTarget, goal);
```

**Where this applies:** `TransportOriginBlobIndex`, `PathAheadLookahead`, `PathAheadBlobInteract`, `PathAheadScanner`, `Rs2Walker.processWalk`.

**Defensive check:** No `PathAheadBlobInteract` on catalog transport footprint unless path uses that transport step; no `[PassageDoorHandler] handled ... Trapdoor` when path does not use that transport edge; minimap interim uses `commitInterimForward` without 2s `sleepUntil` while already moving.

## 4.3 Path committed floor and interim chain

`pathCommittedFloorIdx` advances when `commitInterimForward` runs while moving (click aligned, Chebyshev <= 2) **or** `maybeAdvanceCommittedFloorOnInterimProgress` sees closest path index >= interim / player within 6 tiles of interim while moving. Scan start is `getPathScanStartIndex` (max of closest and floor).

Each `processWalk` tail (after `path_snapshot`): `maybeAdvanceCommittedFloorOnInterimProgress`, then `processInterimBeforeSegmentLoop` (lookahead `tryExecute`, `tryChainInterimCheckpoint` when within 6 tiles and moving, `interim-in-flight` yield before segment `dist2d` gate). Segment loop keeps only stationary interim recovery click.

`tryChainInterimCheckpoint` logs `interim_chain_ok` / `interim_chain_skip` via `WebWalkLog`.

**Defensive check:** `interim_chain_ok` within ~6 tiles of each checkpoint; no rockfall/transport on indices below floor.

## 4.4 Unreachable recovery order (blob, forward commit, passage)

Per tail: **Phase A** `PathAheadLookahead.tryExecute` (skip-ahead queue, transport, ahead-blob at first unreachable). **Phase B** `tryRecoverAtIndex`: skip-ahead +8, then `stepBackAnchorIndices` (-2 per anchor) with `PathAheadBlobInteract`, then `commitForwardToLastReachable` toward last reachable path index, then `PassageDoorHandler` only if stalled (`isStuckTooLongForPassageRecovery`), on-path door (Chebyshev 0 on focus vertex), no reachable ahead, no interim-in-flight.

Reachable anchor during step-back triggers forward commit and **does not** fall through to passage (fixes parallel-gate false opens).

**Defensive check:** `blob_stepback` / `blob_forward_commit` tmarks; `passage_suppressed reason=not_stalled` while walking past fence; `passage_open` only on door tile when truly stuck.

## 5. Suppress the inverse adjacent transport after crossing a same-plane door

Some doors are represented in `transports.tsv` as two adjacent same-plane transports, one for each direction. After the walker clicks one side and arrives on the other, immediately accepting the inverse transport can bounce the player back through the same door instead of letting the next minimap step continue away from it. Mark both tiles of a successful adjacent same-plane transport as recently handled for a short window.

**Why this matters:** Leaving Draynor Manor through the east/back door can alternate between `3123,3360,0` and `3123,3361,0`, repeatedly logging raw-path/current-tile transport handling and burning the route timeout before walking back to Draynor.

**Pattern to follow:**

```java
boolean reachedDestination = sleepUntil(() -> atTransportDestination(transport), 5000);
if (reachedDestination && isAdjacentSamePlaneTransport(transport)) {
    markStationaryDoorOpened(transport.getOrigin());
    markStationaryDoorOpened(transport.getDestination());
}
```

**Where this applies:** `Rs2Walker.handleTransports`, current-tile transport recovery, raw-path transport probing, and bidirectional same-plane door/gate transports.

**Defensive check:** A successful adjacent same-plane transport should be followed by a minimap/path step away from the doorway, not by alternating `Raw path transport handler` and `Current-tile transport handler` logs for the same two tiles.

## 6. Recalculate after long-distance object transports

Not every large map transition changes plane or uses a teleport type. Some object transports, such as the Varrock Sewers ladder, remain on plane 0 while jumping between coordinate bands. After a successful object interaction reaches one of these destinations, run the normal transport finalizer so the shortest path is rebuilt from the new location.

**Why this matters:** A route from Varrock Sewers back to a surface origin can climb the ladder successfully, then continue using a path that was calculated from the underground coordinate band. The walker may drift off path or exit during setup even though the transport itself worked.

**Pattern to follow:**

```java
if (reachedDestination) {
    markAdjacentSamePlaneTransportHandled(transport, object);
    return finishHandledTransport(transport);
}
```

**Where this applies:** `Rs2Walker.handleTransports` object interactions and any object-transport handler that waits for the destination tile directly.

**Defensive check:** Same-plane object transports with a large `distanceTo2D` delta should produce a fresh pathfinder start near the post-transport player location before the next minimap step.

## 7. Model missing collision edges before tuning walker retries

Some static collision gaps are specific edges, not whole tiles. If the pathfinder repeatedly routes through a visible fence/wall and the live client keeps clicking fallback tiles near that boundary, add an explicit blocked edge to pathfinding and smoothing instead of trying to solve it with longer timeouts or broader minimap fallback.

**Why this matters:** The Varrock Palace garden south fence can be missing from the bundled collision map near `3229..3241,3472 -> 3471`. A no-agility F2P route to the Varrock Sewers manhole can walk around the trellis correctly, then stall against that garden boundary because the path says the south edge is traversable.

**Pattern to follow:**

```java
if (config.isBlockedTransportEdge(node.packedPosition, neighborPacked)) {
    continue;
}
```

**Where this applies:** `CollisionMap.getNeighbors`, `PathSmoother.lineOfSight`, and any path data correction where only one edge between adjacent tiles is invalid.

**Defensive check:** Add a core pathfinder regression from the observed stuck tile; assert neither the raw path nor smoothed path crosses the blocked edge, and that the route still reaches the original destination.

## 8. Do not click a visible endpoint before honoring pending route interactions

An endpoint being visible on the minimap does not mean it is the next correct click. If the computed shortest path reaches that endpoint through an intermediate door, gate, transport, shortcut, ladder, or other route object, the walker must process the first route interaction before issuing a direct endpoint click.

**Why this matters:** From Varrock Palace, a destination such as `3229,3473,0` can be visible on the minimap while the shorter route requires opening the palace doors first. Clicking the endpoint lets the game choose a longer collision-valid detour and bypasses the webwalker's route.

**Pattern to follow:**

```java
if (handleNearbyRawPathSceneObjects(rawPath, HANDLER_RANGE)) {
    return true;
}
if (!hasPendingExplicitTransportStepBeforeArrival(rawPath, target, distance)
        && !localRouteDetoursFromComputedRoute(rawPath, end, DIRECT_CLICK_MAX_DISTANCE)) {
    walkMiniMap(end);
}
```

**Where this applies:** `Rs2Walker.walkWithStateInternal`, short local walk kick-starts, final/minimap endpoint clicks, and any future fast-path that bypasses normal path iteration.

**Defensive check:** Reproduce with closed Varrock Palace doors toward `3229,3473,0`; the first action should target the door or route waypoint, not the final endpoint tile.

## 9. Preserve interrupts so walker cancellation stops waits immediately

Ctrl+X and script shutdown cancel the active walk task with `Future.cancel(true)` and clear the walker target. Shared sleep/poll helpers must preserve the interrupted flag and stop polling when interruption is observed; otherwise the walker can continue through several timeout cycles before noticing the cleared target.

**Why this matters:** A user pressing Ctrl+X expects the webwalker to stop issuing route actions immediately. If `InterruptedException` is swallowed, long waits in object, transport, dialogue, or animation handling can keep cycling until their normal timeout elapses.

**Pattern to follow:**

```java
try {
    Thread.sleep(delayMs);
} catch (InterruptedException ignored) {
    Thread.currentThread().interrupt();
}
while (!Thread.currentThread().isInterrupted() && !condition.getAsBoolean()) {
    sleep(pollMs);
}
```

**Where this applies:** `Global.sleep*`, `Global.sleepUntil*`, `Rs2Walker.setTarget(null)`, and any walker helper that waits after clicking a door, shortcut, transport, or minimap tile.

**Defensive check:** Start a long webwalk, press Ctrl+X during movement or a route-object wait, and verify no additional path recalculations or route-object interactions occur after the cancel log.

## 10. Post-teleport stale interim: re-anchor origin after transport handoff

After `TELEPORTATION_ITEM` or similar, sticky `interimTargetWp` can still point at a pre-teleport minimap tile (e.g. bank) while the player is near the goal. `isValidInterimPathAlignment` only checks interim vs path index, not player distance.

`finishHandledTransport` calls `applyPostTransportOriginCommit` before expected/precomputed early return: `commitWalkingOriginFromPath` from pathfinder walk path, `clearInterimCheckpoint`, `PathAheadHandlerState.clear()`. `isInterimStaleForPlayer` (player > 15 tiles from interim or plane/alignment mismatch) clears interim in segment loop, recovery click, and sticky block. `recalculatePath()` clears interim and `pathCommittedFloorIdx`.

**Defensive check:** Bank route -> teleport -> walk continues toward goal; log `transport_origin_commit`; no minimap clicks toward old bank tile.

## 13. Post-gate backward scan on fence-hugging paths

Gate and transport path vertices can stay **`isTileReachable` false** forever (you pass through them, you do not stand on them). **`getClosestTileIndex`** is global: on fence-hugging polylines the gate index often wins again after you are north, so segment loop and path-ahead fire unreachable recovery and repeat Gate Open on the same idx.

**Fix:** **`getForwardClosestTileIndex(path, minIndex)`** + **`getPathScanStartIndex`** use forward-only progress from **`pathCommittedFloorIdx`**. **`isPathIndexBehindProgress`** skips segment unreachable and recovery. After successful passage blob, **`advancePathCommittedFloor(path, focusIdx + 1)`** then **`commitWalkingOriginFromPath(..., "passage_handled")`** and **`markPassageHandled`** re-anchor floor past the gate. Interim floor advance uses forward closest from **`max(pathCommittedFloorIdx, interimTargetIdx)`**.

**Symptom:** logs still show `[Walker] unreachable ... idx=107` / `blob interact Gate` after gate passed; floor stuck at 106; `passage_handled floor=99` with gate at 107.

**Defensive check:** Lumbridge gate walk north; after Open, no repeat idx 107 unreachable; expect `passage_handled` with floor **>= gate idx + 1** (or `transport_origin_commit`).

## 11. ShortestPath FIFO target queue, Set Target N, and leg precalc

`Set Target` and `Set Target 2`+ menu entries use `Text.removeTags` matching (not raw color-tagged `entry.getTarget()`). While a leg is active (`walkTaskRunning` or `Rs2Walker.getCurrentTarget()`), new targets go to `pendingWalkTargets` (FIFO) via `enqueueWalkTarget` — **do not** replace active `triggerWalker` mid-leg. Idle first target clears the queue and starts leg 1.

`precalcLegPathAsync(from, to)` runs on a **separate** `precalcLegPathfinder` slot (never cancels the active walk pathfinder). `consumePrecalcLegIfMatches` promotes precalc when the next leg starts; else `prepareWalkerLeg` falls back to `applyWalkerDestination`.

Explicit stop (`setTriggerWalker(null, reason)`): clears queue + precalc, `clearWalkingRoute(reason)` only — no `Future.cancel(true)`. Walker exits via `isWalkCancelled`. Terminal leg clear only when `pendingWalkTargets` is empty.

**Defensive check:** Set Target A, walk, Set Target 2 and 3 while moving — legs run A then B then C; precalc overlay for B while on A. Ctrl+X clears queue + route with explicit reason.

## 12. Originless teleport before interim / minimap

Tablet, spell, home, and Leagues area teleports often have `transport.getOrigin() == null`. `isCatalogBackedTransportSegment` alone does **not** defer walking — use `TransportPathUtil.findNextOriginlessTeleportOnPath` / `Rs2Walker.hasUpcomingOriginlessTeleport`.

Before `tryDirectShortWalk`, interim checkpoint, segment minimap, and path-ahead queue: dispatch originless transport first (`tryDispatchUpcomingOriginlessTeleport`, `handleTransports` with `isAtTransportDispatchPoint` for null origin at path head). `primeExpectedTransportDestinations` must seed originless landing tiles too.

**Defensive check:** Bank -> withdraw teleport -> walk route with tablet/spell hop — first movement is teleport, not `interim-in-flight` or pre-teleport minimap clicks along bank tiles.

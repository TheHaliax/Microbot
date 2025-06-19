package net.runelite.client.plugins.microbot.kalphitequeen;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer.toggle;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.PROTECT_MAGIC;
import static net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum.PROTECT_MELEE;
import static net.runelite.client.plugins.microbot.util.walker.Rs2Walker.walkTo;

public class KalphiteQueenScript extends Script {

    public static String version = "0.0.1";

    private enum State {
        WAIT_FOR_ENTRY,
        ATTACK_GUARDIANS,
        RUN_TO_UNSTACK1,
        CHECK_WORKERS_SETUP,
        UNSTACK2,
        QUEEN_APPROACH,
        QUEEN_WALK_TO_2,
        QUEEN_WALK_TO_1,
        QUEEN_ATTACK,
        QUEEN_RETURN,
        QUEEN_VERIFY,
        UNSTACK_KQ,
        FINAL_POSITION
    }

    // IDs & key tiles
    private static final int    NPC_KG = 962, NPC_KQ = 963;
    private static final WorldPoint
            WORKER_2              = new WorldPoint(3508, 9493, 0),
            UNSTACK_WORKER_1      = new WorldPoint(3508, 9492, 0),
            UNSTACK_WORKER_2      = new WorldPoint(3504, 9496, 0),
            WORKER_SETUP_1        = new WorldPoint(3506, 9492, 0),
            WORKER_SETUP_2        = new WorldPoint(3502, 9491, 0),
            KQ_1                  = new WorldPoint(3491, 9495, 0),
            KQ_2                  = new WorldPoint(3505, 9495, 0),
            KQ_3                  = new WorldPoint(3491, 9491, 0),
            KALP_WORKER_1         = new WorldPoint(3499, 9495, 0),
            KALP_WORKER_2         = new WorldPoint(3495, 9495, 0),
            KALP_QUEEN            = new WorldPoint(3490, 9495, 0),
            UNSTACK_KQ            = new WorldPoint(3503, 9493, 0);

    private State state = State.WAIT_FOR_ENTRY;
    private boolean workersSetup = false, queenIsSetup = false;

    public boolean run(KalphiteQueenConfig config) {
        WorldPoint player = Microbot.getClient().getLocalPlayer().getWorldLocation();
        List<NPC> guardians = Microbot.getClient().getTopLevelWorldView().npcs().stream()
                .filter(n -> n.getId() == NPC_KG)
                .collect(Collectors.toList());
        Optional<NPC> queenOpt = Microbot.getClient().getTopLevelWorldView().npcs().stream()
                .map(n -> (NPC) n)
                .filter(n -> n.getId() == NPC_KQ)
                .findFirst();
        int queenDist = queenOpt
                .map(q -> q.getWorldLocation().distanceTo(player))
                .orElse(Integer.MAX_VALUE);

        switch (state) {
            case WAIT_FOR_ENTRY:
                if (player.equals(WORKER_2) && queenDist > 25) {
                    Microbot.log("✔ On Worker 2, Queen far away—engaging guardians.");
                    state = State.ATTACK_GUARDIANS;
                }
                break;

            case ATTACK_GUARDIANS:
                toggle(PROTECT_MELEE);
                guardians.forEach(g -> Rs2Npc.interact(g, "attack"));
                state = State.RUN_TO_UNSTACK1;
                break;

            case RUN_TO_UNSTACK1:
                walkTo(UNSTACK_WORKER_1);
                state = State.CHECK_WORKERS_SETUP;
                break;

            case CHECK_WORKERS_SETUP:
                Set<WorldPoint> locs = guardians.stream()
                        .map(NPC::getWorldLocation)
                        .collect(Collectors.toSet());
                workersSetup = locs.contains(WORKER_SETUP_1)
                        && locs.contains(WORKER_SETUP_2);
                state = workersSetup ? State.QUEEN_APPROACH : State.UNSTACK2;
                break;

            case UNSTACK2:
                toggle(PROTECT_MELEE);
                walkTo(UNSTACK_WORKER_2);
                sleep(2000);
                state = State.RUN_TO_UNSTACK1;
                break;

            case QUEEN_APPROACH:
                if (workersSetup && queenDist > 25) {
                    walkTo(WORKER_2);
                    state = State.QUEEN_WALK_TO_2;
                }
                break;

            case QUEEN_WALK_TO_2:
                toggle(PROTECT_MELEE);
                if (!player.equals(KQ_2)) {
                    walkTo(KQ_2);
                    break;
                }
                state = State.QUEEN_WALK_TO_1;
                break;

            case QUEEN_WALK_TO_1:
                if (!player.equals(KQ_1)) {
                    walkTo(KQ_1);
                    break;
                }
                queenOpt.ifPresent(q -> Rs2Npc.interact(q, "attack"));
                state = State.QUEEN_ATTACK;
                break;

            case QUEEN_ATTACK:
                toggle(PROTECT_MAGIC);
                state = State.QUEEN_RETURN;
                break;

            case QUEEN_RETURN:
                if (!player.equals(KQ_2)) {
                    walkTo(KQ_2);
                    break;
                }
                state = State.QUEEN_VERIFY;
                break;

            case QUEEN_VERIFY:
                Set<WorldPoint> locs2 = guardians.stream()
                        .map(NPC::getWorldLocation)
                        .collect(Collectors.toSet());
                if (queenOpt.isPresent()) {
                    WorldPoint ql = queenOpt.get().getWorldLocation();
                    if (!Rs2Npc.isMoving(queenOpt.get())
                            && locs2.contains(KALP_WORKER_1)
                            && locs2.contains(KALP_WORKER_2)
                            && ql.equals(KALP_QUEEN)) {
                        queenIsSetup = true;
                        state = State.FINAL_POSITION;
                    } else {
                        state = State.UNSTACK_KQ;
                    }
                }
                break;

            case UNSTACK_KQ:
                // If queen got stuck, retreat and retry positioning
                toggle(PROTECT_MELEE);
                walkTo(UNSTACK_KQ);
                sleep(1000);
                state = State.QUEEN_WALK_TO_2;
                break;

            case FINAL_POSITION:
                toggle(PROTECT_MAGIC);
                walkTo(KQ_3);
                queenOpt.ifPresent(q -> {
                    WorldPoint ql = q.getWorldLocation();
                    guardians.stream()
                            .filter(g -> g.getWorldLocation().distanceTo(ql) <= 2)
                            .findFirst()
                            .ifPresent(g -> walkTo(new WorldPoint(ql.getX(), ql.getY() + 2, ql.getPlane())));
                });
                break;
        }
        return true;
    }
}

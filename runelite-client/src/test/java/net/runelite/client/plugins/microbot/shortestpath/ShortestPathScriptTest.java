package net.runelite.client.plugins.microbot.shortestpath;



import net.runelite.api.coords.WorldPoint;

import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;

import org.junit.After;

import org.junit.Before;

import org.junit.Test;



import java.lang.reflect.Field;

import java.util.ArrayDeque;

import java.util.Deque;

import java.util.concurrent.atomic.AtomicBoolean;



import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertTrue;



/**

 * Package tests for ShortestPath walk orchestration (FIFO target queue, cooperative stop).

 */

public class ShortestPathScriptTest {



    private ShortestPathScript script;



    @Before

    public void setUp() {

        script = new ShortestPathScript();

        Rs2Walker.clearWalkingRoute("test:setup");

    }



    @After

    public void tearDown() throws Exception {

        if (script != null) {

            script.setTriggerWalker(null, "test:teardown");

        }

        Rs2Walker.clearWalkingRoute("test:teardown");

        setWalkTaskRunning(script, false);

        clearPendingQueue(script);

    }



    @Test

    public void setTargetWhileIdle_setsTrigger() {

        WorldPoint target = new WorldPoint(2689, 3305, 0);

        setWalkTaskRunning(script, false);

        script.setTriggerWalker(target);



        assertEquals(target, script.getTriggerWalker());

        assertTrue(script.getPendingWalkTargets().isEmpty());

    }



    @Test

    public void enqueueWhileBusy_doesNotReplaceActiveTrigger() {

        WorldPoint first = new WorldPoint(2689, 3305, 0);

        WorldPoint second = new WorldPoint(2700, 3310, 0);

        setWalkTaskRunning(script, true);

        setTriggerWalkerDirect(script, first);

        script.enqueueWalkTarget(second);



        assertEquals(first, script.getTriggerWalker());

        assertEquals(second, script.getPendingWalkTargets().peekFirst());

    }



    @Test

    public void enqueueMultiple_fifoOrder() {

        WorldPoint first = new WorldPoint(2689, 3305, 0);

        WorldPoint second = new WorldPoint(2700, 3310, 0);

        WorldPoint third = new WorldPoint(2710, 3320, 0);

        setWalkTaskRunning(script, true);

        setTriggerWalkerDirect(script, first);

        script.enqueueWalkTarget(second);

        script.enqueueWalkTarget(third);



        Deque<WorldPoint> queue = script.getPendingWalkTargets();

        assertEquals(second, queue.pollFirst());

        assertEquals(third, queue.pollFirst());

    }



    @Test

    public void stopClearsTriggerAndPending() {

        setWalkTaskRunning(script, true);

        script.setTriggerWalker(new WorldPoint(2689, 3305, 0));

        script.enqueueWalkTarget(new WorldPoint(2700, 3310, 0));

        script.setTriggerWalker(null, "hotkey:ctrl+x");



        assertNull(script.getTriggerWalker());

        assertTrue(script.getPendingWalkTargets().isEmpty());

    }



    private static void setWalkTaskRunning(ShortestPathScript script, boolean running) {

        try {

            Field field = ShortestPathScript.class.getDeclaredField("walkTaskRunning");

            field.setAccessible(true);

            ((AtomicBoolean) field.get(script)).set(running);

        } catch (ReflectiveOperationException e) {

            throw new AssertionError(e);

        }

    }



    private static void setTriggerWalkerDirect(ShortestPathScript script, WorldPoint point) {

        try {

            Field field = ShortestPathScript.class.getDeclaredField("triggerWalker");

            field.setAccessible(true);

            field.set(script, point);

        } catch (ReflectiveOperationException e) {

            throw new AssertionError(e);

        }

    }



    private static void clearPendingQueue(ShortestPathScript script) {

        script.getPendingWalkTargets().clear();

    }

}


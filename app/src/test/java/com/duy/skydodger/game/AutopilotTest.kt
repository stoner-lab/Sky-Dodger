package com.duy.skydodger.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Plays the game with a trivial autopilot.
 *
 * This is the balance safety net: if a change makes the gap unreachable, the gravity too
 * strong or the bird unsteerable, this test fails long before a human notices.
 */
class AutopilotTest {

    /** Aim at the hole of the closest pipe that still overlaps or is ahead of the bird. */
    private fun targetHeight(world: GameWorld): Float {
        val nextPipe = world.pipes
            .filter { it.right > GameConfig.BIRD_X - GameConfig.BIRD_RADIUS }
            .minByOrNull { it.x }
        return nextPipe?.gapCenter ?: (GameConfig.PLAY_HEIGHT / 2f)
    }

    @Test
    fun `a simple autopilot flies for a minute without crashing`() {
        var world = GameWorld().start()
        val random = Random(2026)
        var frames = 0

        while (frames < 60 * 60 && world.phase == Phase.Running) {
            if (world.birdY > targetHeight(world)) world = world.flap()
            world = world.update(1f / 60f, random)
            frames += 1
        }

        assertEquals(
            "autopilot crashed after $frames frames with score ${world.score}",
            Phase.Running,
            world.phase,
        )
        assertTrue("autopilot never scored", world.score >= 5)
    }
}

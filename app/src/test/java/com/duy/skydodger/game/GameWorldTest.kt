package com.duy.skydodger.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * The whole simulation is plain Kotlin, so all of it can be verified here without a device.
 */
class GameWorldTest {

    private fun running(
        birdY: Float = GameConfig.PLAY_HEIGHT / 2f,
        pipes: List<Pipe> = emptyList(),
        spawnTimer: Float = 100f,
        score: Int = 0,
        elapsed: Float = 0f,
    ) = GameWorld(
        phase = Phase.Running,
        birdY = birdY,
        pipes = pipes,
        spawnTimer = spawnTimer,
        score = score,
        elapsed = elapsed,
    )

    private fun GameWorld.step(times: Int, delta: Float = 1f / 60f) =
        (1..times).fold(this) { world, _ -> world.update(delta, Random(7)) }

    @Test
    fun `start begins a running run with a clean score`() {
        val world = GameWorld().start()

        assertEquals(Phase.Running, world.phase)
        assertEquals(0, world.score)
        assertTrue("pipes should not exist at t=0", world.pipes.isEmpty())
    }

    @Test
    fun `update does nothing when not running`() {
        val ready = GameWorld()
        val over = ready.copy(phase = Phase.GameOver, birdY = 123f)

        assertEquals(ready, ready.update(1f / 60f))
        assertEquals(over, over.update(1f / 60f))
    }

    @Test
    fun `gravity pulls the bird down`() {
        val world = GameWorld().start().step(10)

        assertTrue("bird should fall, y=${world.birdY}", world.birdY > GameConfig.PLAY_HEIGHT / 2f)
        assertTrue("bird should accelerate downwards, v=${world.velocity}", world.velocity > 0f)
    }

    @Test
    fun `flap gives an upward impulse`() {
        val world = GameWorld().start().step(10)

        assertEquals(GameConfig.FLAP_VELOCITY, world.flap().velocity, 0.001f)
    }

    @Test
    fun `bird never leaves the top of the world`() {
        val world = (1..40).fold(GameWorld().start()) { current, _ -> current.flap().update(1f / 60f, Random(1)) }

        assertTrue("y=${world.birdY}", world.birdY >= GameConfig.BIRD_RADIUS)
        assertEquals(Phase.Running, world.phase)
    }

    @Test
    fun `hitting the ground ends the run and pins the bird to the ground`() {
        val world = GameWorld().start().step(120)

        assertEquals(Phase.GameOver, world.phase)
        assertEquals(GameConfig.PLAY_HEIGHT - GameConfig.BIRD_RADIUS, world.birdY, 0.001f)
        assertEquals(0f, world.velocity, 0f)
    }

    @Test
    fun `a pipe scores once when the bird passes it`() {
        val world = running(pipes = listOf(Pipe(x = 10f, gapCenter = 100f)))

        val afterOneFrame = world.update(1f / 60f, Random(3))
        assertEquals(1, afterOneFrame.score)
        assertTrue(afterOneFrame.pipes.single().scored)

        val afterMoreFrames = afterOneFrame.step(30)
        assertEquals("a pipe may only score once", 1, afterMoreFrames.score)
    }

    @Test
    fun `a pipe in the flight path ends the run`() {
        val world = running(pipes = listOf(Pipe(x = 60f, gapCenter = 100f)))

        assertEquals(Phase.GameOver, world.update(1f / 60f, Random(3)).phase)
    }

    @Test
    fun `bird passing through the hole of a pipe survives`() {
        val pipe = Pipe(x = 60f, gapCenter = 300f)
        val world = running(birdY = 300f, pipes = listOf(pipe))

        assertEquals("holes must be flyable", Phase.Running, world.update(1f / 60f, Random(3)).phase)
    }

    @Test
    fun `a new pipe spawns after the spawn interval and starts off screen`() {
        val world = running(spawnTimer = 0.01f).step(1)

        assertEquals(1, world.pipes.size)
        assertTrue(world.pipes.single().x >= GameConfig.WORLD_WIDTH - GameConfig.PIPE_SPEED / 60f)
    }

    @Test
    fun `every generated gap stays inside the playable area`() {
        val random = Random(42)
        repeat(500) {
            val center = randomGapCenter(random)
            assertTrue("center=$center", center >= GameConfig.GAP_MIN_CENTER)
            assertTrue("center=$center", center <= GameConfig.GAP_MAX_CENTER)
            assertTrue("gap must clear the ceiling", center - GameConfig.PIPE_GAP / 2f >= 0f)
            assertTrue(
                "gap must clear the ground",
                center + GameConfig.PIPE_GAP / 2f <= GameConfig.PLAY_HEIGHT,
            )
        }
    }

    @Test
    fun `pipes speed up as the run goes on`() {
        val fresh = running().speed
        val late = running(elapsed = GameConfig.DIFFICULTY_RAMP_SECONDS * 2f).speed

        assertTrue("late=$late fresh=$fresh", late > fresh)
        assertEquals(GameConfig.PIPE_SPEED * (1f + GameConfig.MAX_SPEED_BONUS), late, 0.001f)
    }

    @Test
    fun `a huge frame delta cannot tunnel the bird through a pipe`() {
        val world = running(birdY = 100f, pipes = listOf(Pipe(x = 50f, gapCenter = 400f)))

        // One second in a single call would skip the pipe entirely without the step clamp.
        assertEquals(Phase.GameOver, world.update(1f, Random(3)).phase)
    }
}

package com.duy.skydodger.game

import kotlin.random.Random

enum class Phase {
    /** Waiting for the first tap. */
    Ready,

    /** The bird is flying and pipes are moving. */
    Running,

    /** The bird hit a pipe or the ground. */
    GameOver,
}

/**
 * One obstacle: a top block and a bottom block with a [GameConfig.PIPE_GAP] hole between them.
 *
 * @param x left edge of the pipe in world units.
 * @param gapCenter vertical centre of the hole.
 * @param scored true once the bird has passed the pipe, so a pipe can only score once.
 */
data class Pipe(
    val x: Float,
    val gapCenter: Float,
    val scored: Boolean = false,
) {
    val right: Float get() = x + GameConfig.PIPE_WIDTH
    val gapTop: Float get() = gapCenter - GameConfig.PIPE_GAP / 2f
    val gapBottom: Float get() = gapCenter + GameConfig.PIPE_GAP / 2f

    /** Circle vs. two axis-aligned blocks, tested without allocating. */
    fun hits(circleX: Float, circleY: Float, radius: Float): Boolean =
        hitsBlock(circleX, circleY, radius, x, 0f, right, gapTop) ||
            hitsBlock(circleX, circleY, radius, x, gapBottom, right, GameConfig.PLAY_HEIGHT)

    private fun hitsBlock(
        circleX: Float,
        circleY: Float,
        radius: Float,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Boolean {
        val nearestX = circleX.coerceIn(left, right)
        val nearestY = circleY.coerceIn(top, bottom)
        val dx = circleX - nearestX
        val dy = circleY - nearestY
        return dx * dx + dy * dy <= radius * radius
    }
}

/**
 * Immutable snapshot of the whole game. Everything that changes during a run lives here,
 * so a single [update] call is a pure function from one snapshot to the next.
 */
data class GameWorld(
    val phase: Phase = Phase.Ready,
    val birdY: Float = GameConfig.PLAY_HEIGHT / 2f,
    val velocity: Float = 0f,
    val pipes: List<Pipe> = emptyList(),
    val spawnTimer: Float = GameConfig.PIPE_SPAWN_INTERVAL,
    val score: Int = 0,
    val elapsed: Float = 0f,
) {

    /** Current pipe speed, slowly increasing with [elapsed]. */
    val speed: Float
        get() {
            val ramp = (elapsed / GameConfig.DIFFICULTY_RAMP_SECONDS).coerceIn(0f, 1f)
            return GameConfig.PIPE_SPEED * (1f + ramp * GameConfig.MAX_SPEED_BONUS)
        }

    /** Begins a fresh run from this (idle) snapshot. */
    fun start(): GameWorld = GameWorld(
        phase = Phase.Running,
        spawnTimer = GameConfig.PIPE_SPAWN_INTERVAL * 0.6f,
    )

    /** A tap in the air: give the bird an upward impulse. Ignored unless running. */
    fun flap(): GameWorld =
        if (phase == Phase.Running) copy(velocity = GameConfig.FLAP_VELOCITY) else this

    /**
     * Advances the simulation by [deltaSeconds] and returns the next snapshot.
     * Returns the same instance when the game is not running.
     */
    fun update(deltaSeconds: Float, random: Random = Random.Default): GameWorld {
        if (phase != Phase.Running) return this
        val step = deltaSeconds.coerceIn(0f, GameConfig.MAX_STEP_SECONDS)
        if (step == 0f) return this

        var nextVelocity = (velocity + GameConfig.GRAVITY * step).coerceAtMost(GameConfig.MAX_FALL_SPEED)
        var nextBirdY = birdY + nextVelocity * step
        if (nextBirdY - GameConfig.BIRD_RADIUS < 0f) {
            nextBirdY = GameConfig.BIRD_RADIUS
            nextVelocity = 0f
        }

        val movedPipes = pipes
            .map { it.copy(x = it.x - speed * step) }
            .filter { it.right > -GameConfig.PIPE_WIDTH }

        var nextScore = score
        val scoredPipes = movedPipes.map { pipe ->
            val passed = pipe.right < GameConfig.BIRD_X - GameConfig.BIRD_RADIUS
            if (!pipe.scored && passed) {
                nextScore += 1
                pipe.copy(scored = true)
            } else {
                pipe
            }
        }

        var nextSpawnTimer = spawnTimer - step
        val nextPipes = if (nextSpawnTimer <= 0f) {
            nextSpawnTimer += GameConfig.PIPE_SPAWN_INTERVAL
            scoredPipes + Pipe(x = GameConfig.WORLD_WIDTH, gapCenter = randomGapCenter(random))
        } else {
            scoredPipes
        }

        val hitGround = nextBirdY + GameConfig.BIRD_RADIUS >= GameConfig.PLAY_HEIGHT
        val hitPipe = nextPipes.any { it.hits(GameConfig.BIRD_X, nextBirdY, GameConfig.BIRD_RADIUS) }
        val nextPhase = if (hitGround || hitPipe) Phase.GameOver else Phase.Running

        return copy(
            phase = nextPhase,
            birdY = if (hitGround) GameConfig.PLAY_HEIGHT - GameConfig.BIRD_RADIUS else nextBirdY,
            velocity = if (nextPhase == Phase.GameOver) 0f else nextVelocity,
            pipes = nextPipes,
            spawnTimer = nextSpawnTimer,
            score = nextScore,
            elapsed = elapsed + step,
        )
    }
}

/** A vertical position for a new pipe hole, kept away from the ceiling and the ground. */
fun randomGapCenter(random: Random): Float {
    val span = GameConfig.GAP_MAX_CENTER - GameConfig.GAP_MIN_CENTER
    return GameConfig.GAP_MIN_CENTER + random.nextFloat() * span
}

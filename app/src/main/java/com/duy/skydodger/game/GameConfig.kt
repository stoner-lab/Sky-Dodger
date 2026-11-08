package com.duy.skydodger.game

/**
 * Tunable constants for the game.
 *
 * All values are in "world units": the game is simulated inside a fixed
 * [WORLD_WIDTH] x [WORLD_HEIGHT] rectangle which is scaled to the device screen
 * when drawn. Because the simulation never touches the screen size, gameplay is
 * identical on every device and the physics can be unit tested on the JVM.
 */
object GameConfig {

    const val WORLD_WIDTH = 360f
    const val WORLD_HEIGHT = 640f
    const val GROUND_HEIGHT = 96f
    const val PLAY_HEIGHT = WORLD_HEIGHT - GROUND_HEIGHT

    const val GRAVITY = 1500f
    const val FLAP_VELOCITY = -470f
    const val MAX_FALL_SPEED = 900f

    const val BIRD_X = 96f
    const val BIRD_RADIUS = 15f

    const val PIPE_WIDTH = 68f
    const val PIPE_GAP = 190f
    const val PIPE_SPEED = 165f
    const val PIPE_SPAWN_INTERVAL = 1.55f
    const val PIPE_EDGE_MARGIN = 60f

    const val GAP_MIN_CENTER = PIPE_EDGE_MARGIN + PIPE_GAP / 2f
    const val GAP_MAX_CENTER = PLAY_HEIGHT - PIPE_EDGE_MARGIN - PIPE_GAP / 2f

    /** Longest simulated step. Protects against tunnelling through pipes after a stall. */
    const val MAX_STEP_SECONDS = 1f / 30f

    /** Taps are ignored for this long after a run ends, so a panicky tap cannot restart it. */
    const val RESTART_DELAY_SECONDS = 0.45f

    /** Time it takes the pipe speed to reach its maximum bonus. */
    const val DIFFICULTY_RAMP_SECONDS = 45f

    /** Extra speed at the end of the ramp, as a fraction of [PIPE_SPEED]. */
    const val MAX_SPEED_BONUS = 0.55f
}

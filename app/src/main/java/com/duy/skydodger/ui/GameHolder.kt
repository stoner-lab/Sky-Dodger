package com.duy.skydodger.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.duy.skydodger.data.ScoreStore
import com.duy.skydodger.game.GameConfig
import com.duy.skydodger.game.GameWorld
import com.duy.skydodger.game.Phase
import kotlin.random.Random

/**
 * Bridges the pure simulation in [GameWorld] to Compose.
 *
 * Split of responsibilities on purpose:
 *  - [world] is a plain field and is only read inside the canvas draw lambda, so a new
 *    frame redraws the canvas without recomposing the screen;
 *  - [clock] is a snapshot value read in the same draw lambda. It invalidates the draw
 *    phase every frame and doubles as the time source for idle animation;
 *  - [score], [phase] and [best] are snapshot values read by composables and change rarely.
 */
class GameHolder(
    private val scoreStore: ScoreStore,
    private val random: Random = Random.Default,
) {

    var world: GameWorld = GameWorld()
        private set

    var clock by mutableFloatStateOf(0f)
        private set

    var score by mutableIntStateOf(0)
        private set

    var phase by mutableStateOf(Phase.Ready)
        private set

    var best by mutableIntStateOf(scoreStore.bestScore())
        private set

    /** True when the run that just ended set a new personal best. */
    var isNewBest by mutableStateOf(false)
        private set

    private var restartGuard = 0f

    /** Called once per display frame. */
    fun onFrame(deltaSeconds: Float) {
        val step = deltaSeconds.coerceIn(0f, 0.05f)
        clock += step

        when (phase) {
            Phase.Running -> {
                world = world.update(step, random)
                if (world.score != score) score = world.score
                if (world.phase != phase) finishRun()
            }

            Phase.GameOver -> {
                if (restartGuard > 0f) restartGuard = (restartGuard - step).coerceAtLeast(0f)
            }

            Phase.Ready -> Unit
        }
    }

    /** A tap anywhere on the screen: start, flap, or restart. */
    fun onTap() {
        when (phase) {
            Phase.Ready -> beginRun(GameWorld().start())
            Phase.Running -> world = world.flap()
            Phase.GameOver -> if (restartGuard <= 0f) beginRun(GameWorld().start())
        }
    }

    private fun beginRun(next: GameWorld) {
        world = next.flap()
        phase = Phase.Running
        score = 0
        isNewBest = false
        restartGuard = 0f
    }

    private fun finishRun() {
        phase = Phase.GameOver
        isNewBest = score > scoreStore.bestScore()
        best = scoreStore.submit(score)
        restartGuard = GameConfig.RESTART_DELAY_SECONDS
    }
}

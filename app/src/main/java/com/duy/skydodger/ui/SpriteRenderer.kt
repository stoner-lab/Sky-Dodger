package com.duy.skydodger.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.duy.skydodger.game.GameConfig
import com.duy.skydodger.game.GameWorld
import com.duy.skydodger.game.Phase
import kotlin.math.sin

fun DrawScope.drawPipes(world: GameWorld) {
    world.pipes.forEach { pipe ->
        drawPipeBody(pipe.x, top = 0f, bottom = pipe.gapTop)
        drawPipeCap(pipe.x, edge = pipe.gapTop, onTopBlock = true)

        drawPipeBody(pipe.x, top = pipe.gapBottom, bottom = GameConfig.PLAY_HEIGHT)
        drawPipeCap(pipe.x, edge = pipe.gapBottom, onTopBlock = false)
    }
}

private fun DrawScope.drawPipeBody(x: Float, top: Float, bottom: Float) {
    val height = bottom - top
    if (height <= 0f) return
    val width = GameConfig.PIPE_WIDTH
    drawRoundRect(
        brush = pipeBrush(x, width),
        topLeft = Offset(x, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f),
    )
}

private fun DrawScope.drawPipeCap(x: Float, edge: Float, onTopBlock: Boolean) {
    val capWidth = GameConfig.PIPE_WIDTH + 16f
    val capHeight = 24f
    val top = if (onTopBlock) edge - capHeight else edge
    drawRoundRect(
        brush = pipeBrush(x - 8f, capWidth),
        topLeft = Offset(x - 8f, top),
        size = Size(capWidth, capHeight),
        cornerRadius = CornerRadius(10f, 10f),
    )
}

private fun pipeBrush(x: Float, width: Float) = Brush.horizontalGradient(
    0f to GameColors.PipeDark,
    0.35f to GameColors.PipeMid,
    0.5f to GameColors.PipeLight,
    0.65f to GameColors.PipeMid,
    1f to GameColors.PipeDark,
    startX = x,
    endX = x + width,
)

fun DrawScope.drawGround(world: GameWorld) {
    val groundTop = GameConfig.PLAY_HEIGHT
    drawRect(
        color = GameColors.Ground,
        topLeft = Offset(0f, groundTop),
        size = Size(GameConfig.WORLD_WIDTH, GameConfig.GROUND_HEIGHT),
    )
    drawRect(
        color = GameColors.GroundEdge,
        topLeft = Offset(0f, groundTop),
        size = Size(GameConfig.WORLD_WIDTH, 4f),
    )

    // Stripes scroll with the world speed so the ground reads as moving.
    val stripe = 28f
    val scroll = (world.elapsed * world.speed) % (stripe * 2f)
    var x = -stripe * 2f - scroll
    while (x < GameConfig.WORLD_WIDTH) {
        drawRect(
            color = GameColors.GroundStripe,
            topLeft = Offset(x, groundTop + 12f),
            size = Size(stripe, 7f),
        )
        x += stripe * 2f
    }
}

fun DrawScope.drawBird(world: GameWorld, clock: Float) {
    val bob = if (world.phase == Phase.Running) 0f else sin(clock * 3f) * 4f
    val center = Offset(GameConfig.BIRD_X, world.birdY + bob)
    val radius = GameConfig.BIRD_RADIUS
    val tilt = (world.velocity / GameConfig.MAX_FALL_SPEED).coerceIn(-1f, 1f) * 40f

    rotate(degrees = tilt, pivot = center) {
        drawCircle(GameColors.BirdBody, radius = radius, center = center)
        drawCircle(
            GameColors.BirdBelly,
            radius = radius * 0.72f,
            center = center + Offset(-radius * 0.18f, radius * 0.28f),
        )

        val eyeCenter = center + Offset(radius * 0.38f, -radius * 0.32f)
        drawCircle(GameColors.BirdEyeWhite, radius = radius * 0.3f, center = eyeCenter)
        drawCircle(
            GameColors.BirdEye,
            radius = radius * 0.15f,
            center = eyeCenter + Offset(radius * 0.1f, 0f),
        )

        val beak = Path().apply {
            moveTo(center.x + radius * 0.72f, center.y - radius * 0.05f)
            lineTo(center.x + radius * 1.62f, center.y + radius * 0.32f)
            lineTo(center.x + radius * 0.72f, center.y + radius * 0.66f)
            close()
        }
        drawPath(beak, GameColors.BirdBeak)

        val wingLift = sin(clock * 10f) * radius * 0.4f
        drawCircle(
            GameColors.BirdWing,
            radius = radius * 0.52f,
            center = center + Offset(-radius * 0.3f, wingLift),
        )
    }
}

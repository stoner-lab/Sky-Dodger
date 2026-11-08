package com.duy.skydodger.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.duy.skydodger.game.GameConfig
import kotlin.math.min
import kotlin.math.sin

/** Scale and offset used to fit the fixed world rectangle into the real canvas. */
data class WorldViewport(val scale: Float, val offset: Offset)

fun DrawScope.worldViewport(): WorldViewport {
    val scale = min(
        size.width / GameConfig.WORLD_WIDTH,
        size.height / GameConfig.WORLD_HEIGHT,
    )
    return WorldViewport(
        scale = scale,
        offset = Offset(
            x = (size.width - GameConfig.WORLD_WIDTH * scale) / 2f,
            y = (size.height - GameConfig.WORLD_HEIGHT * scale) / 2f,
        ),
    )
}

/** Full-bleed gradient sky. Drawn without the world transform so no letterbox strip shows. */
fun DrawScope.drawSky() {
    drawRect(
        brush = Brush.verticalGradient(
            0f to GameColors.SkyTop,
            0.55f to GameColors.SkyMid,
            0.85f to GameColors.SkyLow,
            1f to GameColors.Horizon,
        ),
    )
}

/**
 * The world keeps a fixed aspect ratio, so on tall screens a strip is left over at the
 * bottom. Without this the pale end of the sky gradient shows up under the ground.
 */
fun DrawScope.drawLetterbox(viewport: WorldViewport) {
    val worldBottom = viewport.offset.y + GameConfig.WORLD_HEIGHT * viewport.scale
    if (worldBottom < size.height) {
        drawRect(
            color = GameColors.Ground,
            topLeft = Offset(0f, worldBottom),
            size = Size(size.width, size.height - worldBottom),
        )
    }
}

fun DrawScope.drawClouds(clock: Float) {
    val span = GameConfig.WORLD_WIDTH + 180f
    CLOUDS.forEach { cloud ->
        val wrapped = (cloud.x - clock * 9f * cloud.scale) % span
        val x = (if (wrapped < 0f) wrapped + span else wrapped) - 90f
        drawCloud(x, cloud.y, cloud.radius * cloud.scale)
    }
}

private fun DrawScope.drawCloud(x: Float, y: Float, radius: Float) {
    drawCircle(GameColors.Cloud, radius = radius, center = Offset(x, y))
    drawCircle(
        GameColors.Cloud,
        radius = radius * 0.72f,
        center = Offset(x + radius * 0.95f, y + radius * 0.28f),
    )
    drawCircle(
        GameColors.Cloud,
        radius = radius * 0.58f,
        center = Offset(x - radius * 0.95f, y + radius * 0.3f),
    )
}

fun DrawScope.drawHills(clock: Float) {
    drawHillLayer(clock, baseOffset = 0f, amplitude = 26f, wavelength = 52f, color = GameColors.HillFar)
    drawHillLayer(clock, baseOffset = 26f, amplitude = 18f, wavelength = 38f, color = GameColors.HillNear)
}

private fun DrawScope.drawHillLayer(
    clock: Float,
    baseOffset: Float,
    amplitude: Float,
    wavelength: Float,
    color: Color,
) {
    val groundTop = GameConfig.PLAY_HEIGHT
    val drift = (clock * 18f) % (wavelength * 4f)
    val path = Path().apply {
        moveTo(0f, groundTop)
        var x = -wavelength * 4f
        while (x <= GameConfig.WORLD_WIDTH + wavelength) {
            lineTo(x, groundTop - baseOffset - amplitude - amplitude * sin((x + drift) / wavelength))
            x += 6f
        }
        lineTo(GameConfig.WORLD_WIDTH, groundTop)
        close()
    }
    drawPath(path, color)
}

private data class Cloud(val x: Float, val y: Float, val radius: Float, val scale: Float)

private val CLOUDS = listOf(
    Cloud(x = 40f, y = 96f, radius = 26f, scale = 1f),
    Cloud(x = 190f, y = 62f, radius = 20f, scale = 0.8f),
    Cloud(x = 300f, y = 132f, radius = 30f, scale = 1.15f),
    Cloud(x = 130f, y = 186f, radius = 16f, scale = 0.6f),
    Cloud(x = 250f, y = 226f, radius = 18f, scale = 0.7f),
)

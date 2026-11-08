package com.duy.skydodger.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.duy.skydodger.data.ScoreStore

@Composable
fun rememberGameHolder(): GameHolder {
    val context = LocalContext.current
    return remember(context) { GameHolder(ScoreStore(context)) }
}

@Composable
fun GameScreen(holder: GameHolder) {
    // The only clock in the app: one simulation step per display frame.
    LaunchedEffect(holder) {
        var previous = 0L
        while (true) {
            withFrameNanos { now ->
                if (previous != 0L) holder.onFrame((now - previous) / 1_000_000_000f)
                previous = now
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(holder) { detectTapGestures { holder.onTap() } },
        ) {
            // Reading the snapshot clock here subscribes only the draw phase, so the
            // composables above are not recomposed on every frame.
            val clock = holder.clock
            val world = holder.world

            drawSky()

            val viewport = worldViewport()
            withTransform({
                translate(viewport.offset.x, viewport.offset.y)
                scale(viewport.scale, viewport.scale, pivot = Offset.Zero)
            }) {
                drawClouds(clock)
                drawHills(clock)
                drawPipes(world)
                drawGround(world)
                drawBird(world, clock)
            }
            drawLetterbox(viewport)
        }

        GameHud(
            holder = holder,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
        )
    }
}

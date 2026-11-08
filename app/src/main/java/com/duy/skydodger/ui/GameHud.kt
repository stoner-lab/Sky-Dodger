package com.duy.skydodger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duy.skydodger.game.Phase

@Composable
fun GameHud(holder: GameHolder, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = holder.score.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                text = "BEST ${holder.best}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = GameColors.Horizon,
            )
        }

        when (holder.phase) {
            Phase.Ready -> ReadyPanel(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 150.dp),
            )
            Phase.GameOver -> GameOverPanel(
                score = holder.score,
                best = holder.best,
                isNewBest = holder.isNewBest,
                modifier = Modifier.align(Alignment.Center),
            )

            Phase.Running -> Unit
        }
    }
}

@Composable
private fun ReadyPanel(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "SKY DODGER",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(50), color = GameColors.SkyTop.copy(alpha = 0.45f)) {
            Text(
                text = "Tap to fly",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Every pipe you pass scores a point.",
            style = MaterialTheme.typography.bodyMedium,
            color = GameColors.Horizon,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun GameOverPanel(
    score: Int,
    best: Int,
    isNewBest: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = GameColors.SkyTop.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "GAME OVER",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Spacer(Modifier.height(20.dp))
            StatRow(label = "Score", value = score)
            Spacer(Modifier.height(8.dp))
            StatRow(label = "Best", value = best)
            if (isNewBest) {
                Spacer(Modifier.height(14.dp))
                Surface(shape = RoundedCornerShape(50), color = GameColors.BirdBody) {
                    Text(
                        text = "NEW BEST",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF12213B),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Tap to try again",
                style = MaterialTheme.typography.bodyMedium,
                color = GameColors.Horizon,
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = GameColors.Horizon,
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = GameColors.BirdBody,
        )
    }
}

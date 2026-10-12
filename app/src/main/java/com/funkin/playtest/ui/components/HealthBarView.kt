package com.funkin.playtest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfRed

@Composable
fun HealthBarView(
    health: Float, // 0.0 to 1.0 (0 = player dead, 1 = player max)
    currentBeat: Int,
    opponentName: String,
    playerName: String = "BF",
    modifier: Modifier = Modifier
) {
    val animatedHealth by animateFloatAsState(
        targetValue = health.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 500f),
        label = "health_anim"
    )

    // Beat bop scale for character icons
    val isBeatEven = currentBeat % 2 == 0
    val iconScale = if (isBeatEven) 1.15f else 1.0f

    val isPlayerInDanger = health < 0.2f
    val isOpponentInDanger = health > 0.8f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 24.dp)
            .testTag("health_bar_container"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background track (Black border, Opponent red on left, Player green/cyan on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(7.dp))
                .background(Color.Black)
        ) {
            // Opponent portion (1.0 - health)
            val oppWeight = (1f - animatedHealth).coerceIn(0.01f, 0.99f)
            val plyWeight = animatedHealth.coerceIn(0.01f, 0.99f)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(oppWeight)
                    .background(FnfRed)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(plyWeight)
                    .background(Color(0xFF31B0D5)) // FNF Player Cyan
            )
        }

        // Opponent and Player Head Icons placed at the split position
        val oppIconText = when {
            opponentName.contains("Daddy", true) -> "😈"
            opponentName.contains("Skid", true) -> "🎃"
            opponentName.contains("Pico", true) -> "🔫"
            opponentName.contains("Mom", true) -> "👠"
            opponentName.contains("Tank", true) -> "🪖"
            opponentName.contains("Girlfriend", true) -> "💖"
            else -> "🎤"
        }

        val playerIconText = if (isPlayerInDanger) "😵" else "🧢"

        // Center split icon indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        ) {
            // Floating head icons positioned relative to health
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        // Offset from center based on health (health=0.5 is centered)
                        val offsetPx = ((0.5f - animatedHealth) * 300).toInt()
                        IntOffset(offsetPx, 0)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Opponent icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(if (isOpponentInDanger) FnfRed.copy(alpha = 0.5f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOpponentInDanger) "💀" else oppIconText,
                        fontSize = 20.sp
                    )
                }

                // Player BF icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .scale(iconScale)
                        .clip(CircleShape)
                        .background(if (isPlayerInDanger) Color.Red.copy(alpha = 0.5f) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = playerIconText,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

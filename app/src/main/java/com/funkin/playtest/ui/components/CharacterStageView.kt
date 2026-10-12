package com.funkin.playtest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.model.NoteDirection
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfPurple
import com.funkin.playtest.ui.theme.FnfRed

@Composable
fun CharacterStageView(
    opponentName: String,
    playerName: String,
    playerSingingDir: NoteDirection?,
    opponentSingingDir: NoteDirection?,
    playerMissedDir: NoteDirection?,
    currentBeat: Int,
    modifier: Modifier = Modifier
) {
    // Beat bop
    val isBeatEven = currentBeat % 2 == 0
    val bopScale = if (isBeatEven) 1.06f else 1.0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F081C),
                        Color(0xFF20103A)
                    )
                )
            )
            .border(width = 1.dp, color = Color(0xFF33205A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Opponent Stage Actor
            StageCharacter(
                name = opponentName,
                singingDir = opponentSingingDir,
                isMissed = false,
                bopScale = bopScale,
                isPlayer = false
            )

            // Center GF on Speakers
            GirlfriendCenter(bopScale = bopScale)

            // Player BF Stage Actor
            StageCharacter(
                name = playerName,
                singingDir = playerSingingDir,
                isMissed = playerMissedDir != null,
                bopScale = bopScale,
                isPlayer = true
            )
        }
    }
}

@Composable
private fun StageCharacter(
    name: String,
    singingDir: NoteDirection?,
    isMissed: Boolean,
    bopScale: Float,
    isPlayer: Boolean
) {
    val dirOffset = when (singingDir) {
        NoteDirection.LEFT -> IntOffset(-16, 0)
        NoteDirection.RIGHT -> IntOffset(16, 0)
        NoteDirection.UP -> IntOffset(0, -16)
        NoteDirection.DOWN -> IntOffset(0, 12)
        null -> IntOffset(0, 0)
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (singingDir != null) 1.15f else bopScale,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
        label = "char_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .offset { dirOffset }
                .size(62.dp)
                .scale(animatedScale)
                .clip(CircleShape)
                .background(
                    if (isMissed) Color(0xFF6A1A24)
                    else if (singingDir != null) singingDir.color.copy(alpha = 0.4f)
                    else if (isPlayer) FnfCyan.copy(alpha = 0.2f)
                    else FnfPurple.copy(alpha = 0.2f)
                )
                .border(
                    width = 2.dp,
                    color = if (isMissed) FnfRed else (singingDir?.color ?: if (isPlayer) FnfCyan else FnfPink),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            val emoji = when {
                isMissed -> "⚡"
                singingDir != null -> when (singingDir) {
                    NoteDirection.LEFT -> "👈"
                    NoteDirection.DOWN -> "👇"
                    NoteDirection.UP -> "☝️"
                    NoteDirection.RIGHT -> "👉"
                }
                isPlayer -> "🧢"
                name.contains("Daddy", true) -> "😈"
                name.contains("Skid", true) -> "🎃"
                name.contains("Pico", true) -> "🔫"
                name.contains("Mom", true) -> "👠"
                name.contains("Tank", true) -> "🪖"
                else -> "👾"
            }
            Text(text = emoji, fontSize = 28.sp)
        }

        Text(
            text = name,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (isPlayer) FnfCyan else FnfPink,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun GirlfriendCenter(bopScale: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(bopScale)
                .clip(CircleShape)
                .background(Color(0xFF881144).copy(alpha = 0.35f))
                .border(1.5.dp, Color(0xFFFF3377), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "💖", fontSize = 22.sp)
        }
        Text(
            text = "GF",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color(0xFFFF5599),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

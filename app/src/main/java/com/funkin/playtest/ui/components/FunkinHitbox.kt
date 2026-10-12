package com.funkin.playtest.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.model.NoteDirection

@Composable
fun FunkinHitboxOverlay(
    pressedDirections: Set<NoteDirection>,
    opacity: Float,
    onNoteDown: (NoteDirection) -> Unit,
    onNoteUp: (NoteDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .testTag("funkin_hitbox_overlay")
    ) {
        NoteDirection.entries.forEach { dir ->
            val isPressed = pressedDirections.contains(dir)

            HitboxColumn(
                direction = dir,
                isPressed = isPressed,
                opacity = opacity,
                onDown = { onNoteDown(dir) },
                onUp = { onNoteUp(dir) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun HitboxColumn(
    direction: NoteDirection,
    isPressed: Boolean,
    opacity: Float,
    onDown: () -> Unit,
    onUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnDown by rememberUpdatedState(onDown)
    val currentOnUp by rememberUpdatedState(onUp)

    val glowColor by animateColorAsState(
        targetValue = if (isPressed) direction.color.copy(alpha = opacity) else Color.Transparent,
        animationSpec = tween(durationMillis = 60),
        label = "hitbox_glow"
    )

    val gradient = Brush.verticalGradient(
        colors = listOf(
            glowColor.copy(alpha = if (isPressed) opacity * 0.2f else 0.02f),
            glowColor.copy(alpha = if (isPressed) opacity else 0.05f)
        )
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isPressed) 2.5.dp else 1.dp,
                color = if (isPressed) direction.color else direction.color.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(gradient)
            .pointerInput(direction) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    currentOnDown()
                    val up = waitForUpOrCancellation()
                    currentOnUp()
                }
            }
            .testTag("hitbox_${direction.name.lowercase()}"),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Label at bottom
        Text(
            text = direction.label.uppercase(),
            color = if (isPressed) Color.White else direction.color.copy(alpha = 0.6f),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .alpha(opacity.coerceAtLeast(0.4f))
        )
    }
}

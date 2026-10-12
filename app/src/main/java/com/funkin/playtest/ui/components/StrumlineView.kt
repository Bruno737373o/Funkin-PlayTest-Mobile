package com.funkin.playtest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.funkin.playtest.model.GameNote
import com.funkin.playtest.model.NoteDirection
import com.funkin.playtest.ui.theme.NoteCyan
import com.funkin.playtest.ui.theme.NoteGreen
import com.funkin.playtest.ui.theme.NotePurple
import com.funkin.playtest.ui.theme.NoteRed

@Composable
fun StrumlineView(
    playerNotes: List<GameNote>,
    opponentNotes: List<GameNote>,
    songPositionMs: Long,
    pressedDirections: Set<NoteDirection>,
    opponentPressedDirections: Set<NoteDirection>,
    downscroll: Boolean,
    noteSpeed: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("strumline_view")
    ) {
        // Layout: Left half is Opponent strumline, Right half is Player strumline
        Row(modifier = Modifier.fillMaxSize()) {
            // Opponent 4-note lane (Left, Down, Up, Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = if (downscroll) Alignment.BottomCenter else Alignment.TopCenter
            ) {
                StrumLaneGroup(
                    isPlayer = false,
                    notes = opponentNotes,
                    songPositionMs = songPositionMs,
                    pressedDirs = opponentPressedDirections,
                    downscroll = downscroll,
                    noteSpeed = noteSpeed
                )
            }

            // Player 4-note lane (Left, Down, Up, Right)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = if (downscroll) Alignment.BottomCenter else Alignment.TopCenter
            ) {
                StrumLaneGroup(
                    isPlayer = true,
                    notes = playerNotes,
                    songPositionMs = songPositionMs,
                    pressedDirs = pressedDirections,
                    downscroll = downscroll,
                    noteSpeed = noteSpeed
                )
            }
        }
    }
}

@Composable
private fun StrumLaneGroup(
    isPlayer: Boolean,
    notes: List<GameNote>,
    songPositionMs: Long,
    pressedDirs: Set<NoteDirection>,
    downscroll: Boolean,
    noteSpeed: Float
) {
    val directions = NoteDirection.entries

    Box(modifier = Modifier.fillMaxSize()) {
        // Receptors Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(if (downscroll) Alignment.BottomCenter else Alignment.TopCenter)
                .height(64.dp)
        ) {
            directions.forEach { dir ->
                val isPressed = pressedDirs.contains(dir)
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 1.22f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 800f),
                    label = "receptor_scale"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(46.dp)
                            .scale(scale)
                    ) {
                        drawReceptorArrow(dir, isPressed, isPlayer)
                    }
                }
            }
        }

        // Active Moving Notes Canvas
        val speedFactor = 0.45f * noteSpeed // px per ms
        Canvas(modifier = Modifier.fillMaxSize()) {
            val laneWidth = size.width / 4f
            val receptorY = if (downscroll) size.height - 32.dp.toPx() else 32.dp.toPx()

            notes.forEach { note ->
                if (!note.isHit && !note.isMissed) {
                    val timeDiff = note.strumTime - songPositionMs
                    // Visual position calculation
                    val noteY = if (downscroll) {
                        receptorY - (timeDiff * speedFactor)
                    } else {
                        receptorY + (timeDiff * speedFactor)
                    }

                    // Only draw if within viewport bounds
                    if (noteY in -60f..(size.height + 60f)) {
                        val colIndex = note.direction.index
                        val noteX = (colIndex * laneWidth) + (laneWidth / 2f)

                        // Draw note arrow
                        drawNoteArrow(
                            center = Offset(noteX, noteY),
                            direction = note.direction,
                            arrowSize = 42.dp.toPx()
                        )

                        // Draw sustain tail if any
                        if (note.length > 50L) {
                            val sustainTailLength = note.length * speedFactor
                            val tailStartY = if (downscroll) noteY - 10f else noteY + 10f
                            val tailEndY = if (downscroll) noteY - sustainTailLength else noteY + sustainTailLength
                            drawLine(
                                color = note.direction.color.copy(alpha = 0.6f),
                                start = Offset(noteX, tailStartY),
                                end = Offset(noteX, tailEndY),
                                strokeWidth = 14f
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawReceptorArrow(dir: NoteDirection, isPressed: Boolean, isPlayer: Boolean) {
    val arrowSize = size.minDimension * 0.88f
    val center = Offset(size.width / 2f, size.height / 2f)
    val color = if (isPressed) dir.color else Color.White.copy(alpha = if (isPlayer) 0.6f else 0.4f)

    // Base outline receptor
    drawArrowPath(center, dir, arrowSize, color, fill = isPressed, strokeWidth = 5f)
}

private fun DrawScope.drawNoteArrow(center: Offset, direction: NoteDirection, arrowSize: Float) {
    // Glow behind note
    drawCircle(
        color = direction.color.copy(alpha = 0.25f),
        radius = arrowSize * 0.65f,
        center = center
    )
    // Filled arrow
    drawArrowPath(center, direction, arrowSize, direction.color, fill = true, strokeWidth = 4f)
    // Black crisp border
    drawArrowPath(center, direction, arrowSize, Color.Black, fill = false, strokeWidth = 3f)
}

private fun DrawScope.drawArrowPath(
    center: Offset,
    direction: NoteDirection,
    arrowSize: Float,
    color: Color,
    fill: Boolean,
    strokeWidth: Float
) {
    val half = arrowSize / 2f
    val path = Path()

    // Base arrow points pointing UP, then rotated based on direction
    when (direction) {
        NoteDirection.UP -> {
            path.moveTo(center.x, center.y - half)
            path.lineTo(center.x + half, center.y + half * 0.4f)
            path.lineTo(center.x + half * 0.4f, center.y + half * 0.4f)
            path.lineTo(center.x + half * 0.4f, center.y + half)
            path.lineTo(center.x - half * 0.4f, center.y + half)
            path.lineTo(center.x - half * 0.4f, center.y + half * 0.4f)
            path.lineTo(center.x - half, center.y + half * 0.4f)
            path.close()
        }
        NoteDirection.DOWN -> {
            path.moveTo(center.x, center.y + half)
            path.lineTo(center.x + half, center.y - half * 0.4f)
            path.lineTo(center.x + half * 0.4f, center.y - half * 0.4f)
            path.lineTo(center.x + half * 0.4f, center.y - half)
            path.lineTo(center.x - half * 0.4f, center.y - half)
            path.lineTo(center.x - half * 0.4f, center.y - half * 0.4f)
            path.lineTo(center.x - half, center.y - half * 0.4f)
            path.close()
        }
        NoteDirection.LEFT -> {
            path.moveTo(center.x - half, center.y)
            path.lineTo(center.x + half * 0.4f, center.y - half)
            path.lineTo(center.x + half * 0.4f, center.y - half * 0.4f)
            path.lineTo(center.x + half, center.y - half * 0.4f)
            path.lineTo(center.x + half, center.y + half * 0.4f)
            path.lineTo(center.x + half * 0.4f, center.y + half * 0.4f)
            path.lineTo(center.x + half * 0.4f, center.y + half)
            path.close()
        }
        NoteDirection.RIGHT -> {
            path.moveTo(center.x + half, center.y)
            path.lineTo(center.x - half * 0.4f, center.y - half)
            path.lineTo(center.x - half * 0.4f, center.y - half * 0.4f)
            path.lineTo(center.x - half, center.y - half * 0.4f)
            path.lineTo(center.x - half, center.y + half * 0.4f)
            path.lineTo(center.x - half * 0.4f, center.y + half * 0.4f)
            path.lineTo(center.x - half * 0.4f, center.y + half)
            path.close()
        }
    }

    if (fill) {
        drawPath(path = path, color = color, style = Fill)
    } else {
        drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
    }
}

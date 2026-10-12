package com.funkin.playtest.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.model.NoteDirection
import com.funkin.playtest.ui.components.CharacterStageView
import com.funkin.playtest.ui.components.FunkinHitboxOverlay
import com.funkin.playtest.ui.components.HealthBarView
import com.funkin.playtest.ui.components.StrumlineView
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfGreen
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfRed
import com.funkin.playtest.ui.theme.FnfSurface
import com.funkin.playtest.ui.theme.FnfYellow
import com.funkin.playtest.viewmodel.PlayViewModel

@Composable
fun PlayScreen(
    viewModel: PlayViewModel,
    hitboxOpacity: Float,
    onOpenDebugMenu: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler {
        if (!state.isPaused) {
            viewModel.togglePause()
        } else {
            onExit()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FnfDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top HUD Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF140924))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = state.song.name.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FnfPink)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = state.difficulty.title.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "${state.song.bpm.toInt()} BPM • ${if (state.downscroll) "Downscroll" else "Upscroll"}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Debug Menu Trigger (directly accessible in gameplay)
                    IconButton(
                        onClick = {
                            viewModel.togglePause()
                            onOpenDebugMenu()
                        },
                        modifier = Modifier.testTag("in_game_debug_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug Menu",
                            tint = Color(0xFFFF5533)
                        )
                    }

                    // Pause Button
                    IconButton(
                        onClick = { viewModel.togglePause() },
                        modifier = Modifier.testTag("btn_pause")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = FnfCyan
                        )
                    }
                }
            }

            // Song Progress Indicator
            LinearProgressIndicator(
                progress = { (state.songPositionMs.toFloat() / state.songLengthMs).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = FnfCyan,
                trackColor = Color(0xFF23133D)
            )

            // Character Stage
            CharacterStageView(
                opponentName = state.song.opponentName,
                playerName = state.song.playerName,
                playerSingingDir = state.playerSingingDir,
                opponentSingingDir = state.opponentSingingDir,
                playerMissedDir = state.playerMissedDir,
                currentBeat = state.currentBeat
            )

            // Health Bar
            HealthBarView(
                health = state.health,
                currentBeat = state.currentBeat,
                opponentName = state.song.opponentName,
                playerName = state.song.playerName,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Rhythm Strumline Arena
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val visibleNotes = viewModel.getVisibleNotes()
                val playerNotes = visibleNotes.filter { it.isPlayer }
                val opponentNotes = visibleNotes.filter { !it.isPlayer }

                StrumlineView(
                    playerNotes = playerNotes,
                    opponentNotes = opponentNotes,
                    songPositionMs = state.songPositionMs,
                    pressedDirections = state.pressedDirections,
                    opponentPressedDirections = state.opponentPressedDirections,
                    downscroll = state.downscroll,
                    noteSpeed = state.difficulty.baseSpeed,
                    modifier = Modifier.fillMaxSize()
                )

                // Judgement & Combo Popup in center
                state.latestJudgement?.let { popup ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = popup.judgement.displayName,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp,
                                color = popup.judgement.color
                            )
                            if (popup.combo > 0) {
                                Text(
                                    text = "${popup.combo}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Stats Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF140924))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Score: ${state.score}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Text(
                    text = "Misses: ${state.misses}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (state.misses > 0) FnfRed else Color.LightGray
                )
                Text(
                    text = "Rating: ${(state.accuracy * 100).toInt()}%",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = FnfCyan
                )
                if (state.botplay) {
                    Text(
                        text = "[BOTPLAY]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = FnfYellow
                    )
                }
            }

            // Touch Hitbox Zone (Lower 32% of screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                FunkinHitboxOverlay(
                    pressedDirections = state.pressedDirections,
                    opacity = hitboxOpacity,
                    onNoteDown = { dir -> viewModel.onNoteDown(dir) },
                    onNoteUp = { dir -> viewModel.onNoteUp(dir) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Pause Menu Overlay
        if (state.isPaused) {
            PauseOverlay(
                onDismiss = { viewModel.togglePause() },
                onRestart = { viewModel.restartSong() },
                onToggleBotplay = { viewModel.toggleBotplay() },
                onToggleGodMode = { viewModel.toggleGodMode() },
                isBotplay = state.botplay,
                isGodMode = state.godMode,
                onOpenDebug = {
                    viewModel.togglePause()
                    onOpenDebugMenu()
                },
                onExit = onExit
            )
        }

        // Game Over Overlay
        if (state.isGameOver) {
            GameOverOverlay(
                onRetry = { viewModel.restartSong() },
                onExit = onExit
            )
        }

        // Victory Overlay
        if (state.isVictory) {
            VictoryOverlay(
                state = state,
                onRetry = { viewModel.restartSong() },
                onContinue = onExit
            )
        }
    }
}

@Composable
private fun PauseOverlay(
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onToggleBotplay: () -> Unit,
    onToggleGodMode: () -> Unit,
    isBotplay: Boolean,
    isGodMode: Boolean,
    onOpenDebug: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD090412)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, FnfCyan, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = FnfSurface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = FnfCyan
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FnfCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_resume")
                ) {
                    Text("RESUME", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332050)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_restart")
                ) {
                    Text("RESTART SONG", fontFamily = FontFamily.Monospace, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onToggleBotplay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBotplay) FnfYellow else Color(0xFF332050)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBotplay) "BOTPLAY: ON" else "BOTPLAY: OFF",
                        fontFamily = FontFamily.Monospace,
                        color = if (isBotplay) Color.Black else Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onOpenDebug,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5533)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_pause_debug_menu")
                ) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = "Debug", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DEBUG MENU", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = FnfRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_exit_to_menu")
                ) {
                    Text("EXIT TO MENU", fontFamily = FontFamily.Monospace, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE2A050B)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💀", fontSize = 56.sp)
            Text(
                text = "GAME OVER",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                color = FnfRed
            )
            Text(
                text = "You got funked! Keep practicing!",
                fontFamily = FontFamily.SansSerif,
                fontSize = 13.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = FnfCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_retry")
                ) {
                    Text("TRY AGAIN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332050)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("MENU", fontFamily = FontFamily.Monospace, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun VictoryOverlay(
    state: com.funkin.playtest.viewmodel.PlayUiState,
    onRetry: () -> Unit,
    onContinue: () -> Unit
) {
    val rank = when {
        state.accuracy >= 0.98f -> "S+"
        state.accuracy >= 0.90f -> "S"
        state.accuracy >= 0.80f -> "A"
        state.accuracy >= 0.70f -> "B"
        state.accuracy >= 0.60f -> "C"
        else -> "D"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE08162A)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(320.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, FnfGreen, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = FnfSurface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CLEARED!",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = FnfGreen
                )
                Text(
                    text = state.song.name,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(FnfYellow.copy(alpha = 0.2f))
                        .border(2.dp, FnfYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = rank,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        color = FnfYellow
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Score: ${state.score}", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.White)
                Text("Accuracy: ${(state.accuracy * 100).toInt()}%", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = FnfCyan)
                Text("Max Combo: ${state.maxCombo}", fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color.White)
                Text("Sicks: ${state.sicks} • Goods: ${state.goods}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Gray)
                Text("Bads: ${state.bads} • Misses: ${state.misses}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = FnfGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONTINUE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

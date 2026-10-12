package com.funkin.playtest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.model.Difficulty
import com.funkin.playtest.model.GameNote
import com.funkin.playtest.model.NoteDirection
import com.funkin.playtest.model.SongCatalog
import com.funkin.playtest.model.SongData
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfGreen
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfPurple
import com.funkin.playtest.ui.theme.FnfRed
import com.funkin.playtest.ui.theme.FnfSurface
import com.funkin.playtest.ui.theme.FnfYellow
import com.funkin.playtest.viewmodel.DebugTab
import com.funkin.playtest.viewmodel.DebugViewModel

@Composable
fun DebugMenuScreen(
    viewModel: DebugViewModel,
    onBack: () -> Unit,
    onLaunchPlaytest: (SongData, Difficulty, List<GameNote>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FnfDarkBg)
    ) {
        // Debug Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1033))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("debug_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FnfCyan
                )
            }
            Text(
                text = "FUNKIN' DEBUG MENU",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = FnfCyan
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFF3344))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DEV MODE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }

        // Tabs
        val tabs = listOf(
            DebugTab.CHART_EDITOR to "Chart",
            DebugTab.SONG_WARP to "Warp",
            DebugTab.GAMEPLAY_MODS to "Mods",
            DebugTab.LATENCY_CALIBRATION to "Audio",
            DebugTab.ANIMATION_INSPECTOR to "Anim"
        )

        TabRow(
            selectedTabIndex = state.selectedTab.ordinal,
            containerColor = FnfSurface,
            contentColor = FnfCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[state.selectedTab.ordinal]),
                    color = FnfCyan
                )
            }
        ) {
            tabs.forEach { (tab, label) ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { viewModel.setTab(tab) },
                    text = {
                        Text(
                            text = label,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (state.selectedTab == tab) FnfCyan else Color.Gray
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when (state.selectedTab) {
                DebugTab.CHART_EDITOR -> ChartEditorTab(
                    state = state,
                    viewModel = viewModel,
                    onPlaytest = {
                        onLaunchPlaytest(state.selectedSong, state.selectedDifficulty, state.customNotes)
                    }
                )
                DebugTab.SONG_WARP -> SongWarpTab(
                    state = state,
                    viewModel = viewModel,
                    onWarpPlay = { song, diff ->
                        onLaunchPlaytest(song, diff, null)
                    }
                )
                DebugTab.GAMEPLAY_MODS -> GameplayModsTab(state = state, viewModel = viewModel)
                DebugTab.LATENCY_CALIBRATION -> LatencyCalibrationTab(state = state, viewModel = viewModel)
                DebugTab.ANIMATION_INSPECTOR -> AnimationInspectorTab(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ChartEditorTab(
    state: com.funkin.playtest.viewmodel.DebugUiState,
    viewModel: DebugViewModel,
    onPlaytest: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.toggleEditorMode() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.editorIsPlayerMode) FnfCyan else FnfPink
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_toggle_player_opp")
            ) {
                Text(
                    text = if (state.editorIsPlayerMode) "PLAYER NOTES" else "OPPONENT NOTES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.Black
                )
            }

            Row {
                Button(
                    onClick = { viewModel.generateRandomPattern() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Shuffle", modifier = Modifier.size(16.dp))
                }

                Button(
                    onClick = { viewModel.clearChart() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF880E4F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Measure Navigation & Playtest CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { viewModel.setEditorMeasure(state.editorCurrentMeasure - 1) },
                    enabled = state.editorCurrentMeasure > 0,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(36.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text("<", fontWeight = FontWeight.Bold)
                }

                Text(
                    text = " Measure ${state.editorCurrentMeasure + 1} ",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                Button(
                    onClick = { viewModel.setEditorMeasure(state.editorCurrentMeasure + 1) },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(36.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(">", fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onPlaytest,
                colors = ButtonDefaults.buttonColors(containerColor = FnfGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_playtest_chart")
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("PLAYTEST", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 16-step Grid View for current measure
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140B24)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // Header Columns
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.width(36.dp)) {
                        Text("STEP", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Gray)
                    }
                    NoteDirection.entries.forEach { dir ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = dir.label.take(2).uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = dir.color
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(16) { step ->
                        val isBeatStart = step % 4 == 0
                        val stepMs = (60_000f / state.editorBpm) / 4f
                        val measureStartMs = state.editorCurrentMeasure * 16 * stepMs
                        val targetStrum = (measureStartMs + (step * stepMs)).toLong()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(if (isBeatStart) Color(0x333F2B66) else Color.Transparent)
                                .border(0.5.dp, Color(0xFF281845)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.CenterStart) {
                                Text(
                                    text = "$step",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = if (isBeatStart) FnfYellow else Color.Gray
                                )
                            }

                            NoteDirection.entries.forEach { dir ->
                                val hasNote = state.customNotes.any {
                                    it.isPlayer == state.editorIsPlayerMode &&
                                            it.direction == dir &&
                                            kotlin.math.abs(it.strumTime - targetStrum) < (stepMs * 0.4f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, Color(0xFF20103A))
                                        .background(if (hasNote) dir.color else Color.Transparent)
                                        .clickable {
                                            viewModel.toggleGridNote(step, dir)
                                        }
                                        .testTag("grid_${step}_${dir.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (hasNote) {
                                        Text("●", color = Color.Black, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongWarpTab(
    state: com.funkin.playtest.viewmodel.DebugUiState,
    viewModel: DebugViewModel,
    onWarpPlay: (SongData, Difficulty) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "INSTANT SONG WARP",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = FnfCyan
        )
        Text(
            text = "Select any track and difficulty to instantly playtest",
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Difficulty selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Difficulty.entries.forEach { diff ->
                val isSelected = state.selectedDifficulty == diff
                Button(
                    onClick = { viewModel.selectDifficulty(diff) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) FnfCyan else FnfSurface
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                ) {
                    Text(
                        text = diff.title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SongCatalog.songs) { song ->
                val isCurrent = state.selectedSong.id == song.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) FnfCyan else Color(0xFF2C194C),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.selectSong(song) }
                        .testTag("warp_song_${song.id}"),
                    colors = CardDefaults.cardColors(containerColor = FnfSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${song.week} • ${song.bpm.toInt()} BPM • vs ${song.opponentName}",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { onWarpPlay(song, state.selectedDifficulty) },
                            colors = ButtonDefaults.buttonColors(containerColor = FnfYellow),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("btn_warp_play_${song.id}")
                        ) {
                            Text(
                                text = "PLAY",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameplayModsTab(
    state: com.funkin.playtest.viewmodel.DebugUiState,
    viewModel: DebugViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "GAMEPLAY MODIFIERS & DEBUG FLAGS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = FnfCyan
            )
        }

        item {
            ModToggleRow(
                title = "BOTPLAY MODE",
                desc = "Auto-plays player notes perfectly for chart auditing",
                checked = state.botplay,
                onCheckedChange = { viewModel.toggleBotplay() },
                testTag = "switch_botplay"
            )
        }

        item {
            ModToggleRow(
                title = "GOD MODE / PRACTICE",
                desc = "Disables death and health drain on missed notes",
                checked = state.godMode,
                onCheckedChange = { viewModel.toggleGodMode() },
                testTag = "switch_god_mode"
            )
        }

        item {
            ModToggleRow(
                title = "GHOST TAPPING",
                desc = "Allow tapping without being penalized when no notes are near",
                checked = state.ghostTapping,
                onCheckedChange = { viewModel.toggleGhostTapping() },
                testTag = "switch_ghost_tapping"
            )
        }

        item {
            ModToggleRow(
                title = "DOWNSCROLL",
                desc = "Notes fall downward instead of rising upward",
                checked = state.downscroll,
                onCheckedChange = { viewModel.toggleDownscroll() },
                testTag = "switch_downscroll"
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FnfSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "HITBOX OPACITY: ${(state.hitboxOpacity * 100).toInt()}%",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Slider(
                        value = state.hitboxOpacity,
                        onValueChange = { viewModel.setHitboxOpacity(it) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = FnfCyan, activeTrackColor = FnfCyan),
                        modifier = Modifier.testTag("slider_hitbox_opacity")
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = FnfSurface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "TIMING WINDOWS (MS)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FnfYellow
                    )
                    Text("Sick: < ${state.sickWindowMs.toInt()}ms", color = Color.LightGray, fontSize = 11.sp)
                    Text("Good: < ${state.goodWindowMs.toInt()}ms", color = Color.LightGray, fontSize = 11.sp)
                    Text("Bad: < ${state.badWindowMs.toInt()}ms", color = Color.LightGray, fontSize = 11.sp)
                    Text("Shit: < ${state.shitWindowMs.toInt()}ms", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ModToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FnfSurface),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Text(
                    text = desc,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = FnfCyan, checkedTrackColor = Color(0xFF005577)),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun LatencyCalibrationTab(
    state: com.funkin.playtest.viewmodel.DebugUiState,
    viewModel: DebugViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AUDIO LATENCY CALIBRATION",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = FnfCyan
        )
        Text(
            text = "Tap the beat box below in rhythm to calibrate your audio-video delay",
            fontFamily = FontFamily.SansSerif,
            fontSize = 11.sp,
            color = Color.LightGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Metronome Tap Target
        Card(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .border(3.dp, FnfCyan, CircleShape)
                .clickable { viewModel.recordCalibrationTap() }
                .testTag("calibration_tap_box"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B0B33))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥁", fontSize = 42.sp)
                    Text(
                        text = "TAP HERE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = FnfCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Current Offset: ${state.audioOffsetMs} ms",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = FnfYellow
        )

        Slider(
            value = state.audioOffsetMs.toFloat(),
            onValueChange = { viewModel.saveCalibrationOffset(it.toInt()) },
            valueRange = -150f..150f,
            colors = SliderDefaults.colors(thumbColor = FnfYellow, activeTrackColor = FnfYellow),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .testTag("slider_audio_offset")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.saveCalibrationOffset(0) },
                colors = ButtonDefaults.buttonColors(containerColor = FnfSurface)
            ) {
                Text("RESET (0ms)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Button(
                onClick = { viewModel.saveCalibrationOffset(state.estimatedLatencyMs) },
                enabled = state.estimatedLatencyMs > 0,
                colors = ButtonDefaults.buttonColors(containerColor = FnfCyan)
            ) {
                Text("APPLY ESTIMATE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun AnimationInspectorTab(
    state: com.funkin.playtest.viewmodel.DebugUiState,
    viewModel: DebugViewModel
) {
    val characters = listOf("Boyfriend", "Daddy Dearest", "Skid and Pump", "Pico", "Mommy Mearest", "Tankman")
    val anims = listOf("idle", "left", "down", "up", "right", "miss")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CHARACTER & ANIMATION INSPECTOR",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp,
            color = FnfCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Character Select
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(characters) { char ->
                val isSelected = state.inspectorCharacter == char
                Button(
                    onClick = { viewModel.setInspectorCharacter(char) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) FnfCyan else FnfSurface
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = char,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Character Stage Box Preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140826)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val emoji = when (state.inspectorCurrentAnimation) {
                        "left" -> "👈"
                        "down" -> "👇"
                        "up" -> "☝️"
                        "right" -> "👉"
                        "miss" -> "⚡😵⚡"
                        else -> "🎤"
                    }
                    Text(text = emoji, fontSize = 48.sp)
                    Text(
                        text = "${state.inspectorCharacter} [${state.inspectorCurrentAnimation.uppercase()}]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = FnfPink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TRIGGER ANIMATIONS:",
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Trigger buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            anims.forEach { anim ->
                Button(
                    onClick = { viewModel.triggerAnimation(anim) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("anim_btn_$anim"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (anim) {
                            "left" -> FnfPurple
                            "down" -> FnfCyan
                            "up" -> FnfGreen
                            "right" -> FnfRed
                            "miss" -> Color(0xFF7B1FA2)
                            else -> FnfYellow
                        }
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = anim.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}


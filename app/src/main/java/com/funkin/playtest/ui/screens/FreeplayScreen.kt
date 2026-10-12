package com.funkin.playtest.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.audio.AudioSynth
import com.funkin.playtest.model.Difficulty
import com.funkin.playtest.model.PreferencesManager
import com.funkin.playtest.model.SongCatalog
import com.funkin.playtest.model.SongData
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfGreen
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfSurface
import com.funkin.playtest.ui.theme.FnfYellow

@Composable
fun FreeplayScreen(
    onPlaySong: (SongData, Difficulty) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var selectedDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }
    var selectedSong by remember { mutableStateOf(SongCatalog.songs[0]) }

    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FnfDarkBg)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF140A26))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("freeplay_back_button")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FnfCyan)
            }
            Text(
                text = "FREEPLAY",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = FnfCyan
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${SongCatalog.songs.size} TRACKS",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // Difficulty Selection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Difficulty.entries.forEach { diff ->
                val isSelected = selectedDifficulty == diff
                Button(
                    onClick = { selectedDifficulty = diff },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) FnfCyan else FnfSurface
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        text = diff.title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        // Songs List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SongCatalog.songs) { song ->
                val isSelected = selectedSong.id == song.id
                val highScore = prefs.getHighScore(song.id, selectedDifficulty)
                val accuracy = prefs.getAccuracy(song.id, selectedDifficulty)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) FnfCyan else Color(0xFF2E194D),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedSong = song
                            AudioSynth.playBeat(0)
                        }
                        .testTag("freeplay_item_${song.id}"),
                    colors = CardDefaults.cardColors(containerColor = FnfSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = song.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = if (isSelected) FnfCyan else Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${song.week})",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = FnfPink
                                )
                            }
                            Text(
                                text = "vs ${song.opponentName} • ${song.bpm.toInt()} BPM",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            if (highScore > 0) {
                                Text(
                                    text = "RECORD: $highScore pts (${(accuracy * 100).toInt()}%)",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = FnfYellow,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { onPlaySong(song, selectedDifficulty) },
                            colors = ButtonDefaults.buttonColors(containerColor = FnfGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_play_${song.id}")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
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

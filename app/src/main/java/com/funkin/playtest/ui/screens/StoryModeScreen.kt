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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.model.Difficulty
import com.funkin.playtest.model.SongCatalog
import com.funkin.playtest.model.SongData
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfGreen
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfSurface
import com.funkin.playtest.ui.theme.FnfYellow

@Composable
fun StoryModeScreen(
    onStartSong: (SongData, Difficulty) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weeks = SongCatalog.weeks
    var selectedWeek by remember { mutableStateOf(weeks[0]) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.NORMAL) }

    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FnfDarkBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF140A26))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("story_back_button")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FnfCyan)
            }
            Text(
                text = "STORY MODE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = FnfCyan
            )
        }

        // Difficulty Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD).forEach { diff ->
                val isSelected = selectedDifficulty == diff
                Button(
                    onClick = { selectedDifficulty = diff },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) FnfYellow else FnfSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = diff.title.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        // Week List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(weeks) { week ->
                val isSelected = selectedWeek.id == week.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) FnfCyan else Color(0xFF281548),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedWeek = week }
                        .testTag("week_card_${week.id}"),
                    colors = CardDefaults.cardColors(containerColor = FnfSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = week.weekName.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = FnfPink
                            )
                            Text(
                                text = "vs ${week.opponentName}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = FnfYellow
                            )
                        }

                        Text(
                            text = week.title,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Tracklist
                        Text(
                            text = "TRACKS: " + week.songs.joinToString(" • ") { it.name },
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onStartSong(week.songs[0], selectedDifficulty) },
                                colors = ButtonDefaults.buttonColors(containerColor = FnfGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_start_week_${week.id}")
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "START ${week.weekName.uppercase()}",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

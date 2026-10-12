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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.funkin.playtest.model.PreferencesManager
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfSurface

@Composable
fun OptionsScreen(
    onOpenDebugMenu: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

    var downscroll by remember { mutableStateOf(prefs.downscroll) }
    var ghostTapping by remember { mutableStateOf(prefs.ghostTapping) }
    var haptics by remember { mutableStateOf(prefs.hapticFeedback) }
    var noteSplashes by remember { mutableStateOf(prefs.noteSplashes) }
    var hitboxOpacity by remember { mutableFloatStateOf(prefs.hitboxOpacity) }

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
            IconButton(onClick = onBack, modifier = Modifier.testTag("options_back_button")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FnfCyan)
            }
            Text(
                text = "OPTIONS & PREFERENCES",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = FnfCyan
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // DEBUG MENU ITEM IN GAME OPTIONS (as requested by user)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, Color(0xFFFF5533), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenDebugMenu)
                    .testTag("opt_debug_menu_item"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261026))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF5533).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFF5533), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BugReport,
                            contentDescription = "Debug Menu",
                            tint = Color(0xFFFF5533),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DEBUG MENU",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFFFF7755)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFF3344))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "FEATURE_DEBUG_MENU",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "Access chart editor, song warps, audio latency calibrator & sprite tester",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            OptionToggleCard(
                title = "DOWNSCROLL",
                desc = "Notes travel from top to bottom (recommended for DDR/Mania players)",
                checked = downscroll,
                onCheckedChange = {
                    downscroll = it
                    prefs.downscroll = it
                },
                testTag = "opt_downscroll"
            )

            OptionToggleCard(
                title = "GHOST TAPPING",
                desc = "Tapping without notes does not penalize misses or break combo",
                checked = ghostTapping,
                onCheckedChange = {
                    ghostTapping = it
                    prefs.ghostTapping = it
                },
                testTag = "opt_ghost_tapping"
            )

            OptionToggleCard(
                title = "HAPTIC FEEDBACK",
                desc = "Vibrate phone on note hits for tactile rhythm feel",
                checked = haptics,
                onCheckedChange = {
                    haptics = it
                    prefs.hapticFeedback = it
                },
                testTag = "opt_haptics"
            )

            OptionToggleCard(
                title = "NOTE SPLASHES",
                desc = "Show particle splash animation on 'SICK!!' rating hits",
                checked = noteSplashes,
                onCheckedChange = {
                    noteSplashes = it
                    prefs.noteSplashes = it
                },
                testTag = "opt_note_splashes"
            )

            // Hitbox opacity slider
            Card(
                colors = CardDefaults.cardColors(containerColor = FnfSurface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "TOUCH HITBOX OPACITY: ${(hitboxOpacity * 100).toInt()}%",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Adjust the visibility of the 4 on-screen touch columns",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Slider(
                        value = hitboxOpacity,
                        onValueChange = {
                            hitboxOpacity = it
                            prefs.hitboxOpacity = it
                        },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = FnfCyan, activeTrackColor = FnfCyan),
                        modifier = Modifier.testTag("opt_slider_hitbox_opacity")
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionToggleCard(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = FnfSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
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

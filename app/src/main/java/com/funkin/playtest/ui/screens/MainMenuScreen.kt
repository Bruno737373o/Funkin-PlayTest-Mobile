package com.funkin.playtest.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.R
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfSurface
import com.funkin.playtest.ui.theme.FnfYellow

@Composable
fun MainMenuScreen(
    onNavigateStory: () -> Unit,
    onNavigateFreeplay: () -> Unit,
    onNavigateDebug: () -> Unit,
    onNavigateOptions: () -> Unit,
    onNavigateCredits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FnfDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Banner Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, FnfCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = FnfSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.fnf_stage_hero),
                        contentDescription = "FNF Stage Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay with title
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD10081C))
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column {
                            Text(
                                text = "FRIDAY NIGHT FUNKIN'",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = FnfCyan
                            )
                            Text(
                                text = "PLAYTEST MOBILE v0.8.5.1",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = FnfPink
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Menu Action Cards
            MenuCard(
                title = "STORY MODE",
                subtitle = "Play through weekly rap battles",
                icon = Icons.Default.PlayArrow,
                accentColor = FnfYellow,
                testTag = "btn_story_mode",
                onClick = onNavigateStory
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                title = "FREEPLAY",
                subtitle = "Pick any track, difficulty, and practice",
                icon = Icons.Default.LibraryMusic,
                accentColor = FnfCyan,
                testTag = "btn_freeplay",
                onClick = onNavigateFreeplay
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Prominent Debug Menu Card (as requested by user)
            MenuCard(
                title = "DEBUG MENU",
                subtitle = "Chart editor, song warp, botplay, latency calibration",
                icon = Icons.Default.BugReport,
                accentColor = Color(0xFFFF5533),
                badge = "DEV & TEST",
                testTag = "btn_debug_menu",
                onClick = onNavigateDebug
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                title = "OPTIONS",
                subtitle = "Hitbox controls, downscroll, haptics & calibration",
                icon = Icons.Default.Settings,
                accentColor = FnfPink,
                testTag = "btn_options",
                onClick = onNavigateOptions
            )

            Spacer(modifier = Modifier.height(12.dp))

            MenuCard(
                title = "CREDITS",
                subtitle = "The Funkin' Crew & Mobile PlayTest contributors",
                icon = Icons.Default.Info,
                accentColor = Color(0xFFB388FF),
                testTag = "btn_credits",
                onClick = onNavigateCredits
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Tap DEBUG MENU to edit charts, warp tracks, or test timing",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = FnfSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

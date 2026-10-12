package com.funkin.playtest.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funkin.playtest.ui.theme.FnfCyan
import com.funkin.playtest.ui.theme.FnfDarkBg
import com.funkin.playtest.ui.theme.FnfPink
import com.funkin.playtest.ui.theme.FnfSurface
import com.funkin.playtest.ui.theme.FnfYellow

@Composable
fun CreditsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("credits_back_button")) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FnfCyan)
            }
            Text(
                text = "CREDITS",
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
            CreditSection("THE FUNKIN' CREW", listOf(
                "ninjamuffin99" to "Original Game Programmer & Design",
                "PhantomArcade" to "Art Director & Animator",
                "evilsk8r" to "Artist & Character Design",
                "Kawai Sprite" to "Original Music Composer"
            ))

            CreditSection("MOBILE PLAYTEST PORT", listOf(
                "Bruno737373o" to "PlayTest Mobile Core Developer",
                "Funkin' Mobile Contributors" to "Touch input algorithms & testing",
                "OpenFL / HaxeFlixel Community" to "Cross-platform mobile groundwork"
            ))

            CreditSection("ANDROID REWRITE & ENGINES", listOf(
                "Jetpack Compose Team" to "Modern Declarative Android UI",
                "Google AI Studio" to "Cross-Platform Framework Rewrite Engine"
            ))
        }
    }
}

@Composable
private fun CreditSection(title: String, entries: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = FnfSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = FnfPink
            )
            Spacer(modifier = Modifier.height(8.dp))
            entries.forEach { (name, role) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = role,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

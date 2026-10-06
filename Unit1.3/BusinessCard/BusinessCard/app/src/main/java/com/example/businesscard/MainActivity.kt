package com.example.businesscard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businesscard.ui.theme.BusinessCardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusinessCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BusinessCard(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(R.drawable.gamer_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFFF4DFF), CircleShape),
            ) {
                Image(
                    painter = painterResource(R.drawable.profile_photo),
                    contentDescription = "Profile photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "PLAYER PROFILE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4DFF),
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Inés Lucia Aerts",
                fontSize = 20.sp,
                color = Color(0xFFFF4DFF)
            )
            Text(
                text = "LV. 25 • Student Programmer",
                fontSize = 14.sp,
                color = Color(0xFFB366FF)
            )

            Spacer(Modifier.height(20.dp))

            SectionTitle("Player Stats")
            StatList(
                listOf(
                    "HTML",
                    "CSS",
                    "JavaScript",
                    "C#",
                    "SQL",
                    "Kotlin (new)"
                )
            )

            Spacer(Modifier.height(16.dp))

            SectionTitle("Languages Unlocked")
            StatList(
                listOf(
                    "Dutch",
                    "French",
                    "English",
                    "Spanish"
                )
            )

            Spacer(Modifier.height(16.dp))

            SectionTitle("Side Quests")
            StatList(
                listOf(
                    "Photography",
                    "Gaming",
                    "Traveling",
                    "Music"
                )
            )

            Spacer(Modifier.height(16.dp))

            SectionTitle("Current Quest")
            Text(
                text = "Diploma Graduaat Programmeren",
                fontSize = 14.sp,
                color = Color(0xFFB366FF)
            )

            Spacer(Modifier.height(16.dp))

            SectionTitle("Completed Quests")
            StatList(
                listOf(
                    "WPLA: CVO Lethas",
                    "Secundaire Diploma: Toegepaste Beeldende Kunst Textiel",
                    "Volwassenonderwijs Basis Naaien",
                    "Kunstacademie 1ste jaar notenleer"
                )
            )

            Spacer(Modifier.height(20.dp))

            SectionTitle("Communication")
            StatList(listOf("inesaerts@live.com", "+32 468 13 15 52"))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFF4DFF),
    )
    Spacer(Modifier.height(4.dp))
}

@Composable
fun StatList(items: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEach { item ->
            Text(
                text = "• $item",
                fontSize = 14.sp,
                color = Color(0xFFB366FF)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BusinessCardPreview() {
    BusinessCardTheme {
        BusinessCard()
    }
}

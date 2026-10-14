package com.example.spaceart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spaceart.ui.theme.SpaceArtTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaceArtTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArtSpaceApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class Photo(
    val imageRes: Int,
    val title: String,
    val artist: String,
    val year: String,
)

@Composable
fun ArtSpaceApp(modifier: Modifier = Modifier) {
    val photos = listOf(
        Photo(
            R.drawable.lagos_de_atitlan,
            "Lagos de Atitlán y vulcanos",
            "Inés",
            "Augustus 2026",
        ),
        Photo(
            R.drawable.central_de_guatemala_city,
            "Central de Guatemala",
            "Inés",
            "September 2026",
        ),
        Photo(
            R.drawable.antigua_guatemala,
            "Antigua Guatemala",
            "Inés",
            "September 2026",
        ),
        Photo(
            R.drawable.rio_dulce,
            "Rio Dulce: Castillo de San Felipe de Lara",
            "Inés",
            "Augustus 2026",
        ),
        Photo(
            imageRes = R.drawable.iglesia_de_la_merced,
            title = "Iglesia de la Merced",
            artist = "Inés",
            year = "September 2026",
        ),
        Photo(
            imageRes = R.drawable.san_juan,
            title = "San Juan",
            artist = "Inés",
            year = "September 2026",
        ),

    )

    var currentPhoto by remember {
        mutableIntStateOf(0)
    }

    val photo = photos[currentPhoto]

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = photo.imageRes),
            contentDescription = photo.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .border(2.dp, Color.LightGray),
            contentScale = ContentScale.Crop,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = photo.title,
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "${photo.artist} (${photo.year})"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (currentPhoto > 0) {
                        currentPhoto--
                    }
                },
                enabled = currentPhoto > 0
            ) {
                Text("Previous")
            }

            Button(
                onClick = {
                    if (currentPhoto < (photos.size - 1)) {
                        currentPhoto++
                    }
                },
                enabled = currentPhoto < (photos.size - 1)
            ) {
                Text("Next")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArtSpaceAppPreview() {
    SpaceArtTheme {
        ArtSpaceApp()
    }
}

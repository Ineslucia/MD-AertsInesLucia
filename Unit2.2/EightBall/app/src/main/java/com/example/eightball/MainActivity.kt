package com.example.eightball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.eightball.ui.theme.EightBallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EightBallTheme {
                EightBallApp()
            }
        }
    }
}

@Composable
fun EightBallApp() {
    // List of possible answers
    val answers = listOf(
        "Yes",
        "No",
        "Maybe",
        "Ask again later",
        "Definitely",
        "Uncertain",
    )

    // State variable that holds the current answer shown on screen.
    // 'remember' keeps the value during recompositions.
    var currentAnswer by remember { mutableStateOf("Tap the button to ask!") }


    // Scaffold provides basic Material layout structure (safe areas, padding)
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        // Column arranges items vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentAnswer,
                style = MaterialTheme.typography.headlineMedium
            )
           // Adds space between the text and the button
            Spacer(modifier = Modifier.height(24.dp))


            // Button that picks a random answer when clicked
            Button(onClick = {
                currentAnswer = answers.random()
            }) {
                Text("Ask the Eight Ball")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EightBallTheme {
        EightBallApp()
    }
}

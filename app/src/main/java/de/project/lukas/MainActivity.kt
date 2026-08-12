package de.project.lukas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.project.lukas.ui.AppScreen
import de.project.lukas.ui.theme.LukasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LukasTheme {
                AppScreen()
            }
        }
    }
}

package com.zingayu.barmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BarManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BarManagementApp()
                }
            }
        }
    }
}

@Composable
fun BarManagementTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFB6A2FF),
            secondary = Color(0xFF9BE7D5),
            tertiary = Color(0xFFFFD57A)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF5E3BEE),
            secondary = Color(0xFF2DB39B),
            tertiary = Color(0xFFE9B949)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

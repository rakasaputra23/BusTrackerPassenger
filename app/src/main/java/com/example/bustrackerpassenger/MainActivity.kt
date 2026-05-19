package com.example.bustrackerpassenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bustrackerpassenger.ui.screens.MapScreen
import com.example.bustrackerpassenger.ui.theme.BusTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusTrackerTheme {
                MapScreen()
            }
        }
    }
}
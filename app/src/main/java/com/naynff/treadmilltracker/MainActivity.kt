package com.naynff.treadmilltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.naynff.treadmilltracker.ui.navigation.TreadmillApp
import com.naynff.treadmilltracker.ui.theme.TreadmillTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreadmillTrackerTheme {
                TreadmillApp()
            }
        }
    }
}

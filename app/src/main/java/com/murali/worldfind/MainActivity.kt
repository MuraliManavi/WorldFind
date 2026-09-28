package com.murali.worldfind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.murali.worldfind.data.local.PreferenceManager
import com.murali.worldfind.navigation.AppNavigation
import com.murali.worldfind.ui.theme.WorldFindTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferenceManager = PreferenceManager(this)

        setContent {
            val themeMode by preferenceManager.themeMode.collectAsState(initial = "dark")

            WorldFindTheme(
                darkTheme = themeMode == "dark"
            ) {
                AppNavigation()
            }
        }
    }
}
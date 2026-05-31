package com.onelineaday.journal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.onelineaday.journal.ui.screens.MainNavigation
import com.onelineaday.journal.ui.theme.OneLineADayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            val systemDarkMode = isSystemInDarkTheme()
            
            LaunchedEffect(Unit) {
                isDarkMode = systemDarkMode
            }
            
            OneLineADayTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { isDarkMode = it }
                    )
                }
            }
        }
    }
}

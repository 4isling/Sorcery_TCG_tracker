package com.hayse.sorcery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hayse.sorcery.core.navigation.AppScaffold
import com.hayse.sorcery.core.ui.theme.SorceryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SorceryTheme {
                AppScaffold()
            }
        }
    }
}

package com.hayse.sorcery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.navigation.AppScaffold
import com.hayse.sorcery.core.ui.AppLocalization
import com.hayse.sorcery.core.ui.theme.SorceryTheme
import com.hayse.sorcery.core.ui.theme.skin.Skins
import com.hayse.sorcery.core.ui.theme.skin.skinFor
import com.hayse.sorcery.feature.settings.domain.model.AppSettings
import com.hayse.sorcery.feature.settings.domain.model.SkinMode
import com.hayse.sorcery.feature.settings.domain.model.ThemeMode
import com.hayse.sorcery.feature.settings.domain.repository.SettingsRepository
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsRepository = koinInject<SettingsRepository>()
            val settings by settingsRepository.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())

            val windowSizeClass = calculateWindowSizeClass(this)
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val skin = when (settings.skinMode) {
                SkinMode.OFF -> Skins.Default
                SkinMode.FIXED -> skinFor(settings.themeSet)
            }

            AppLocalization(settings.language) {
                SorceryTheme(set = settings.themeSet, skin = skin, darkTheme = darkTheme) {
                    AppScaffold(
                        widthSizeClass = windowSizeClass.widthSizeClass,
                        socialEnabled = settings.socialEnabled,
                    )
                }
            }
        }
    }
}

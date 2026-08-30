package com.hayse.sorcery.core.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.hayse.sorcery.feature.settings.domain.model.AppLanguage
import java.util.Locale

/**
 * Applique la langue choisie à toute la sous-arborescence en surchargeant [LocalContext] et
 * [LocalConfiguration], de sorte que `stringResource` et le formatage résolvent la bonne locale
 * sans recréer l'Activity. [AppLanguage.SYSTEM] laisse la configuration système intacte.
 */
@Composable
fun AppLocalization(language: AppLanguage, content: @Composable () -> Unit) {
    val tag = language.localeTag
    if (tag == null) {
        content()
        return
    }
    val locale = Locale.forLanguageTag(tag)
    val configuration = Configuration(LocalConfiguration.current).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    val localizedContext = LocalContext.current.createConfigurationContext(configuration)
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides localizedContext,
        content = content,
    )
}

package com.hayse.sorcery.feature.settings.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.core.ui.theme.SorcerySet
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.paletteFor
import com.hayse.sorcery.feature.settings.domain.model.AppLanguage
import com.hayse.sorcery.feature.settings.domain.model.ThemeMode
import com.hayse.sorcery.feature.settings.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val settings by viewModel.state.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        SettingsSection(title = "Thème") {
            ThemeMode.entries.forEach { mode ->
                OptionRow(
                    label = mode.label,
                    selected = settings.themeMode == mode,
                    onSelect = { viewModel.setThemeMode(mode) },
                )
            }
        }

        SettingsSection(title = "Couleur") {
            SetOptionRow(
                set = null,
                selected = settings.themeSet == null,
                onSelect = { viewModel.setThemeSet(null) },
            )
            SorcerySet.entries.forEach { set ->
                SetOptionRow(
                    set = set,
                    selected = settings.themeSet == set,
                    onSelect = { viewModel.setThemeSet(set) },
                )
            }
        }

        SettingsSection(title = "Langue") {
            AppLanguage.entries.forEach { language ->
                OptionRow(
                    label = language.label,
                    selected = settings.language == language,
                    onSelect = { viewModel.setLanguage(language) },
                )
            }
        }

        AboutSection()
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = spacing.xs),
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = spacing.md),
        )
    }
}

@Composable
private fun SetOptionRow(set: SorcerySet?, selected: Boolean, onSelect: () -> Unit) {
    val spacing = LocalSpacing.current
    val swatch = paletteFor(set).light.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Surface(
            color = swatch,
            shape = CircleShape,
            modifier = Modifier
                .padding(start = spacing.md)
                .size(20.dp)
                .clip(CircleShape),
        ) {}
        Text(
            text = set.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = spacing.md),
        )
    }
}

@Composable
private fun AboutSection() {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    val version = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "—"
    }
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            text = "À propos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = spacing.xs),
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(text = "Sorcery Tracker", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Application compagnon non officielle pour Sorcery: Contested Realm.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Version $version",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.SYSTEM -> "Système"
        ThemeMode.LIGHT -> "Clair"
        ThemeMode.DARK -> "Sombre"
    }

private val AppLanguage.label: String
    get() = when (this) {
        AppLanguage.SYSTEM -> "Système"
        AppLanguage.FRENCH -> "Français"
        AppLanguage.ENGLISH -> "English"
    }

private val SorcerySet?.label: String
    get() = when (this) {
        null -> "Défaut"
        SorcerySet.Alpha -> "Alpha"
        SorcerySet.Beta -> "Bêta"
        SorcerySet.ArthurianLegends -> "Légendes arthuriennes"
        SorcerySet.Gothic -> "Gothique"
        SorcerySet.Dragonlord -> "Seigneur-dragon"
        SorcerySet.Promotional -> "Promotionnel"
    }

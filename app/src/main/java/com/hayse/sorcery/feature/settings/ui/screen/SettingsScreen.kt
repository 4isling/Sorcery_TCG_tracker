package com.hayse.sorcery.feature.settings.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hayse.sorcery.R
import com.hayse.sorcery.core.ui.theme.SorcerySet
import com.hayse.sorcery.core.ui.theme.dimensions.LocalSpacing
import com.hayse.sorcery.core.ui.theme.paletteFor
import com.hayse.sorcery.feature.settings.domain.model.AppLanguage
import com.hayse.sorcery.feature.settings.domain.model.SkinMode
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
        SettingsSection(title = stringResource(R.string.settings_theme)) {
            ThemeMode.entries.forEach { mode ->
                OptionRow(
                    label = mode.label(),
                    selected = settings.themeMode == mode,
                    onSelect = { viewModel.setThemeMode(mode) },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.settings_color)) {
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

        SettingsSection(title = stringResource(R.string.settings_skin)) {
            SkinMode.entries.forEach { mode ->
                OptionRow(
                    label = mode.label(),
                    selected = settings.skinMode == mode,
                    onSelect = { viewModel.setSkinMode(mode) },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.settings_language)) {
            AppLanguage.entries.forEach { language ->
                OptionRow(
                    label = language.label(),
                    selected = settings.language == language,
                    onSelect = { viewModel.setLanguage(language) },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.settings_social)) {
            SwitchRow(
                label = stringResource(R.string.settings_social_enabled),
                checked = settings.socialEnabled,
                onCheckedChange = viewModel::setSocialEnabled,
            )
            PseudoRow(
                pseudo = settings.pseudo,
                onPseudoChange = viewModel::setPseudo,
            )
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
private fun PseudoRow(pseudo: String, onPseudoChange: (String) -> Unit) {
    val spacing = LocalSpacing.current
    var text by remember { mutableStateOf(pseudo) }
    LaunchedEffect(pseudo) {
        if (pseudo != text) text = pseudo
    }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onPseudoChange(it)
        },
        label = { Text(stringResource(R.string.settings_pseudo)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = spacing.sm),
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
            text = set.label(),
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
            text = stringResource(R.string.settings_about),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = spacing.xs),
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Text(text = stringResource(R.string.settings_app_name), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(R.string.settings_app_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.settings_version, version),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.settings_author),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val sourceUrl = stringResource(R.string.settings_source_url)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl)))
                        }
                        .padding(top = spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.settings_source_code),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeMode.label(): String = when (this) {
    ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
    ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
}

@Composable
private fun SkinMode.label(): String = when (this) {
    SkinMode.OFF -> stringResource(R.string.settings_skin_off)
    SkinMode.FIXED -> stringResource(R.string.settings_skin_fixed)
}

@Composable
private fun AppLanguage.label(): String = when (this) {
    AppLanguage.SYSTEM -> stringResource(R.string.settings_language_system)
    AppLanguage.FRENCH -> stringResource(R.string.settings_language_french)
    AppLanguage.ENGLISH -> stringResource(R.string.settings_language_english)
    AppLanguage.SPANISH -> stringResource(R.string.settings_language_spanish)
    AppLanguage.CATALAN -> stringResource(R.string.settings_language_catalan)
}

@Composable
private fun SorcerySet?.label(): String = when (this) {
    null -> stringResource(R.string.settings_set_default)
    SorcerySet.Alpha -> stringResource(R.string.settings_set_alpha)
    SorcerySet.Beta -> stringResource(R.string.settings_set_beta)
    SorcerySet.ArthurianLegends -> stringResource(R.string.settings_set_arthurian_legends)
    SorcerySet.Gothic -> stringResource(R.string.settings_set_gothic)
    SorcerySet.Dragonlord -> stringResource(R.string.settings_set_dragonlord)
    SorcerySet.Promotional -> stringResource(R.string.settings_set_promotional)
}

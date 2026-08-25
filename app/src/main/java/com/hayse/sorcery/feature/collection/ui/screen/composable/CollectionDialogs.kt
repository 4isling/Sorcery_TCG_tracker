package com.hayse.sorcery.feature.collection.ui.screen.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.feature.cards.domain.model.Card
import com.hayse.sorcery.feature.collection.domain.model.ImportReport

/** Choix Remplacer / Fusionner avant un import CSV. */
@Composable
fun ImportModeDialog(
    onReplace: () -> Unit,
    onMerge: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Importer la collection") },
        text = { Text("Remplacer la collection actuelle ou fusionner l'import avec l'existant ?") },
        confirmButton = { TextButton(onClick = onReplace) { Text("Remplacer") } },
        dismissButton = { TextButton(onClick = onMerge) { Text("Fusionner") } },
    )
}

/** Rapport d'import : nombre de lignes matchées + détail des rejets. */
@Composable
fun ImportReportDialog(report: ImportReport, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import terminé") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("${report.matched} ligne(s) importée(s).")
                if (report.ambiguous.isNotEmpty()) {
                    Text(
                        "${report.ambiguous.size} ligne(s) ambiguë(s) (1er slug retenu) :",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    report.ambiguous.take(20).forEach { Text("• ${it.cardName} (${it.set}, ${it.finish})") }
                }
                if (report.unmatched.isNotEmpty()) {
                    Text(
                        "${report.unmatched.size} ligne(s) sans correspondance :",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    report.unmatched.take(20).forEach { Text("• ${it.cardName} (${it.set}, ${it.finish})") }
                }
                if (report.invalid.isNotEmpty()) {
                    Text(
                        "${report.invalid.size} ligne(s) invalide(s).",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
    )
}

/** Cartes manquantes d'un set (issu de la vue Complétion). */
@Composable
fun MissingDialog(setName: String, missing: List<Card>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manquantes — $setName") },
        text = {
            if (missing.isEmpty()) {
                Text("Set complété !")
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    missing.forEach { Text("• ${it.name}") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } },
    )
}

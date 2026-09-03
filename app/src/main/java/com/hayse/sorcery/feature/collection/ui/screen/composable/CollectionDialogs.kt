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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hayse.sorcery.R
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
        title = { Text(stringResource(R.string.collection_import_mode_title)) },
        text = { Text(stringResource(R.string.collection_import_mode_message)) },
        confirmButton = { TextButton(onClick = onReplace) { Text(stringResource(R.string.collection_replace)) } },
        dismissButton = { TextButton(onClick = onMerge) { Text(stringResource(R.string.collection_merge)) } },
    )
}

/** Rapport d'import : nombre de lignes matchées + détail des rejets. */
@Composable
fun ImportReportDialog(report: ImportReport, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.collection_import_done_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(stringResource(R.string.collection_import_matched, report.matched))
                if (report.ambiguous.isNotEmpty()) {
                    Text(
                        stringResource(R.string.collection_import_ambiguous, report.ambiguous.size),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    report.ambiguous.take(20).forEach {
                        Text(stringResource(R.string.collection_import_line_detail, it.cardName, it.set, it.finish))
                    }
                }
                if (report.unmatched.isNotEmpty()) {
                    Text(
                        stringResource(R.string.collection_import_unmatched, report.unmatched.size),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    report.unmatched.take(20).forEach {
                        Text(stringResource(R.string.collection_import_line_detail, it.cardName, it.set, it.finish))
                    }
                }
                if (report.invalid.isNotEmpty()) {
                    Text(
                        stringResource(R.string.collection_import_invalid, report.invalid.size),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.collection_ok)) } },
    )
}

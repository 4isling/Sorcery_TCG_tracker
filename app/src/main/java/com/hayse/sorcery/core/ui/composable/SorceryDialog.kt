package com.hayse.sorcery.core.ui.composable

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SorceryDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String? = null,
    content: @Composable () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = content,
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = confirmText) }
        },
        dismissButton = dismissText?.let {
            { TextButton(onClick = onDismiss) { Text(text = it) } }
        },
    )
}

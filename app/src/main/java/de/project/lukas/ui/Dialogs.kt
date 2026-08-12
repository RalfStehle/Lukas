package de.project.lukas.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.project.lukas.R
import de.project.lukas.ui.theme.LukasTheme

private const val MAX_NAME_LENGTH = 14

@Composable
fun RenameDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= MAX_NAME_LENGTH) text = it },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) { Text(stringResource(R.string.rename)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
fun ServoDialog(
    initialLow: Int,
    initialHigh: Int,
    onDismiss: () -> Unit,
    onConfirm: (low: Int, high: Int) -> Unit
) {
    var low by remember { mutableStateOf(initialLow.toString()) }
    var high by remember { mutableStateOf(initialHigh.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.menu_servo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = low,
                    onValueChange = { low = it.filter(Char::isDigit) },
                    label = { Text("Position 1") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = high,
                    onValueChange = { high = it.filter(Char::isDigit) },
                    label = { Text("Position 2") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(low.toIntOrNull() ?: 0, high.toIntOrNull() ?: 0)
                }
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Preview
@Composable
private fun RenameDialogPreview() {
    LukasTheme {
        RenameDialog(current = "Train Hub #1", onDismiss = {}, onConfirm = {})
    }
}

@Preview
@Composable
private fun ServoDialogPreview() {
    LukasTheme {
        ServoDialog(initialLow = 0, initialHigh = 120, onDismiss = {}, onConfirm = { _, _ -> })
    }
}

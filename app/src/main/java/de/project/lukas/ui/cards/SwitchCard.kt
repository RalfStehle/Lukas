package de.project.lukas.ui.cards

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.project.lukas.R
import de.project.lukas.model.DevicesManager
import de.project.lukas.model.Switch
import de.project.lukas.ui.ServoDialog
import de.project.lukas.ui.theme.LukasTheme
import de.project.lukas.ui.theme.Toggle1Green
import de.project.lukas.ui.theme.Toggle2Green

@Composable
fun SwitchCard(device: Switch, viewModel: DevicesManager) {
    val name by device.name.collectAsStateWithLifecycle()
    var showServo by remember { mutableStateOf(false) }

    SwitchCardContent(
        name = name,
        onToggle1 = device::toggle1,
        onToggle2 = device::toggle2,
        onSwitchOff = { viewModel.switchOffDevice(device) },
        onServo = { showServo = true }
    )

    if (showServo) {
        ServoDialog(
            initialLow = device.servoLow,
            initialHigh = device.servoHigh,
            onDismiss = { showServo = false },
            onConfirm = { low, high ->
                device.adjustServo(low, high)
                showServo = false
            }
        )
    }
}

@Composable
fun SwitchCardContent(
    name: String,
    onToggle1: () -> Unit,
    onToggle2: () -> Unit,
    onSwitchOff: () -> Unit,
    onServo: () -> Unit
) {
    DeviceCardFrame(
        typeLabel = stringResource(R.string.label_switch),
        name = name,
        battery = null,
        message = null,
        menu = { dismiss ->
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_switchoff)) }, onClick = {
                dismiss()
                onSwitchOff()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_servo)) }, onClick = {
                dismiss()
                onServo()
            })
        }
    ) {
        CardButtonRow {
            CardButton(onToggle1, Toggle1Green, weight = 1f) {
                CardIcon(
                    R.drawable.ic_baseline_switch_right_24,
                    stringResource(R.string.button_toggle)
                )
                Spacer(Modifier.width(6.dp))
                Text("1")
            }
            CardButton(onToggle2, Toggle2Green, weight = 1f) {
                CardIcon(
                    R.drawable.ic_baseline_switch_right_24,
                    stringResource(R.string.button_toggle)
                )
                Spacer(Modifier.width(6.dp))
                Text("2")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SwitchCardPreview() {
    LukasTheme {
        SwitchCardContent(
            name = "Switch #1",
            onToggle1 = {},
            onToggle2 = {},
            onSwitchOff = {},
            onServo = {}
        )
    }
}

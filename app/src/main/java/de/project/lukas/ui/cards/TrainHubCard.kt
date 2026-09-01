package de.project.lukas.ui.cards

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import de.project.lukas.model.TrainHub
import de.project.lukas.ui.RenameDialog
import de.project.lukas.ui.theme.LedOrange
import de.project.lukas.ui.theme.LightOrange
import de.project.lukas.ui.theme.LukasTheme
import de.project.lukas.ui.theme.MotorBlue
import de.project.lukas.ui.theme.MotorBlueDark

@Composable
fun TrainHubCard(device: TrainHub, viewModel: DevicesManager) {
    val name by device.name.collectAsStateWithLifecycle()
    val battery by device.battery.collectAsStateWithLifecycle()
    val message by device.message.collectAsStateWithLifecycle()
    val maxSpeed by device.maxSpeed.collectAsStateWithLifecycle()
    var showRename by remember { mutableStateOf(false) }

    TrainHubCardContent(
        name = name,
        battery = battery,
        message = message,
        maxSpeed = maxSpeed,
        onSlower = device::motorSlower,
        onStop = device::motorStop,
        onFaster = device::motorFaster,
        onDarker = device::lightDarker,
        onLedColor = device::setLedColorHub,
        onBrighter = device::lightBrighter,
        onDisconnect = { viewModel.removeDevice(device) },
        onSwitchOff = { viewModel.switchOffDevice(device) },
        onRename = { showRename = true },
        onCycleSpeedLimit = device::cycleSpeedLimit
    )

    if (showRename) {
        RenameDialog(
            current = name,
            onDismiss = { showRename = false },
            onConfirm = {
                device.rename(it)
                showRename = false
            }
        )
    }
}

@Composable
fun TrainHubCardContent(
    name: String,
    battery: Int,
    message: String,
    maxSpeed: Int,
    onSlower: () -> Unit,
    onStop: () -> Unit,
    onFaster: () -> Unit,
    onDarker: () -> Unit,
    onLedColor: () -> Unit,
    onBrighter: () -> Unit,
    onDisconnect: () -> Unit,
    onSwitchOff: () -> Unit,
    onRename: () -> Unit,
    onCycleSpeedLimit: () -> Unit
) {
    DeviceCardFrame(
        typeLabel = stringResource(R.string.label_train),
        name = name,
        battery = battery,
        message = message,
        menu = { dismiss ->
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_disconnect)) }, onClick = {
                dismiss()
                onDisconnect()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_switchoff)) }, onClick = {
                dismiss()
                onSwitchOff()
            })
            DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, onClick = {
                dismiss()
                onRename()
            })
            DropdownMenuItem(
                text = {
                    val label = stringResource(R.string.menu_speed_limit)
                    Text(if (maxSpeed == 100) label else "$label ($maxSpeed%)")
                },
                trailingIcon = { if (maxSpeed < 100) Icon(Icons.Filled.Check, contentDescription = null) },
                onClick = {
                    dismiss()
                    onCycleSpeedLimit()
                }
            )
        }
    ) {
        CardButtonRow {
            CardButton(onSlower, MotorBlue, weight = 2f) {
                CardIcon(
                    R.drawable.ic_baseline_arrow_back_24,
                    stringResource(R.string.button_slower)
                )
            }
            CardButton(onStop, MotorBlueDark, weight = 1f) {
                CardIcon(R.drawable.ic_outline_stop_24, stringResource(R.string.button_stop))
            }
            CardButton(onFaster, MotorBlue, weight = 2f) {
                CardIcon(
                    R.drawable.ic_baseline_arrow_forward_24,
                    stringResource(R.string.button_faster)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        CardButtonRow {
            CardButton(onDarker, LightOrange, weight = 2f) {
                CardIcon(R.drawable.ic_baseline_remove_24, stringResource(R.string.button_darker))
            }
            CardButton(onLedColor, LedOrange, weight = 1f) {
                CardIcon(R.drawable.ic_outline_lightbulb_24, stringResource(R.string.button_light))
            }
            CardButton(onBrighter, LightOrange, weight = 2f) {
                CardIcon(R.drawable.ic_baseline_add_24, stringResource(R.string.button_brighter))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TrainHubCardPreview() {
    LukasTheme {
        TrainHubCardContent(
            name = "Train Hub #1",
            battery = 87,
            message = "Blue (3)",
            maxSpeed = 100,
            onSlower = {}, onStop = {}, onFaster = {},
            onDarker = {}, onLedColor = {}, onBrighter = {},
            onDisconnect = {}, onSwitchOff = {}, onRename = {}, onCycleSpeedLimit = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TrainHubCardPreviewNoMessage() {
    LukasTheme {
        TrainHubCardContent(
            name = "Train Hub #1",
            battery = 87,
            message = "",
            maxSpeed = 60,
            onSlower = {}, onStop = {}, onFaster = {},
            onDarker = {}, onLedColor = {}, onBrighter = {},
            onDisconnect = {}, onSwitchOff = {}, onRename = {}, onCycleSpeedLimit = {}
        )
    }
}

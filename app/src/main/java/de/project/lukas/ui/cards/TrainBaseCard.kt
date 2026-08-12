package de.project.lukas.ui.cards

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import de.project.lukas.model.TrainBase
import de.project.lukas.ui.RenameDialog
import de.project.lukas.ui.theme.LightOrange
import de.project.lukas.ui.theme.LedOrange
import de.project.lukas.ui.theme.LukasTheme
import de.project.lukas.ui.theme.MotorBlue
import de.project.lukas.ui.theme.MotorBlueDark

@Composable
fun TrainBaseCard(device: TrainBase, viewModel: DevicesManager) {
    val name by device.name.collectAsStateWithLifecycle()
    val battery by device.battery.collectAsStateWithLifecycle()
    val message by device.message.collectAsStateWithLifecycle()
    var showRename by remember { mutableStateOf(false) }

    TrainBaseCardContent(
        name = name,
        battery = battery,
        message = message,
        onSlower = device::motorSlower,
        onStop = device::motorStop,
        onFaster = device::motorFaster,
        onSound1 = device::sound1,
        onSound2 = device::sound2,
        onLedColor = device::setLedColorHub,
        onSound3 = device::sound3,
        onSound4 = device::sound4,
        onDisconnect = { viewModel.removeDevice(device) },
        onSwitchOff = { viewModel.switchOffDevice(device) },
        onRename = { showRename = true },
    )

    if (showRename) {
        RenameDialog(
            current = name,
            onDismiss = { showRename = false },
            onConfirm = { device.rename(it); showRename = false },
        )
    }
}

@Composable
fun TrainBaseCardContent(
    name: String,
    battery: Int,
    message: String,
    onSlower: () -> Unit,
    onStop: () -> Unit,
    onFaster: () -> Unit,
    onSound1: () -> Unit,
    onSound2: () -> Unit,
    onLedColor: () -> Unit,
    onSound3: () -> Unit,
    onSound4: () -> Unit,
    onDisconnect: () -> Unit,
    onSwitchOff: () -> Unit,
    onRename: () -> Unit,
) {
    DeviceCardFrame(
        typeLabel = stringResource(R.string.duplo),
        name = name,
        battery = battery,
        message = message,
        menu = { dismiss ->
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_disconnect)) }, onClick = { dismiss(); onDisconnect() })
            DropdownMenuItem(text = { Text(stringResource(R.string.menu_switchoff)) }, onClick = { dismiss(); onSwitchOff() })
            DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, onClick = { dismiss(); onRename() })
        },
    ) {
        CardButtonRow {
            CardButton(onSlower, MotorBlue, weight = 2f) { CardIcon(R.drawable.ic_baseline_arrow_back_24, stringResource(R.string.button_slower)) }
            CardButton(onStop, MotorBlueDark, weight = 1f) { CardIcon(R.drawable.ic_outline_stop_24, stringResource(R.string.button_stop)) }
            CardButton(onFaster, MotorBlue, weight = 2f) { CardIcon(R.drawable.ic_baseline_arrow_forward_24, stringResource(R.string.button_faster)) }
        }
        Spacer(Modifier.height(6.dp))
        CardButtonRow {
            CardButton(onSound1, LightOrange, weight = 1f) { CardIcon(R.drawable.baseline_horn_24, "Horn") }
            CardButton(onSound2, LightOrange, weight = 1f) { CardIcon(R.drawable.baseline_steam_24, "Steam") }
            CardButton(onLedColor, LedOrange, weight = 1f) { CardIcon(R.drawable.ic_outline_lightbulb_24, stringResource(R.string.button_light)) }
            CardButton(onSound3, LightOrange, weight = 1f) { CardIcon(R.drawable.baseline_water_drop_24, "Water") }
            CardButton(onSound4, LightOrange, weight = 1f) { CardIcon(R.drawable.baseline_railway_alert_24, "Depart") }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TrainBaseCardPreview() {
    LukasTheme {
        TrainBaseCardContent(
            name = "Duplo Train",
            battery = 63,
            message = "Red (0x09)",
            onSlower = {}, onStop = {}, onFaster = {},
            onSound1 = {}, onSound2 = {}, onLedColor = {}, onSound3 = {}, onSound4 = {},
            onDisconnect = {}, onSwitchOff = {}, onRename = {},
        )
    }
}

package de.project.lukas.ui.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.project.lukas.R
import de.project.lukas.model.Device
import de.project.lukas.model.DevicesManager
import de.project.lukas.model.Remote
import de.project.lukas.model.RemoteController
import de.project.lukas.model.Switch
import de.project.lukas.model.TrainBase
import de.project.lukas.model.TrainHub
import de.project.lukas.ui.RenameDialog
import de.project.lukas.ui.theme.ListBackground
import de.project.lukas.ui.theme.LukasTheme

@Composable
fun RemoteCard(remote: Remote, allDevices: List<Device>, viewModel: DevicesManager) {
    val name by remote.name.collectAsStateWithLifecycle()
    val battery by remote.battery.collectAsStateWithLifecycle()
    var showRename by remember { mutableStateOf(false) }

    var controllerA by remember(remote) { mutableStateOf(remote.controllerA) }
    var controllerB by remember(remote) { mutableStateOf(remote.controllerB) }

    val controllers = remember(allDevices) {
        buildList {
            add(RemoteController.Noop)
            allDevices.forEach { device ->
                when (device) {
                    is TrainHub -> {
                        add(device.motorController)
                        add(device.lightController)
                    }
                    is TrainBase -> {
                        add(device.motorController)
                        add(device.lightController)
                    }
                    is Switch -> add(device.controller)
                }
            }
        }
    }

    RemoteCardContent(
        name = name,
        battery = battery,
        controllers = controllers,
        selectedA = controllerA,
        selectedB = controllerB,
        onSelectA = {
            controllerA = it
            remote.controllerA = it
        },
        onSelectB = {
            controllerB = it
            remote.controllerB = it
        },
        onDisconnect = { viewModel.removeDevice(remote) },
        onSwitchOff = { viewModel.switchOffDevice(remote) },
        onRename = { showRename = true }
    )

    if (showRename) {
        RenameDialog(
            current = name,
            onDismiss = { showRename = false },
            onConfirm = {
                remote.rename(it)
                showRename = false
            }
        )
    }
}

@Composable
fun RemoteCardContent(
    name: String,
    battery: Int,
    controllers: List<RemoteController>,
    selectedA: RemoteController,
    selectedB: RemoteController,
    onSelectA: (RemoteController) -> Unit,
    onSelectB: (RemoteController) -> Unit,
    onDisconnect: () -> Unit,
    onSwitchOff: () -> Unit,
    onRename: () -> Unit
) {
    DeviceCardFrame(
        typeLabel = stringResource(R.string.label_remote),
        name = name,
        battery = battery,
        message = null,
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
        }
    ) {
        CardButtonRow {
            ControllerDropdown(
                controllers = controllers,
                selected = selectedA,
                onSelect = onSelectA,
                modifier = Modifier.weight(1f)
            )
            ControllerDropdown(
                controllers = controllers,
                selected = selectedB,
                onSelect = onSelectB,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ControllerDropdown(
    controllers: List<RemoteController>,
    selected: RemoteController,
    onSelect: (RemoteController) -> Unit,
    modifier: Modifier = Modifier
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEEEEEE))
                .clickable { open = true }
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected.displayName,
                color = Color(0xFF1B1B1B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF1B1B1B))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            controllers.forEach { controller ->
                DropdownMenuItem(
                    text = { Text(controller.displayName) },
                    onClick = {
                        onSelect(controller)
                        open = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun RemoteCardPreview() {
    LukasTheme {
        RemoteCardContent(
            name = "Remote #1",
            battery = 92,
            controllers = listOf(RemoteController.Noop),
            selectedA = RemoteController.Noop,
            selectedB = RemoteController.Noop,
            onSelectA = {}, onSelectB = {},
            onDisconnect = {}, onSwitchOff = {}, onRename = {}
        )
    }
}

@Preview(name = "Remote – large screen (max width)", showBackground = true, widthDp = 800)
@Composable
private fun RemoteCardLargeScreenPreview() {
    LukasTheme {
        Box(Modifier.fillMaxWidth().background(ListBackground).padding(8.dp)) {
            RemoteCardContent(
                name = "Remote #1",
                battery = 92,
                controllers = listOf(RemoteController.Noop),
                selectedA = RemoteController.Noop,
                selectedB = RemoteController.Noop,
                onSelectA = {}, onSelectB = {},
                onDisconnect = {}, onSwitchOff = {}, onRename = {}
            )
        }
    }
}

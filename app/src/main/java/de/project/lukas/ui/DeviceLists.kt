package de.project.lukas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.project.lukas.model.Device
import de.project.lukas.model.DevicesManager
import de.project.lukas.model.Remote
import de.project.lukas.model.Switch
import de.project.lukas.model.TrainBase
import de.project.lukas.model.TrainHub
import de.project.lukas.ui.cards.RemoteCard
import de.project.lukas.ui.cards.SwitchCard
import de.project.lukas.ui.cards.TrainBaseCard
import de.project.lukas.ui.cards.TrainHubCard
import de.project.lukas.ui.theme.ListBackground

@Composable
private fun listBackground() =
    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else ListBackground

@Composable
fun DevicesGrid(devices: List<Device>, viewModel: DevicesManager) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier.fillMaxSize().background(listBackground()),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        gridItems(devices, key = { it.address }) { device ->
            when (device) {
                is TrainHub -> TrainHubCard(device, viewModel)
                is TrainBase -> TrainBaseCard(device, viewModel)
                is Switch -> SwitchCard(device, viewModel)
                is Remote -> Unit
            }
        }
    }
}

@Composable
fun RemotesList(remotes: List<Remote>, allDevices: List<Device>, viewModel: DevicesManager) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier.fillMaxSize().background(listBackground()),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        gridItems(remotes, key = { it.address }) { remote ->
            RemoteCard(remote, allDevices, viewModel)
        }
    }
}

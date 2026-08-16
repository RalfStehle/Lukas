package de.project.lukas.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.project.lukas.R
import de.project.lukas.model.DevicesManager
import de.project.lukas.model.Remote
import de.project.lukas.model.RemoteController
import de.project.lukas.ui.cards.RemoteCardContent
import de.project.lukas.ui.cards.SwitchCardContent
import de.project.lukas.ui.cards.TrainHubCardContent
import de.project.lukas.ui.theme.ListBackground
import de.project.lukas.ui.theme.LukasTheme
import de.project.lukas.ui.theme.ScanGreen
import de.project.lukas.ui.theme.ScanStopRed
import de.project.lukas.ui.theme.StopAllRed
import kotlinx.coroutines.launch

private val requiredPermissions: Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

@Composable
fun AppScreen(viewModel: DevicesManager = viewModel()) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.startScanning()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            if (viewModel.isBluetoothEnabled) {
                viewModel.startScanning()
            } else {
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
        }
    }

    fun onScanClick() {
        val granted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        when {
            !granted -> permissionLauncher.launch(requiredPermissions)
            !viewModel.isBluetoothEnabled ->
                enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            else -> viewModel.startScanning()
        }
    }

    AppScreenContent(
        isScanning = isScanning,
        hasDevices = devices.isNotEmpty(),
        onScan = { onScanClick() },
        onStopScan = { viewModel.stopScanning() },
        onStopAll = { viewModel.stopAllTrains() },
        devicesContent = {
            DevicesGrid(devices = devices.filter { it !is Remote }, viewModel = viewModel)
        },
        remotesContent = {
            RemotesList(
                remotes = devices.filterIsInstance<Remote>(),
                allDevices = devices,
                viewModel = viewModel
            )
        }
    )
}

/**
 * Stateless scaffold: top bar with the scan/stop action, the scan/stop/stop-all floating action
 * button, and the two tabs (devices / remotes). Kept free of the ViewModel so it can be previewed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreenContent(
    isScanning: Boolean,
    hasDevices: Boolean,
    onScan: () -> Unit,
    onStopScan: () -> Unit,
    onStopAll: () -> Unit,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    devicesContent: @Composable () -> Unit,
    remotesContent: @Composable () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    val white = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    if (isScanning) {
                        TextButton(onClick = onStopScan, colors = white) {
                            Icon(painterResource(R.drawable.ic_outline_stop_24), null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.label_scan_stop))
                        }
                    } else {
                        TextButton(onClick = onScan, colors = white) {
                            Icon(painterResource(R.drawable.ic_baseline_bluetooth_24), null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.label_scan))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            when {
                hasDevices -> FloatingActionButton(
                    onClick = onStopAll,
                    containerColor = StopAllRed,
                    contentColor = Color.White
                ) {
                    Icon(
                        painterResource(R.drawable.ic_baseline_front_hand_24),
                        contentDescription = "Stop all trains"
                    )
                }

                isScanning -> ExtendedFloatingActionButton(
                    onClick = onStopScan,
                    containerColor = ScanStopRed,
                    contentColor = Color.White,
                    icon = { Icon(painterResource(R.drawable.ic_outline_stop_24), null) },
                    text = { Text(stringResource(R.string.label_scan_stop)) }
                )

                else -> ExtendedFloatingActionButton(
                    onClick = onScan,
                    containerColor = ScanGreen,
                    contentColor = Color.White,
                    icon = { Icon(painterResource(R.drawable.ic_baseline_bluetooth_24), null) },
                    text = { Text(stringResource(R.string.label_scan)) }
                )
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.title_devices)) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.title_remotes)) }
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page == 0) devicesContent() else remotesContent()
            }
        }
    }
}

// ---- Previews ----------------------------------------------------------------------------------

@Composable
private fun SampleDevices() {
    Column(
        modifier = Modifier.fillMaxSize().background(ListBackground).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrainHubCardContent(
            name = "Train Hub #1", battery = 87, message = "Blue (3)", speedLimited = false,
            onSlower = {}, onStop = {}, onFaster = {},
            onDarker = {}, onLedColor = {}, onBrighter = {},
            onDisconnect = {}, onSwitchOff = {}, onRename = {}, onToggleSpeedLimit = {}
        )
        SwitchCardContent(name = "Switch #1", onToggle1 = {
        }, onToggle2 = {}, onSwitchOff = {}, onServo = {})
    }
}

@Composable
private fun SampleRemotes() {
    Column(
        modifier = Modifier.fillMaxSize().background(ListBackground).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RemoteCardContent(
            name = "Remote #1", battery = 92,
            controllers = listOf(RemoteController.Noop),
            selectedA = RemoteController.Noop, selectedB = RemoteController.Noop,
            onSelectA = {}, onSelectB = {},
            onDisconnect = {}, onSwitchOff = {}, onRename = {}
        )
    }
}

@Preview(name = "Devices tab – default", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AppScreenDefaultPreview() {
    LukasTheme {
        AppScreenContent(
            isScanning = false,
            hasDevices = false,
            onScan = {},
            onStopScan = {},
            onStopAll = {},
            devicesContent = { SampleEmpty() },
            remotesContent = { SampleEmpty() }
        )
    }
}

@Preview(name = "Devices tab – scanning", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AppScreenScanningPreview() {
    LukasTheme {
        AppScreenContent(
            isScanning = true,
            hasDevices = false,
            onScan = {},
            onStopScan = {},
            onStopAll = {},
            devicesContent = { SampleEmpty() },
            remotesContent = { SampleEmpty() }
        )
    }
}

@Preview(name = "Devices tab – with device", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AppScreenWithDevicePreview() {
    LukasTheme {
        AppScreenContent(
            isScanning = false,
            hasDevices = true,
            onScan = {},
            onStopScan = {},
            onStopAll = {},
            devicesContent = { SampleDevices() },
            remotesContent = { SampleRemotes() }
        )
    }
}

@Preview(name = "Remotes tab", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun AppScreenRemotesTabPreview() {
    LukasTheme {
        AppScreenContent(
            isScanning = false,
            hasDevices = true,
            initialPage = 1,
            onScan = {},
            onStopScan = {},
            onStopAll = {},
            devicesContent = { SampleDevices() },
            remotesContent = { SampleRemotes() }
        )
    }
}

@Composable
private fun SampleEmpty() {
    Column(Modifier.fillMaxSize().background(ListBackground)) {}
}

package de.project.lukas.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.project.lukas.R
import de.project.lukas.model.DevicesManager
import de.project.lukas.model.Remote
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(viewModel: DevicesManager = viewModel()) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) viewModel.startScanning()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
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

    // Mirrors the original behaviour: after a full scan cycle the bottom button becomes the
    // permanent "stop all trains" button and scanning moves to the top bar.
    var scanStateChanges by remember { mutableIntStateOf(0) }
    LaunchedEffect(isScanning) { scanStateChanges++ }
    val showStopAllFab = scanStateChanges >= 3

    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    val white = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    if (isScanning) {
                        TextButton(onClick = { viewModel.stopScanning() }, colors = white) {
                            Icon(painterResource(R.drawable.ic_outline_stop_24), null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.label_scan_stop))
                        }
                    } else {
                        TextButton(onClick = { onScanClick() }, colors = white) {
                            Icon(painterResource(R.drawable.ic_baseline_bluetooth_24), null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.label_scan))
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            when {
                showStopAllFab -> FloatingActionButton(
                    onClick = { viewModel.stopAllTrains() },
                    containerColor = StopAllRed,
                    contentColor = Color.White,
                ) {
                    Icon(painterResource(R.drawable.ic_baseline_front_hand_24), contentDescription = "Stop all trains")
                }

                isScanning -> ExtendedFloatingActionButton(
                    onClick = { viewModel.stopScanning() },
                    containerColor = ScanStopRed,
                    contentColor = Color.White,
                    icon = { Icon(painterResource(R.drawable.ic_outline_stop_24), null) },
                    text = { Text(stringResource(R.string.label_scan_stop)) },
                )

                else -> ExtendedFloatingActionButton(
                    onClick = { onScanClick() },
                    containerColor = ScanGreen,
                    contentColor = Color.White,
                    icon = { Icon(painterResource(R.drawable.ic_baseline_bluetooth_24), null) },
                    text = { Text(stringResource(R.string.label_scan)) },
                )
            }
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.title_devices)) },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.title_remotes)) },
                )
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                if (page == 0) {
                    DevicesGrid(devices = devices.filter { it !is Remote }, viewModel = viewModel)
                } else {
                    RemotesList(
                        remotes = devices.filterIsInstance<Remote>(),
                        allDevices = devices,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

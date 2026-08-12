package de.project.lukas.model

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns the BLE scan and the set of connected devices. As an [AndroidViewModel] it survives
 * configuration changes but is bound to the Activity's lifecycle (no static singleton), and it
 * cleans up its per-device collectors instead of leaking [kotlinx.coroutines.flow.StateFlow]
 * observers.
 */
@SuppressLint("MissingPermission")
class DevicesManager(application: Application) : AndroidViewModel(application) {

    private val bluetoothAdapter =
        application.getSystemService(BluetoothManager::class.java)?.adapter
    private var scanner: BluetoothLeScanner? = null

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    val devices: StateFlow<List<Device>> = _devices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val deviceJobs = mutableMapOf<Device, Job>()

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val bluetoothDevice = result.device
            bluetoothDevice.name ?: return
            if (hasDevice(bluetoothDevice.address)) return

            val context = getApplication<Application>()
            when {
                TrainHub.canConnect(result) -> addDevice(TrainHub(context, bluetoothDevice))
                TrainBase.canConnect(result) -> addDevice(TrainBase(context, bluetoothDevice))
                Remote.canConnect(result) -> addDevice(Remote(context, bluetoothDevice))
                Switch.canConnect(result) -> addDevice(Switch(context, bluetoothDevice))
            }
        }
    }

    fun startScanning() {
        if (_isScanning.value) return
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return
        val leScanner = adapter.bluetoothLeScanner ?: return

        scanner = leScanner
        _isScanning.value = true
        leScanner.startScan(scanCallback)

        // Stop scanning automatically after 20 seconds.
        viewModelScope.launch {
            delay(SCAN_DURATION_MS)
            stopScanning()
        }
    }

    fun stopScanning() {
        if (!_isScanning.value) return
        _isScanning.value = false
        scanner?.stopScan(scanCallback)
    }

    fun removeDevice(device: Device) {
        device.disconnect()
        detach(device)
    }

    fun switchOffDevice(device: Device) {
        device.switchOff()
        detach(device)
    }

    fun stopAllTrains() {
        _devices.value.forEach { device ->
            when (device) {
                is TrainHub -> device.motorStop()
                is TrainBase -> device.motorStop()
            }
        }
    }

    private fun addDevice(device: Device) {
        _devices.update { (it + device).sortedForDisplay() }

        deviceJobs[device] = viewModelScope.launch {
            // Re-sort when a device is renamed.
            launch { device.name.collect { _devices.update { list -> list.sortedForDisplay() } } }
            // Drop the device automatically once it disconnects.
            device.isConnected.collect { connected ->
                if (!connected) detach(device)
            }
        }
    }

    private fun detach(device: Device) {
        deviceJobs.remove(device)?.cancel()
        _devices.update { it - device }
    }

    private fun hasDevice(address: String) = _devices.value.any { it.address == address }

    private fun List<Device>.sortedForDisplay(): List<Device> = sortedWith(
        compareByDescending<Device> { it.javaClass.name }.thenBy { it.name.value },
    )

    override fun onCleared() {
        super.onCleared()
        _devices.value.forEach { it.disconnect() }
    }

    companion object {
        private const val SCAN_DURATION_MS = 20_000L
    }
}

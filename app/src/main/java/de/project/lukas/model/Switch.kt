package de.project.lukas.model

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import de.project.lukas.utils.LegoWriterQueue
import java.util.UUID

/** A custom Arduino-ESP32 track switch that speaks the Powered Up write protocol. */
@SuppressLint("MissingPermission")
class Switch(context: Context, private val device: BluetoothDevice) : Device() {

    override val address: String get() = device.address

    val controller: RemoteController = SwitchController(this)

    var servoLow: Int = 0
        private set
    var servoHigh: Int = 120
        private set

    private val preferences = context.getSharedPreferences("lukas_prefs", Context.MODE_PRIVATE)
    private var gatt: BluetoothGatt? = null
    private var writerQueue: LegoWriterQueue? = null

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_DISCONNECTED -> setConnected(false)
                BluetoothGatt.STATE_CONNECTED -> {
                    setConnected(true)
                    Handler(
                        Looper.getMainLooper()
                    ).postDelayed({ this@Switch.gatt?.discoverServices() }, 500)
                    initializeService()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) = initializeService()

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            writerQueue?.confirmWrite()
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            handleBattery(value)
        }

        @Deprecated("Deprecated in API 33, still delivered on older devices")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            handleBattery(characteristic.value)
        }
    }

    init {
        setName(device.name ?: "")
        gatt = device.connectGatt(context, true, callback, BluetoothDevice.TRANSPORT_LE)
        servoLow = preferences.getInt("${address}_ServoLow", 0)
        servoHigh = preferences.getInt("${address}_ServoHigh", 120)
    }

    private fun handleBattery(bytes: ByteArray) {
        if (bytes.isEmpty()) return
        val battery = (bytes[0].toInt() and 0xFF) or
            ((bytes[1].toInt() and 0xFF) shl 8) or
            ((bytes[1].toInt() and 0xFF) shl 16) or
            ((bytes[1].toInt() and 0xFF) shl 24)
        setBattery(battery)
    }

    fun toggle1() = send(byteArrayOf(servoLow.toByte()))

    fun toggle2() = send(byteArrayOf(servoHigh.toByte()))

    fun adjustServo(low: Int, high: Int) {
        servoLow = low
        servoHigh = high
        preferences.edit {
            putInt("${address}_ServoLow", low)
            putInt("${address}_ServoHigh", high)
        }
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    private fun send(data: ByteArray) {
        initializeService()
        writerQueue?.write(data)
    }

    private fun initializeService() {
        if (writerQueue != null) return
        val gatt = gatt ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return

        writerQueue = LegoWriterQueue(gatt, characteristic)
        gatt.setCharacteristicNotification(characteristic, true)
    }

    companion object {
        private val SERVICE_UUID = UUID.fromString("196988b3-b878-4b5b-a4cc-2e3eb64c1e00")
        private val CHARACTERISTIC_UUID = UUID.fromString("196988b4-b878-4b5b-a4cc-2e3eb64c1e00")

        fun canConnect(scanResult: ScanResult): Boolean {
            val record = scanResult.scanRecord ?: return false
            val uuids = record.serviceUuids ?: return false
            return uuids.any { it.uuid == SERVICE_UUID }
        }
    }
}

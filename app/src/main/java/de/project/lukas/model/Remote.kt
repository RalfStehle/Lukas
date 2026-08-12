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
import de.project.lukas.utils.LegoHelper
import de.project.lukas.utils.LegoProtocol
import de.project.lukas.utils.LegoWriterQueue
import java.util.UUID

/** A LEGO Powered Up remote. Its two sides (A/B) can each drive any connected device. */
@SuppressLint("MissingPermission")
class Remote(context: Context, private val device: BluetoothDevice) : Device() {

    override val address: String get() = device.address

    var controllerA: RemoteController = RemoteController.Noop
    var controllerB: RemoteController = RemoteController.Noop

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
                    ).postDelayed({ this@Remote.gatt?.discoverServices() }, 500)
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
            handleNotification(LegoHelper.envelopeToData(value))
        }

        @Deprecated("Deprecated in API 33, still delivered on older devices")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            handleNotification(LegoHelper.envelopeToData(characteristic.value))
        }
    }

    init {
        setName(device.name ?: "")
        gatt = device.connectGatt(context, true, callback)
    }

    private fun handleNotification(value: ByteArray) {
        when (value[0].toInt() and 0xFF) {
            0x01 -> parseDeviceInfo(value)
            0x45 -> parseButtons(value)
        }
    }

    private fun parseButtons(value: ByteArray) {
        // 0: 0x45, 1: Button Side (0 A / 1 B), 2: Button Mode (-1 Down, 1 Up, 127 Red)
        val side = value[1].toInt()
        val mode = value[2].toInt()
        val controller = if (side == 0) controllerA else controllerB

        when (mode) {
            1 -> controller.up(this)
            -1 -> controller.down(this)
            127 -> controller.middle(this)
        }
    }

    private fun parseDeviceInfo(value: ByteArray) {
        if (value[1].toInt() == 0x06) {
            setBattery(value[3].toInt())
        }
    }

    fun rename(name: String) {
        setName(name)
        send(LegoHelper.createRenameRequest(name))
    }

    fun setLedColorRemote(color: Int) {
        send(byteArrayOf(0x81.toByte(), 0x34, 0x11, 0x51, 0x00, color.toByte()))
    }

    override fun disconnect() {
        send(LegoProtocol.disconnectHub)
        gatt?.close()
    }

    override fun switchOff() {
        send(LegoProtocol.switchOffHub)
        gatt?.close()
    }

    private fun send(data: ByteArray) {
        initializeService()
        writerQueue?.write(LegoHelper.dataToEnvelope(data))
    }

    private fun initializeService() {
        if (writerQueue != null) return
        val gatt = gatt ?: return
        val service = gatt.getService(SERVICE_UUID) ?: return
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return

        writerQueue = LegoWriterQueue(gatt, characteristic)
        LegoHelper.enableNotifications(gatt, characteristic)

        Handler(Looper.getMainLooper()).postDelayed({
            // Activate button reports for both sides.
            send(LegoProtocol.activatePortMode(0x00))
            send(LegoProtocol.activatePortMode(0x01))
            send(LegoProtocol.activateBatteryReports)
        }, 2000)
    }

    companion object {
        private val SERVICE_UUID = UUID.fromString("00001623-1212-efde-1623-785feabcd123")
        private val CHARACTERISTIC_UUID = UUID.fromString("00001624-1212-efde-1623-785feabcd123")

        fun canConnect(scanResult: ScanResult): Boolean {
            val record = scanResult.scanRecord ?: return false
            val uuids = record.serviceUuids ?: return false
            if (uuids.none { it.uuid == SERVICE_UUID }) return false
            val data = record.getManufacturerSpecificData(0x397) ?: return false
            return data.size > 1 && data[1].toInt() == 66 // 010 00010
        }
    }
}

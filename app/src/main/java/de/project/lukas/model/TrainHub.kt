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
import android.util.Log
import de.project.lukas.utils.HexUtils
import de.project.lukas.utils.LegoHelper
import de.project.lukas.utils.LegoProtocol
import de.project.lukas.utils.LegoWriterQueue
import java.util.UUID

/** A LEGO Powered Up train hub (88009), incl. motor, LED light and optional colour sensor. */
@SuppressLint("MissingPermission")
class TrainHub(context: Context, private val device: BluetoothDevice) : Device() {

    enum class PortType { None, Motor, Light, ColorSensor }

    override val address: String get() = device.address

    var currentColor: Int = 0
        private set

    val motorController: RemoteController = MotorController(this)
    val lightController: RemoteController = LightController(this)

    private var gatt: BluetoothGatt? = null
    private var writerQueue: LegoWriterQueue? = null
    private var currentSpeed = 0
    private var currentBrightness = 0
    private var lastColorTime = 0L // colour sensor must not trigger more than once per second
    private var portA = PortType.None
    private var portB = PortType.None

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_DISCONNECTED -> setConnected(false)
                BluetoothGatt.STATE_CONNECTED -> {
                    setConnected(true)
                    // It is more stable to wait a little before service discovery.
                    Handler(Looper.getMainLooper()).postDelayed({ this@TrainHub.gatt?.discoverServices() }, 500)
                    initializeService()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) = initializeService()

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            writerQueue?.confirmWrite()
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleNotification(LegoHelper.envelopeToData(value))
        }

        @Deprecated("Deprecated in API 33, still delivered on older devices")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            @Suppress("DEPRECATION")
            handleNotification(LegoHelper.envelopeToData(characteristic.value))
        }
    }

    init {
        setName(device.name ?: "")
        gatt = device.connectGatt(context, true, callback)
    }

    private fun handleNotification(value: ByteArray) {
        Log.i("TrainHub", "Bytes=\t${HexUtils.byteToHexString(value)}")
        when (value[0].toInt() and 0xFF) {
            0x01 -> parseDeviceInfo(value)
            0x04 -> parsePortInfo(value)
            0x45 -> parseColor(value)
        }
    }

    private fun parsePortInfo(value: ByteArray) {
        // 0: 0x04, 1: Port Index, 2: Port Mode (PlugIn 0 / PlugOut 1), 3: Device Type
        var port = PortType.None

        if (value[2].toInt() != 0 && value.size > 3) {
            when (value[3].toInt() and 0xFF) {
                0x02 -> port = PortType.Motor // Train Motor 88002
                0x2E -> port = PortType.Motor // Technic Large Motor 88013
                0x08 -> port = PortType.Light // LED Light 88005
                0x25 -> {
                    port = PortType.ColorSensor // Colour & Distance Sensor 88007
                    // value[1] = port, value[2] = colour, value[3] = distance, value[5] = nearfield
                    send(LegoProtocol.activatePortMode(value[1]))
                }
            }
        }

        when (value[1].toInt()) {
            0x00 -> portA = port
            0x01 -> portB = port
        }
    }

    private fun parseDeviceInfo(value: ByteArray) {
        // 0: 0x01, 1: Property Type, 3: Value
        if (value[1].toInt() == 0x06) {
            setBattery(value[3].toInt())
        }
    }

    private fun parseColor(value: ByteArray) {
        // Throttle: one colour bar generates several events.
        if (System.currentTimeMillis() < lastColorTime + 1000) return

        when (value[2].toInt() and 0xFF) {
            0x00 -> setMessage("") // Black
            0x03 -> {
                setMessage("Blue (${value[2]})")
                lastColorTime = System.currentTimeMillis()
            }
            0x05 -> {
                setMessage("Green (${value[2]})")
                lastColorTime = System.currentTimeMillis()
            }
            0x07 -> {
                setMessage("Yellow (${value[2]})")
                lastColorTime = System.currentTimeMillis()
            }
            0x09 -> {
                setMessage("Red (${value[2]})")
                lastColorTime = System.currentTimeMillis()
            }
            0x0A -> {
                setMessage("White (${value[2]})")
                lastColorTime = System.currentTimeMillis()
            }
            0xFF -> setMessage("") // sensor reports FF when no colour detected
        }

        Log.i("TrainHub", "ColorSensor=\t${HexUtils.byteToHexString(value)}")

        // Blue = stop the train automatically.
        if ((value[2].toInt() and 0xFF) == 0x03) {
            motorStop()
        }
    }

    fun setLedColorHub() {
        currentColor++
        if (currentColor == 11) currentColor = 0
        send(LegoProtocol.setColor(0x32, currentColor.toByte()))
    }

    fun lightDarker() {
        if (currentBrightness > -100) currentBrightness -= 25
        updateBrightness()
    }

    fun lightBrighter() {
        if (currentBrightness < 100) currentBrightness += 25
        updateBrightness()
    }

    fun lightOff() {
        currentBrightness = 0
        updateBrightness()
    }

    private fun updateBrightness() {
        val brightness = LegoHelper.mapBrightness(currentBrightness)
        if (portA == PortType.Light) send(LegoProtocol.setBrightness(0x00, brightness))
        if (portB == PortType.Light) send(LegoProtocol.setBrightness(0x01, brightness))
    }

    fun motorStop() {
        currentSpeed = 0
        updateSpeed()
    }

    fun motorSlower() {
        if (currentSpeed > -100) currentSpeed -= 25
        updateSpeed()
    }

    fun motorFaster() {
        if (currentSpeed < 100) currentSpeed += 25
        updateSpeed()
    }

    private fun updateSpeed() {
        val speed = LegoHelper.mapSpeed(currentSpeed)
        if (portA == PortType.Motor) send(LegoProtocol.setSpeed(0x00, speed))
        if (portB == PortType.Motor) send(LegoProtocol.setSpeed(0x01, speed))
    }

    fun rename(name: String) {
        setName(name)
        send(LegoHelper.createRenameRequest(name))
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

        // The first writes usually fail, so wait a little before activating reports.
        Handler(Looper.getMainLooper()).postDelayed({
            send(LegoProtocol.activateBatteryReports)
            send(LegoProtocol.activatePortReports)
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
            return data.size > 1 && data[1].toInt() == 65 // 010 00001
        }
    }
}

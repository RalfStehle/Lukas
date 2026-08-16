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
import java.util.Locale
import java.util.UUID

/**
 * A LEGO Duplo train base. Similar protocol to [TrainHub], but adds sounds/tones and reacts to
 * the built-in colour sensor and speedometer.
 */
@SuppressLint("MissingPermission")
class TrainBase(context: Context, private val device: BluetoothDevice) : Device() {

    enum class PortType { None, Motor, Light }

    override val address: String get() = device.address

    val motorController: RemoteController = BaseMotorController(this)
    val lightController: RemoteController = BaseLightController(this)

    private var gatt: BluetoothGatt? = null
    private var writerQueue: LegoWriterQueue? = null
    private var currentSpeed = 0
    private var currentColor = 0
    private var colorValues = ""
    private var lastColorTime = 0L // colour sensor should switch at most once per second
    private var lastSpeedTime = 0L // speedometer should switch at most once per second
    private var portA = PortType.None
    private var portB = PortType.None

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothGatt.STATE_DISCONNECTED -> setConnected(false)
                BluetoothGatt.STATE_CONNECTED -> {
                    setConnected(true)
                    Handler(
                        Looper.getMainLooper()
                    ).postDelayed({ this@TrainBase.gatt?.discoverServices() }, 500)
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
            Handler(Looper.getMainLooper()).postDelayed({ writerQueue?.confirmWrite() }, 100)
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
        gatt = device.connectGatt(context, true, callback, BluetoothDevice.TRANSPORT_LE)
    }

    private fun handleNotification(value: ByteArray) {
        val header = value[0].toInt() and 0xFF
        val sub = value[1].toInt() and 0xFF
        when {
            header == 0x01 -> parseDeviceInfo(value)
            header == 0x04 -> parsePortInfo(value)
            header == 0x45 && sub == 0x12 -> parseColorInfo(value)
            header == 0x45 && sub == 0x13 -> {
                Log.i("TrainBase", "parseSpeedInfo: value=\t${HexUtils.byteToHexString(value)}")
                parseSpeedInfo(value)
            }
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
            }
        }

        when (value[1].toInt()) {
            0x00 -> portA = port
            0x01 -> portB = port
        }
    }

    private fun parseDeviceInfo(value: ByteArray) {
        if (value[1].toInt() == 0x06) {
            setBattery(value[3].toInt())
        }
    }

    private fun parseColorInfo(value: ByteArray) {
        Log.i(
            "TrainBase",
            "parseColorInfo: value=\t${HexUtils.byteToHexString(
                value
            )}\t${System.currentTimeMillis() - lastColorTime}"
        )
        // 0 black, 1 pink, 2 purple, 3 blue, 4 lightblue, 5 cyan, 6 green, 7 yellow, 8 orange, 9 red, 10 white

        // The Duplo colour sensor under-reports green/purple by one index; correct it like
        // the reference legoino library does (Lpf2Hub::parseColor).
        val raw = value[2].toInt() and 0xFF
        val color = if (raw == 1 || raw == 5) raw + 1 else raw
        colorValues = String.format(Locale.ROOT, "%02d", color) + " " + colorValues

        if (System.currentTimeMillis() < lastColorTime + 500) return

        when {
            // Blue 0x03 = Pause & Water Refill
            color == 0x03 -> {
                lastColorTime = System.currentTimeMillis() + 3000
                colorValues = ""
                setMessage("Blue (0x03)")
                playSound(0x07) // Water_Refill
            }
            // Red 0x09 = Motor Stop
            color == 0x09 -> {
                lastColorTime = System.currentTimeMillis()
                colorValues = ""
                setMessage("Red (0x09)")
                playTone(0x03)
            }
            // Green (06 10) = reverse direction
            colorValues.indexOf("06 10") >= 0 -> {
                lastColorTime = System.currentTimeMillis()
                colorValues = ""
                setMessage("Green (06 10)")
                playTone(0x09)
            }
            // Yellow (07 06) = Horn
            colorValues.indexOf("07 06") >= 0 -> {
                lastColorTime = System.currentTimeMillis()
                colorValues = ""
                setMessage("Yellow (07 06)")
                playSound(0x09)
            }
            // White (07 10 06) = change LED colour
            colorValues.indexOf("07 10 06") >= 0 -> {
                lastColorTime = System.currentTimeMillis()
                colorValues = ""
                setMessage("White (07 10 06)")
                playTone(0x05)
                setLedColorHub()
            }
        }
    }

    private fun parseSpeedInfo(value: ByteArray) {
        if (System.currentTimeMillis() < lastSpeedTime + 1000) return // else motorStop() cannot hold at 0

        if (currentSpeed == 0 && value[2].toInt() > 10) {
            currentSpeed = if (value[3].toInt() == 0) 50 else -50 // value[3] == FF => reverse
            updateSpeed()
        }
        // Train was stopped by hand, so switch the motor off.
        if (value[2].toInt() == 0) {
            currentSpeed = 0
            updateSpeed()
        }
    }

    fun setLedColorHub() {
        currentColor++
        if (currentColor == 11) currentColor = 0
        // BLACK 0, PINK 1, PURPLE 2, BLUE 3, LIGHTBLUE 4, CYAN 5, GREEN 6, YELLOW 7, ORANGE 8, RED 9, WHITE 10
        send(LegoProtocol.setColor(0x11, currentColor.toByte()))
    }

    fun sound1() = playSound(0x09) // Horn
    fun sound2() = playSound(0x0A) // Steam
    fun sound3() = playSound(0x07) // Water_Refill
    fun sound4() = playSound(0x05) // Station_Departure

    fun motorStop() {
        lastSpeedTime = System.currentTimeMillis() // speedometer would otherwise restart while decelerating
        playSound(0x03) // Brake
        currentSpeed = 0
        updateSpeed()
    }

    fun motorSlower() {
        if (currentSpeed > -80) currentSpeed -= 20
        Log.i("TrainBase", "currentSpeed=\t$currentSpeed")
        updateSpeed()
    }

    fun motorFaster() {
        if (currentSpeed < 80) currentSpeed += 20
        Log.i("TrainBase", "currentSpeed=\t$currentSpeed")
        updateSpeed()
    }

    private fun updateSpeed() {
        send(LegoProtocol.setSpeed(0x00, LegoHelper.mapSpeed(currentSpeed)))
    }

    fun rename(name: String) {
        setName(name)
        send(LegoHelper.createRenameRequest(name))
    }

    fun playTone(tone: Byte) {
        send(byteArrayOf(0x41, 0x01, 0x02, 0x01, 0x00, 0x00, 0x00, 0x01)) // setToneMode
        send(byteArrayOf(0x81.toByte(), 0x01, 0x11, 0x51, 0x02, tone)) // playTone
    }

    fun playSound(sound: Byte) {
        // BRAKE 3, STATION_DEPARTURE 5, WATER_REFILL 7, HORN 9, STEAM 10
        send(byteArrayOf(0x41, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x01)) // setSoundMode
        send(byteArrayOf(0x81.toByte(), 0x01, 0x11, 0x51, 0x01, sound)) // playSound
    }

    override fun disconnect() {
        send(LegoProtocol.switchOffHub)
        gatt?.close()
    }

    override fun switchOff() {
        send(byteArrayOf(0x01, 0x01))
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
            send(LegoProtocol.activateBatteryReports)
            send(LegoProtocol.activatePortReports)
            send(LegoProtocol.activatePortMode(0x12)) // colour sensor
            send(LegoProtocol.activatePortMode(0x13)) // speedometer
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
            return data.size > 1 && data[1].toInt() == 32 // 001 00000
        }
    }
}

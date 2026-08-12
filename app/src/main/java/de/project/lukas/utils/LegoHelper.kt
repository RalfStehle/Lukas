package de.project.lukas.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import java.nio.charset.StandardCharsets
import java.util.UUID

/** Low-level helpers for the LEGO Powered Up BLE protocol framing. */
object LegoHelper {
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    /** Prefixes a payload with the LEGO message envelope (length + hub id). */
    fun dataToEnvelope(data: ByteArray): ByteArray {
        val envelope = ByteArray(data.size + 2)
        envelope[0] = (data.size + 2).toByte() // total length
        envelope[1] = 0
        System.arraycopy(data, 0, envelope, 2, data.size)
        return envelope
    }

    /** Strips the 2-byte envelope from an incoming message. */
    fun envelopeToData(envelope: ByteArray): ByteArray =
        envelope.copyOfRange(2, envelope.size)

    fun mapSpeed(speed: Int): Byte = when {
        speed == 0 -> 127 // stop motor
        speed > 0 -> map(speed, 0, 100, 0, 126).toByte()
        else -> map(-speed, 0, 100, 255, 128).toByte()
    }

    fun mapBrightness(brightness: Int): Byte = brightness.toByte()

    fun createRenameRequest(name: String): ByteArray {
        val nameBytes = name.toByteArray(StandardCharsets.UTF_8)
        val payload = ByteArray(nameBytes.size + 3)
        payload[0] = 0x01
        payload[1] = 0x01 // Operation: Set
        payload[2] = 0x01 // Property: Name
        System.arraycopy(nameBytes, 0, payload, 3, nameBytes.size)
        return payload
    }

    fun map(x: Int, inMin: Int, inMax: Int, outMin: Int, outMax: Int): Int =
        (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin

    /** Enables notifications on a characteristic, using the non-deprecated API on Android 13+. */
    @SuppressLint("MissingPermission")
    fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: return
        gatt.setCharacteristicNotification(characteristic, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            @Suppress("DEPRECATION")
            gatt.writeDescriptor(descriptor)
        }
    }
}

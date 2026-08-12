package de.project.lukas.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.Build
import android.util.Log
import java.util.LinkedList
import java.util.Queue

/**
 * Guarantees that BLE writes are sent sequentially: the next value is only written once the
 * previous one was confirmed via [confirmWrite] (called from onCharacteristicWrite).
 */
@SuppressLint("MissingPermission")
class LegoWriterQueue(
    private val gatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic
) {
    private val queue: Queue<ByteArray> = LinkedList()
    private var writeUnconfirmed = false

    @Synchronized
    fun write(data: ByteArray) {
        queue.add(data)
        writeNext()
    }

    @Synchronized
    fun confirmWrite() {
        writeUnconfirmed = false
        writeNext()
    }

    private fun writeNext() {
        if (writeUnconfirmed) return
        val data = queue.poll() ?: return

        writeUnconfirmed = true
        val success = writeCompat(data)

        if (!success) {
            Log.e("Bluetooth", "Failed to send message.")
            // Don't block the queue permanently if the platform rejected the write.
            writeUnconfirmed = false
        }
    }

    private fun writeCompat(data: ByteArray): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ) == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            run {
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                characteristic.value = data
                gatt.writeCharacteristic(characteristic)
            }
        }
    }
}

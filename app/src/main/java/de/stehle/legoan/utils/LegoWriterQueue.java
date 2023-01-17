package de.stehle.legoan.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class LegoWriterQueue {
    private final BluetoothGatt bluetoothGatt;
    private final BluetoothGattCharacteristic bluetoothCharacteristic;
    private final Queue<byte[]> writerQueue = new LinkedList<>();
    private boolean writeUnconfirmed;

    public LegoWriterQueue(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothCharacteristic) {
        this.bluetoothGatt = bluetoothGatt;
        this.bluetoothCharacteristic = bluetoothCharacteristic;
    }

    public void confirmWrite() {
        synchronized (writerQueue) {
            writeUnconfirmed = false;
            writeInternal();
        }
    }

    public void write(byte[] data) {
        synchronized (writerQueue) {
            writerQueue.add(data);

            writeInternal();
        }
    }

    @SuppressLint("MissingPermission")
    private void writeInternal() {
        if (writeUnconfirmed) {
            return;
        }

        byte[] data = writerQueue.poll();

        if (data == null) {
            return;
        }

        writeUnconfirmed = true;

        bluetoothCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        bluetoothCharacteristic.setValue(LegoHelper.dataToEnvelope(data));

        boolean success = bluetoothGatt.writeCharacteristic(bluetoothCharacteristic);

        if (!success) {
            Log.e("Bluetooth", "Failed to send message.");
        } else {
            Log.i("Bluetooth", "Message written.");
        }
    }

    @SuppressLint("MissingPermission")
    public void enableNotifications() {
        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        // Use a special descriptor to enable notifications.
        BluetoothGattDescriptor bluetoothDescriptor = bluetoothCharacteristic.getDescriptor(uuid);
        bluetoothDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        bluetoothGatt.setCharacteristicNotification(bluetoothCharacteristic, true);
        bluetoothGatt.writeDescriptor(bluetoothDescriptor);
    }
}

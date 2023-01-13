package de.stehle.legoan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

public class LogoWriterQueue {
    private final BluetoothGatt bluetoothGatt;
    private final BluetoothGattCharacteristic bluetoothCharacteristic;
    private final Queue<byte[]> writerQueue = new LinkedList<>();
    private boolean writeUnconfirmed;

    public LogoWriterQueue(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothCharacteristic) {
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
}

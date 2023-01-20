package de.stehle.legoan.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

public class LegoHelper {
    public static byte[] dataToEnvelope(byte[] data) {
        byte[] envelope = new byte[data.length + 2];

        // The first value must be the length.
        envelope[0] = (byte) (data.length + 2);
        envelope[1] = 0;

        // Copy the rest of the value.
        System.arraycopy(data, 0, envelope, 2, data.length);
        return envelope;
    }

    public static byte[] envelopeToData(byte[] envelope) {
        byte[] data = new byte[envelope.length - 2];

        System.arraycopy(envelope, 2, data, 0, envelope.length - 2);
        return data;
    }

    public static byte mapSpeed(int speed) {
        if (speed == 0) {
            return 127; // stop motor
        } else if (speed > 0) {
            return (byte) map(speed, 0, 100, 0, 126);
        } else {
            return (byte) map(-speed, 0, 100, 255, 128);
        }
    }

    public static byte mapBrightness(int brightness) {
        return (byte) brightness;
    }

    public static int map(int x, int inMin, int inMax, int outMin, int outMax) {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

    @SuppressLint("MissingPermission")
    public static void enableNotifications(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothCharacteristic) {
        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        // Use a special descriptor to enable notifications.
        BluetoothGattDescriptor bluetoothDescriptor = bluetoothCharacteristic.getDescriptor(uuid);
        bluetoothDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        bluetoothGatt.setCharacteristicNotification(bluetoothCharacteristic, true);
        bluetoothGatt.writeDescriptor(bluetoothDescriptor);
    }
}

package de.stehle.legoan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.ParcelUuid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class TrainHub extends BluetoothGattCallback {
    private final static byte[] speeds = {0x7E, 0x6C, 0x5A, 0x48, 0x36, 0x24, 0x12, 0x7F, (byte) 0xEC, (byte) 0xDA, (byte) 0xC8, (byte) 0xB6, (byte) 0xA4, (byte) 0x92, (byte) 0x80};
    private final BluetoothDevice bluetoothDevice;
    private final BluetoothGatt bluetoothGatt;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic devicesCharacteristic;
    private final static int stopSpeed = speeds.length / 2;
    private final List<ChangeListener> listeners = new ArrayList<>();
    private int currentSpeed = stopSpeed;
    private int currentColor;
    private boolean isConnected;

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int getBattery() {
        return 100;
    }

    public TrainHub(BluetoothDevice bluetoothDevice, Context context) {
        this.bluetoothDevice = bluetoothDevice;
        this.bluetoothGatt = bluetoothDevice.connectGatt(context, true, this);
    }

    public void subscribe(ChangeListener listener) {
        this.listeners.add(listener);
    }

    private void notifyChanged() {
        for (ChangeListener listener : this.listeners) {
            listener.notifyChanged();
        }
    }

    public void nextLedColor() {
        currentColor++;

        if (currentColor == 11) {
            currentColor = 0;
        }

        // send(new byte[]{0x0a, 0x00, (byte) 0x41, 0x32, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00});  // Set Colour Mode
        send(new byte[]{0x08, 0x00, (byte) 0x81, 0x32, 0x11, 0x51, 0x00, (byte)currentColor}); // Set color
    }

    public void stop() {
        currentSpeed = stopSpeed;

        setSpeed(currentSpeed);
    }

    public void decrementSpeed() {
        if (currentSpeed > 0) {
            currentSpeed--;
        }

        setSpeed(currentSpeed);
    }

    public void incrementSpeed() {
        if (currentSpeed < speeds.length - 1) {
            currentSpeed++;
        }

        setSpeed(currentSpeed);
    }

    private void setSpeed(int speed) {
        send(new byte[]{0x08, 0x00, (byte) 0x81, 0x00, 0x11, 0x51, 0x00, (byte) speeds[speed]}); // Port A
    }

    public static boolean canConnect(ScanResult scanResult) {
        UUID requiredService = UUID.fromString("00001623-1212-efde-1623-785feabcd123");

        String name = scanResult.getDevice().getName();
        if (scanResult.getScanRecord().getServiceUuids() == null) {
            return false;
        }

        for (ParcelUuid uuid: scanResult.getScanRecord().getServiceUuids()) {
            if (uuid.getUuid().equals(requiredService)) {
                return true;
            }
        }

        return false;
    }

    public boolean send(byte[] data) {
        if (service == null) {
            service = bluetoothGatt.getService(UUID.fromString("00001623-1212-efde-1623-785feabcd123"));
        }

        if (service == null) {
            return false;
        }

        if (devicesCharacteristic == null) {
            devicesCharacteristic = service.getCharacteristic(UUID.fromString("00001624-1212-efde-1623-785feabcd123"));
        }

        if (devicesCharacteristic == null) {
            return false;
        }

        devicesCharacteristic.setValue(data);

        return bluetoothGatt.writeCharacteristic(devicesCharacteristic);
    }

    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
        switch (newState) {
            case 0:
                this.isConnected = false;
                this.notifyChanged();
                break;
            case 2:
                this.isConnected = true;
                this.notifyChanged();
                // discover services and characteristics for this device
                this.bluetoothGatt.discoverServices();
                break;
        }
    }
}
